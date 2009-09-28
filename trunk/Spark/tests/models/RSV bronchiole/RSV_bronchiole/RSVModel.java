package RSV_bronchiole;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.startup.ABMModel;

public class RSVModel implements ABMModel
{
	public static double initMac = 0;
	public static double initVirus = 0;
	public static double virusProliferationRate = 0;
	public static double virusLifespanMax = 0;
	public static double macrophageActivationThresh = 0;
	public static double inflammActivationProb = 0;
	public static double inflammThreshold = 0;
	public static double il10HealingProb = 0;
	public static double il10HealingThreshold = 0;
	public static double tnfSens = 0;
	public static double tnfThreshold = 0;
	public static double macrophageTnfProduction = 0;
	public static double macrophageIl10Production = 0;
	public static double virusNumber = 0;
	public static double macrophageNumber = 0;
	public static double neutrophilNumber = 0;
	public static double deadanddebrisNumber = 0;
	public static double epithelialNumber = 0;
	public static double clearedNumber = 0;
	public static double totalHealth = 0;
	public static double totalGmcsf = 0;
	public static double totalIl8 = 0;
	public static double totalTnf = 0;
	public static double totalIl10 = 0;
	public static double totalHmgb1 = 0;
	public static double lumenArea = 0;
	public static Grid gmcsf = null;
	public static Grid il8 = null;
	public static Grid tnf = null;
	public static Grid il10 = null;
	public static Grid hmgb1 = null;
	public static double segments = 0;
	public static double centralForce = 0;
	public static double accumDamageCoefficient = 0;
	public static double velocityCoefficient = 0;
	public static double separationCoefficient = 0;
	public static double collisionCoefficient = 0;
	public static double dt = 0;
	public static double springCoefficient = 0;
	protected double originalArea = 0;
	public static Space anotherSpace = null;
	
	
	public void _init()
	{
		RSVModel.initMac = 5.0;
		RSVModel.virusNumber = 0.0;
		RSVModel.macrophageNumber = 0.0;
		RSVModel.neutrophilNumber = 0.0;
		RSVModel.deadanddebrisNumber = 0.0;
		RSVModel.epithelialNumber = 0.0;
		RSVModel.clearedNumber = 0.0;
		RSVModel.totalHealth = 0.0;
		RSVModel.totalGmcsf = 0.0;
		RSVModel.totalIl8 = 0.0;
		RSVModel.totalTnf = 0.0;
		RSVModel.totalIl10 = 0.0;
		RSVModel.totalHmgb1 = 0.0;
		RSVModel.lumenArea = 0.0;
		RSVModel.gmcsf = Observer.getDefaultSpace().addDataLayer("gmcsf", new Grid((int)(((BoundedSpace) Observer.getDefaultSpace()).getXSize()), (int)(((BoundedSpace) Observer.getDefaultSpace()).getYSize())));
		RSVModel.il8 = Observer.getDefaultSpace().addDataLayer("il8", new Grid((int)(((BoundedSpace) Observer.getDefaultSpace()).getXSize()), (int)(((BoundedSpace) Observer.getDefaultSpace()).getYSize())));
		RSVModel.tnf = Observer.getDefaultSpace().addDataLayer("tnf", new Grid((int)(((BoundedSpace) Observer.getDefaultSpace()).getXSize()), (int)(((BoundedSpace) Observer.getDefaultSpace()).getYSize())));
		RSVModel.il10 = Observer.getDefaultSpace().addDataLayer("il10", new Grid((int)(((BoundedSpace) Observer.getDefaultSpace()).getXSize()), (int)(((BoundedSpace) Observer.getDefaultSpace()).getYSize())));
		RSVModel.hmgb1 = Observer.getDefaultSpace().addDataLayer("hmgb1", new Grid((int)(((BoundedSpace) Observer.getDefaultSpace()).getXSize()), (int)(((BoundedSpace) Observer.getDefaultSpace()).getYSize())));
		Space _space = Observer.getInstance().addSpace("Another Space", new StandardSpace(-10.0, 10.0, -10.0, 10.0, false, false));
		RSVModel.anotherSpace = _space;
	}
	
