import org.junit.Test;
import static org.junit.Assert.assertTrue;
import MKAgent.MonteCarloAgent;

public class MonteCarloAgentTest
{
    @Test	
    public void respondToStart_NotFirst_Swaps() 
    {
	MonteCarloAgent testAgent = new MonteCarloAgent();
	String response = testAgent.respondToStart("START;North\n");
	assertTrue(response.equals("SWAP\n"));
    }

    @Test
    public void respondToStart_First_DoesFirstValidMove()
    {
	MonteCarloAgent testAgent = new MonteCarloAgent();
	String response = testAgent.respondToStart("START;South\n");
	assertTrue(response.equals("MOVE;" + MonteCarloAgent.HOLE_COUNT + "\n" ));
    }

    @Test
    public void respondToState_DoesFirstValidMove()
    {
	MonteCarloAgent testAgent = new MonteCarloAgent();
	String response = testAgent.respondToState("CHANGE;2;1,2,3,4,5,6;OPP\n");
	assertTrue(response.equals("MOVE;" + MonteCarloAgent.HOLE_COUNT + "\n" ));
    }
}
