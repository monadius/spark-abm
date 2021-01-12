package org.sparkabm.runtime.commands;

import org.sparkabm.core.ModelMethod;
import org.sparkabm.core.SparkModel;
import org.sparkabm.runtime.internal.engine.AbstractSimulationEngine;


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
	public void execute(SparkModel model, AbstractSimulationEngine engine) throws Exception {
		ModelMethod method = model.getMethod(methodName);
		if (method == null)
			throw new Exception("Undefined method: " + methodName);
		
		method.invoke();
	}
}
