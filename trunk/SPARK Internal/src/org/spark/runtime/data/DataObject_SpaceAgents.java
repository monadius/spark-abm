package org.spark.runtime.data;

import org.spark.math.Vector;
import org.spark.math.Vector4d;

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
	private double[] rotations;
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
			rotations = new double[agentsNumber];
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
	public void addAgent(Vector position, double r, Vector4d color, double rotation, int shape, int spaceIndex) {
		// Cannot hold any more agents
		if (counter >= n)
			return;
		
		positions[counter] = position;
		radii[counter] = r;
		colors[counter] = color;
		rotations[counter] = rotation;
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
	
	public double[] getRotations() {
		return rotations;
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
