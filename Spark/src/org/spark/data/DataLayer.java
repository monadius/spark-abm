/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package org.spark.data;

import java.io.Serializable;

import org.spark.math.Function;
import org.spark.space.Space;
import org.spark.space.SpaceAgent;
import org.spark.utils.Vector;

/**
 * The basic interface for representing a data layer.
 * A data layer allows to specify numerical values at each point
 * of a space. 
 */
public interface DataLayer extends Serializable {
	
	public void setValue(int x, int y, double value);
	
	public double getValue(int x, int y);
	
	/**
	 * Sets the value of the data at the point x
	 * @param x a point
	 * @param value a value
	 */
	public void setValue(Vector x, double value);

	/**
	 * Sets the value of the data at each point
	 * @param value a value
	 */
	public void setValue(double value);
	
	/**
	 * Sets the value of the data at the place occupied by the agent
	 * @param agent an agent
	 * @param value a value
	 */
	public void setValue(SpaceAgent agent, double value);
	

	/**
	 * Sets the value of the data at each point with respect
	 * to the values of the function f
	 * @param f a function
	 */
	public void setValue(Function f);
	
	
	/**
	 * Gets the value of the data at the point x
	 * @param x a point
	 * @return value a value
	 */
	public double getValue(Vector x);
	
	/**
	 * Gets the value of the data at the place occupied by the agent
	 * @param agent an agent
	 * @return value a value
	 */
	public double getValue(SpaceAgent agent);

	/**
	 * Adds the value to the data at the point x
	 * @param x a point
	 * @param value a value
	 * @return new value
	 */
	public double addValue(Vector x, double value);
	
	/**
	 * Adds the value to the data at the place occupied by the agent
	 * @param agent an agent
	 * @param value a value
	 * @return new value
	 */
	public double addValue(SpaceAgent agent, double value);

	/**
	 * Gets the vector at the point x which gives the direction
	 * of the gradient of the data
	 * @param x a point
	 * @return gradient
	 */
	public Vector getGradient(Vector x);

	/**
	 * Gets a total number of data stored in the data layer
	 * @return a total number
	 */
	public double getTotalNumber();
	
	/**
	 * Do not call this method directly.
	 * It is used internally for processing the data layer
	 */
	public void process(long tick);
	
	
	/**
	 * Called internally before processing agents
	 * Used for the implementation of the concurrent execution mode
	 */
	public void beginStep();
	
	/**
	 * Called internally after processing agents
	 * Used for the implementation of the concurrent execution mode
	 */
	public void endStep();
	
	
	/**
	 * Returns a space in which this data layer is defined
	 * @return
	 */
	public Space getSpace();
}
