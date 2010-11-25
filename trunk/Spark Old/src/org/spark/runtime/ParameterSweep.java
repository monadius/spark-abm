package org.spark.runtime;

import java.util.ArrayList;



/**
 * Class for batch running and parameter sweep
 * @author Monad
 *
 */
public class ParameterSweep {
	/**
	 * Auxiliary class with options for a parameter
	 * @author Monad
	 */
	private class ParameterOptions {
		public Parameter_Old parameter;
		
		public double startValue;
//		public double endValue;
		
		public double step;
		/* Either steps number or end value are specified */
		public int stepsNumber;
		
		/* Current iteration */
		public int iteration;
		
		/* Parameters with the same group number are changed simultaneously */
//		public int group;
	}
	
	/* End flag */
	private boolean endFlag = false;
	
	
	/* A list of all parameters */
	private final ArrayList<ParameterOptions> parOptions;
	
	/**
	 * Default constructor
	 */
	public ParameterSweep() {
		parOptions = new ArrayList<ParameterOptions>();
		endFlag = false;
	}
	
	
	/**
	 * Adds a parameter to the controller
	 * @param par
	 * @param start
	 * @param end
	 * @param step
	 */
	public void addParameter(Parameter_Old par, double start, double end, double step) {
		ParameterOptions p = new ParameterOptions();
		p.parameter = par;
		p.startValue = start;
//		p.endValue = end;
		p.step = step;
		p.iteration = 0;
		p.stepsNumber = (int)((end - start) / step) + 1;
		
		parOptions.add(p);
	}
	
	
	/**
	 * Returns the index of the current iteration
	 * @return
	 */
	public int getIndex() {
		int index = 0;
		int m = 1;
		
		for (int i = 0; i < parOptions.size(); i++) {
			ParameterOptions p = parOptions.get(i);
			index += p.iteration * m;
			m *= p.stepsNumber;
		}
		
		return index;
	}
	
	
	/**
	 * Sets the values of parameters from the given index
	 * @param index
	 */
	public void setValuesFromIndex(int index) {
		int m = 1;
		
		for (ParameterOptions p : parOptions) {
			m *= p.stepsNumber;
		}
		
		// Compute iterations
		for (int i = parOptions.size() - 1; i >= 0; i--) {
			ParameterOptions p = parOptions.get(i);
			m /= p.stepsNumber;

			int iteration = index / m;
			index -= iteration * m;
			
			p.iteration = iteration;
		}

		// Set values
		for (ParameterOptions p : parOptions) {
			p.parameter.setValue(p.startValue + p.step * p.iteration);
		}
	}
	
	
	/**
	 * Sets initial values for all parameters and advances
	 * by one step
	 */
	public void setInitialValuesAndAdvance() {
		for (ParameterOptions p : parOptions) {
			p.iteration = 0;
			p.parameter.setValue(p.startValue);
		}
		
		advance();
	}
	
	/**
	 * Sets the current values of parameters
	 * and advances the iteration
	 * @return false if no current values are available
	 */
	public boolean setCurrentValuesAndAdvance() {
		if (endFlag)
			return false;
		
		// Set the current values of parameters
		for (ParameterOptions p : parOptions) {
			p.parameter.setValue(p.startValue + p.step * p.iteration);
		}

		// Advance
		advance();
		
		return true;
	}
	
	
	/**
	 * Advance by one step
	 */
	protected void advance() {
		for (int n = 0; n < parOptions.size(); n++) {
			ParameterOptions p = parOptions.get(n);
			p.iteration++;
		
			if (p.iteration >= p.stepsNumber) {
				p.iteration = 0;
			}
			else {
				return;
			}
		}
		
		// No new values are available
		endFlag = true;
	}
}
