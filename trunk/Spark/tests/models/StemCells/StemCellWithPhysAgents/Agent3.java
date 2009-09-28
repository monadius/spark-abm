package StemCellWithPhysAgents;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;
import org.spark.startup.ABMModel;

public class Agent3 extends CellAgent
{
	
	
	public void _init()
	{
	}
	
	public  Agent3()
	{
		_init();
		this.init(0.7, SpaceAgent.BLUE);
	}
	
	public CellAgent createNew()
	{
		Agent3 _object = new Agent3();
		return _object;
	}
	
	public void migration(double tick)
	{
		Vector f = new Vector();
		
		f = StemCellModel.oxygen.getSmoothGradient(this.position());
		this.applyForce((new Vector((new Vector((new Vector(f)).negate())).mul(2.0))).mul(StemCellModel.centralForce));
	}
	
}
