package org.spark.test.basictests;

import org.spark.core.Observer;
import org.spark.core.SimulationTime;
import org.spark.core.SparkModel;
import org.spark.space.SpaceAgent;
import org.spark.space.StandardSpace;
import org.spark.utils.Vector;

/**
 * Test 1-C: circular moving agents 
 * @author Monad
 *
 */
public class MovingAgentsC extends SparkModel {

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
		double heading = 0;
		Vector v = new Vector();
		
		public Agent() {
			super(0.3);
			setRandomPosition();
		}
		
		private static double cosTable[];
		private static double sinTable[];

		static {
			cosTable = new double[360];
			sinTable = new double[360];
			
			for (int i = 0; i < 360; i++) {
				cosTable[i] = Math.cos(i * Math.PI / 180);
				sinTable[i] = Math.sin(i * Math.PI / 180);
			}
		}
		
		@Override
		public void step(SimulationTime time) {
//			Vector v = Vector.getVector(0.1, heading);
			if (heading < 0)
				heading += 360;
			else if (heading >= 360)
				heading -= 360;
			
			v.x = 0.1 * cosTable[(int) heading];
			v.y = 0.1 * sinTable[(int) heading];
			move(v);
			
			heading += 1;
		}
	}

}
