package GameOfLife;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class DeadCell extends SpaceAgent
{
	
	
	public void _init()
	{
	}
	
	public  DeadCell()
	{
		super(0.5, SpaceAgent.SQUARE);
		_init();
		this.setColor(SpaceAgent.BLUE);
	}
	
	public void step(SimulationTime time)
	{
		double n = 0;
		
		ArrayList<Cell> _result = Observer.getDefaultSpace().getAgents(this.getPosition(), 1.5, Cell.class);
		n = _result.size();
		if ((n == 3.0))
		{
			Cell _agent;
			_agent = new Cell();
			_agent.moveToSpace(this.getSpace(), this.getPosition());
			this.die();
		}
	}
	
}
