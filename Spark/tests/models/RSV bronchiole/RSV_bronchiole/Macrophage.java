package RSV_bronchiole;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.startup.ABMModel;

public class Macrophage extends CellAgent
{
	protected boolean infected = false;
	
	
	public void _init()
	{
	}
	
	public  Macrophage()
	{
		_init();
		((CircleNode) this.getNode()).setRadius((6.0 / 15.0));
		this.age = RandomHelper.random(50.0, 90.0);
		this.setColor(SpaceAgent.BLUE);
		this.infected = false;
	}
	
	public void becomeDeadMac()
	{
		DeadInf _agent;
		_agent = new DeadInf();
		_agent.moveToSpace(this.getSpace(), this.getPosition());
		this.die();
	}
	
	public void step(long tick)
	{
		double il10Val = 0;
		double hmgb1Val = 0;
		
		this.wiggle(0.05);
		this.sniff(RSVModel.gmcsf, 0.1);
		il10Val = RSVModel.il10.getValue(this);
		hmgb1Val = RSVModel.hmgb1.getValue(this);
		if (((RSVModel.hmgb1.getValue(this) > RSVModel.macrophageActivationThresh) && (RSVModel.inflammActivationProb > RandomHelper.random(1.0))))
		{
			RSVModel.tnf.addValue(this, (1.0 / (il10Val + 0.01)));
			RSVModel.il10.addValue(this, 2.0);
		}
		ArrayList<Dead> _result = this.getSpace().getAgents(this, Dead.class);
		if (_result != null)
		{
			for (int _i = 0; _i < _result.size(); _i++)
			{
				Dead __agent2 = _result.get(_i);
				__agent2.becomeClear();
			}
		}
		ArrayList<DeadInf> _result1 = this.getSpace().getAgents(this, DeadInf.class);
		if (_result1 != null)
		{
			for (int _i1 = 0; _i1 < _result1.size(); _i1++)
			{
				DeadInf __agent2 = _result1.get(_i1);
				__agent2.die();
			}
		}
		this.age -= 1.0;
		if (((this.age < 0.0) && (this.infected == false)))
		{
			this.die();
		}
	}
	
}
