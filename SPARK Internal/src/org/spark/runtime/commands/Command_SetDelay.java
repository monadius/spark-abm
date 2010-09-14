package org.spark.runtime.commands;

import org.spark.core.SparkModel;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Sets a delay for simulation
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_SetDelay extends ModelManagerCommand {
	/* Delay time in ms */
	private int time;
	
	
	public Command_SetDelay(int time) {
		if (time < 0)
			time = 0;
		
		this.time = time;
	}
	
	
	@Override
	public void execute(SparkModel model, AbstractSimulationEngine engine)
			throws Exception {
		engine.setDelay(time);
	}
}
