package main.annotation;

import main.type.AgentType;

/**
 * Annotation for agents
 * @author Monad
 */
public abstract class AgentAnnotation extends InterfaceAnnotation {
	/* Associated agent type */
	protected AgentType agent;
	
	/**
	 * Associates an agent type with the annotation
	 * @param agent
	 */
	public void associateAgentType(AgentType agent) {
		this.agent = agent;
	}
}
