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
	currentTree = new Node<MonteCarloData>(new MonteCarloData(-1));
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

	    return getMonteCarloSelectedResult();
	}
	catch (Exception e)
	{
	}
	
	return getFirstValidMove();
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
	Vector<Integer> validMoves = getAllLegalMoves();

	ExecutorService executor = Executors.newFixedThreadPool(validMoves.size());
	Vector<Future<ExpansionTaskResult>> tasksInFlight = submitExpansionTasks(executor, validMoves);	

	int bestMove = getBestMove(tasksInFlight);
		
	executor.shutdown();

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
		currentTree.children.add(new Node<MonteCarloData>(new MonteCarloData(consideredMove)));
		child = currentTree.children.get(currentTree.children.size() - 1);
	    }

	    taskResults.add(inExecutor.submit(
	        new ExpansionTask
		(
		   child,
		   currentBoard.clone(), 
		   consideredMove,
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
	    if (newConfidenceBound > bestConfidenceBound)
	    {
		bestConfidenceBound = newConfidenceBound;
		currentBestMove = currentChild.data.Move;
	    }
	}
	
	return currentBestMove;
    }

    private Node<MonteCarloData> getChildForMove(int inMove)
    {
	for (Node<MonteCarloData> currentChild : currentTree.children)
	{
	    if (currentChild.data.Move == inMove)
	    {
		return currentChild;
	    }
	}

	return null;
    }
}
