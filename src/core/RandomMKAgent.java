package MKAgent;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class RandomMKAgent implements AgentInterface
{
  private Board currentBoard;
  private Side currentSide;

  public static final int HOLE_COUNT = 7;
  public static final int SEED_COUNT = 7;

  public RandomMKAgent()
  {
    currentBoard = new Board(HOLE_COUNT, SEED_COUNT);
    currentSide = Side.SOUTH;
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
      return getRandomValidMove();
    }

    if (!isStarting)
    {
      return Protocol.createSwapMsg();
    }
    else
    {
      return getRandomValidMove();
    }
  }

  public String respondToState(String receivedStateMessage)
  {
    try
    {
      Protocol.MoveTurn move = Protocol.interpretStateMsg(receivedStateMessage, currentBoard);
      if (move.move == Protocol.SWAP_MOVE)
      {
        currentSide = Side.NORTH;
      }
      
      if (!move.again)
      {
        return Protocol.NO_MOVE;
      }
    }
    catch (Exception e)
    {
      System.out.println("Bad State Message!");
    }

    return getRandomValidMove();
  }

  private String getRandomValidMove()
  {
    return Protocol.createMoveMsg(getRandomValidHole());
  }

  private int getRandomValidHole()
  {
    List<Integer> holes = new ArrayList<>();
    for (int i = 1; i < 8; i++)
    {
      holes.add(i);
    }
    
    Collections.shuffle(holes);

    int index = 0;
    int hole = holes.get(index);

    while (!Kalah.isLegalMove(currentBoard, new Move(currentSide, hole)))
    {
      if(index++ < 7)
        hole = holes.get(index);
      else
        hole = 1;
    }

    return hole;
  }
}
