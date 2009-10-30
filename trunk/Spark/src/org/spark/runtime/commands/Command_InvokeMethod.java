package org.spark.runtime.commands;

import org.spark.core.SparkModel;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;


/**
 * Invokes a specific method
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_InvokeMethod extends ModelManagerCommand {
	private String methodName;
	
	
	public Command_InvokeMethod(String methodName) {
		this.methodName = methodName;
	}
	
	
	/**
	 * Executes the command on the given model
	 * @param model
	 * @throws Exception
	 */
	public void execute(SparkModel model, AbstractSimulationEngine engine) {
		model.getMethod(methodName).invoke();
	}
}
