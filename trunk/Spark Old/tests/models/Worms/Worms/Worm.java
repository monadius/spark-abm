package Worms;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class Worm extends Agent
{
	protected PhysAgent head = null;
	protected PhysAgent[] tail = null;
	protected double wanderAngle = 0;
	
	
	public void _init()
	{
	}
	
	public  Worm()
	{
		_init();
		this.wanderAngle = RandomHelper.random(360.0);
	}
	
	public void init(double n, Vector position, Vector color, double radius)
	{
		int _nn = (int)(n);
		PhysAgent[] _objects = new PhysAgent[_nn];
		for (int _i = 0; _i < _nn; _i++)
		{
			_objects[_i] = new PhysAgent();
		}
		this.tail = _objects;
		this.head = this.tail[(int)(0.0)];
		double _tmpto = (n - 1.0);
		double _tmpstep = 1.0;
		
		for (double i = 0.0; i <= _tmpto; i += _tmpstep)
		{
			
			if (this.tail[(int)(i)] != null)
			{
				PhysAgent __agent3 = this.tail[(int)(i)];
				Vector _v;
				_v = new Vector(position);
				_v.add((new Vector(new Vector(1.0, 0.0, 0.0))).mul(((2.0 * i) * radius)));
				__agent3.jump(_v);
				__agent3.setColor(color);
				((CircleNode) __agent3.getNode()).setRadius(radius);
			}
			if ((i > 0.0))
			{
				Spring _object = new Spring();
				if (_object != null)
				{
					Spring __agent4 = _object;
					__agent4.init(this.tail[(int)(i)], this.tail[(int)((i - 1.0))], Model.springCoefficient);
				}
			}
		}
	}
	
	public void step(SimulationTime time)
	{
		this.wanderAngle += RandomHelper.random((-Model.wanderSpeed), Model.wanderSpeed);
		if (this.head != null)
		{
			PhysAgent __agent2 = this.head;
			double v = 0;
			
			v = Model.medium.getValue(__agent2);
			__agent2.applyForce(Vector.getVector((Model.wormSpeed * Math.exp(((-v) * 10.0))), this.wanderAngle));
			Model.medium.setValue(__agent2, (v * 0.99));
		}
	}
	
}
