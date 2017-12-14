import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import MKAgent.MonteCarloData;
import MKAgent.SimulationResult;

public class MonteCarloDataTests
{
    @Test
    public void Creation_WinsAndMatchesPlayedIsZero()
    {
	MonteCarloData testData = new MonteCarloData(-1, null, null);
	assertEquals(0, testData.getWins());
	assertEquals(0, testData.getMatchesPlayed());
    }

    @Test
    public void Update_Win_AddsExpectedWinsAndMatchesPlayed()
    {	
	MonteCarloData testData = new MonteCarloData(-1, null, null);
	testData.update(SimulationResult.Win);
	assertEquals(1, testData.getWins());
	assertEquals(1, testData.getMatchesPlayed());
    }

    @Test
    public void Update_Loss_AddsExpectedMatchesPlayed()
    {	
	MonteCarloData testData = new MonteCarloData(-1, null, null);
	testData.update(SimulationResult.Loss);
	assertEquals(0, testData.getWins());
	assertEquals(1, testData.getMatchesPlayed());
    }
}
