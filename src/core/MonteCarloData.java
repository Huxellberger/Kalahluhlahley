package MKAgent;

public class MonteCarloData
{
    public final int Move;

    public MonteCarloData(int inMove, Board inBoard, Side inSide)
    {
	Move = inMove;
	wins = 0;
	matchesPlayed = 0;
	currentBoard = inBoard;
	currentSide = inSide;
    }

    public void update(SimulationResult inResult)
    {
	matchesPlayed += 1;
	if (inResult == SimulationResult.Win)
	{
	    wins += 1;
	}
    }

    public void addResults(int inWins, int inMatchesPlayed)
    {
	wins += inWins;
	matchesPlayed += inMatchesPlayed;
    }

    public int getWins()
    {
	return wins;
    }

    public int getMatchesPlayed()
    {
	return matchesPlayed;
    }

    public double getUpperConfidenceBound(int inTotalMatches)
    {
    	if (inTotalMatches == 0 || matchesPlayed == 0)
    	{
    	    return 0.0;
    	}

    	return wins/matchesPlayed + Math.sqrt((Math.log(inTotalMatches)) /matchesPlayed);
    }

    public Board getCurrentBoard()
    {
        return currentBoard;
    }

    public Side getCurrentSide()
    {
        return currentSide;
    }

    private int wins;
    private int matchesPlayed;
    private Board currentBoard;
    private Side currentSide;
}
