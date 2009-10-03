package org.spark.test.basictests;

import static org.spark.utils.RandomHelper.nextDoubleFromTo;

import java.util.ArrayList;

import org.spark.core.Observer;
import org.spark.core.SimulationTime;
import org.spark.core.SparkModel;
import org.spark.data.Grid;
import org.spark.data.GridFactory;
import org.spark.space.GridSpace;
import org.spark.space.PhysicalNode;
import org.spark.space.PhysicalSpace2d;
import org.spark.space.Space;
import org.spark.space.SpaceAgent;
import org.spark.utils.RandomHelper;
import org.spark.utils.Vector;

/**
 * ToyInfectionModel: moving agents + interacting agents + data layers
 * @author Monad
 *
 */
public class MultipleSpaces extends SparkModel {
	public static Grid cidalCompound;
	public static Grid toxin;
	public static Grid tissueLife;
	
	public static Grid newLayer;
	
	static boolean yesToxin = false;
	static int initialInfectionNumber = 500;
	static double chemotaxisThreshold = 0.5;
	static double cidalCompoundProduction = 4.0;
	static double totalTissueDamage;
	
	static Space space2;

	public boolean begin(long tick) {
		return false;
	}

	public boolean end(long tick) {
		double[][] tissueLife = MultipleSpaces.tissueLife.getData();
		double[][] cidalCompound = MultipleSpaces.cidalCompound.getData();
		double[][] toxin = MultipleSpaces.toxin.getData();
		
		int xSize = tissueLife.length;
		int ySize = tissueLife[0].length;

		double damage = 0;

		for (int i = 0; i < xSize; i++)
			for (int j = 0; j < ySize; j++) {
				tissueLife[i][j] -= cidalCompound[i][j] + toxin[i][j];
				if (tissueLife[i][j] < 0) 
					tissueLife[i][j] = 0;
				damage += 100 - tissueLife[i][j];
			}
		
		MultipleSpaces.cidalCompound.multiply(0.9);
		MultipleSpaces.toxin.multiply(0.9);
		
		MultipleSpaces.cidalCompound.diffuse(0.9);
		MultipleSpaces.toxin.diffuse(0.9);
		
		newLayer.diffuse(1);
		newLayer.multiply(0.99);
		
		totalTissueDamage = damage;
		
		return false;	
	}
	

	public void setup() {
		setup(9);
	}

	public void setup(int k) {
		int xSize = 40, ySize = 40;
		
		Observer observer = Observer.getInstance();
		Space space = observer.addSpace("space", new GridSpace(-xSize, xSize, -ySize, ySize, true, true));
		space2 = observer.addSpace("space2", new PhysicalSpace2d(-xSize, xSize, -ySize, ySize, false, false));
		
		tissueLife = space.addDataLayer("tissue-life", GridFactory.createGrid(2 * xSize, 2 * ySize));
		toxin = space.addDataLayer("toxin", GridFactory.createGrid(2 * xSize, 2 * ySize));
		cidalCompound = space.addDataLayer("cidal-compound", GridFactory.createGrid(2 * xSize, 2 * ySize));
		
		newLayer = space2.addDataLayer("new-layer", GridFactory.createGrid(space2, xSize, ySize));
				
		tissueLife.setValue(100);
		totalTissueDamage = 0;
		
		
		for (int i = 0; i < (k + 1) * 1000; i++) {
			InflamCell inflamCell = new InflamCell();
			inflamCell.jump(RandomHelper.random(100));
		}

	
		int n = initialInfectionNumber * (k + 1);
		for (int i = 0; i < n; i++) {
			InfectAgent infectAgent = new InfectAgent();
			infectAgent.jump(nextDoubleFromTo(0, Math.sqrt(n)));
		}
		
		for (int i = 0; i < 5; i++) {
			AnotherAgent agent = new AnotherAgent(space2);
			agent.setRandomPosition();
		}
	}
	
	
	/**
	 * Basic agent class
	 * @author Monad
	 */
	@SuppressWarnings("serial")
	public class BasicAgent extends SpaceAgent {
		protected double heading;
		
		public void setHeading(double heading) {
			this.heading = heading;
		}
		
		
		public void jump(double number) {
			move(Vector.getVector(number, heading));
		}
		
		
		public BasicAgent() {
			this(0.5, SpaceAgent.CIRCLE);
		}
		
		
		public BasicAgent(double r, int type) {
			super(r, type);
			heading = RandomHelper.random() * 360;
		}
		
		
		protected void wiggle() {
			heading += RandomHelper.random() * 45;
			heading -= RandomHelper.random() * 45;
			jump(1);
		}

	}
	
	
	
	/**
	 * Another agent
	 * @author Monad
	 */
	@SuppressWarnings("serial")
	class AnotherAgent extends SpaceAgent {
		public AnotherAgent(Space space) {
			super(space, 1, SpaceAgent.CIRCLE);
			setColor(SpaceAgent.RED);
		}
		
		
		@Override
		public void step(SimulationTime time) {
			newLayer.addValue(this, 1);
			((PhysicalNode) getNode()).applyForce(Vector.randomUnitVector2d().mul(10));
		}
	}

	

	/**
	 * Inlammatory cell 
	 * @author Monad
	 *
	 */
	@SuppressWarnings("serial")
	public class InflamCell extends BasicAgent {
		
		public InflamCell() {
			setColor(SpaceAgent.WHITE);
		}

		@Override
		public void step(SimulationTime time) {
			ArrayList<InfectAgent> infectAgents = 
				Observer.getDefaultSpace().getAgents(this, InfectAgent.class);
			
			if (infectAgents.size() > 0) {
				cidalCompound.addValue(this, cidalCompoundProduction);
			}
			else {
				if (cidalCompound.getValue(this) > chemotaxisThreshold) {
					Vector gradient = cidalCompound.getUphillDirection(getPosition());
					move( gradient );
				}
				else {
					wiggle();
				}
			}
			
		}
	}



	
	/**
	 * Infection agent
	 * @author Monad
	 */
	@SuppressWarnings("serial")
	public class InfectAgent extends BasicAgent {
		boolean anotherLife = false;
		
		public InfectAgent() {
			super(0.8, SpaceAgent.CIRCLE);
			setColor(SpaceAgent.GREY);
		}
		
		
		private void anotherStep() {
			
		}
		
		
		@Override
		public void step(SimulationTime time) {
			if (anotherLife) {
				anotherStep();
				return;
			}
			
			if (cidalCompound.getValue(this) > 10) {
				moveToSpace(space2, getPosition());
				anotherLife = true;
				return;
			}
			
			double life = tissueLife.getValue(this) - 1.0;
			if (life < 0) life = 0;
			tissueLife.setValue(this, life);
			
			if (yesToxin) toxin.addValue(this, 1.0);
			
			if (time.getTick() % 100 == 0) {
				ArrayList<InfectAgent> agents = 
					Observer.getDefaultSpace().getAgents(this, InfectAgent.class);
				
				if (agents.size() < 3) {
					InfectAgent infectAgent = new InfectAgent();
					infectAgent.jump(getPosition());
					infectAgent.jump(1.0);
				}
				
				jump(1.0);
			}
		}
		
		
	}



}
