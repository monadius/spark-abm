package org.spark.core;

import java.io.Serializable;

/**
 * Class for transferring agents between cluster nodes
 * @author Monad
 */
public class AgentBuffer implements Serializable {
	/**
	 * Serial Version UID 
	 */
	private static final long serialVersionUID = 2388522077030643891L;

	// All agents
	private Agent[] agents;
	
	/**
	 * Private constructor
	 */
	private AgentBuffer(Agent[] agents) {
		this.agents = agents;
	}
	
	/**
	 * Returns a buffer with all agents in the context
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static AgentBuffer allAgents() {
		Agent[] agents = Observer.getInstance().getAgents();
		return new AgentBuffer(agents);
	}
	
	
	public static AgentBuffer empty() {
		return new AgentBuffer(null);
	}
	

	// TODO: move this functionality into deserialization method
	public void addAgentsToTheObserver() {
		if (agents == null)
			return;
		
		for (int i = 0; i < agents.length; i++) {
			Observer.getInstance().addAgent(agents[i]);
		}
	}
	
	
}
