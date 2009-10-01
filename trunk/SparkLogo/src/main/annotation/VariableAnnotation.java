package main.annotation;

import main.Variable;

/**
 * Annotation for (global) variables
 * @author Monad
 */
public abstract class VariableAnnotation extends InterfaceAnnotation{
	/* Associated variable */
	protected Variable variable;
	
	/**
	 * Associates a variable with the annotation
	 * @param var
	 */
	public void associateVariable(Variable var) {
		this.variable = var;
	}
}
