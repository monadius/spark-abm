package org.spark.core;

import java.util.ArrayList;

import org.spark.math.RationalNumber;

/**
 * Base class for any SPARK model
 * @author Monad
 *
 */
public abstract class SparkModel {
	/* Observer for this model */
	private Observer modelObserver;
	
	/* Agent types of the model */
	private final ArrayList<Observer.AgentType> agentTypes = 
		new ArrayList<Observer.AgentType>(); 
	
	
	/**
	 * Sets the model observer
	 * @param observer
	 */
	void setObserver(Observer observer) {
		this.modelObserver = observer;
		
		// Add definitions of agent types to the observer
		for (Observer.AgentType type : agentTypes) {
			observer.setAgentType(type.type, type.timeStep, type.priority);
		}
	}
	

	/**
	 * Adds a definition of the agent type for the model
	 * @param type
	 * @param timeStep
	 * @param priority
	 */
	public final void AddAgentType(Class<? extends Agent> type, RationalNumber timeStep, int priority) {
		agentTypes.add(new Observer.AgentType(type, timeStep, priority));
	}
	
	
	/**
	 * Returns the model observer
	 * @return
	 */
	public final Observer getObserver() {
		return modelObserver;
	}
	
	
	public abstract void setup();
	
	
	public boolean begin(long tick) {
		return false;
	}
	
	
	public boolean end(long tick) {
		return false;
	}
}
