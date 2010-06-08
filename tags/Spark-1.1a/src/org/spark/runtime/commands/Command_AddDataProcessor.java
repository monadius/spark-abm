package org.spark.runtime.commands;

import org.spark.core.SparkModel;
import org.spark.runtime.internal.data.DataProcessor;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Tells to add a data processor to a simulation engine
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_AddDataProcessor extends ModelManagerCommand {
	private DataProcessor dp;
	
	
	public Command_AddDataProcessor(DataProcessor dp) {
		this.dp = dp;
	}
	
	@Override
	public void execute(SparkModel model, AbstractSimulationEngine engine)
			throws Exception {
		engine.addDataProcessor(dp);
	}

}