	public double computeLumenArea()
	{
		double S = 0;
		PhysAgent next = null;
		PhysAgent current = null;
		double n = 0;
		ArrayList<PhysAgent> physAgents = null;
		
		ArrayList<PhysAgent> _result = Observer.getInstance().getAgentsList(PhysAgent.class);
		physAgents = _result;
		n = physAgents.size();
		S = 0.0;
		double _tmpto = (n - 1.0);
		double _tmpstep = 1.0;
		
		for (double i = 0.0; i <= _tmpto; i += _tmpstep)
		{
			Vector p2 = new Vector();
			Vector p1 = new Vector();
			
			current = physAgents.get((int)(i));
			if (((i + 1.0) < n))
			{
				next = physAgents.get((int)((i + 1.0)));
			}
			else
			{
				next = physAgents.get((int)(0.0));
			}
			p1 = current.getPosition();
			p2 = next.getPosition();
			S += Math.abs(((p1.x * p2.y) - (p2.x * p1.y)));
		}
		return (S / 2.0);
	}
	
	public double estimateLumenArea()
	{
		double S = 0;
		
		S = 0.0;
		ArrayList<PhysAgent> _result = Observer.getInstance().getAgentsList(PhysAgent.class);
		if (_result != null)
		{
			for (int _i = 0; _i < _result.size(); _i++)
			{
				PhysAgent __agent2 = _result.get(_i);
				S += __agent2.getPosition().length();
			}
		}
		return (S / Observer.getInstance().getAgentsNumber(PhysAgent.class));
	}
	
	public boolean end(long tick)
	{
		double m = 0;
		
		ArrayList<PhysAgent> _result = Observer.getInstance().getAgentsList(PhysAgent.class);
		if (_result != null)
		{
			for (int _i = 0; _i < _result.size(); _i++)
			{
				PhysAgent __agent2 = _result.get(_i);
				__agent2.makeStep(RSVModel.dt);
			}
		}
		RSVModel.tnf.diffuse(0.95);
		RSVModel.tnf.multiply(0.95);
		RSVModel.il10.diffuse(0.95);
		RSVModel.il10.multiply(0.95);
		RSVModel.gmcsf.diffuse(0.95);
		RSVModel.gmcsf.multiply(0.95);
		RSVModel.il8.diffuse(0.95);
		RSVModel.il8.multiply(0.95);
		RSVModel.hmgb1.diffuse(0.95);
		RSVModel.hmgb1.multiply(0.95);
		m = Math.round(RandomHelper.random(2.0));
		if ((10.0 > RandomHelper.random(100.0)))
		{
			int _nn = (int)(m);
			Macrophage[] _objects = new Macrophage[_nn];
			for (int _i1 = 0; _i1 < _nn; _i1++)
			{
				_objects[_i1] = new Macrophage();
			}
			if (_objects != null)
			{
				for (int _i2 = 0; _i2 < _objects.length; _i2++)
				{
					Macrophage __agent3 = _objects[_i2];
					__agent3.setRandomPosition();
				}
			}
		}
		if (((RSVModel.totalGmcsf > RSVModel.inflammThreshold) && (50.0 > RandomHelper.random(100.0))))
		{
			double n1 = 0;
			double macChemo = 0;
			double m1 = 0;
			double neuChemo = 0;
			
			macChemo = (RSVModel.totalGmcsf + RSVModel.totalHmgb1);
			neuChemo = (RSVModel.totalGmcsf + RSVModel.totalIl8);
			m1 = ((Math.round(macChemo) * RSVModel.inflammActivationProb) / 10.0);
			n1 = ((Math.round(neuChemo) * RSVModel.inflammActivationProb) / 10.0);
			int _nn1 = (int)(m1);
			Macrophage[] _objects1 = new Macrophage[_nn1];
			for (int _i3 = 0; _i3 < _nn1; _i3++)
			{
				_objects1[_i3] = new Macrophage();
			}
			if (_objects1 != null)
			{
				for (int _i4 = 0; _i4 < _objects1.length; _i4++)
				{
					Macrophage __agent3 = _objects1[_i4];
					__agent3.setRandomPosition();
				}
			}
			int _nn2 = (int)(n1);
			Neutrophil[] _objects2 = new Neutrophil[_nn2];
			for (int _i5 = 0; _i5 < _nn2; _i5++)
			{
				_objects2[_i5] = new Neutrophil();
			}
			if (_objects2 != null)
			{
				for (int _i6 = 0; _i6 < _objects2.length; _i6++)
				{
					Neutrophil __agent3 = _objects2[_i6];
					__agent3.setRandomPosition();
				}
			}
		}
		RSVModel.virusNumber = Observer.getInstance().getAgentsNumber(Virus.class);
		RSVModel.macrophageNumber = Observer.getInstance().getAgentsNumber(Macrophage.class);
		RSVModel.neutrophilNumber = Observer.getInstance().getAgentsNumber(Neutrophil.class);
		RSVModel.deadanddebrisNumber = (Observer.getInstance().getAgentsNumber(Dead.class) + Observer.getInstance().getAgentsNumber(DeadInf.class));
		RSVModel.epithelialNumber = Observer.getInstance().getAgentsNumber(Epithelial.class);
		RSVModel.clearedNumber = Observer.getInstance().getAgentsNumber(Cleared.class);
		RSVModel.totalHealth = (RSVModel.epithelialNumber + RSVModel.clearedNumber);
		RSVModel.totalGmcsf = RSVModel.gmcsf.getTotalNumber();
		RSVModel.totalIl8 = RSVModel.il8.getTotalNumber();
		RSVModel.totalTnf = RSVModel.tnf.getTotalNumber();
		RSVModel.totalIl10 = RSVModel.il10.getTotalNumber();
		RSVModel.totalHmgb1 = RSVModel.hmgb1.getTotalNumber();
		RSVModel.lumenArea = ((this.computeLumenArea() * 100.0) / this.originalArea);
		return false;
	}
	
