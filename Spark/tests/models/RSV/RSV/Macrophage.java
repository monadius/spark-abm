package RSV;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class Macrophage extends CellAgent
{
	
	
	public void _init()
	{
	}
	
	public  Macrophage()
	{
		_init();
		((CircleNode) this.getNode()).setRadius((6.0 / 15.0));
		this.age = RandomHelper.random(50.0, 90.0);
		this.setColor(SpaceAgent.BLUE);
	}
	
	public void step(SimulationTime time)
	{
		double antiVal = 0;
		
		this.wiggle(0.05);
		antiVal = RSVModel.anti.getValue(this);
		RSVModel.pro.addValue(this, (RSVModel.macrophageProProduction / Math.pow((1.0 + (10.0 * antiVal)), 2.0)));
		RSVModel.anti.addValue(this, RSVModel.macrophageAntiProduction);
		this.sniff(RSVModel.chemokine, 0.1);
		ArrayList<Virus> _result = this.getSpace().getAgents(this, Virus.class);
		if (_result != null)
		{
			for (int _i = 0; _i < _result.size(); _i++)
			{
				Virus __agent2 = _result.get(_i);
				if ((RandomHelper.random(1.0) < RSVModel.virusEngulfProb))
				{
					__agent2.die();
				}
			}
		}
		this.age -= 1.0;
		if ((this.age < 0.0))
		{
			this.die();
		}
	}
	
}
