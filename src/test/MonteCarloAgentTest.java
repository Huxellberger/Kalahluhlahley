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
	String response = testAgent.respondToState("CHANGE;1;0,9,9,9,9,9,8,1,8,7,7,7,7,7,0,1;YOU\n");
	assertTrue(response.equals("MOVE;" + MonteCarloAgent.HOLE_COUNT + "\n" ));
    }
}
