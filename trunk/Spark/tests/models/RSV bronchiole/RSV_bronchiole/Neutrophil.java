package RSV_bronchiole;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.startup.ABMModel;

public class Neutrophil extends CellAgent
{
	
	
	public void _init()
	{
	}
	
	public  Neutrophil()
	{
		_init();
		((CircleNode) this.getNode()).setRadius((5.0 / 15.0));
		this.setColor(SpaceAgent.CYAN);
		this.age = RandomHelper.random(30.0, 70.0);
	}
	
	public void becomeDeadNeu()
	{
		DeadInf _agent;
		_agent = new DeadInf();
		_agent.moveToSpace(this.getSpace(), this.getPosition());
		this.die();
	}
	
	public void step(long tick)
	{
		double deadNum = 0;
		
		this.wiggle(0.05);
		this.sniff(RSVModel.il8, 0.1);
		RSVModel.il8.addValue(this, 0.01);
		ArrayList<Dead> _result = this.getSpace().getAgents(this, Dead.class);
		deadNum = _result.size();
		if ((deadNum > 0.0))
		{
			this.age -= 5.0;
		}
		ArrayList<Dead> _result1 = this.getSpace().getAgents(this, Dead.class);
		if (_result1 != null)
		{
			for (int _i = 0; _i < _result1.size(); _i++)
			{
				Dead __agent2 = _result1.get(_i);
				__agent2.becomeClear();
			}
		}
		this.age -= 1.0;
		if ((this.age < 0.0))
		{
			this.becomeDeadNeu();
		}
	}
	
}
