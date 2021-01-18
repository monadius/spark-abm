package org.sparklogo.main.annotation;

import org.sparklogo.main.type.AgentType;

/**
 * Annotation for agents
 *
 * @author Monad
 */
public abstract class AgentAnnotation extends InterfaceAnnotation {
    /* Associated agent type */
    protected AgentType agent;

    /**
     * Constructor
     */
    protected AgentAnnotation(int id) {
        super(id);
    }

    /**
     * Associates an agent type with the annotation
     */
    public void associateAgentType(AgentType agent) {
        this.agent = agent;
    }
}
