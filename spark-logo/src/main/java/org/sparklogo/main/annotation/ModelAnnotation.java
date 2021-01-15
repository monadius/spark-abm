package org.sparklogo.main.annotation;

import org.sparklogo.main.type.ModelType;;

/**
 * Annotation for models
 *
 * @author Monad
 */
public abstract class ModelAnnotation extends InterfaceAnnotation {
    /* Associated agent type */
    protected ModelType agent;

    /**
     * Constructor
     *
     * @param id
     */
    protected ModelAnnotation(int id) {
        super(id);
    }

    /**
     * Associates an agent type with the annotation
     *
     * @param agent
     */
    public void associateModelType(ModelType agent) {
        this.agent = agent;
    }
}
