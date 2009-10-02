package org.spark.core;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


/**
 * Implementation of Observer methods
 */
abstract class ObserverImpl {
	// A reference to the Observer
	protected Observer observer;
	
	
	/**
	 * Default constructor
	 * @param observer
	 */
	ObserverImpl() {
	}
	
	
	/**
	 * Sets the observer for this implementation
	 * @param observer
	 */
	void setObserver(Observer observer) {
		this.observer = observer;
	}
	
	
	/**
	 * Verifies that the given execution mode is supported by the implementation
	 * Returns a supported mode if the given mode is not appropriate
	 * @param executionMode
	 * @return
	 */
	public int filterExecutionMode(int executionMode) {
		if (ExecutionMode.isMode(executionMode))
			return executionMode;
		else
			return ExecutionMode.SERIAL_MODE;
	}
	
	/**
	 * Clears the context by removing all agents, data layers, etc.
	 */
	public abstract void clear();
	
	/**
	 * Removes all agents from the context
	 */
	public abstract void clearAgents();

	/**
	 * Adds the agent into the context
	 * @param agent to be added to the context
	 */
	protected abstract void addAgent(Agent agent, Class<? extends Agent> cl);
	
	/**
	 * Adds all agents from the array list into the context
	 * @param agents
	 */
	protected abstract void addAllAgents(ArrayList<Agent> newAgents, Class<? extends Agent> cl);

	/**
	 * Removes the agent from the context
	 * @param agent
	 * @return true if agent was found and removed
	 */
	protected abstract boolean removeAgent(Agent agent);

	/**
	 * Returns the number of specific agents
	 * @param type a type of agents for which the number is retrieved
	 * @return the number of agents of the given type
	 */
	public abstract int getAgentsNumber(Class<? extends Agent> type);
	
	/**
	 * Returns the number of agents derived from a specific type
	 * @param type
	 * @return
	 */
	public abstract int getAgentsNumberOfKind(Class<? extends Agent> type);

	/**
	 * @deprecated
	 * Processes all agents
	 * @param tick a number representing time passed since the model start
	 */
	public abstract void processAllAgents(long tick);
	

	/**
	 * Processes all agents of the given type
	 * @param type
	 * @param time
	 */
	public abstract void processAgents(Class<? extends Agent> type, SimulationTime time);
	
	
	// TODO: should be protected or remove this method at all
	// it is used only in the render and internally by the observer
	/**
	 * Returns a set of specific agents
	 * @param type a type of agents to be returned
	 * @return a set of all agents of the given type
	 */
	public abstract <T extends Agent> T[] getAgents(Class<T> type);

	// TODO: implement in subclasses
	public <T extends Agent> ArrayList<T> getAgentsList(Class<T> type) {
		throw new Error("Not implemented");
	}
	
	
	// TODO: implement in subclasses
	public <T extends Agent> ArrayList<T> getAgentsListOfKind(Class<T> type) {
		throw new Error("Not implemented");
	}
	

	/**
	 * Returns a set of all agents
	 */
	public abstract Agent[] getAgents();

	/**
	 * Prints performance statistics
	 */
	public void printStatistics() {
	}
	
	
	/**
	 * Checks whether a given type is derived from a given kind
	 * @param type
	 * @param kind
	 * @return
	 */
	protected boolean derived(Class<?> type, Class<? extends Agent> kind) {
		while (true) {
			if (type == kind)
				return true;
			
			if (type == Agent.class)
				return false;
			
			type = type.getSuperclass();
		}
	}
	
	
	/**
	 * Serializes all agents
	 * @param oos
	 */
	// TODO: implement in subclasses more efficiently
	protected void serializeAgents(ObjectOutputStream oos) throws Exception {
		Agent[] agents = getAgents();

		// oos.writeObject(agents);
		if (agents == null || agents.length == 0) {
			oos.writeInt(0);
			return;
		}
		
		oos.writeInt(agents.length);
		for (int i = 0; i < agents.length; i++) {
			agents[i].deepSerialization = false;
			oos.writeObject(agents[i]);
		}
	}

	
	/**
	 * Loads all agents
	 * @param ois
	 */
	protected void loadAgents(ObjectInputStream ois) throws Exception {
		int n = ois.readInt();

		// Too many objects
		if (n < 0 || n > 1e+7)
			throw new Exception("Wrong number of agents: " + n);

		for (int i = 0; i < n; i++) {
			Agent agent = (Agent) ois.readObject();
			addAgent(agent, agent.getClass());
		}
	}
}
