package StemCellWithPhysAgents;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;
import org.spark.startup.ABMModel;

public class Agent4 extends CellAgent
{
	
	
	public void _init()
	{
	}
	
	public  Agent4()
	{
		_init();
		this.init(0.5, SpaceAgent.GREEN);
	}
	
	public CellAgent createNew()
	{
		Agent4 _object = new Agent4();
		return _object;
	}
	
	public void migration(double tick)
	{
		Vector f = new Vector();
		
		f = StemCellModel.oxygen.getSmoothGradient(this.position());
		this.applyForce((new Vector((new Vector((new Vector(f)).negate())).mul(6.0))).mul(StemCellModel.centralForce));
	}
	
}
