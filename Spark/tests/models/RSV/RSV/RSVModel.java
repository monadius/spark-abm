package RSV;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class RSVModel extends SparkModel
{
	public static double totalMac = 0;
	public static double initVirus = 0;
	public static double totalNeu = 0;
	public static double proliferationRate = 0;
	public static double neutrophilIl8Production = 0;
	public static double neutrophilIl9Production = 0;
	public static double virusEngulfProb = 0;
	public static double macrophageProProduction = 0;
	public static double macrophageAntiProduction = 0;
	public static double healingThreshold = 0;
	public static double macrophageActivationProb = 0;
	public static double virusNumber = 0;
	public static double macrophageNumber = 0;
	public static double neutrophilNumber = 0;
	public static Grid chemokine = null;
	public static Grid il8 = null;
	public static Grid il9 = null;
	public static Grid pro = null;
	public static Grid anti = null;
	
	
	public void _init()
	{
		RSVModel.totalMac = 1.0;
		RSVModel.totalNeu = 1.0;
		RSVModel.neutrophilIl8Production = 1.0;
		RSVModel.neutrophilIl9Production = 1.0;
		RSVModel.virusEngulfProb = 0.5;
		RSVModel.macrophageProProduction = 1.0;
		RSVModel.macrophageAntiProduction = 1.0;
		RSVModel.macrophageActivationProb = 0.1;
		RSVModel.virusNumber = 0.0;
		RSVModel.macrophageNumber = 0.0;
		RSVModel.neutrophilNumber = 0.0;
		RSVModel.chemokine = Observer.getDefaultSpace().addDataLayer("chemokine", GridFactory.createGrid((int)(((BoundedSpace) Observer.getDefaultSpace()).getXSize()), (int)(((BoundedSpace) Observer.getDefaultSpace()).getYSize())));
		RSVModel.il8 = Observer.getDefaultSpace().addDataLayer("il8", GridFactory.createGrid((int)(((BoundedSpace) Observer.getDefaultSpace()).getXSize()), (int)(((BoundedSpace) Observer.getDefaultSpace()).getYSize())));
		RSVModel.il9 = Observer.getDefaultSpace().addDataLayer("il9", GridFactory.createGrid((int)(((BoundedSpace) Observer.getDefaultSpace()).getXSize()), (int)(((BoundedSpace) Observer.getDefaultSpace()).getYSize())));
		RSVModel.pro = Observer.getDefaultSpace().addDataLayer("pro", GridFactory.createGrid((int)(((BoundedSpace) Observer.getDefaultSpace()).getXSize()), (int)(((BoundedSpace) Observer.getDefaultSpace()).getYSize())));
		RSVModel.anti = Observer.getDefaultSpace().addDataLayer("anti", GridFactory.createGrid((int)(((BoundedSpace) Observer.getDefaultSpace()).getXSize()), (int)(((BoundedSpace) Observer.getDefaultSpace()).getYSize())));
	}
	
	public boolean end(long tick)
	{
		RSVModel.pro.multiply(0.95);
		RSVModel.anti.diffuse(0.95);
		RSVModel.anti.multiply(0.95);
		RSVModel.chemokine.diffuse(0.95);
		RSVModel.chemokine.multiply(0.95);
		RSVModel.il8.diffuse(0.95);
		RSVModel.il8.multiply(0.95);
		RSVModel.virusNumber = Observer.getInstance().getAgentsNumber(Virus.class);
		RSVModel.macrophageNumber = Observer.getInstance().getAgentsNumber(Macrophage.class);
		RSVModel.neutrophilNumber = Observer.getInstance().getAgentsNumber(Neutrophil.class);
		return false;
	}
	
	public void setup()
	{
		StandardSpace __space = new StandardSpace(-20.0, 20.0, -20.0, 20.0, true, true);
		Observer.getInstance().addSpace("space", __space);
		_init();
		int _nn = (int)(RSVModel.initVirus);
		Virus[] _objects = new Virus[_nn];
		for (int _i = 0; _i < _nn; _i++)
		{
			_objects[_i] = new Virus();
		}
		if (_objects != null)
		{
			for (int _i1 = 0; _i1 < _objects.length; _i1++)
			{
				Virus __agent2 = _objects[_i1];
				__agent2.setRandomPosition();
			}
		}
		double _tmpto1 = ((BoundedSpace) Observer.getDefaultSpace()).getXMax();
		double _tmpstep1 = 1.0;
		
		for (double x = (((BoundedSpace) Observer.getDefaultSpace()).getXMin() + 0.5); x <= _tmpto1; x += _tmpstep1)
		{
			
			double _tmpto = ((BoundedSpace) Observer.getDefaultSpace()).getYMax();
			double _tmpstep = 1.0;
			
			for (double y = (((BoundedSpace) Observer.getDefaultSpace()).getYMin() + 0.5); y <= _tmpto; y += _tmpstep)
			{
				
				Epithelial _object = new Epithelial();
				if (_object != null)
				{
					Epithelial __agent4 = _object;
					__agent4.jump((new Vector(x, y, 0.0)));
				}
			}
		}
		int _nn1 = (int)(RSVModel.totalNeu);
		Neutrophil[] _objects1 = new Neutrophil[_nn1];
		for (int _i2 = 0; _i2 < _nn1; _i2++)
		{
			_objects1[_i2] = new Neutrophil();
		}
		if (_objects1 != null)
		{
			for (int _i3 = 0; _i3 < _objects1.length; _i3++)
			{
				Neutrophil __agent2 = _objects1[_i3];
				__agent2.setRandomPosition();
			}
		}
		int _nn2 = (int)(RSVModel.totalMac);
		Macrophage[] _objects2 = new Macrophage[_nn2];
		for (int _i4 = 0; _i4 < _nn2; _i4++)
		{
			_objects2[_i4] = new Macrophage();
		}
		if (_objects2 != null)
		{
			for (int _i5 = 0; _i5 < _objects2.length; _i5++)
			{
				Macrophage __agent2 = _objects2[_i5];
				__agent2.setRandomPosition();
			}
		}
	}
	
	public boolean begin(long tick)
	{
		return false;
	}
	
	
	public static double get_totalMac()
	{
		return totalMac;
	}
	
	public static void set_totalMac(Double value)
	{
		totalMac = value;
	}
	
	public static double get_initVirus()
	{
		return initVirus;
	}
	
	public static void set_initVirus(Double value)
	{
		initVirus = value;
	}
	
	public static double get_totalNeu()
	{
		return totalNeu;
	}
	
	public static void set_totalNeu(Double value)
	{
		totalNeu = value;
	}
	
	public static double get_proliferationRate()
	{
		return proliferationRate;
	}
	
	public static void set_proliferationRate(Double value)
	{
		proliferationRate = value;
	}
	
	public static double get_neutrophilIl8Production()
	{
		return neutrophilIl8Production;
	}
	
	public static void set_neutrophilIl8Production(Double value)
	{
		neutrophilIl8Production = value;
	}
	
	public static double get_neutrophilIl9Production()
	{
		return neutrophilIl9Production;
	}
	
	public static void set_neutrophilIl9Production(Double value)
	{
		neutrophilIl9Production = value;
	}
	
	public static double get_virusEngulfProb()
	{
		return virusEngulfProb;
	}
	
	public static void set_virusEngulfProb(Double value)
	{
		virusEngulfProb = value;
	}
	
	public static double get_macrophageProProduction()
	{
		return macrophageProProduction;
	}
	
	public static void set_macrophageProProduction(Double value)
	{
		macrophageProProduction = value;
	}
	
	public static double get_macrophageAntiProduction()
	{
		return macrophageAntiProduction;
	}
	
	public static void set_macrophageAntiProduction(Double value)
	{
		macrophageAntiProduction = value;
	}
	
	public static double get_healingThreshold()
	{
		return healingThreshold;
	}
	
	public static void set_healingThreshold(Double value)
	{
		healingThreshold = value;
	}
	
	public static double get_macrophageActivationProb()
	{
		return macrophageActivationProb;
	}
	
	public static void set_macrophageActivationProb(Double value)
	{
		macrophageActivationProb = value;
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
	
	
	
	
	
}
