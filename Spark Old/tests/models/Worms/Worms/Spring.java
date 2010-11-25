package Worms;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class Spring extends Agent
{
	protected PhysAgent end1 = null;
	protected PhysAgent end2 = null;
	protected double k = 0;
	protected double l = 0;
	
	
	public void _init()
	{
	}
	
	public  Spring()
	{
		_init();
	}
	
	public void init(PhysAgent a, PhysAgent b, double k)
	{
		this.end1 = a;
		this.end2 = b;
		this.k = k;
		this.l = Observer.getDefaultSpace().getVector(a, b).length();
	}
	
	public void applyForces()
	{
		Vector d = new Vector();
		double dist = 0;
		
		Object _tmpLeft = this.end1;
		Object _tmpRight = null;
		Object _tmpLeft1 = this.end2;
		Object _tmpRight1 = null;
		if (((_tmpLeft != null ? _tmpLeft.equals(_tmpRight) : _tmpRight == null) || (_tmpLeft1 != null ? _tmpLeft1.equals(_tmpRight1) : _tmpRight1 == null)))
		{
			return;
		}
		d = Observer.getDefaultSpace().getVector(this.end1, this.end2);
		dist = d.length();
		d.normalize();
		d.mul(((dist - this.l) * this.k));
		this.end1.applyForce(d);
		this.end2.applyForce((new Vector(d)).negate());
	}
	
	public void step(SimulationTime time)
	{
		this.applyForces();
	}
	
}
