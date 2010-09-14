package org.spark.runtime.commands;

import org.spark.core.SparkModel;
import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Tells to remove a data collector
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_RemoveDataCollector extends ModelManagerCommand {
	private DataCollectorDescription dcd;
	
	
	public Command_RemoveDataCollector(DataCollectorDescription dcd) {
		this.dcd = dcd;
	}


	@Override
	public void execute(SparkModel model, AbstractSimulationEngine engine)
			throws Exception {
		engine.removeDataCollector(dcd);
	}
}
