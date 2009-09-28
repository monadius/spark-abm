package RSV_bronchiole;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.startup.ABMModel;

public class DeadInf extends SpaceAgent
{
	protected double age = 0;
	
	
	public void _init()
	{
	}
	
	public  DeadInf()
	{
		_init();
		((CircleNode) this.getNode()).setRadius((5.0 / 15.0));
		this.setColor(SpaceAgent.GREY);
		this.age = RandomHelper.random(5.0, 15.0);
	}
	
	public void step(long tick)
	{
		this.age -= 1.0;
		if ((this.age < 0.0))
		{
			RSVModel.hmgb1.addValue(this, 0.05);
		}
	}
	
}
