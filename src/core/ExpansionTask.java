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
  private int startingMove;
  private Side playerSide;
  private int timeout;
  private Side nextSideToMove;
  private Node<MonteCarloData> tree;
  private Node<MonteCarloData> currentNode;
  private BufferedWriter writer;

  public ExpansionTask(Node<MonteCarloData> inRoot, Board inBoard, int inMove, Side inSide, int inTimeout) throws CloneNotSupportedException
  {
   startingBoard = inBoard;
   simulationBoard = startingBoard.clone();
   playerSide = inSide;
   nextSideToMove = playerSide;
   tree = inRoot;
   currentNode = tree;

   startingMove = inMove;
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
	// Called by the executor service when we submit the task.
	// This task should run until the timeout is exceeded, at which point it will return
	//   an expansion task result that contains the starting move and the winrate that produced.

	// LOGIC
	// While we haven't timed out
	//   clone the starting board and make the expected move (could start from expected move)
	//   play out the game with random moves using Kalah.makeMove(simulationBoard, move)
	//   if we win add to total wins, lose add to total losses.
	// work out the average win% and return new ExpansionTaskResult(startingMove, win%);

  nextSideToMove = playerSide;
  Move nextMove = null;

  long startTimeMillis = System.currentTimeMillis();
  long currentTimeMillis = startTimeMillis;
  long endTime = startTimeMillis + timeout;

  nextMove = new Move(nextSideToMove, startingMove);

  if (Kalah.isLegalMove(startingBoard, nextMove))
  {
    nextSideToMove =  Kalah.makeMove(startingBoard, nextMove);
  }
  else
  {
    return new ExpansionTaskResult(startingMove,tree.data);
  }

  try
  {
      writer = new BufferedWriter(new FileWriter(currentTimeMillis + "-" + startingMove + ".txt"));
      writer.write("\nBegin tree operation!");
      writer.flush();
      while (currentTimeMillis < endTime)
      {
	simulationBoard.setBoard(startingBoard);  

	int chosenMove = selection();
	expansion(chosenMove);
	SimulationResult newResult = simulation();
	backTrace(newResult);
	
        currentTimeMillis = System.currentTimeMillis();
      }  

      writer.close();
      
      return new ExpansionTaskResult(startingMove, tree.data);
    }
  catch(Exception ex)
  {    
    return new ExpansionTaskResult(1, tree.data);
  }
}

 private int selection() throws IOException
 {
     int unplayedMove = getLegalUnplayedMove();
     // Find best unvisited node
     while (unplayedMove == -1)
     {
	 writer.write("\nUnplayed node does not exist!");
	 writer.flush();
	 Node<MonteCarloData> highestConfidenceBoundChild = null;
	 double highestConfidenceBound = -1.0;

	 for (Node<MonteCarloData> currentChild : currentNode.children)
	 {
	     writer.write("\nCurrentMove:" + currentChild.data.Move);
	     writer.flush();
	     double currentConfidenceBound = currentChild.data.getUpperConfidenceBound(tree.data.getMatchesPlayed());
	     if ( currentConfidenceBound > highestConfidenceBound)
	     {
		 highestConfidenceBoundChild = currentChild;
		 highestConfidenceBound = currentConfidenceBound;
	     }
	 }

	 currentNode = highestConfidenceBoundChild;
	 nextSideToMove = Kalah.makeMove(simulationBoard, new Move(nextSideToMove, currentNode.data.Move));

	 unplayedMove = getLegalUnplayedMove();
     }

     return unplayedMove;
 }

 private int getLegalUnplayedMove() throws IOException
 {
     writer.write("Try to find legal unplayed move!");
     writer.flush();
     if (currentNode.children.size() < MonteCarloAgent.HOLE_COUNT)
     {
	 Vector<Integer> alreadySelectedMoves = new Vector<Integer>();
	 for (Node<MonteCarloData> currentChild : currentNode.children)
	 {
	     alreadySelectedMoves.add(currentChild.data.Move);
	 }

	 for (int i = 1; i <= MonteCarloAgent.HOLE_COUNT;  ++i)
	 {
	     if (Kalah.isLegalMove(simulationBoard, new Move(nextSideToMove, i)) && !containsMove(i, alreadySelectedMoves))
	     {
		 writer.write("\nFound unplayed move: " + i);
		 writer.flush();
		 return i;
	     }
	 } 
     }
     
     return -1;
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

 private SimulationResult simulation() throws CloneNotSupportedException
 {
      while (!Kalah.gameOver(simulationBoard))
      {
          //int randomLegalHole = getRandomLegalHole();
          int heuristicHole = getHeuristicHole(simulationBoard.clone());      
          Move nextMove = new Move(nextSideToMove, heuristicHole);
          nextSideToMove = Kalah.makeMove(simulationBoard, nextMove);          
      }

      if (simulationBoard.getSeedsInStore(playerSide) > simulationBoard.getSeedsInStore(playerSide.opposite()))
      {
	  return SimulationResult.Win;
      }   
      else 
      {
	  return SimulationResult.Loss;
      }
 }

 private void expansion(int newNode)
 {
     currentNode.children.add(new Node<MonteCarloData>(new MonteCarloData(newNode)));

     for (int i = 0; i < currentNode.children.size(); ++i)
     {
	 if (currentNode.children.get(i).data.Move == newNode)
	 {
	     currentNode = currentNode.children.get(i);
	     nextSideToMove = Kalah.makeMove(simulationBoard, new Move(nextSideToMove, newNode));

	     return;
	 }
     }
 }

 private void backTrace(SimulationResult inResult)
 {
     while (currentNode != tree)
     {
	 currentNode.data.update(inResult);
	 currentNode = currentNode.parent;
     }
     tree.data.update(inResult);
 }
}
