package org.spark.runtime.external;


/**
 * A model parameter
 * @author Monad
 */
public class Parameter {
	/* Associated variable's name */
	private String varName;
	
	/* Current value */
	private String value;
	
	
	Parameter(String varName) {
		this.varName = varName;
	}
	
	
	public void setValue(String value) {
		this.value = value;
	}
	
	
	public String getValue() {
		return value;
	}
	
	
	public String getVarName() {
		return varName;
	}
}
