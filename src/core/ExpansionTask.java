package MKAgent;

import java.util.concurrent.Callable;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;

import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileWriter;
import java.io.IOException;

public class ExpansionTask implements Callable<ExpansionTaskResult>
{
  private Board startingBoard;
  private Board simulationBoard;
  private Side playerSide;
  private int timeout;
  private Side nextSideToMove;
  private Node<MonteCarloData> tree;
  private Node<MonteCarloData> currentNode;
  private BufferedWriter writer;

  public ExpansionTask(Node<MonteCarloData> inRoot, Board inBoard, Side inSide, int inTimeout) throws CloneNotSupportedException
  {
   startingBoard = inBoard;
   simulationBoard = startingBoard.clone();
   playerSide = inSide;
   nextSideToMove = playerSide;
   tree = inRoot;
   currentNode = tree;

   timeout = inTimeout;
 }

 private int getRandomLegalHole()
 {
  List<Integer> holes = new ArrayList<>();
  for (int i = 1; i < 8; i++)
  {
    holes.add(i);
  }
  
  Collections.shuffle(holes);

  int index = 0;
  int hole = holes.get(index);

  while (!Kalah.isLegalMove(simulationBoard, new Move(nextSideToMove, hole)))
  {
    if(index++ < 7)
      hole = holes.get(index);
    else
      hole = -1; //ALL HOLES CONTAIN 0 SEEDS
  }

  return hole;
 }

  private int getHeuristicHole(Board copyOfSimulationBoard)
 {
  int[] scores = new int[2];
  scores[0] = -1; //current best pit
  scores[1] = 0; //current best pit score

  for (int i = 1; i < 8; i++)
  {
    if (Kalah.isLegalMove(copyOfSimulationBoard, new Move(nextSideToMove, i)))
    {
      if (scores[0] == -1)
      {
        scores[0] = i;
      }

      Kalah.makeMove(simulationBoard, new Move(nextSideToMove, currentNode.data.Move));
      if (simulationBoard.getSeedsInStore(playerSide) > scores[1])
      {
        scores[0] = i;
        scores[1] = simulationBoard.getSeedsInStore(playerSide);
      }
    }
  }
  return scores[0];
 }

 public ExpansionTaskResult call()
 {
  nextSideToMove = playerSide;
  Move nextMove = null;

  long startTimeMillis = System.currentTimeMillis();
  long currentTimeMillis = startTimeMillis;
  long endTime = startTimeMillis + timeout;

  try
  {
      writer = new BufferedWriter(new FileWriter(currentTimeMillis + ".txt"));
      writer.write("\nBegin tree operation!");
      writer.flush();
      while (currentTimeMillis < endTime)
      {
	simulationBoard.setBoard(startingBoard);  

	int chosenMove = selection();
	if (chosenMove == -1)
	{
	    return new ExpansionTaskResult(1, tree.data);
	}
	expansion(chosenMove);
	SimulationResult newResult = simulation();
	backTrace(newResult);
	
        currentTimeMillis = System.currentTimeMillis();
	long timeLeft = endTime - currentTimeMillis;
	// writer.write("\n\nTime left at end is: " + timeLeft);
	// writer.flush();
      }

      long timeLeft = endTime - currentTimeMillis;
       writer.write("\n\nTime left at end is: " + timeLeft);
      writer.flush();
      writer.close();
      
      return new ExpansionTaskResult(1, tree.data);
    }
  catch(Exception ex)
  {    
    return new ExpansionTaskResult(1, tree.data);
  }
}

 private int selection() throws Exception
 {
     int unplayedMove = getLegalUnplayedMove(currentNode);
     Board previousBoard = simulationBoard.clone();
     // Find best unvisited node
     while (unplayedMove == -1)
     {
	 // writer.write("\nUnplayed node does not exist!");
	 // writer.flush();
	 Node<MonteCarloData> highestConfidenceBoundChild = null;
	 double highestConfidenceBound = -1.0;
	 
	 for (Node<MonteCarloData> currentChild : currentNode.children)
	 {
	     // writer.write("\nCurrentMove:" + currentChild.data.Move);
	     // writer.flush();
	     double currentConfidenceBound = currentChild.data.getUpperConfidenceBound(tree.data.getMatchesPlayed());
	     if ( currentConfidenceBound > highestConfidenceBound )
	     {
		 highestConfidenceBoundChild = currentChild;
		 highestConfidenceBound = currentConfidenceBound;
	     }
	 }
	 
	 if (highestConfidenceBoundChild == null)
	 {
	     writer.write("\nReachedEndOfTree!");
	     writer.flush();
	     return -1;
	 }

	 currentNode = highestConfidenceBoundChild;
	 nextSideToMove = Kalah.makeMove(simulationBoard, new Move(nextSideToMove, currentNode.data.Move));

	 unplayedMove = getLegalUnplayedMove(currentNode);	
     }

     // writer.write("\nSelection is complete, move is " + unplayedMove);
     // writer.flush();
     return unplayedMove;
 }

