package MKAgent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * The main application class. It also provides methods for communication
 * with the game engine.
 */
public class Main
{
    /**
     * Input from the game engine.
     */
    private static Reader input = new BufferedReader(new InputStreamReader(System.in));
    private static BufferedWriter writer;

    private static AgentInterface agent = new MonteCarloAgent();

    /**
     * Sends a message to the game engine.
     * @param msg The message.
     */
    public static void sendMsg (String msg) throws IOException
    {
	writer.write(msg);
    	System.out.print(msg);
    	System.out.flush();
    }

    /**
     * Receives a message from the game engine. Messages are terminated by
     * a '\n' character.
     * @return The message.
     * @throws IOException if there has been an I/O error.
     */
    public static String recvMsg() throws IOException
    {
    	StringBuilder message = new StringBuilder();
    	int newCharacter;

    	do
    	{
    		newCharacter = input.read();
    		if (newCharacter == -1)
    			throw new EOFException("Input ended unexpectedly.");
    		message.append((char)newCharacter);

    	} while((char)newCharacter != '\n');

		return message.toString();
    }

    /**
     * The main method, invoked when the program is started.
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
	boolean playingGame = true;
	try
	{
            writer = new BufferedWriter(new FileWriter("log.txt"));
	    while (playingGame)
	    {   
		String receivedMessage = recvMsg();
		String response = MoveTurn.NO_MOVE;

		writer.write("received message:" + receivedMessage + "\n");
		switch(Protocol.getMessageType(receivedMessage))
		{
	          case START:
		      response = Main.agent.respondToStart(receivedMessage);
		      break;
	          case STATE:
		      response = Main.agent.respondToState(receivedMessage);
		      break;
	          case END:
		      playingGame = false;
	          default:
	        }
		
		if (!response.equals(MoveTurn.NO_MOVE))
		{
		    sendMsg(response);
		}
	    }
	    writer.close();
	}
	catch(Exception exception)
	{
	    playingGame = false;
	    System.out.println("Something went wrong, exiting.");
	}
    }
}
