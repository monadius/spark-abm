package RSV_bronchiole;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class Epithelial extends SpaceAgent
{
	protected double life = 0;
	protected boolean infected = false;
	protected double infectedAge = 0;
	
	
	public void _init()
	{
	}
	
	public  Epithelial()
	{
		super(0.5, SpaceAgent.SQUARE);
		_init();
		this.life = RandomHelper.random(50.0, 100.0);
		this.infected = false;
		this.setColor(SpaceAgent.GREEN);
	}
	
	public void becomeSyncytia()
	{
		Syncytia _agent;
		_agent = new Syncytia();
		_agent.moveToSpace(this.getSpace(), this.getPosition());
		this.die();
	}
	
	public void becomeDead()
	{
		Dead _agent;
		_agent = new Dead();
		_agent.moveToSpace(this.getSpace(), this.getPosition());
		this.die();
	}
	
	public void heal()
	{
		Vector p = new Vector();
		double y = 0;
		Vector pos = new Vector();
		double x = 0;
		
		x = Math.round(RandomHelper.random(-1.0, 1.0));
		y = Math.round(RandomHelper.random(-1.0, 1.0));
		pos = this.getPosition();
		p = (new Vector((pos.x + x), (pos.y + y), 0.0));
		ArrayList<Cleared> _result = Observer.getDefaultSpace().getAgents(p, 0.1, Cleared.class);
		if ((_result.size() > 0.0))
		{
			Epithelial _object = new Epithelial();
			if (_object != null)
			{
				Epithelial __agent3 = _object;
				__agent3.jump(p);
			}
			ArrayList<Cleared> _result1 = Observer.getDefaultSpace().getAgents(p, 0.1, Cleared.class);
			if (_result1 != null)
			{
				for (int _i = 0; _i < _result1.size(); _i++)
				{
					Cleared __agent3 = _result1.get(_i);
					__agent3.die();
				}
			}
		}
	}
	
	public void step(SimulationTime tick)
	{
		if (this.infected)
		{
			this.life -= 1.0;
			RSVModel.gmcsf.addValue(this, 0.01);
			RSVModel.il8.addValue(this, 0.01);
			this.infectedAge = (this.life * 2.0);
			this.setColor((new Vector((this.infectedAge / 255.0), 0.0, 0.0)));
			if ((0.5 > RandomHelper.random(100.0)))
			{
				if ((RSVModel.tnf.getValue(this) > RSVModel.tnfThreshold))
				{
					int _nn = (int)(Math.round((RandomHelper.random(RSVModel.virusProliferationRate) / 2.0)));
					Vector _pos = this.getPosition();
					Space _space = this.getSpace();
					Virus[] _agents = new Virus[_nn];
					for (int _i = 0; _i < _nn; _i++)
					{
						_agents[_i] = new Virus();
						_agents[_i].moveToSpace(_space, _pos);
					}
				}
				else
				{
					int _nn1 = (int)(RandomHelper.random(RSVModel.virusProliferationRate));
					Vector _pos1 = this.getPosition();
					Space _space1 = this.getSpace();
					Virus[] _agents1 = new Virus[_nn1];
					for (int _i1 = 0; _i1 < _nn1; _i1++)
					{
						_agents1[_i1] = new Virus();
						_agents1[_i1].moveToSpace(_space1, _pos1);
					}
				}
			}
			if ((this.life < 0.0))
			{
				this.becomeSyncytia();
			}
		}
		if ((0.01 > RandomHelper.random(1.0)))
		{
			this.heal();
		}
		if (((RSVModel.il10.getValue(this) > RSVModel.il10HealingThreshold) && (RSVModel.il10HealingProb > RandomHelper.random(1.0))))
		{
			this.heal();
		}
		if (((RSVModel.tnf.getValue(this) > RSVModel.tnfThreshold) && (RSVModel.tnfSens > RandomHelper.random(1.0))))
		{
			this.becomeDead();
		}
	}
	
}
