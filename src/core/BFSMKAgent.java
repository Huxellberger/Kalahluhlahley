package MKAgent;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class BFSMKAgent implements AgentInterface
{
  private Board currentBoard;
  private Side currentSide;

  public static final int HOLE_COUNT = 7;
  public static final int SEED_COUNT = 7;

  public BFSMKAgent()
  {
    currentBoard = new Board(HOLE_COUNT, SEED_COUNT);
    currentSide = Side.SOUTH;
  }

  Tree bob;

  public String respondToStart(String receivedStartMessage)
  {
    boolean isStarting = true;
    try
    {
      isStarting = Protocol.interpretStartMsg(receivedStartMessage);
    }
    catch (Exception e)
    {
      return getBestValidMove();
    }

    if (!isStarting)
    {
      return Protocol.createSwapMsg();
    }
    else
    {
      return getBestValidMove();
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
    }

    return getBestValidMove();
  }

  private String getBestValidMove()
  {
    return Protocol.createMoveMsg(getBestValidHole());
  }

  private int getBestValidHole()
  {
    return 1;
  }
}
