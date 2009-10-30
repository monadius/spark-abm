package org.spark.runtime.commands;

import org.spark.core.SparkModel;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Command containing a string message
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_String extends ModelManagerCommand {
	private String str;
	
	public Command_String(String str) {
		this.str = str;
	}

	public void execute(SparkModel model, AbstractSimulationEngine engine) {
	}

	
	public String getCommand() {
		return str;
	}
}
