package org.spark.runtime.data;

import org.spark.utils.Vector;
import org.spark.utils.Vector4d;

/**
 * Data for space links
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class DataObject_SpaceLinks extends DataObject_SpaceAgents {
	private Vector[] end1;
	private Vector[] end2;
	private double[] width;
	private Vector4d[] color;
	private int[] spaceIndices;
	
	private int counter;
	private int n;
	
	
	/**
	 * Creates a data object for the given number of space links
	 * @param linksNumber
	 */
	public DataObject_SpaceLinks(int linksNumber) {
		if (linksNumber < 0)
			linksNumber = 0;
		
		if (linksNumber > 0) {
			end1 = new Vector[linksNumber];
			end2 = new Vector[linksNumber];
			width = new double[linksNumber];
			color = new Vector4d[linksNumber];
			spaceIndices = new int[linksNumber];
		}
		
		n = linksNumber;
		counter = 0;
	}
	
	
	/**
	 * Adds link's parameters into the data object
	 */
	public void addLink(Vector end1, Vector end2, double width, Vector4d color, int spaceIndex) {
		// Cannot hold any more links
		if (counter >= n)
			return;
		
		this.end1[counter] = end1;
		this.end2[counter] = end2;
		this.width[counter] = width;
		this.color[counter] = color;
		spaceIndices[counter] = spaceIndex;
		
		counter++;
	}
	
	
	/**
	 * Returns the total number of agents in the data object
	 * @return
	 */
	@Override
	public int getTotalNumber() {
		return counter;
	}
	
	
	public Vector[] getEnd1() {
		return end1;
	}
	
	public Vector[] getEnd2() {
		return end2;
	}
	
	public double[] getWidth() {
		return width;
	}
	
	@Override
	public Vector4d[] getColors() {
		return color;
	}
	
	@Override
	public int[] getSpaceIndices() {
		return spaceIndices;
	}
	
	@Override
	public String toString() {
		if (end1 == null)
			return "0";
		else
			return String.valueOf(end1.length);
	}


}
