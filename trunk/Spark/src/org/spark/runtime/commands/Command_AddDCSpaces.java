package org.spark.runtime.commands;

import org.spark.core.SparkModel;
import org.spark.runtime.data.DCSpaces;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Adds a grid data collector
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_AddDCSpaces extends ModelManagerCommand {
	private int interval;
	
	
	public Command_AddDCSpaces(int interval) {
		this.interval = interval;
	}
	
	
	/**
	 * Executes the command on the given simulation engine
	 * @param engine
	 */
	public void execute(SparkModel model, AbstractSimulationEngine engine) {
		DCSpaces dc = new DCSpaces();
		dc.setCollectionInterval(interval);
		
		engine.addDataCollector(dc);
	}
}
