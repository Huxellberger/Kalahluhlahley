import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import MKAgent.MonteCarloAgent;
import MKAgent.MoveTurn;
import MKAgent.Side;

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
    public void respondToStart_HasCorrectSide() 
    {
	MonteCarloAgent testAgent = new MonteCarloAgent();
	testAgent.respondToStart("START;North\n");
        assertEquals(Side.SOUTH, testAgent.getCurrentSide());    
    }

    @Test
    public void respondToStart_First_DoesFirstValidMove()
    {
	MonteCarloAgent testAgent = new MonteCarloAgent();
	String response = testAgent.respondToStart("START;South\n");
	assertTrue(response.equals("MOVE;" + MonteCarloAgent.HOLE_COUNT + "\n" ));
    }

    @Test
    public void respondToState_NotMove_ReturnsNoMoveMessage()
    {
	MonteCarloAgent testAgent = new MonteCarloAgent();
	String response = testAgent.respondToState("CHANGE;1;0,9,9,9,9,9,8,1,8,7,7,7,7,7,0,1;OPP\n");
	assertTrue(response.equals(MoveTurn.NO_MOVE));
    }

    @Test
    public void respondToState_OpponentSwaps_UpdatesCurrentSide()
    {
	MonteCarloAgent testAgent = new MonteCarloAgent();
	testAgent.respondToState("CHANGE;SWAP;OPP\n");
	assertEquals(Side.NORTH, testAgent.getCurrentSide());
    }
}
