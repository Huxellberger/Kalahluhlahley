package MKAgent;

public enum SimulationResult
{
    Win,
    Loss;

    public SimulationResult opposite()
    {
	if (this == SimulationResult.Win)
	{
	    return SimulationResult.Loss;
	}
	return SimulationResult.Win;
    }
}
