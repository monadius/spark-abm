package Worms;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class PhysAgent extends SpaceAgent
{
	protected Vector force = new Vector();
	protected Vector velocity = new Vector();
	protected double mass = 0;
	protected double group = 0;
	protected static double groupCounter = 0;
	protected double x1 = 0;
	protected double x2 = 0;
	
	
	public void _init()
	{
	}
	
	public  PhysAgent()
	{
		super(0.5, SpaceAgent.CIRCLE);
		_init();
		this.setColor(SpaceAgent.GREEN);
		this.mass = 1.0;
		this.group = this.groupCounter;
		this.groupCounter += 1.0;
	}
	
	public void applyCollisionForce(SpaceAgent a)
	{
		Vector d = new Vector();
		double r = 0;
		double dist = 0;
		
		Vector _v;
		_v = new Vector(this.getPosition());
		_v.sub(a.getPosition());
		d = _v;
		dist = d.length();
		if ((dist < 1.0E-6))
		{
			dist = 1.0E-6;
		}
		r = ((this.getNode().getRelativeSize() + a.getNode().getRelativeSize()) - dist);
		if ((r < 0.0))
		{
			return;
		}
		d.mul(((r / dist) * Model.collisionCoefficient));
		this.force.add(d);
	}
	
	public void applyCollisionForces()
	{
		Space s = null;
		
		s = this.getNode().getSpace();
		ArrayList<PhysAgent> _result = this.getSpace().getAgents(this, PhysAgent.class);
		if (_result != null)
		{
			for (int _i = 0; _i < _result.size(); _i++)
			{
				PhysAgent __agent2 = _result.get(_i);
				if ((this.group == __agent2.group))
				{
					continue;
				}
				this.applyCollisionForce(__agent2);
			}
		}
		ArrayList<Obstacle> _result1 = this.getSpace().getAgents(this, Obstacle.class);
		if (_result1 != null)
		{
			for (int _i1 = 0; _i1 < _result1.size(); _i1++)
			{
				Obstacle __agent2 = _result1.get(_i1);
				this.applyCollisionForce(__agent2);
			}
		}
	}
	
	public void applySeparationForces()
	{
		Vector f = new Vector();
		Space s = null;
		
		s = this.getNode().getSpace();
		ArrayList<PhysAgent> _result = s.getAgents(this.getPosition(), 2.0, PhysAgent.class);
		if (_result != null)
		{
			for (int _i = 0; _i < _result.size(); _i++)
			{
				PhysAgent __agent2 = _result.get(_i);
				Vector v = new Vector();
				double d = 0;
				
				if ((__agent2.group == this.group))
				{
					continue;
				}
				v = Observer.getDefaultSpace().getVector(__agent2, this);
				d = v.length();
				if ((d < 1.0E-6))
				{
					continue;
				}
				f.add((new Vector((new Vector(v)).div(Math.pow(d, 2.0)))).mul(Model.separationCoefficient));
			}
		}
		this.applyForce(f);
	}
	
	public void integrate(double dt)
	{
		this.velocity.add((new Vector(this.force)).mul((dt / this.mass)));
		this.move((new Vector(this.velocity)).mul(dt));
	}
	
	public void applyForce(Vector f)
	{
		this.force.add(f);
	}
	
	public void clearForces()
	{
		this.force = new Vector(0.0, 0.0, 0.0);
	}
	
	public void makeStep(double dt)
	{
		if ((Model.collisionCoefficient > 0.0))
		{
			this.applyCollisionForces();
		}
		if ((Model.separationCoefficient > 0.0))
		{
			this.applySeparationForces();
		}
		this.integrate(dt);
		this.velocity.mul(Model.velocityCoefficient);
		this.clearForces();
	}
	
}
