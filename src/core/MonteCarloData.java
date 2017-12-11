package MKAgent;

public class MonteCarloData
{
    public MonteCarloData()
    {
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

    public int getWins()
    {
	return wins;
    }

    public int getMatchesPlayed()
    {
	return matchesPlayed;
    }

    private int wins;
    private int matchesPlayed;
}
