package org.spark.runtime.commands;

import org.spark.core.SparkModel;
import org.spark.runtime.internal.ModelVariable;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;

/**
 * A command for setting a value of a variable
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_SetVariableValue extends ModelManagerCommand {
	private String varName;
	private Object value;
	private String strValue;
	
	
	public Command_SetVariableValue(String varName, Object value) {
		this.varName = varName;
		this.value = value;
	}
	
	
	public Command_SetVariableValue(String varName, String varValue) {
		this.varName = varName;
		this.strValue = varValue;
	}
	

	/**
	 * Executes the command on the given model
	 * @param model
	 */
	public void execute(SparkModel model, AbstractSimulationEngine engine) throws Exception {
		ModelVariable var = model.getVariable(varName);
		if (var == null)
			throw new Exception("Variable " + varName + " is not defined");
		
		if (value != null) {
			var.setValue(value);
		}
		else if (strValue != null) {
			var.setValue(strValue);
		}
		else {
			// value == null here
			var.setValue(value);
		}
	}
	
	
	@Override
	public String toString() {
		String str = "Set value: ";
		str += varName;
		str += " = ";
		if (value != null)
			str += value.toString();
		else
			str += strValue;

		return str;
	}
}
