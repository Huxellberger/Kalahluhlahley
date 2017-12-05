package MKAgent;

import java.util.concurrent.Callable;

public class SimpleTask implements Callable<ExpansionTaskResult>
{
    private int startingMove;
    private int timeout;

    public SimpleTask(Board inBoard, int inMove, Side inCurrentSide, float inTimeout) throws CloneNotSupportedException
    {
	startingMove = inMove;
	timeout = (int)inTimeout;
    }

    public ExpansionTaskResult call() throws Exception
    {
	Thread.sleep(timeout);
	return new ExpansionTaskResult(startingMove, 1.0f);
    }
}
