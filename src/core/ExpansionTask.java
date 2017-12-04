package MKAgent;

import java.util.concurrent.Callable;

public class ExpansionTask implements Callable<Board>
{
    public Board call() throws Exception
    {
	return new Board(MonteCarloAgent.HOLE_COUNT, MonteCarloAgent.SEED_COUNT);
    }
}
