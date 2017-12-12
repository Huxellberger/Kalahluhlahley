package MKAgent;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileWriter;

public class BFSMKAgent implements AgentInterface
{
  private Board currentBoard;
  private Side currentSide;

  public static final int HOLE_COUNT = 7;
  public static final int SEED_COUNT = 7;

  private static BufferedWriter writer;

  public BFSMKAgent()
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

  private Board simulateBoardChange(Board currentBoard, int potIndex)
  {
    //TODO: simulate playing potIndex and return the new board state
    return currentBoard;
  }

  private String getBestValidMove()
  {
    return Protocol.createMoveMsg(getBestValidHole());
  }

  private int getBestValidHole()
  {
      // Node currentBoardNode = new Node(currentBoard);

    // currentBoardNode.addChild(simulateBoardChange(currentBoard, 1));

    try
    {
      writer = new BufferedWriter(new FileWriter("boardtree.txt"));
      // writer.write(currentBoardNode.toString() + "\n");
      writer.close();
    }
    catch(Exception exception)
    {

    }

    return 1;
  }
}
