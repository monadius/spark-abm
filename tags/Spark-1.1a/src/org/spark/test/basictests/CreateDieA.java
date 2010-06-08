package org.spark.test.basictests;

import org.spark.core.Observer;
import org.spark.core.SimulationTime;
import org.spark.core.SparkModel;
import org.spark.space.SpaceAgent;
import org.spark.space.StandardSpace;
import org.spark.utils.RandomHelper;

/**
 * Test 2-A: agents which can die and proliferate
 * @author Monad
 *
 */
public class CreateDieA extends SparkModel {

	public boolean begin(long tick) {
		return false;
	}

	public boolean end(long tick) {
		return false;
	}

	public void setup() {
		setup(9);
	}

	public void setup(int k) {
		int xSize = 20;
		int ySize = 20;
		
		Observer observer = Observer.getInstance();
		observer.addSpace("space", new StandardSpace(-xSize, xSize, -ySize, ySize, true, true));
		
		int agentsNumber = (k + 1) * 1000;
		for (int i = 0; i < agentsNumber; i++) {
			new Agent();
		}
	}
	
	
	@SuppressWarnings("serial")
	public static class Agent extends SpaceAgent {
		double life;
		boolean red;
		
		public Agent() {
			super(0.3);
			setRandomPosition();
			life = RandomHelper.random() * 100 + 20;
			setColor(SpaceAgent.RED);
			red = true;
		}
		
		@Override
		public void step(SimulationTime time) {
			if (life < 10 && RandomHelper.random() < 0.1) {
				Agent agent = new Agent();
				agent.jump(getPosition());
				if (red) {
					agent.setColor(SpaceAgent.GREEN);
					agent.red = false;
				}
			}
			
			life -= 1;
			
			if (life < 0)
				die();
		}
	}

}