	public void setup()
	{
		FixedPhysAgent[] shadowAgents = null;
		PhysAgent[] physAgents = null;
		FixedPhysAgent[] fixedAgents = null;
		
		StandardSpace __space = new StandardSpace(-60.0, 60.0, -20.0, 20.0, false, true);
		Observer.getInstance().addSpace("space", __space);
		_init();
		if (RSVModel.anotherSpace != null)
		{
			Space __agent2 = RSVModel.anotherSpace;
			double r = 0;
			
			int _nn = (int)(RSVModel.segments);
			PhysAgent[] _agents = new PhysAgent[_nn];
			for (int _i = 0; _i < _nn; _i++)
			{
				_agents[_i] = new PhysAgent();
				_agents[_i].moveToSpace(__agent2, new Vector());
			}
			physAgents = _agents;
			int _nn1 = (int)(RSVModel.segments);
			FixedPhysAgent[] _agents1 = new FixedPhysAgent[_nn1];
			for (int _i1 = 0; _i1 < _nn1; _i1++)
			{
				_agents1[_i1] = new FixedPhysAgent();
				_agents1[_i1].moveToSpace(__agent2, new Vector());
			}
			fixedAgents = _agents1;
			int _nn2 = (int)(RSVModel.segments);
			FixedPhysAgent[] _agents2 = new FixedPhysAgent[_nn2];
			for (int _i2 = 0; _i2 < _nn2; _i2++)
			{
				_agents2[_i2] = new FixedPhysAgent();
				_agents2[_i2].moveToSpace(__agent2, new Vector());
			}
			shadowAgents = _agents2;
			r = ((Math.floor(Math.PI) * 5.0) / RSVModel.segments);
			double _tmpto = (RSVModel.segments - 1.0);
			double _tmpstep = 1.0;
			
			for (double i = 0.0; i <= _tmpto; i += _tmpstep)
			{
				PhysAgent physAgent = null;
				FixedPhysAgent shadowAgent = null;
				FixedPhysAgent fixedAgent = null;
				Vector pos = new Vector();
				
				physAgent = physAgents[(int)(i)];
				fixedAgent = fixedAgents[(int)(i)];
				shadowAgent = shadowAgents[(int)(i)];
				physAgent.x1 = (((BoundedSpace) Observer.getDefaultSpace()).getXMin() + ((((BoundedSpace) Observer.getDefaultSpace()).getXSize() * i) / RSVModel.segments));
				physAgent.x2 = (((BoundedSpace) Observer.getDefaultSpace()).getXMin() + ((((BoundedSpace) Observer.getDefaultSpace()).getXSize() * (i + 1.0)) / RSVModel.segments));
				pos = Vector.getVector(5.0, ((i * 360.0) / RSVModel.segments));
				if (physAgent != null)
				{
					PhysAgent __agent4 = physAgent;
					__agent4.jump(pos);
					((CircleNode) __agent4.getNode()).setRadius(r);
				}
				if (shadowAgent != null)
				{
					FixedPhysAgent __agent4 = shadowAgent;
					__agent4.jump(pos);
					((CircleNode) __agent4.getNode()).setRadius(r);
					__agent4.setColor(new Vector(0.5, 0.5, 0.5));
				}
				pos = Vector.getVector(9.0, ((i * 360.0) / RSVModel.segments));
				if (fixedAgent != null)
				{
					FixedPhysAgent __agent4 = fixedAgent;
					__agent4.jump(pos);
					((CircleNode) __agent4.getNode()).setRadius(0.5);
				}
			}
		}
		double _tmpto1 = (RSVModel.segments - 1.0);
		double _tmpstep1 = 1.0;
		
		for (double i = 0.0; i <= _tmpto1; i += _tmpstep1)
		{
			PhysAgent physAgent2 = null;
			PhysAgent physAgent = null;
			FixedPhysAgent fixedAgent = null;
			
			physAgent = physAgents[(int)(i)];
			fixedAgent = fixedAgents[(int)(i)];
			Spring _object = new Spring();
			if (_object != null)
			{
				Spring __agent3 = _object;
				__agent3.init(physAgent, fixedAgent, RSVModel.springCoefficient);
			}
			if ((i > 0.0))
			{
				physAgent2 = physAgents[(int)((i - 1.0))];
			}
			else
			{
				physAgent2 = physAgents[(int)((RSVModel.segments - 1.0))];
			}
			Spring _object1 = new Spring();
			if (_object1 != null)
			{
				Spring __agent3 = _object1;
				__agent3.init(physAgent, physAgent2, RSVModel.springCoefficient);
			}
		}
		int _nn3 = (int)(RSVModel.initMac);
		Macrophage[] _objects = new Macrophage[_nn3];
		for (int _i3 = 0; _i3 < _nn3; _i3++)
		{
			_objects[_i3] = new Macrophage();
		}
		if (_objects != null)
		{
			for (int _i4 = 0; _i4 < _objects.length; _i4++)
			{
				Macrophage __agent2 = _objects[_i4];
				__agent2.setRandomPosition();
			}
		}
		int _nn4 = (int)(RSVModel.initVirus);
		Virus[] _objects1 = new Virus[_nn4];
		for (int _i5 = 0; _i5 < _nn4; _i5++)
		{
			_objects1[_i5] = new Virus();
		}
		if (_objects1 != null)
		{
			for (int _i6 = 0; _i6 < _objects1.length; _i6++)
			{
				Virus __agent2 = _objects1[_i6];
				__agent2.setRandomPosition();
			}
		}
		double _tmpto3 = ((BoundedSpace) Observer.getDefaultSpace()).getXMax();
		double _tmpstep3 = 1.0;
		
		for (double x = (((BoundedSpace) Observer.getDefaultSpace()).getXMin() + 0.5); x <= _tmpto3; x += _tmpstep3)
		{
			
			double _tmpto2 = ((BoundedSpace) Observer.getDefaultSpace()).getYMax();
			double _tmpstep2 = 1.0;
			
			for (double y = (((BoundedSpace) Observer.getDefaultSpace()).getYMin() + 0.5); y <= _tmpto2; y += _tmpstep2)
			{
				
				Epithelial _object2 = new Epithelial();
				if (_object2 != null)
				{
					Epithelial __agent4 = _object2;
					__agent4.jump((new Vector(x, y, 0.0)));
				}
			}
		}
		this.originalArea = this.computeLumenArea();
	}
	
