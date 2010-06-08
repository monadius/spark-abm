/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package org.spark.math;

import org.spark.utils.Vector;

/**
 * A basic interface for a vector function 
 */
public interface Function {
	/**
	 * The only method of a vector function:
	 * returns the value of the function at the point x 
	 * @param x a point
	 * @return a value of a function
	 */
	public double getValue(Vector x);
}
