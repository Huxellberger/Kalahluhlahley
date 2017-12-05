package MKAgent;

import java.util.concurrent.Callable;

public class ExpansionTask implements Callable<ExpansionTaskResult>
{
    private Board startingBoard;
    private Board simulationBoard;
    private int startingMove;
    private float timeout;

    public ExpansionTask(Board inBoard, int inMove, float inTimeout) throws CloneNotSupportedException
    {
	super();
	
	startingBoard = inBoard.clone();
	simulationBoard = null;
	startingMove = inMove;
	timeout = inTimeout;
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
	return new ExpansionTaskResult(1, 1.0f);
    }
}
