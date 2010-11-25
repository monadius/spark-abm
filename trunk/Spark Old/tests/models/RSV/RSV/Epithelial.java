package RSV;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class Epithelial extends SpaceAgent
{
	protected double life = 0;
	protected boolean activation = false;
	
	
	public void _init()
	{
	}
	
	public  Epithelial()
	{
		super(0.5, SpaceAgent.SQUARE);
		_init();
		this.life = 100.0;
		this.setColor(SpaceAgent.GREEN);
	}
	
	public void step(SimulationTime tick)
	{
		double virusNum = 0;
		
		if (((tick.getTick() % 10.0) == 0.0))
		{
			this.macCreate();
		}
		ArrayList<Virus> _result = this.getSpace().getAgents(this, Virus.class);
		virusNum = _result.size();
		this.life -= virusNum;
		if ((this.life < 100.0))
		{
			this.activation = true;
			this.life += RandomHelper.random(0.5);
			if ((RSVModel.anti.getValue(this) > RSVModel.healingThreshold))
			{
				this.life += (RandomHelper.random(1.0) + 1.0);
				if ((this.life > 100.0))
				{
					this.activation = false;
					this.life = 100.0;
				}
			}
		}
		if (this.activation)
		{
			RSVModel.chemokine.addValue(this, 1.0);
			RSVModel.il8.addValue(this, 1.0);
			if ((this.life < -200.0))
			{
				this.die();
				return;
			}
		}
		if ((this.life > 0.0))
		{
			this.setColor((new Vector(0.0, (this.life / 100.0), 0.0)));
		}
		else
		{
			if ((this.life > (-200.0)))
			{
				this.setColor((new Vector(((-this.life) / 100.0), 0.0, 0.0)));
			}
			else
			{
				this.setColor(new Vector(1.0, 1.0, 1.0));
			}
		}
	}
	
	public void macCreate()
	{
		double neuNum = 0;
		double macNum = 0;
		
		if ((this.life > -200.0))
		{
			macNum = Math.floor(((100.0 - this.life) / 100.0));
		}
		else
		{
			macNum = 2.0;
		}
		neuNum = (macNum * 3.0);
		double _tmpto = neuNum;
		double _tmpstep = 1.0;
		
		for (double i = 1.0; i <= _tmpto; i += _tmpstep)
		{
			
			if ((RandomHelper.random(1.0) < 0.1))
			{
				Neutrophil _agent;
				_agent = new Neutrophil();
				_agent.moveToSpace(this.getSpace(), this.getPosition());
			}
		}
		double _tmpto1 = macNum;
		double _tmpstep1 = 1.0;
		
		for (double i = 1.0; i <= _tmpto1; i += _tmpstep1)
		{
			
			if (((RSVModel.macrophageActivationProb / 10.0) > RandomHelper.random(1.0)))
			{
				Macrophage _agent1;
				_agent1 = new Macrophage();
				_agent1.moveToSpace(this.getSpace(), this.getPosition());
			}
		}
	}
	
}
