package MKAgent;

/**
* An object of this type is returned by interpretStateMsg().
* @see Protocol#interpretStateMsg(String, Board)
*/
public class MoveTurn
{
    public static final int SWAP_MOVE = -1;
    public static final String NO_MOVE = "";
   
    /**
    * "true" if the game is over, "false" otherwise.
    */
    public boolean end;
    /**
    * "true" if it's this agent's turn again, "false" otherwise.
    */
    public boolean again;
    /**
    * The number of the hole that characterises the move which has been
    * made (the move starts with picking the seeds from the given hole)
    * or -1 if the opponent has made a swap.
    */
    public int move;
}
