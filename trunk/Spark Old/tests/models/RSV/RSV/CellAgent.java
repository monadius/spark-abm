package RSV;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class CellAgent extends SpaceAgent
{
	protected double age = 0;
	protected double heading = 0;
	
	
	public void _init()
	{
	}
	
	public  CellAgent()
	{
		_init();
		this.heading = RandomHelper.random(360.0);
		this.setColor(SpaceAgent.BLACK);
	}
	
	public void setAge(double age)
	{
		this.age = age;
	}
	
	public void setHeading(double heading)
	{
		this.heading = heading;
	}
	
	public void jump(double number)
	{
		this.move(Vector.getVector(number, this.heading));
	}
	
	public void wiggle(double size)
	{
		this.heading += RandomHelper.random(-45.0, 45.0);
		this.jump(size);
	}
	
	public void sniff(Grid data, double jumpSize)
	{
		Vector v = new Vector();
		double ahead = 0;
		Vector p = new Vector();
		double left = 0;
		double right = 0;
		
		p = this.getPosition();
		Vector _v;
		_v = new Vector(Vector.getVector(1.0, this.heading));
		_v.add(p);
		v = _v;
		ahead = data.getValue(v);
		Vector _v1;
		_v1 = new Vector(Vector.getVector(1.0, (this.heading - 90.0)));
		_v1.add(p);
		v = _v1;
		right = data.getValue(v);
		Vector _v2;
		_v2 = new Vector(Vector.getVector(1.0, (this.heading + 90.0)));
		_v2.add(p);
		v = _v2;
		left = data.getValue(v);
		if (((right >= ahead) && (right >= left)))
		{
			this.heading -= 90.0;
		}
		else
		{
			if ((left >= ahead))
			{
				this.heading += 90.0;
			}
		}
		this.jump(jumpSize);
	}
	
}
