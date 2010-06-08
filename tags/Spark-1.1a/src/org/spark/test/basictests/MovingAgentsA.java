package org.spark.test.basictests;

import org.spark.core.Observer;
import org.spark.core.SimulationTime;
import org.spark.core.SparkModel;
import org.spark.space.SpaceAgent;
import org.spark.space.StandardSpace;
import org.spark.utils.Vector;

/**
 * Test 1-A: agents moving with a constant speed
 * @author Monad
 *
 */
public class MovingAgentsA extends SparkModel {

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
		
		k = 10;
		
		int agentsNumber = (k + 1) * 10000;
		for (int i = 0; i < agentsNumber; i++) {
			new Agent();
		}
	}
	
	
	@SuppressWarnings("serial")
	public static class Agent extends SpaceAgent {
		Vector v = new Vector(0.1, 0, 0);
		
		public Agent() {
			super(0.1);
			setRandomPosition();
		}
		
		@Override
		public void step(SimulationTime time) {
			move(v);
		}
	}

}
