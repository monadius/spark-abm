package main.annotation;

import main.type.ModelType;;

/**
 * Annotation for models
 * @author Monad
 */
public abstract class ModelAnnotation extends InterfaceAnnotation {
	/* Associated agent type */
	protected ModelType agent;
	
	/**
	 * Associates an agent type with the annotation
	 * @param agent
	 */
	public void associateModelType(ModelType agent) {
		this.agent = agent;
	}
}
