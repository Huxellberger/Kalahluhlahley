package MKAgent;

public class ExpansionTaskResult
{
    private int startingMove;
    private MonteCarloData data;

    public ExpansionTaskResult(int inMove, MonteCarloData inData)
    {
	startingMove = inMove;
	data = inData;
    }

    public int getStartingMove()
    {
	return startingMove;
    }

    public MonteCarloData getData()
    {
	return data;
    }
}
