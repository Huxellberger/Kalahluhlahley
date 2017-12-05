package MKAgent;

import java.util.concurrent.Callable;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class ExpansionTask implements Callable<ExpansionTaskResult>
{
  private Board startingBoard;
  private Board simulationBoard;
  private int startingMove;
  private float timeout;
  private Side currentSide;

  public ExpansionTask(Board inBoard, int inMove, float inTimeout) throws CloneNotSupportedException
  {
   super();

   startingBoard = inBoard.clone();
   simulationBoard = null;
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

  while (!Kalah.isLegalMove(startingBoard, new Move(currentSide, hole)))
  {
    if(index++ < 7)
      hole = holes.get(index);
    else
      hole = -1; //ALL HOLES CONTAIN 0 SEEDS
  }

  return hole;
 }

 public ExpansionTaskResult call() throws Exception
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

  int wins = 0;
  int losses = 0;

  currentSide = Side.SOUTH;
  Side nextSideToMove = currentSide;
  Move nextMove = null;

  while (timeout > 0)
  {
    simulationBoard = startingBoard.clone();
    nextMove = new Move(currentSide, startingMove);

    if (Kalah.isLegalMove(simulationBoard, nextMove))
    {
      nextSideToMove = Kalah.makeMove(simulationBoard, nextMove);
    }
    else
    {
      return new ExpansionTaskResult(startingMove, 0.0f);
    }

    while (!Kalah.gameOver(simulationBoard))
    {
      int randomLegalHole = getRandomLegalHole();      
      nextMove = new Move(nextSideToMove, randomLegalHole);
      nextSideToMove = Kalah.makeMove(simulationBoard, nextMove);
    } 

    if (simulationBoard.getSeedsInStore(Side.SOUTH) > simulationBoard.getSeedsInStore(Side.NORTH))
    {
      wins++;
    }   
    else if (simulationBoard.getSeedsInStore(Side.SOUTH) < simulationBoard.getSeedsInStore(Side.NORTH))
    {
      losses++;
    }
  }

  return new ExpansionTaskResult(startingMove, (float)(wins / losses * 100));
 }
}
