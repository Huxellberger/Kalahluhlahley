package MKAgent;

public interface AgentInterface
{
    public String respondToStart(String receivedStartMessage);
    public String respondToState(String receivedStateMessage);
}
