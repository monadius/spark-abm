package StemCellWithPhysAgents;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class StemCellModel extends SparkModel
{
	public static double velocityCoefficient = 0;
	public static double separationCoefficient = 0;
	public static double adhesionCoefficient = 0;
	public static double collisionCoefficient = 0;
	public static double centralForce = 0;
	public static double maxForce = 0;
	public static double randomCoefficient = 0;
	public static double initialLife = 0;
	public static double proliferationProbability = 0;
	public static double dt = 0;
	public static double initialAgentNumber = 0;
	public static Grid oxygen = null;
	public static double oxygenProduction = 0;
	public static double oxygenConsumption = 0;
	public static double oxygenThreshold = 0;
	public static double transformationProbability = 0;
	public static double timeUntilRemoved = 0;
	public static double initialRadius = 0;
	public static double numberOfAliveCells = 0;
	public static double numberOfCells = 0;
	public static double numberOfDeadCells = 0;
	public static double numberOfYellowCells = 0;
	public static double numberOfRedCells = 0;
	public static double numberOfGreenCells = 0;
	public static double numberOfBlueCells = 0;
	public static double totalOxygen = 0;
	
	
	public void _init()
	{
		StemCellModel.oxygen = Observer.getDefaultSpace().addDataLayer("oxygen", new Grid((int)(((BoundedSpace) Observer.getDefaultSpace()).getXSize()), (int)(((BoundedSpace) Observer.getDefaultSpace()).getYSize())));
		StemCellModel.numberOfAliveCells = 0.0;
		StemCellModel.numberOfCells = 0.0;
		StemCellModel.numberOfDeadCells = 0.0;
		StemCellModel.numberOfYellowCells = 0.0;
		StemCellModel.numberOfRedCells = 0.0;
		StemCellModel.numberOfGreenCells = 0.0;
		StemCellModel.numberOfBlueCells = 0.0;
		StemCellModel.totalOxygen = 0.0;
	}
	
	public boolean begin(long tick)
	{
		return false;
	}
	
	public boolean end(long tick)
	{
		ArrayList<PhysAgent> _result = Observer.getInstance().getAgentsList(PhysAgent.class);
		if (_result != null)
		{
			for (int _i = 0; _i < _result.size(); _i++)
			{
				PhysAgent __agent2 = _result.get(_i);
				__agent2.makeStep(StemCellModel.dt);
			}
		}
		StemCellModel.numberOfCells = Observer.getInstance().getAgentsNumber(PhysAgent.class);
		StemCellModel.numberOfDeadCells = Observer.getInstance().getAgentsNumber(DeadCell.class);
		StemCellModel.numberOfAliveCells = (StemCellModel.numberOfCells - StemCellModel.numberOfDeadCells);
		StemCellModel.numberOfYellowCells = Observer.getInstance().getAgentsNumber(Agent1.class);
		StemCellModel.numberOfRedCells = Observer.getInstance().getAgentsNumber(Agent2.class);
		StemCellModel.numberOfBlueCells = Observer.getInstance().getAgentsNumber(Agent3.class);
		StemCellModel.numberOfGreenCells = Observer.getInstance().getAgentsNumber(Agent4.class);
		StemCellModel.totalOxygen = StemCellModel.oxygen.getTotalNumber();
		for (double _i1 = 0; _i1 < 1.0; _i1++)
		{
			StemCellModel.oxygen.diffuse(1.0);
		}
		StemCellModel.oxygen.multiply(0.999);
		return false;
	}
	
	public void setup()
	{
		double n = 0;
		double size = 0;
		
		StandardSpace __space = new StandardSpace(-40.0, 40.0, -40.0, 40.0, false, false);
		Observer.getInstance().addSpace("space", __space);
		_init();
		StemCellModel.oxygen.setValue(2.0);
		n = 100.0;
		double _tmpto = n;
		double _tmpstep = 1.0;
		
		for (double i = 1.0; i <= _tmpto; i += _tmpstep)
		{
			double angle = 0;
			
			angle = ((i * 360.0) / n);
			OxygenSource _object = new OxygenSource();
			if (_object != null)
			{
				OxygenSource __agent3 = _object;
				__agent3.jump(Vector.getVector(40.0, angle));
			}
		}
		size = StemCellModel.initialRadius;
		int _nn = (int)(StemCellModel.initialAgentNumber);
		Agent1[] _objects = new Agent1[_nn];
		for (int _i = 0; _i < _nn; _i++)
		{
			_objects[_i] = new Agent1();
		}
		if (_objects != null)
		{
			for (int _i1 = 0; _i1 < _objects.length; _i1++)
			{
				Agent1 __agent2 = _objects[_i1];
				__agent2.moveTo(Vector.randomVector2d(size));
			}
		}
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
	
	public static double get_adhesionCoefficient()
	{
		return adhesionCoefficient;
	}
	
	public static void set_adhesionCoefficient(Double value)
	{
		adhesionCoefficient = value;
	}
	
	public static double get_collisionCoefficient()
	{
		return collisionCoefficient;
	}
	
	public static void set_collisionCoefficient(Double value)
	{
		collisionCoefficient = value;
	}
	
	public static double get_centralForce()
	{
		return centralForce;
	}
	
	public static void set_centralForce(Double value)
	{
		centralForce = value;
	}
	
	public static double get_maxForce()
	{
		return maxForce;
	}
	
	public static void set_maxForce(Double value)
	{
		maxForce = value;
	}
	
	public static double get_randomCoefficient()
	{
		return randomCoefficient;
	}
	
	public static void set_randomCoefficient(Double value)
	{
		randomCoefficient = value;
	}
	
	public static double get_initialLife()
	{
		return initialLife;
	}
	
	public static void set_initialLife(Double value)
	{
		initialLife = value;
	}
	
	public static double get_proliferationProbability()
	{
		return proliferationProbability;
	}
	
	public static void set_proliferationProbability(Double value)
	{
		proliferationProbability = value;
	}
	
	public static double get_dt()
	{
		return dt;
	}
	
	public static void set_dt(Double value)
	{
		dt = value;
	}
	
	public static double get_initialAgentNumber()
	{
		return initialAgentNumber;
	}
	
	public static void set_initialAgentNumber(Double value)
	{
		initialAgentNumber = value;
	}
	
	
	public static double get_oxygenProduction()
	{
		return oxygenProduction;
	}
	
	public static void set_oxygenProduction(Double value)
	{
		oxygenProduction = value;
	}
	
	public static double get_oxygenConsumption()
	{
		return oxygenConsumption;
	}
	
	public static void set_oxygenConsumption(Double value)
	{
		oxygenConsumption = value;
	}
	
	public static double get_oxygenThreshold()
	{
		return oxygenThreshold;
	}
	
	public static void set_oxygenThreshold(Double value)
	{
		oxygenThreshold = value;
	}
	
	public static double get_transformationProbability()
	{
		return transformationProbability;
	}
	
	public static void set_transformationProbability(Double value)
	{
		transformationProbability = value;
	}
	
	public static double get_timeUntilRemoved()
	{
		return timeUntilRemoved;
	}
	
	public static void set_timeUntilRemoved(Double value)
	{
		timeUntilRemoved = value;
	}
	
	public static double get_initialRadius()
	{
		return initialRadius;
	}
	
	public static void set_initialRadius(Double value)
	{
		initialRadius = value;
	}
	
	public static double get_numberOfAliveCells()
	{
		return numberOfAliveCells;
	}
	
	public static void set_numberOfAliveCells(Double value)
	{
		numberOfAliveCells = value;
	}
	
	public static double get_numberOfCells()
	{
		return numberOfCells;
	}
	
	public static void set_numberOfCells(Double value)
	{
		numberOfCells = value;
	}
	
	public static double get_numberOfDeadCells()
	{
		return numberOfDeadCells;
	}
	
	public static void set_numberOfDeadCells(Double value)
	{
		numberOfDeadCells = value;
	}
	
	public static double get_numberOfYellowCells()
	{
		return numberOfYellowCells;
	}
	
	public static void set_numberOfYellowCells(Double value)
	{
		numberOfYellowCells = value;
	}
	
	public static double get_numberOfRedCells()
	{
		return numberOfRedCells;
	}
	
	public static void set_numberOfRedCells(Double value)
	{
		numberOfRedCells = value;
	}
	
	public static double get_numberOfGreenCells()
	{
		return numberOfGreenCells;
	}
	
	public static void set_numberOfGreenCells(Double value)
	{
		numberOfGreenCells = value;
	}
	
	public static double get_numberOfBlueCells()
	{
		return numberOfBlueCells;
	}
	
	public static void set_numberOfBlueCells(Double value)
	{
		numberOfBlueCells = value;
	}
	
	public static double get_totalOxygen()
	{
		return totalOxygen;
	}
	
	public static void set_totalOxygen(Double value)
	{
		totalOxygen = value;
	}
}
