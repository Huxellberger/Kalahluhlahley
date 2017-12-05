package MKAgent;

public class ExpansionTaskResult
{
    private int startingMove;
    private float winRate;

    public ExpansionTaskResult(int inMove, float inWinRate)
    {
	startingMove = inMove;
	winRate = inWinRate;
    }

    public int getStartingMove()
    {
	return startingMove;
    }

    public float getWinRate()
    {
	return winRate;
    }
}