 private int getLegalUnplayedMove(Node<MonteCarloData> inNode) throws Exception
 {
     if (inNode.children.size() < MonteCarloAgent.HOLE_COUNT)
     {
	 Vector<Integer> alreadySelectedMoves = new Vector<Integer>();
	 for (Node<MonteCarloData> currentChild : inNode.children)
	 {
	     alreadySelectedMoves.add(currentChild.data.Move);
	 }

	 for (int i = 1; i <= MonteCarloAgent.HOLE_COUNT;  ++i)
	 {
	     writer.write("\n current children: " + inNode.children.size());
	     writer.flush();
	     Move possibleMove = new Move(nextSideToMove, i);
	     writer.write("\nPossible move is for square " + i);
	     
	     boolean isLegalMove = Kalah.isLegalMove(simulationBoard, possibleMove);
	     boolean containsMove = containsMove(i, alreadySelectedMoves);
	     boolean gameOver = isGameOver(simulationBoard, possibleMove);
	     writer.write("\n Is legal?: " + isLegalMove);
	     writer.write("\n containing move?: " + containsMove);
	     writer.write("\n game over??: " + gameOver);
	     writer.flush();

	     if ( isLegalMove && !containsMove && !gameOver )
	     {
		 writer.write("\nFound unplayed move: " + i);
		 writer.flush();
		 return i;
	     }
	 } 
     }
     
     return -1;
 }

 private boolean isGameOver(Board inBoard, Move inMove) throws Exception
 {
     Board possibleMove = inBoard.clone();

     Kalah.makeMove(possibleMove, inMove);
     writer.write("\n board state:: " + possibleMove.toString());
     writer.flush();
     return Kalah.gameOver(possibleMove);
 }

 private boolean containsMove(int inMove, Vector<Integer> inExistingMoves)
 {
     for (int currentChild : inExistingMoves)
     {
	 if (currentChild == inMove)
	 {
	     return true;
	 }
     }

     return false;
 }

 private SimulationResult simulation() throws Exception
 {
      while (!Kalah.gameOver(simulationBoard))
      {
	  int foundHole = getRandomLegalHole();
          // int foundHole = getHeuristicHole(simulationBoard.clone());      
          Move nextMove = new Move(nextSideToMove, foundHole);
          nextSideToMove = Kalah.makeMove(simulationBoard, nextMove);          
      }

      if (simulationBoard.getSeedsInStore(playerSide) > simulationBoard.getSeedsInStore(playerSide.opposite()))
      {
	  //writer.write("\nSimulation complete! Game is won!");
	  //writer.flush();
	  return SimulationResult.Win;
      }   
      else 
      {
	  //writer.write("\nSimulation complete! Game is lost!");
	  //writer.flush();
	  return SimulationResult.Loss;
      }
 }

 private void expansion(int newNode) throws IOException
 {
     currentNode.children.add(new Node<MonteCarloData>(new MonteCarloData(newNode), currentNode));

     for (int i = 0; i < currentNode.children.size(); ++i)
     {
	 if (currentNode.children.get(i).data.Move == newNode)
	 {
	     currentNode = currentNode.children.get(i);
	     nextSideToMove = Kalah.makeMove(simulationBoard, new Move(nextSideToMove, newNode));
	     //writer.write("expanded node " + newNode + "\n");
	     //writer.flush();
	     return;
	 }
     }
 }

 private void backTrace(SimulationResult inResult) throws IOException
 {
     while (currentNode != tree)
     {
	 currentNode.data.update(inResult);
	 currentNode = currentNode.parent;
	 writer.write("\nCurrent matches played: " + currentNode.data.getMatchesPlayed());
	 writer.write("\nCurrent wins: " + currentNode.data.getWins());
	 writer.flush();
     }
     // writer.write("\nWe fucking did it yeeeeeehah");
     // writer.flush();
     tree.data.update(inResult);
 }
}
