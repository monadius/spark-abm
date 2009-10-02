package StemCellWithPhysAgents;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class Agent2 extends CellAgent
{
	
	
	public void _init()
	{
	}
	
	public  Agent2()
	{
		_init();
		this.init(0.6, SpaceAgent.RED);
	}
	
	public CellAgent createNew()
	{
		Agent2 _object = new Agent2();
		return _object;
	}
	
	public void migration(double tick)
	{
		Vector f = new Vector();
		
		f = StemCellModel.oxygen.getSmoothGradient(this.position());
		this.applyForce((new Vector((new Vector((new Vector(f)).negate())).mul(4.0))).mul(StemCellModel.centralForce));
	}
	
}