	public boolean begin(long tick)
	{
		return false;
	}
	
	
	public static double get_initMac()
	{
		return initMac;
	}
	
	public static void set_initMac(Double value)
	{
		initMac = value;
	}
	
	public static double get_initVirus()
	{
		return initVirus;
	}
	
	public static void set_initVirus(Double value)
	{
		initVirus = value;
	}
	
	public static double get_virusProliferationRate()
	{
		return virusProliferationRate;
	}
	
	public static void set_virusProliferationRate(Double value)
	{
		virusProliferationRate = value;
	}
	
	public static double get_virusLifespanMax()
	{
		return virusLifespanMax;
	}
	
	public static void set_virusLifespanMax(Double value)
	{
		virusLifespanMax = value;
	}
	
	public static double get_macrophageActivationThresh()
	{
		return macrophageActivationThresh;
	}
	
	public static void set_macrophageActivationThresh(Double value)
	{
		macrophageActivationThresh = value;
	}
	
	public static double get_inflammActivationProb()
	{
		return inflammActivationProb;
	}
	
	public static void set_inflammActivationProb(Double value)
	{
		inflammActivationProb = value;
	}
	
	public static double get_inflammThreshold()
	{
		return inflammThreshold;
	}
	
	public static void set_inflammThreshold(Double value)
	{
		inflammThreshold = value;
	}
	
