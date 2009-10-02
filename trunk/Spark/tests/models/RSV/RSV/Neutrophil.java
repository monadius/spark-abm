package RSV;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

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
		this.age = RandomHelper.random(10.0, 20.0);
	}
	
	public void step(SimulationTime tick)
	{
		this.wiggle(0.05);
		this.sniff(RSVModel.il8, 0.1);
		RSVModel.il8.addValue(this, RSVModel.neutrophilIl8Production);
		RSVModel.il9.addValue(this, RSVModel.neutrophilIl9Production);
		this.age -= 1.0;
		if ((this.age < 0.0))
		{
			this.die();
		}
	}
	
}
