/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package org.spark.data;


/**
 * Extension of the standard data layer interface
 * It specifies functions for working with all data stored in a data layer
 * simultaneously.
 */
public interface AdvancedDataLayer extends DataLayer {
	/**
	 * Multiplies all data by the given value
	 * @param value a value
	 */
	public void multiply(double value);
	
	/**
	 * Adds to data the given value
	 * @param value a value
	 */
	public void add(double value);
	
	
	/**
	 * Returns a total number in the specific region
	 * @return a total number in the region
	 */
	public double getTotalNumber(double xMin, double xMax, double yMin, double yMax);
	

	/**
	 * Returns the total number filtered by the filter
	 * @param filter a filter represented by a data layer
	 * @param val a threshold value for filtering
	 * @return a total number
	 */
	public double getTotalNumber(DataLayer filter, double val);
	
	
	/**
	 * Returns the maximum value in the data layer
	 * @return
	 */
	public double getMax();
	
	
	/**
	 * Return the minimum value in the data layer
	 * @return
	 */
	public double getMin();
}