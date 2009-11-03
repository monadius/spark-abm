package org.spark.runtime.data;

import org.spark.utils.Vector;

/**
 * Information about spaces in a model
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class DataObject_Spaces extends DataObject {
	private String[] names;
	private int[] indices;
	private Vector[] mins;
	private Vector[] maxs;
	
	private int counter = 0;
	private transient int number = 0;
	
	
	/**
	 * Creates a data object for the given number of spaces
	 * @param number
	 */
	public DataObject_Spaces(int number) {
		if (number < 0)
			number = 0;
		
		this.number = number;
		
		names = new String[number];
		indices = new int[number];
		mins = new Vector[number];
		maxs = new Vector[number];
	}
	
	
	/**
	 * Adds a space information
	 * @param name
	 * @param index
	 * @param min
	 * @param max
	 */
	public void addSpace(String name, int index, Vector min, Vector max) {
		if (counter >= number)
			return;
		
		names[counter] = name;
		indices[counter] = index;
		mins[counter] = min;
		maxs[counter] = max;
		
		counter++;
	}
	
	
	/**
	 * Returns the total number of spaces
	 * @return
	 */
	public int getTotalNumber() {
		return counter;
	}
	
	
	public String[] getNames() {
		return names;
	}
	
	
	public int[] getIndices() {
		return indices;
	}
	
	
	public Vector[] getMins() {
		return mins;
	}
	
	
	public Vector[] getMaxs() {
		return maxs;
	}
	
	
	/**
	 * Returns the index inside this object of the given space
	 * @param name
	 * @return
	 */
	public int getIndex(String name) {
		for (int i = 0; i < names.length; i++) {
			if (names[i].equals(name))
				return i;
		}
		
		return -1;
	}
}
