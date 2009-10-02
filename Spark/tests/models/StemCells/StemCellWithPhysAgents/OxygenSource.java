package StemCellWithPhysAgents;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class OxygenSource extends SpaceAgent
{
	
	
	public void _init()
	{
	}
	
	public  OxygenSource()
	{
		super(0.5, SpaceAgent.TORUS);
		_init();
	}
	
	public void step(SimulationTime time)
	{
		StemCellModel.oxygen.addValue(this, StemCellModel.oxygenProduction);
	}
	
}
