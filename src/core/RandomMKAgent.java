package MKAgent;

public class RandomMKAgent implements AgentInterface
{
  private Board currentBoard;

  public static final int HOLE_COUNT = 7;
  public static final int START_SEED_COUNT = 7;

  public RandomMKAgent()
  {
    currentBoard = new Board(HOLE_COUNT, START_SEED_COUNT);
  }

  public String respondToStart(String receivedStartMessage)
  {
    // True if South starts, false if North
    boolean isStarting = true;
    try
    {
      isStarting = Protocol.interpretStartMsg(receivedStartMessage);
    }
    catch (InvalidMessageException e)
    {
      System.out.println("Failed to read start message! " + e);
      return getFirstValidMove();
    }

    System.out.println("Are we starting? " + isStarting);
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
      Protocol.interpretStateMsg(receivedStateMessage, currentBoard);
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
    for (int i = HOLE_COUNT; HOLE_COUNT >= 1; --i)
    {
      if (currentBoard.getSeeds(Side.NORTH, i) > 0)
      {
        return i;
      }
    }
  }
}
