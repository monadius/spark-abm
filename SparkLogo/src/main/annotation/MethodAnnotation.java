package main.annotation;

import main.type.Method;

/**
 * Annotation for methods
 * @author Monad
 *
 */
public abstract class MethodAnnotation extends InterfaceAnnotation {
	/* Associated method */
	protected Method method;
	
	/**
	 * Associates a variable with the annotation
	 * @param var
	 */
	public void associateMethod(Method method) throws Exception {
		this.method = method;
	}
}
