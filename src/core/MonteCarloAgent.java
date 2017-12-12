package MKAgent;

import java.util.Vector;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class MonteCarloAgent implements AgentInterface
{
    private Board currentBoard;
    private Side currentSide;
    private Node<MonteCarloData> currentTree;

    public static final int HOLE_COUNT = 7;
    public static final int SEED_COUNT = 7;
    public static final int EXECUTION_TIMEOUT_MILLIS = 1500;

    public MonteCarloAgent()
    {
	currentBoard = new Board(HOLE_COUNT, SEED_COUNT);
	currentSide = Side.SOUTH;
	currentTree = new Node<MonteCarloData>(new MonteCarloData(-1), null);
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

	    updateTreeToNewState(move);
	    
	    if (!move.again)
	    {
		return MoveTurn.NO_MOVE;
	    }  

	    return getMonteCarloSelectedResult();
	}
	catch (Exception e)
	{
	}
	
	return getFirstValidMove();
    }

    private void updateTreeToNewState(MoveTurn move)
    {
	boolean foundChild = false;
	for (Node<MonteCarloData> currentChild : currentTree.children)
	{
	    if (currentChild.data.Move == move.move)
	    {
		foundChild = true;
		currentTree = currentChild;
	    }
	}

	if (!foundChild)
	{
	    currentTree.children.add(new Node<MonteCarloData>(new MonteCarloData(move.move), currentTree));

	    for (Node<MonteCarloData> currentChild : currentTree.children)
	    {
	       if (currentChild.data.Move == move.move)
	       {
		  currentTree = currentChild;
	       }
	    }
	}
    }

    private String getFirstValidMove()
    {
	return Protocol.createMoveMsg(getFirstValidHole());
    }

    private int getFirstValidHole()
    {
	for (int i = HOLE_COUNT; i >= 1; --i)
	{
	    if (Kalah.isLegalMove(currentBoard, new Move(currentSide, i)))
	    {
		return i;
	    }
	}

	return 1;
    }

    private String getMonteCarloSelectedResult() throws Exception
    {
	new ExpansionTask
	(
	    currentTree,
	    currentBoard.clone(), 
	    currentSide,
	    EXECUTION_TIMEOUT_MILLIS
	).call();

	int bestMove = getNewBestMove();

	return Protocol.createMoveMsg(bestMove);
    }

    private Vector<Integer> getAllLegalMoves()
    {
	Vector<Integer> validMoves = new Vector<Integer>();

	// Find all possible valid moves
	for (int possibleMove = 1; possibleMove <= HOLE_COUNT; ++possibleMove)
	{
	    if (Kalah.isLegalMove(currentBoard, new Move(currentSide, possibleMove)))
	    {
		validMoves.add(possibleMove);
	    }
	}

	return validMoves;
    }

    private Vector<Future<ExpansionTaskResult>> submitExpansionTasks
    (
       ExecutorService inExecutor, 
       Vector<Integer> inValidMoves
    ) throws Exception
    {
	Vector<Future<ExpansionTaskResult>> taskResults = new Vector<Future<ExpansionTaskResult>>();

	// Delegate tasks to montecarlo them all
	for (Integer consideredMove : inValidMoves)
	{
	    Node<MonteCarloData> child = getChildForMove(consideredMove);

	    if (child == null)
	    {
		currentTree.children.add(new Node<MonteCarloData>(new MonteCarloData(consideredMove), currentTree));
		child = currentTree.children.get(currentTree.children.size() - 1);
	    }

	    taskResults.add(inExecutor.submit(
	        new ExpansionTask
		(
		   child,
		   currentBoard.clone(), 
		   currentSide,
		   EXECUTION_TIMEOUT_MILLIS
		 )
	    ));
	}

	return taskResults;
    }

    private int getBestMove(Vector<Future<ExpansionTaskResult>> inExpansions) throws Exception
    {	
	// Find best result
	for (Future<ExpansionTaskResult> result : inExpansions)
	{
	    ExpansionTaskResult newResult = result.get();
	    currentTree.data.addResults(newResult.getData().getWins(), newResult.getData().getMatchesPlayed());
	}

	int currentBestMove = -1;
	double bestConfidenceBound = -1;

	for (Node<MonteCarloData> currentChild : currentTree.children)
	{
	    double newConfidenceBound = currentChild.data.getUpperConfidenceBound(currentTree.data.getMatchesPlayed());
	    Main.writer.write("\nConfidence bound is " + newConfidenceBound);
	    if (newConfidenceBound > bestConfidenceBound)
	    {
		bestConfidenceBound = newConfidenceBound;
		currentBestMove = currentChild.data.Move;
		Main.writer.write("Best move is now " + currentBestMove + "\n");
	    }
	}
	
	return currentBestMove;
    }

    private int getNewBestMove() throws Exception
    {	
	int currentBestMove = -1;
	double bestConfidenceBound = -1;

	for (Node<MonteCarloData> currentChild : currentTree.children)
	{
	    double newConfidenceBound = currentChild.data.getUpperConfidenceBound(currentTree.data.getMatchesPlayed());
	    Main.writer.write("\nConfidence bound is " + newConfidenceBound);
	    if (newConfidenceBound > bestConfidenceBound)
	    {
		bestConfidenceBound = newConfidenceBound;
		currentBestMove = currentChild.data.Move;
		Main.writer.write("Best move is now " + currentBestMove + "\n");
	    }
	}
	
	return currentBestMove;
    }

    private Node<MonteCarloData> getChildForMove(int inMove) throws Exception
    {
	for (Node<MonteCarloData> currentChild : currentTree.children)
	{
	    if (currentChild.data.Move == inMove)
	    {
		Main.writer.write("Getting Child node " + currentChild.data.Move + "\n");
		Main.writer.flush();
		return currentChild;
	    }
	}

	return null;
    }
}
