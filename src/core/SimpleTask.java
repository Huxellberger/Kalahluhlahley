package MKAgent;

import java.util.concurrent.Callable;

public class SimpleTask implements Callable<ExpansionTaskResult>
{
    private int startingMove;
    private int timeout;

    public SimpleTask(Board inBoard, int inMove, float inTimeout) throws CloneNotSupportedException
    {
	startingMove = inMove;
	timeout = (int)inTimeout;
    }

    public ExpansionTaskResult call() throws Exception
    {
	Thread.sleep(timeout * 10);
	return new ExpansionTaskResult(startingMove, 1.0f);
    }
}
