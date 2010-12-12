package org.spark.runtime.commands;

import org.spark.core.SparkModel;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Key is pressed
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_KeyPressed extends ModelManagerCommand {
	private String key;
	
	
	public Command_KeyPressed(String key) {
		this.key = key;
	}
	
	/**
	 * Executes the command on the given model
	 * @param model
	 * @throws Exception
	 */
	public void execute(SparkModel model, AbstractSimulationEngine engine) throws Exception {
		model.getObserver().addCommand(key);
	}
	
	
	@Override
	public String toString() {
		return "Command_KeyPressed: " + key;
	}
}
