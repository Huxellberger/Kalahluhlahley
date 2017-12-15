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

  public class BestPossibleMove
  {
      public final Board beforeBoard;
      public final Board afterBoard;
      public final Side beforeSide;
      public final Side afterSide;
      public final int move;
      public int score;

      public BestPossibleMove(Board inBeforeBoard, Board inAfterBoard, Side inBeforeSide, Side inAfterSide, int inMove)
      {
	  beforeBoard = inBeforeBoard;
	  afterBoard = inAfterBoard;
	  
	  beforeSide = inBeforeSide;
	  afterSide = inAfterSide;

	  move = inMove;

	  score = 0;
      }
  }

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

  private int getBestMove(Vector<BestPossibleMove> inMoveResults) throws IOException
  {
      evaluateRepeatGoHole(inMoveResults);
      evaluateMostEmptyHoles(inMoveResults);
      evaluateMaxScoreHole(inMoveResults);
      
      int bestMove = -1;
      int bestScore = -1;

      for (BestPossibleMove currentMove : inMoveResults)
      {
	  // writer.write("\nCurrentMove: " + currentMove.move + "\tCurrentScore: " + currentMove.score);
	  // writer.flush();
	  if (currentMove.score > bestScore)
	  {
	      bestMove = currentMove.move;
	      bestScore = currentMove.score;
	  }
      }

      return bestMove;
  }

  private void evaluateRepeatGoHole(Vector<BestPossibleMove> inMoveResults)
  {
      for(BestPossibleMove currentMove : inMoveResults)
      {
	  if (currentMove.beforeSide == currentMove.afterSide)
	  {
	      currentMove.score += 1;
	  }
      } 
  }

  private void evaluateMostEmptyHoles(Vector<BestPossibleMove> inMoveResults)
  {
      int bestEmptyHoleCount = -1;
      int bestMove = -1;
      for (BestPossibleMove currentMove : inMoveResults)
      {
	  int currentEmptyHoles = 0;
	  for (int currentHole = 1; currentHole < MonteCarloAgent.HOLE_COUNT; currentHole++)
	  {
	      if (currentMove.afterBoard.getSeeds(currentMove.beforeSide, currentHole) == 0)
	      {
		  currentEmptyHoles++;
	      }
	  }

	  if (currentEmptyHoles > bestEmptyHoleCount)
	  {
	      bestEmptyHoleCount = currentEmptyHoles;
	      bestMove = currentMove.move;
	  }
      }

      for (BestPossibleMove currentMove : inMoveResults)
      {
	  if (currentMove.move == bestMove)
	  {
	      currentMove.score++;
	      break;
	  }
      }
  }

  private void evaluateMaxScoreHole(Vector<BestPossibleMove> inMoveResults)
  {
      int bestScore = -1;
      int bestMove = -1;

      for (BestPossibleMove currentMove : inMoveResults)
      {
	  int currentScore = currentMove.afterBoard.getSeedsInStore(currentMove.beforeSide);
	      
	  if (currentScore > bestScore)
	  {
	      bestScore = currentScore;
	      bestMove = currentMove.move;
	  }
      }

      for (BestPossibleMove currentMove : inMoveResults)
      {
	  if (currentMove.move == bestMove)
	  {
	      currentMove.score++;
	      break;
	  }
      }
  }

 public ExpansionTaskResult call()
 {
  long startTimeMillis = System.currentTimeMillis();
  long currentTimeMillis = startTimeMillis;
  long endTime = startTimeMillis + timeout;

  try
  {
      writer = new BufferedWriter(new FileWriter(currentTimeMillis + ".txt"));
      // writer.write("\nBegin tree operation!");
      // writer.flush();
      while (currentTimeMillis < endTime)
      {  
	for (Node<MonteCarloData> currentChild : tree.children)
	{
	    writer.write("\n matches played for root child is " + currentChild.data.getMatchesPlayed() + "\tWins: " + currentChild.data.getWins());
	    writer.flush();
	}

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
      // writer.write("\n\nTime left at end is: " + timeLeft);
      // writer.flush();
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
	     writer.write("\nCurrent Confidence Bound: " + currentConfidenceBound);
	     writer.flush();
	     if ( currentConfidenceBound > highestConfidenceBound )
	     {
		 highestConfidenceBoundChild = currentChild;
		 highestConfidenceBound = currentConfidenceBound;
	     }
	 }
	 
	 // Need to patch this up
	 if (highestConfidenceBoundChild == null)
	 {
	     // writer.write("\nReachedEndOfTree!");
	     // writer.flush();
	     return null;
	 }
	 
	 Node<MonteCarloData> selectedChild = selection(highestConfidenceBoundChild);
	     
	 if (selectedChild == null)
	 {
	     for (Node<MonteCarloData> currentChild : inNode.children)
	     {
		 selectedChild = selection(currentChild);
		 if (selectedChild != null)
		 {
		     break;
		 }
	     }
	 }

	 return selectedChild;
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
	     //  writer.write("\n current children: " + inNode.children.size());
	     // writer.flush();
	     Move possibleMove = new Move(inNode.data.getCurrentSide(), i);
	     // writer.write("\nPossible move is for square " + i);
	     
	     boolean isLegalMove = Kalah.isLegalMove(inNode.data.getCurrentBoard(), possibleMove);
	     boolean containsMove = containsMove(i, alreadySelectedMoves);
	     boolean gameOver = isGameOver(inNode.data.getCurrentBoard(), possibleMove);
	     // writer.write("\n Is legal?: " + isLegalMove);
	     // writer.write("\n containing move?: " + containsMove);
	     // writer.write("\n game over??: " + gameOver);
	     // writer.flush();

	     if ( isLegalMove && !containsMove && !gameOver )
	     {
		 // writer.write("\nFound unplayed move: " + i);
		 // writer.flush();
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
     // writer.write("\n board state:: " + possibleMove.toString());
     // writer.flush();
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
	  // Evaluate heuristics
	  Vector<BestPossibleMove> playedOutMoves = new Vector<BestPossibleMove>();
	  for (int currentMove = 1; currentMove <= MonteCarloAgent.HOLE_COUNT; currentMove++)
	  {
	      if (Kalah.isLegalMove(simulationBoard, new Move(nextSideToMove, currentMove)))
	      {
		  Board copyOfSimulationBoard = simulationBoard.clone();
		  Side sideAfterMove = Kalah.makeMove(copyOfSimulationBoard, new Move(nextSideToMove, currentMove));
		  playedOutMoves.add
		  (
		     new BestPossibleMove
		     (
		       simulationBoard, 
		       copyOfSimulationBoard, 
		       nextSideToMove, 
		       sideAfterMove, 
		       currentMove
		     )
		   );
	      }
	     
	  }

          Move nextMove = new Move(nextSideToMove, getBestMove(playedOutMoves));
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
     if (chosenNode.data.getCurrentSide() == playerSide.opposite())
     {
	 inResult = inResult.opposite();
     }

     chosenNode.data.update(inResult);
     // writer.write("\nNode Status: Wins are " + chosenNode.data.getWins() + " and games played is " + chosenNode.data.getMatchesPlayed());
     // writer.flush();
     if (chosenNode.parent != null)
     {
	 backTrace(inResult, chosenNode.parent);
     }
 }
}
