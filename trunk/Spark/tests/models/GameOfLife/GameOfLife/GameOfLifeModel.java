package GameOfLife;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class GameOfLifeModel extends SparkModel
{
	public static double density = 0;
	
	
	public void _init()
	{
	}
	
	public void setup()
	{
		StandardSpace __space = new StandardSpace(-20.5, 20.5, -20.5, 20.5, true, true);
		Observer.getInstance().addSpace("space", __space);
		_init();
		double _tmpto1 = 20.0;
		double _tmpstep1 = 1.0;
		
		for (double i = -20.0; i <= _tmpto1; i += _tmpstep1)
		{
			
			double _tmpto = 20.0;
			double _tmpstep = 1.0;
			
			for (double j = -20.0; j <= _tmpto; j += _tmpstep)
			{
				SpaceAgent cell = null;
				
				if ((RandomHelper.random(1.0) < GameOfLifeModel.density))
				{
					Cell _object = new Cell();
					cell = _object;
				}
				else
				{
					DeadCell _object1 = new DeadCell();
					cell = _object1;
				}
				cell.jump((new Vector(i, j, 0.0)));
			}
		}
	}
	
	public boolean end(long tick)
	{
		return false;
	}
	
	public boolean begin(long tick)
	{
		return false;
	}
	
	
	public static double get_density()
	{
		return density;
	}
	
	public static void set_density(Double value)
	{
		density = value;
	}
}
