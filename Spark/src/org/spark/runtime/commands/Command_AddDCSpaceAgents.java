package org.spark.runtime.commands;

import org.spark.core.SparkModel;
import org.spark.runtime.data.DCSpaceAgents;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Adds a space agents data collector
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_AddDCSpaceAgents extends ModelManagerCommand {
	private String typeName;
	private int interval;
	
	
	public Command_AddDCSpaceAgents(String typeName, int interval) {
		this.typeName = typeName;
		this.interval = interval;
	}
	
	
	/**
	 * Executes the command on the given simulation engine
	 * @param engine
	 */
	public void execute(SparkModel model, AbstractSimulationEngine engine) {
		DCSpaceAgents dc = new DCSpaceAgents(typeName);
		dc.setCollectionInterval(interval);
		
		engine.addDataCollector(dc);
	}
}
