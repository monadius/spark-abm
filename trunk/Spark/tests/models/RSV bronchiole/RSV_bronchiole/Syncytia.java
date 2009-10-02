package RSV_bronchiole;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class Syncytia extends SpaceAgent
{
	protected double age = 0;
	
	
	public void _init()
	{
	}
	
	public  Syncytia()
	{
		super(0.5, SpaceAgent.SQUARE);
		_init();
		this.setColor(SpaceAgent.BLACK);
		this.age = RandomHelper.random(5.0, 15.0);
	}
	
	public void becomeDead()
	{
		Dead _agent;
		_agent = new Dead();
		_agent.moveToSpace(this.getSpace(), this.getPosition());
		this.die();
	}
	
	public void step(SimulationTime tick)
	{
		this.age -= 1.0;
		RSVModel.hmgb1.addValue(this, 0.05);
		if ((this.age < 0.0))
		{
			RSVModel.hmgb1.addValue(this, 0.1);
			this.becomeDead();
		}
	}
	
}