	public static double get_il10HealingProb()
	{
		return il10HealingProb;
	}
	
	public static void set_il10HealingProb(Double value)
	{
		il10HealingProb = value;
	}
	
	public static double get_il10HealingThreshold()
	{
		return il10HealingThreshold;
	}
	
	public static void set_il10HealingThreshold(Double value)
	{
		il10HealingThreshold = value;
	}
	
	public static double get_tnfSens()
	{
		return tnfSens;
	}
	
	public static void set_tnfSens(Double value)
	{
		tnfSens = value;
	}
	
	public static double get_tnfThreshold()
	{
		return tnfThreshold;
	}
	
	public static void set_tnfThreshold(Double value)
	{
		tnfThreshold = value;
	}
	
	public static double get_macrophageTnfProduction()
	{
		return macrophageTnfProduction;
	}
	
	public static void set_macrophageTnfProduction(Double value)
	{
		macrophageTnfProduction = value;
	}
	
	public static double get_macrophageIl10Production()
	{
		return macrophageIl10Production;
	}
	
	public static void set_macrophageIl10Production(Double value)
	{
		macrophageIl10Production = value;
	}
	
	public static double get_virusNumber()
	{
		return virusNumber;
	}
	
	public static void set_virusNumber(Double value)
	{
		virusNumber = value;
	}
	
	public static double get_macrophageNumber()
	{
		return macrophageNumber;
	}
	
	public static void set_macrophageNumber(Double value)
	{
		macrophageNumber = value;
	}
	
	public static double get_neutrophilNumber()
	{
		return neutrophilNumber;
	}
	
	public static void set_neutrophilNumber(Double value)
	{
		neutrophilNumber = value;
	}
	
	public static double get_deadanddebrisNumber()
	{
		return deadanddebrisNumber;
	}
	
	public static void set_deadanddebrisNumber(Double value)
	{
		deadanddebrisNumber = value;
	}
	
	public static double get_epithelialNumber()
	{
		return epithelialNumber;
	}
	
	public static void set_epithelialNumber(Double value)
	{
		epithelialNumber = value;
	}
	
	public static double get_clearedNumber()
	{
		return clearedNumber;
	}
	
	public static void set_clearedNumber(Double value)
	{
		clearedNumber = value;
	}
	
	public static double get_totalHealth()
	{
		return totalHealth;
	}
	
	public static void set_totalHealth(Double value)
	{
		totalHealth = value;
	}
	
	public static double get_totalGmcsf()
	{
		return totalGmcsf;
	}
	
	public static void set_totalGmcsf(Double value)
	{
		totalGmcsf = value;
	}
	
	public static double get_totalIl8()
	{
		return totalIl8;
	}
	
	public static void set_totalIl8(Double value)
	{
		totalIl8 = value;
	}
	
	public static double get_totalTnf()
	{
		return totalTnf;
	}
	
	public static void set_totalTnf(Double value)
	{
		totalTnf = value;
	}
	
	public static double get_totalIl10()
	{
		return totalIl10;
	}
	
	public static void set_totalIl10(Double value)
	{
		totalIl10 = value;
	}
	
	public static double get_totalHmgb1()
	{
		return totalHmgb1;
	}
	
	public static void set_totalHmgb1(Double value)
	{
		totalHmgb1 = value;
	}
	
	public static double get_lumenArea()
	{
		return lumenArea;
	}
	
	public static void set_lumenArea(Double value)
	{
		lumenArea = value;
	}
	
	
	
	
	
	
	public static double get_segments()
	{
		return segments;
	}
	
	public static void set_segments(Double value)
	{
		segments = value;
	}
	
	public static double get_centralForce()
	{
		return centralForce;
	}
	
	public static void set_centralForce(Double value)
	{
		centralForce = value;
	}
	
	public static double get_accumDamageCoefficient()
	{
		return accumDamageCoefficient;
	}
	
	public static void set_accumDamageCoefficient(Double value)
	{
		accumDamageCoefficient = value;
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
	
}
