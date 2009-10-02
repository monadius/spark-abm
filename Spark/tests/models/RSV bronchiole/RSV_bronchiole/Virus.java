package RSV_bronchiole;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class Virus extends CellAgent
{
	protected static Vector virusColor = new Vector();
	
	
	public void _init()
	{
		this.virusColor = new Vector(1.0, 1.0, 0.0);
	}
	
	public  Virus()
	{
		_init();
		((CircleNode) this.getNode()).setRadius((4.0 / 15.0));
		this.age = RandomHelper.random(10.0, RSVModel.virusLifespanMax);
		this.setColor(this.virusColor);
	}
	
	public void step(SimulationTime tick)
	{
		this.wiggleVirus(0.1);
		this.age -= 1.0;
		if ((this.age < 0.0))
		{
			this.die();
			return;
		}
		ArrayList<Epithelial> _result = this.getSpace().getAgents(this, Epithelial.class);
		if (_result != null)
		{
			for (int _i = 0; _i < _result.size(); _i++)
			{
				Epithelial __agent2 = _result.get(_i);
				if (((__agent2.infected == false) && (90.0 > RandomHelper.random(100.0))))
				{
					__agent2.setColor(new Vector(1.0, 0.0, 0.0));
					__agent2.infected = true;
				}
			}
		}
	}
	
	public void wiggleVirus(double stepSize)
	{
		Vector oldPosition = new Vector();
		
		oldPosition = this.getPosition();
		this.wiggle(stepSize);
		ArrayList<Epithelial> _result = this.getSpace().getAgents(this, Epithelial.class);
		if ((_result.size() == 0.0))
		{
			this.jump(oldPosition);
		}
	}
	
}
