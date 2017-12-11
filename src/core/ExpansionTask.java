package MKAgent;

import java.util.concurrent.Callable;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

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
      while (currentTimeMillis < endTime)
      {
	simulationBoard.setBoard(startingBoard);  

	int chosenMove = selection();
	expansion(chosenMove);
	SimulationResult newResult = simulation();
	backTrace(newResult);
	
        currentTimeMillis = System.currentTimeMillis();
      }  
      
      return new ExpansionTaskResult(startingMove, tree.data);
    }
  catch(Exception ex)
  {    
    return new ExpansionTaskResult(1, tree.data);
  }
}

 private int selection()
 {
     int unplayedMove = getLegalUnplayedMove();
     // Find best unvisited node
     while (unplayedMove == -1)
     {
	 Node<MonteCarloData> highestConfidenceBoundChild = null;
	 double highestConfidenceBound = -1.0;

	 for (Node<MonteCarloData> currentChild : currentNode.children)
	 {
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

 private int getLegalUnplayedMove()
 {
     if (currentNode.children.size() < MonteCarloAgent.HOLE_COUNT)
     {
	 for (int i = currentNode.children.size(); i <= MonteCarloAgent.HOLE_COUNT;  ++i)
	 {
	     if (Kalah.isLegalMove(simulationBoard, new Move(nextSideToMove, i)))
	     {
		 return i;
	     }
	 } 
     }
     
     return -1;
 }

 private SimulationResult simulation()
 {
      while (!Kalah.gameOver(simulationBoard))
      {
          int randomLegalHole = getRandomLegalHole();      
          Move nextMove = new Move(nextSideToMove, randomLegalHole);
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
