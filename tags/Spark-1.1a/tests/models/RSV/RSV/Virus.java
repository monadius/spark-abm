package RSV;

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
		this.age = RandomHelper.random(40.0, 50.0);
		this.setColor(this.virusColor);
	}
	
	public void step(SimulationTime tick)
	{
		double epilNum = 0;
		
		this.wiggleVirus(0.05);
		ArrayList<Epithelial> _result = this.getSpace().getAgents(this, Epithelial.class);
		epilNum = _result.size();
		if ((epilNum == 0.0))
		{
			this.die();
			return;
		}
		this.age -= 1.0;
		if ((this.age < 0.0))
		{
			this.die();
			return;
		}
		if (((tick.getTick() % 20.0) == 0.0))
		{
			this.proliferate();
		}
	}
	
	public void proliferate()
	{
		if ((RSVModel.proliferationRate > RandomHelper.random(1.0)))
		{
			Virus _agent;
			_agent = new Virus();
			_agent.moveToSpace(this.getSpace(), this.getPosition());
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
