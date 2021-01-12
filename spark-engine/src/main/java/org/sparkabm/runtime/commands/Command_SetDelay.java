package org.sparkabm.runtime.commands;

import org.sparkabm.core.SparkModel;
import org.sparkabm.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Sets a delay for simulation
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_SetDelay extends ModelManagerCommand {
	/* Delay time in ms */
	private int delay;
	
	public Command_SetDelay(int delay) {
		if (delay < 0)
			delay = 0;
		
		this.delay = delay;
	}
	
	
	@Override
	public void execute(SparkModel model, AbstractSimulationEngine engine)
			throws Exception {
		engine.setDelay(delay);
	}
	
	@Override
	public String toString() {
		return "SetDelay: " + delay;
	}

}
