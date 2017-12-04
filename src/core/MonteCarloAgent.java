package MKAgent;

public class MonteCarloAgent implements AgentInterface
{
    private Board currentBoard;
    private Side currentSide;

    public static final int HOLE_COUNT = 7;
    public static final int SEED_COUNT = 7;

    public MonteCarloAgent()
    {
	currentBoard = new Board(HOLE_COUNT, SEED_COUNT);
	currentSide = Side.SOUTH;
    }

    public Side getCurrentSide()
    {
	return currentSide;
    }
    
    public String respondToStart(String receivedStartMessage)
    {
	boolean isStarting = true;
	try
	{
	    isStarting = Protocol.interpretStartMsg(receivedStartMessage);
	}
	catch (Exception e)
	{
	    System.out.println("Failed to read start message!");
	    return getFirstValidMove();
	}

	if (!isStarting)
	{
	    return Protocol.createSwapMsg();
	}
	else
	{
	    return getFirstValidMove();
	}
    }
    
    public String respondToState(String receivedStateMessage)
    {
	try
	{
	    MoveTurn move = Protocol.interpretStateMsg(receivedStateMessage, currentBoard);
	    if (move.move == MoveTurn.SWAP_MOVE)
	    {
		currentSide = Side.NORTH;
	    }
	    
	    if (!move.again)
	    {
		return MoveTurn.NO_MOVE;
	    }
	}
	catch (Exception e)
	{
	    System.out.println("Bad State Message!");
	}
	
	return getFirstValidMove();
    }

    private String getFirstValidMove()
    {
	return Protocol.createMoveMsg(getFirstValidHole());
    }

    private int getFirstValidHole()
    {
	for (int i = 1; i <= HOLE_COUNT; ++i)
	{
	    if (Kalah.isLegalMove(currentBoard, new Move(currentSide, i)))
	    {
		return i;
	    }
	}
	
	return 1;
    }
}
