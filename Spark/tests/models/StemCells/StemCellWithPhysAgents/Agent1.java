package StemCellWithPhysAgents;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;
import org.spark.startup.ABMModel;

public class Agent1 extends CellAgent
{
	
	
	public void _init()
	{
	}
	
	public  Agent1()
	{
		_init();
		this.init(0.5, SpaceAgent.YELLOW);
	}
	
	public CellAgent createNew()
	{
		Agent1 _object = new Agent1();
		return _object;
	}
	
	public void migration(double tick)
	{
		Vector f = new Vector();
		
		f = StemCellModel.oxygen.getSmoothGradient(this.position());
		this.applyForce((new Vector((new Vector((new Vector(f)).negate())).mul(10.0))).mul(StemCellModel.centralForce));
	}
	
	public void transformation(double tick)
	{
		if ((StemCellModel.numberOfYellowCells < 10.0))
		{
			return;
		}
		if ((RandomHelper.random(1.0) < StemCellModel.transformationProbability))
		{
			CellAgent a = null;
			double k = 0;
			
			k = Math.floor(RandomHelper.random(4.0));
			if ((k == 0.0))
			{
				Agent1 _object = new Agent1();
				a = _object;
			}
			else
			{
				if ((k == 1.0))
				{
					Agent2 _object1 = new Agent2();
					a = _object1;
				}
				else
				{
					if ((k == 2.0))
					{
						Agent3 _object2 = new Agent3();
						a = _object2;
					}
					else
					{
						Agent4 _object3 = new Agent4();
						a = _object3;
					}
				}
			}
			a.moveTo(this.position());
			this.die();
		}
	}
	
	public void step(SimulationTime tick)
	{
		if ((RandomHelper.random(1.0) < StemCellModel.proliferationProbability))
		{
			this.proliferate();
		}
		this.mainStep(tick.getTick());
	}
	
}
