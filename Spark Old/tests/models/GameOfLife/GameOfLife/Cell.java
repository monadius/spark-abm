package GameOfLife;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class Cell extends SpaceAgent
{
	
	
	public void _init()
	{
	}
	
	public  Cell()
	{
		super(0.5, SpaceAgent.SQUARE);
		_init();
		this.setColor(SpaceAgent.GREEN);
	}
	
	public void step(SimulationTime time)
	{
		double n = 0;
		
		ArrayList<Cell> _result = Observer.getDefaultSpace().getAgents(this.getPosition(), 1.5, Cell.class);
		n = (_result.size() - 1.0);
		if (((n < 2.0) || (n > 3.0)))
		{
			DeadCell _agent;
			_agent = new DeadCell();
			_agent.moveToSpace(this.getSpace(), this.getPosition());
			this.die();
		}
	}
	
}
