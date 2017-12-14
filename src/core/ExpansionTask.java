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
  private Side playerSide;
  private int timeout;
  private Node<MonteCarloData> tree;
  private BufferedWriter writer;

  public ExpansionTask(Node<MonteCarloData> inRoot, int inTimeout)
  {
   playerSide = inRoot.data.getCurrentSide();
   tree = inRoot;

   timeout = inTimeout;
 }

  private int getRandomLegalHole(Board inBoard, Side inSide)
 {
  List<Integer> holes = new ArrayList<>();
  for (int i = 1; i < 8; i++)
  {
    holes.add(i);
  }
  
  Collections.shuffle(holes);

  int index = 0;
  int hole = holes.get(index);

  while (!Kalah.isLegalMove(inBoard, new Move(inSide, hole)))
  {
    if(index++ < 7)
      hole = holes.get(index);
    else
      hole = -1; //ALL HOLES CONTAIN 0 SEEDS
  }

  return hole;
 }

  private int getHeuristicHole(Board currentSimulationBoard, Side nextSideToMove) throws CloneNotSupportedException
 {
  int[] scores = new int[2];
  scores[0] = -1; //current best pit
  scores[1] = 0; //current best pit score

  for (int i = 1; i < 8; i++)
  {
    Board copyOfSimulationBoard = currentSimulationBoard.clone();
    if (Kalah.isLegalMove(copyOfSimulationBoard, new Move(nextSideToMove, i)))
    {
      if (scores[0] == -1)
      {
        scores[0] = i;
      }

      Kalah.makeMove(copyOfSimulationBoard, new Move(nextSideToMove, i));
      if (copyOfSimulationBoard.getSeedsInStore(playerSide) > scores[1])
      {
        scores[0] = i;
        scores[1] = copyOfSimulationBoard.getSeedsInStore(playerSide);
      }
    }
  }
  return scores[0];
 }

 public ExpansionTaskResult call()
 {
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
	Node<MonteCarloData> chosenNode = selection(tree);
	if (chosenNode == null)
	{
	    return new ExpansionTaskResult(1, tree.data);
	}
	SimulationResult newResult = simulation(chosenNode);
	backTrace(newResult, chosenNode);
	
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

 private Node<MonteCarloData> selection(Node<MonteCarloData> inNode) throws Exception
 {
     int unplayedMove = getLegalUnplayedMove(inNode);
     // Find best unvisited node
     if (unplayedMove == -1)
     {
	 // writer.write("\nUnplayed node does not exist!");
	 // writer.flush();
	 Node<MonteCarloData> highestConfidenceBoundChild = null;
	 double highestConfidenceBound = -1.0;
	 
	 for (Node<MonteCarloData> currentChild : inNode.children)
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
	 
	 // Need to patch this up
	 if (highestConfidenceBoundChild == null)
	 {
	     writer.write("\nReachedEndOfTree!");
	     writer.flush();
	     return null;
	 }

	 return selection(highestConfidenceBoundChild);
     }

     // writer.write("\nSelection is complete, move is " + unplayedMove);
     // writer.flush();
     return expansion(unplayedMove, inNode);
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
	     Move possibleMove = new Move(inNode.data.getCurrentSide(), i);
	     writer.write("\nPossible move is for square " + i);
	     
	     boolean isLegalMove = Kalah.isLegalMove(inNode.data.getCurrentBoard(), possibleMove);
	     boolean containsMove = containsMove(i, alreadySelectedMoves);
	     boolean gameOver = isGameOver(inNode.data.getCurrentBoard(), possibleMove);
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

 private SimulationResult simulation(Node<MonteCarloData> currentNode) throws Exception
 {
      Board simulationBoard = currentNode.data.getCurrentBoard().clone();
      Side nextSideToMove = currentNode.data.getCurrentSide();
      while (!Kalah.gameOver(simulationBoard))
      {
	  // int foundHole = getRandomLegalHole(simulationBoard, nextSideToMove);
          int foundHole = getHeuristicHole(simulationBoard.clone(), nextSideToMove);      
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

 private Node<MonteCarloData> expansion(int nextMove, Node<MonteCarloData> currentNode) throws Exception
 {
     Board newClonedBoard = currentNode.data.getCurrentBoard().clone();
     Side newSideToMove = Kalah.makeMove(newClonedBoard, new Move(currentNode.data.getCurrentSide(), nextMove));

     Node<MonteCarloData> expandedNode = new Node<MonteCarloData>(new MonteCarloData(nextMove, newClonedBoard, newSideToMove), currentNode);
     currentNode.children.add(expandedNode);
     
     return expandedNode;
 }

 private void backTrace(SimulationResult inResult, Node<MonteCarloData> chosenNode) throws IOException
 {
     chosenNode.data.update(inResult);
     if (chosenNode.parent != null)
     {
	 backTrace(inResult, chosenNode.parent);
     }
 }
}
