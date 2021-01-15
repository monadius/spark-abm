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
     *
     * @param id
     */
    protected AgentAnnotation(int id) {
        super(id);
    }

    /**
     * Associates an agent type with the annotation
     *
     * @param agent
     */
    public void associateAgentType(AgentType agent) {
        this.agent = agent;
    }
}
