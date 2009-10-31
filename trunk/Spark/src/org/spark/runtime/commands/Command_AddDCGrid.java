package org.spark.runtime.commands;

import org.spark.core.SparkModel;
import org.spark.runtime.data.DCDataLayer;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Adds a grid data collector
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_AddDCGrid extends ModelManagerCommand {
//	private String spaceName;
	private String gridName;
	private int interval;
	
	
	public Command_AddDCGrid(String gridName, int interval) {
//		this.spaceName = spaceName;
		this.gridName = gridName;
		this.interval = interval;
	}
	
	
	/**
	 * Executes the command on the given simulation engine
	 * @param engine
	 */
	public void execute(SparkModel model, AbstractSimulationEngine engine) {
		DCDataLayer dc = new DCDataLayer(gridName);
		dc.setCollectionInterval(interval);
		
		engine.addDataCollector(dc);
	}
}
