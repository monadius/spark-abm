package org.spark.test.basictests;

import org.spark.core.Observer;
import org.spark.core.SimulationTime;
import org.spark.core.SparkModel;
import org.spark.data.Grid;
import org.spark.data.GridFactory;
import org.spark.space.Space;
import org.spark.space.SpaceAgent;
import org.spark.space.StandardSpace;
import org.spark.utils.RandomHelper;

/**
 * Test 2-A: agents which can die and proliferate
 * @author Monad
 *
 */
public class EvaporationAndDiffusion extends SparkModel {
	public static Grid data;
	

	public boolean begin(long tick) {
		return false;
	}

	public boolean end(long tick) {
		data.diffuse(1);
		data.multiply(0.9);
		
		return false;
	}

	public void setup() {
		setup(9);
	}

	public void setup(int k) {
		int xSize = 4 * (k + 1);
		int ySize = 4 * (k + 1);
		
		Observer observer = Observer.getInstance();
		Space space = observer.addSpace("space", new StandardSpace(-xSize, xSize, -ySize, ySize, true, true));
		
		data = space.addDataLayer("data", GridFactory.createGrid(xSize * 2, ySize * 2));
		
		int agentsNumber = (k + 1) * 2;
		for (int i = 0; i < agentsNumber; i++) {
			new Agent();
		}
	}
	
	
	@SuppressWarnings("serial")
	public static class Agent extends SpaceAgent {
		double productionRate;
		
		public Agent() {
			super(0.3);
			setRandomPosition();
			productionRate = RandomHelper.random() + 0.1;
			setColor(SpaceAgent.BLUE);
		}
		
		@Override
		public void step(SimulationTime time) {
			data.setValue(this, productionRate);
		}
	}

}
