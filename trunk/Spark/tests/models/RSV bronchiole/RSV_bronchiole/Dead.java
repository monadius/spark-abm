package RSV_bronchiole;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class Dead extends SpaceAgent
{
	protected double age = 0;
	
	
	public void _init()
	{
	}
	
	public  Dead()
	{
		super(0.5, SpaceAgent.SQUARE);
		_init();
		this.setColor(SpaceAgent.GREY);
		this.age = RandomHelper.random(5.0, 15.0);
	}
	
	public void becomeClear()
	{
		Cleared _agent;
		_agent = new Cleared();
		_agent.moveToSpace(this.getSpace(), this.getPosition());
		this.die();
	}
	
	public void step(SimulationTime tick)
	{
		this.age -= 1.0;
		if ((this.age < 0.0))
		{
			RSVModel.hmgb1.addValue(this, 0.05);
		}
	}
	
}
