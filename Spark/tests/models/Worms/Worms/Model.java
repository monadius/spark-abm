package Worms;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class Model extends SparkModel
{
	public static double wormSpeed = 0;
	public static double wanderSpeed = 0;
	public static double wormsNumber = 0;
	public static double obstaclesNumber = 0;
	public static double velocityCoefficient = 0;
	public static double separationCoefficient = 0;
	public static double collisionCoefficient = 0;
	public static double dt = 0;
	public static double springCoefficient = 0;
	public static double food = 0;
	public static Grid medium = null;
	
	
	public void _init()
	{
		Model.food = 0.0;
		Model.medium = Observer.getDefaultSpace().addDataLayer("medium", GridFactory.createGrid((int)(60.0), (int)(60.0)));
	}
	
	public boolean end(long tick)
	{
		ArrayList<PhysAgent> _result = Observer.getInstance().getAgentsList(PhysAgent.class);
		if (_result != null)
		{
			for (int _i = 0; _i < _result.size(); _i++)
			{
				PhysAgent __agent2 = _result.get(_i);
				__agent2.makeStep(Model.dt);
			}
		}
		Model.food = Model.medium.getTotalNumber();
		return false;
	}
	
	public void setup()
	{
		StandardSpace __space = new StandardSpace(-10.0, 10.0, -10.0, 10.0, true, true);
		Observer.getInstance().addSpace("space", __space);
		_init();
		Model.medium.setValue(1.0);
		int _nn = (int)(Model.wormsNumber);
		Worm[] _objects = new Worm[_nn];
		for (int _i = 0; _i < _nn; _i++)
		{
			_objects[_i] = new Worm();
		}
		if (_objects != null)
		{
			for (int _i1 = 0; _i1 < _objects.length; _i1++)
			{
				Worm __agent2 = _objects[_i1];
				Vector position = new Vector();
				Vector color = new Vector();
				double n = 0;
				
				color.x = RandomHelper.random(0.8);
				color.y = RandomHelper.random(0.8);
				color.z = RandomHelper.random(0.8);
				position = Vector.randomVector2d(-10.0, 10.0);
				n = (RandomHelper.random(4.0) + 3.0);
				__agent2.init(n, position, color, 0.3);
			}
		}
		int _nn1 = (int)(Model.obstaclesNumber);
		Obstacle[] _objects1 = new Obstacle[_nn1];
		for (int _i2 = 0; _i2 < _nn1; _i2++)
		{
			_objects1[_i2] = new Obstacle();
		}
		if (_objects1 != null)
		{
			for (int _i3 = 0; _i3 < _objects1.length; _i3++)
			{
				Obstacle __agent2 = _objects1[_i3];
				__agent2.setRandomPosition();
			}
		}
	}
	
	public void addWorm()
	{
		Worm _object = new Worm();
		if (_object != null)
		{
			Worm __agent2 = _object;
			Vector position = new Vector();
			Vector color = new Vector();
			double n = 0;
			
			color.x = RandomHelper.random(0.8);
			color.y = RandomHelper.random(0.8);
			color.z = RandomHelper.random(0.8);
			position = Vector.randomVector2d(-10.0, 10.0);
			n = (RandomHelper.random(4.0) + 3.0);
			__agent2.init(n, position, color, 0.3);
		}
	}
	
	public boolean begin(long tick)
	{
		return false;
	}
	
	
	public static double get_wormSpeed()
	{
		return wormSpeed;
	}
	
	public static void set_wormSpeed(Double value)
	{
		wormSpeed = value;
	}
	
	public static double get_wanderSpeed()
	{
		return wanderSpeed;
	}
	
	public static void set_wanderSpeed(Double value)
	{
		wanderSpeed = value;
	}
	
	public static double get_wormsNumber()
	{
		return wormsNumber;
	}
	
	public static void set_wormsNumber(Double value)
	{
		wormsNumber = value;
	}
	
	public static double get_obstaclesNumber()
	{
		return obstaclesNumber;
	}
	
	public static void set_obstaclesNumber(Double value)
	{
		obstaclesNumber = value;
	}
	
	public static double get_velocityCoefficient()
	{
		return velocityCoefficient;
	}
	
	public static void set_velocityCoefficient(Double value)
	{
		velocityCoefficient = value;
	}
	
	public static double get_separationCoefficient()
	{
		return separationCoefficient;
	}
	
	public static void set_separationCoefficient(Double value)
	{
		separationCoefficient = value;
	}
	
	public static double get_collisionCoefficient()
	{
		return collisionCoefficient;
	}
	
	public static void set_collisionCoefficient(Double value)
	{
		collisionCoefficient = value;
	}
	
	public static double get_dt()
	{
		return dt;
	}
	
	public static void set_dt(Double value)
	{
		dt = value;
	}
	
	public static double get_springCoefficient()
	{
		return springCoefficient;
	}
	
	public static void set_springCoefficient(Double value)
	{
		springCoefficient = value;
	}
	
	public static double get_food()
	{
		return food;
	}
	
	public static void set_food(Double value)
	{
		food = value;
	}
	
}
