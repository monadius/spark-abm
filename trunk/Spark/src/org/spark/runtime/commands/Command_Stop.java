package org.spark.runtime.commands;

import org.spark.core.SparkModel;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Tells the server to stop the current simulation (if any)
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_Stop extends ModelManagerCommand {
	public Command_Stop() {
	}
	
	@Override
	public void execute(SparkModel model, AbstractSimulationEngine engine)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
