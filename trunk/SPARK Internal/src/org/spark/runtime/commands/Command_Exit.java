package org.spark.runtime.commands;

import org.spark.core.SparkModel;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Tells the server to stop simulations and exit
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_Exit extends ModelManagerCommand {
	public Command_Exit() {
	}

	@Override
	public void execute(SparkModel model, AbstractSimulationEngine engine)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	
}
