package StemCellWithPhysAgents;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;
import org.spark.startup.ABMModel;

public class CellAgent extends Agent
{
	protected PhysAgent physAgent = null;
	protected double life = 0;
	
	
	public void _init()
	{
	}
	
	public  CellAgent()
	{
		_init();
		this.life = StemCellModel.initialLife;
	}
	
	public void init(double radius, Vector color)
	{
		PhysAgent _object = new PhysAgent();
		this.physAgent = _object;
		((CircleNode) this.physAgent.getNode()).setRadius(radius);
		this.physAgent.setColor(color);
	}
	
	public void moveTo(Vector pos)
	{
		this.physAgent.jump(pos);
	}
	
	public void setRandomPosition()
	{
		double y = 0;
		double x = 0;
		
		x = RandomHelper.random(((BoundedSpace) Observer.getDefaultSpace()).getXMin(), ((BoundedSpace) Observer.getDefaultSpace()).getXMax());
		y = RandomHelper.random(((BoundedSpace) Observer.getDefaultSpace()).getYMin(), ((BoundedSpace) Observer.getDefaultSpace()).getYMax());
		this.moveTo((new Vector(x, y, 0.0)));
	}
	
	public void setColor(Vector c)
	{
		this.physAgent.setColor(c);
	}
	
	public void applyForce(Vector f)
	{
		this.physAgent.applyForce(f);
	}
	
	public Vector position()
	{
		return this.physAgent.getPosition();
	}
	
	public CellAgent createNew()
	{
		CellAgent _object = new CellAgent();
		return _object;
	}
	
	public void migration(double tick)
	{
	}
	
	public void transformation(double tick)
	{
	}
	
	public void proliferate()
	{
		CellAgent newAgent = null;
		Vector p = new Vector();
		
		Vector _v;
		_v = new Vector(this.physAgent.getPosition());
		_v.add(Vector.randomVector2d(0.1));
		p = _v;
		newAgent = this.createNew();
		if (newAgent != null)
		{
			CellAgent __agent2 = newAgent;
			__agent2.moveTo(p);
		}
	}
	
	public void die()
	{
		Agent _tmp = this.physAgent;
		
		if (_tmp != null)
		{
			_tmp.die();
		}
		super.die();
	}
	
	public void step(SimulationTime time)
	{
		this.mainStep(time.getTick());
	}
	
	public void mainStep(double tick)
	{
		Vector p = new Vector();
		
		this.transformation(tick);
		this.migration(tick);
		this.applyForce(Vector.randomVector2d(1.0));
		p = this.physAgent.getPosition();
		if ((StemCellModel.oxygen.getValue(p) < StemCellModel.oxygenThreshold))
		{
			this.life -= 1.0;
			if ((this.life < 0.0))
			{
				this.becomeDead();
				return;
			}
		}
		StemCellModel.oxygen.addValue(p, (-StemCellModel.oxygenConsumption));
	}
	
	public void becomeDead()
	{
		DeadCell _object = new DeadCell();
		if (_object != null)
		{
			DeadCell __agent2 = _object;
			__agent2.init(this.physAgent);
		}
		super.die();
	}
	
}
