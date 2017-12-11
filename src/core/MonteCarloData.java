package MKAgent;

public class MonteCarloData
{
    public final int Move;

    public MonteCarloData(int inMove)
    {
	Move = inMove;
	wins = 0;
	matchesPlayed = 0;
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
	return wins/matchesPlayed + Math.sqrt((2 * Math.log(matchesPlayed)) /inTotalMatches);
    }

    private int wins;
    private int matchesPlayed;
}
