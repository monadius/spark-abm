package org.spark.runtime.data;

import org.spark.utils.Vector;
import org.spark.utils.Vector4d;

/**
 * Data for a set of space agents
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class DataObject_SpaceAgents extends DataObject {
	private Vector[] positions;
	private double[] radii;
	private Vector4d[] colors;
	private int[] shapes;
	private int[] spaceIndices;
	
	private int counter;
	private int n;
	
	
	/**
	 * Creates a data object for the given number of space agents
	 * @param agentsNumber
	 */
	public DataObject_SpaceAgents(int agentsNumber) {
		if (agentsNumber < 0)
			agentsNumber = 0;
		
		if (agentsNumber > 0) {
			positions = new Vector[agentsNumber];
			radii = new double[agentsNumber];
			colors = new Vector4d[agentsNumber];
			shapes = new int[agentsNumber];
			spaceIndices = new int[agentsNumber];
		}
		
		n = agentsNumber;
		counter = 0;
	}
	
	
	/**
	 * Empty protected constructor
	 */
	protected DataObject_SpaceAgents() {
		
	}
	
	
	/**
	 * Adds agent's parameters into the data object
	 * @param position
	 * @param r
	 * @param color
	 * @param shape
	 */
	public void addAgent(Vector position, double r, Vector4d color, int shape, int spaceIndex) {
		// Cannot hold any more agents
		if (counter >= n)
			return;
		
		positions[counter] = position;
		radii[counter] = r;
		colors[counter] = color;
		shapes[counter] = shape;
		spaceIndices[counter] = spaceIndex;
		
		counter++;
	}
	
	
	/**
	 * Returns the total number of agents in the data object
	 * @return
	 */
	public int getTotalNumber() {
		return counter;
	}
	
	
	public Vector[] getPositions() {
		return positions;
	}
	
	public double[] getRadii() {
		return radii;
	}
	
	public Vector4d[] getColors() {
		return colors;
	}
	
	public int[] getShapes() {
		return shapes;
	}
	
	
	public int[] getSpaceIndices() {
		return spaceIndices;
	}
	
	@Override
	public String toString() {
		if (positions == null)
			return "0";
		else
			return String.valueOf(positions.length);
	}

}
