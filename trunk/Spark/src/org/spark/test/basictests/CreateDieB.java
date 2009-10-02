package org.spark.test.basictests;

import org.spark.core.Observer;
import org.spark.core.SimulationTime;
import org.spark.core.SparkModel;
import org.spark.space.SpaceAgent;
import org.spark.space.StandardSpace;

/**
 * Test 2-A: agents which can die and proliferate
 * @author Monad
 *
 */
public class CreateDieB extends SparkModel {

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
			new Agent(i);
		}
	}
	
	
	@SuppressWarnings("serial")
	public static class Agent extends SpaceAgent {
		double life;
		int index;
		
		public Agent(int i) {
			super(0.3);
			index = i;
			setRandomPosition();
			life = i % 100 + 20;
			setColor(SpaceAgent.RED);
		}
		
		@Override
		public void step(SimulationTime time) {
			if (life == 1) {
				new Agent(index);
			}
			
			life -= 1;
			
			if (life < 0)
				die();
		}
	}

}
