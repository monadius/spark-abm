package org.spark.runtime;

import java.util.ArrayList;

import org.spark.core.SparkModel;
import org.spark.runtime.internal.ModelVariable;

// TODO: create a data set factory
// with static methods for creating data sets,
// removing data sets, and for updating data sets (as variables)
/**
 * Class for collecting data
 * @author Monad
 */
public class DataSet {
	/* Data set's name */
//	private String name;
	
	/**
	 * Auxiliary class
	 */
	private class VariableData {
		/* Variable */
		public ModelVariable variable;
		/* Time interval for collecting the data */
//		public long interval;
		
		/* Collected data */
		public ArrayList<Number> data = new ArrayList<Number>(10000);
		/* Corresponding ticks */
//		public ArrayList<Long> ticks = new ArrayList<Long>(10000);
		
		/**
		 * Default constructor
		 * @param var
		 * @param interval
		 */
		public VariableData(ModelVariable var, long interval) {
			this.variable = var;
//			this.interval = interval;
		}
	}
	
	
	/* Data for all variables */
	private final ArrayList<VariableData> variables;
	
	/**
	 * Default constructor
	 */
	public DataSet() {
		variables = new ArrayList<VariableData>();
	}
	
	
	/**
	 * Adds a variable to the data set
	 * @param name
	 */
	public void addVariable(SparkModel model, String name) {
		ModelVariable var = model.getVariable(name);
		if (var != null)
			variables.add(new VariableData(var, 1));
	}
	
	
	/**
	 * Adds a variable to the data set
	 * @param var
	 */
	public void addVariable(ModelVariable var) {
		if (var != null)
			variables.add(new VariableData(var, 1));
	}
	
	
	/**
	 * Collects the data
	 * @param tick
	 */
	public void collectData(long tick) {
		for (VariableData var : variables) {
//			if (tick % var.interval == 0) {
				var.data.add((Number) var.variable.getValue());
//				var.ticks.add(tick);
//			}
		}
	}
	
	
	/**
	 * Clears the data set
	 */
	public void clear() {
		for (VariableData var : variables) {
			var.data.clear();
			// var.ticks.clear();
		}
	}
	
	
	/**
	 * Returns data for the specific variable at the given
	 * tick points
	 * @param varName
	 * @param ticks could be null, in which case all data is returned
	 * @return null if no such variable in the data set
	 */
	public ArrayList<Number> getDataAtGivenTicks(String varName, ArrayList<Long> ticks) {
		for (VariableData var : variables) {
			if (var.variable.getName().equals(varName)) {
				if (ticks == null)
					return var.data;
				
				ArrayList<Number> data = new ArrayList<Number>(ticks.size());
				
				int currentTickIndex = 0;
				long currentTick = ticks.get(0);
				
				for (int i = 0; i < var.data.size(); i++) {
//					long tick = var.ticks.get(i);
					long tick = i;
					
					if (tick == currentTick) {
						data.add(var.data.get(i));
						
						currentTickIndex++;
						if (currentTickIndex < ticks.size())
							currentTick = ticks.get(currentTickIndex);
						else
							break;
					}
				}
				
				return data;
			}
		}
		
		return null;
	}
	
	
}
