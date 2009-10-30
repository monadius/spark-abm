package org.spark.runtime.commands;

import org.spark.core.SparkModel;
import org.spark.runtime.data.DCVariable;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Adds a variable data collector
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_AddDCVariable extends ModelManagerCommand {
	private String varName;
	private int interval;
	
	
	public Command_AddDCVariable(String varName, int interval) {
		this.varName = varName;
		this.interval = interval;
	}
	
	
	/**
	 * Executes the command on the given simulation engine
	 * @param engine
	 */
	public void execute(SparkModel model, AbstractSimulationEngine engine) {
		DCVariable dc = new DCVariable(varName);
		dc.setCollectionInterval(interval);
		
		engine.addDataCollector(dc);
	}
}
