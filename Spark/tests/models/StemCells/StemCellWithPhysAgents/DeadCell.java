package StemCellWithPhysAgents;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;
import org.spark.startup.ABMModel;

public class DeadCell extends Agent
{
	protected PhysAgent physAgent = null;
	protected double time = 0;
	
	
	public void _init()
	{
	}
	
	public  DeadCell()
	{
		_init();
	}
	
	public void init(PhysAgent physAgent)
	{
		this.physAgent = physAgent;
		this.physAgent.setColor(SpaceAgent.GREY);
		physAgent.staticFlag = true;
		this.time = (StemCellModel.timeUntilRemoved + RandomHelper.random((StemCellModel.timeUntilRemoved * 0.1)));
	}
	
	public void step(SimulationTime tick)
	{
		this.time -= 1.0;
		if ((this.time < 0.0))
		{
			Agent _tmp = this.physAgent;
			
			if (_tmp != null)
			{
				_tmp.die();
			}
			this.die();
			return;
		}
	}
	
}
