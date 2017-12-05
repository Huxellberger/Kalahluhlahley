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
    // private BufferedWriter writer;

  private Board startingBoard;
  private Board simulationBoard;
  private int startingMove;
  private Side playerSide;
  private int timeout;
  private Side nextSideToMove;

  public ExpansionTask(Board inBoard, int inMove, Side inSide, int inTimeout) throws CloneNotSupportedException
  {
   startingBoard = inBoard;
   simulationBoard = null;
   playerSide = inSide;
   nextSideToMove = playerSide;

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
  int draws = 0;

  nextSideToMove = playerSide;
  Move nextMove = null;

  long startTimeMillis = System.currentTimeMillis();
  long currentTimeMillis = startTimeMillis;
  long endTime = startTimeMillis + timeout;


  // String result = "log" + currentTimeMillis + startingMove + ".txt";
  // writer = new BufferedWriter(new FileWriter(result));

  try
  {
      while (currentTimeMillis < endTime)
      {
        simulationBoard = startingBoard.clone();
        nextMove = new Move(nextSideToMove, startingMove);

        if (Kalah.isLegalMove(simulationBoard, nextMove))
        {
	  nextSideToMove =  Kalah.makeMove(simulationBoard, nextMove);
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

        if (simulationBoard.getSeedsInStore(playerSide) > simulationBoard.getSeedsInStore(playerSide.opposite()))
        {
          wins++;
        }   
        else if (simulationBoard.getSeedsInStore(playerSide) < simulationBoard.getSeedsInStore(playerSide.opposite()))
        {
          losses++;
        }
        else
        {
	  draws++;
        }

        currentTimeMillis = System.currentTimeMillis();
    }  
      
      float winRate = (float)(((float)wins / (wins+losses+draws)) * 100);
      //  writer.write("Wins: " + wins + " Losses: " + losses + " Draws: " + draws + " Final winrate: " + winRate + "\n");
      // writer.close();
      return new ExpansionTaskResult(startingMove, winRate);
    }
  catch(Exception ex)
  {    
    return new ExpansionTaskResult(1, 0.3f);
  }
}
}
