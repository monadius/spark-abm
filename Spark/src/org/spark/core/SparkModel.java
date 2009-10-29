package org.spark.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.spark.math.RationalNumber;
import org.spark.runtime.internal.ModelMethod;
import org.spark.runtime.internal.ModelVariable;

import com.spinn3r.log5j.Logger;

/**
 * Base class for any SPARK model
 * @author Monad
 *
 */
public abstract class SparkModel {
	/* Logger */
	private static final Logger logger = Logger.getLogger();
	
	/* Observer for this model */
	private Observer modelObserver;
	
	/* Agent types of the model */
	private final ArrayList<Observer.AgentType> agentTypes = 
		new ArrayList<Observer.AgentType>(); 
	
	/* Time corresponding to one tick */
	private RationalNumber tickTime;
	
	/* Collection of all model variables */
	private final HashMap<String, ModelVariable> modelVariables =
		new HashMap<String, ModelVariable>();
	
	/* Collection of all model methods */
	private final HashMap<String, ModelMethod> methods =
		new HashMap<String, ModelMethod>();
	
	
	/**
	 * Adds a method
	 * @param method
	 * @return
	 */
	public boolean addMethod(ModelMethod method) {
		if (methods.containsKey(method.getName()))
			return false;
		
		methods.put(method.getName(), method);
		return true;
	}
	
	
	/**
	 * Returns a method by its name
	 * @param name
	 * @return
	 */
	public ModelMethod getMethod(String name) {
		return methods.get(name);
	}
	
	
	
	/**
	 * Synchronizes method calls
	 */
	public void synchronizeMethods() {
		for (ModelMethod method : methods.values()) {
			try {
				method.synchronize(this);
			}
			catch (Exception e) {
				logger.error("Error during external method invokation: " + method.getName());
				logger.error(e.getMessage());
			}
		}
	}

	
	
	/**
	 * Returns a model variable by its name
	 * @param name
	 * @return null if there is no variable with the given name
	 */
	public ModelVariable getVariable(String name) {
		return modelVariables.get(name);
	}
	
	
	/**
	 * Returns all model variables
	 * @return
	 */
	public ModelVariable[] getVariables() {
		ModelVariable[] vars = new ModelVariable[modelVariables.size()];
		
		int i = 0;
		for (ModelVariable var : modelVariables.values()) {
			vars[i++] = var;
		}
		
		return vars;
	}
	
	
	/**
	 * Adds the model variable into the collection of variables for the model
	 * @param var
	 */
	public boolean addMovelVariable(ModelVariable var) {
		if (modelVariables.containsKey(var.getName()))
			return false;
		
		modelVariables.put(var.getName(), var);
		return true;
	}
	
	
	/**
	 * Clears all model variables
	 */
	public void clearVariables() {
		modelVariables.clear();
	}
	
	
	
	/**
	 * Synchronizes values of all variables
	 */
	public void synchronizeVariables() {
		for (ModelVariable var : modelVariables.values()) {
			try {
				var.synchronizeValue();
			}
			catch (Exception e) {
				logger.error("Error during variable synchronization: " + var.getName());
				logger.error(e.getMessage());
			}
		}
	}
		
	
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
	 * Returns time of one tick
	 * @return
	 */
	public final RationalNumber getTickTime() {
		return tickTime;
	}
	
	
	/**
	 * Sets the tick time
	 */
	public final void setTickTime(RationalNumber time) {
		tickTime = new RationalNumber(time);
	}
	
	
	/**
	 * Returns the model observer
	 * @return
	 */
	public final Observer getObserver() {
		return modelObserver;
	}
	
	
	/**
	 * Main setup method of a model
	 */
	public abstract void setup();
	
	
	/**
	 * This method is called before each simulation step
	 * @param tick
	 * @return true for pausing a simulation
	 */
	public boolean begin(long tick) {
		return false;
	}
	
	
	/**
	 * This method is called after each simulation step
	 * @param tick
	 * @return true for pausing a simulation
	 */
	public boolean end(long tick) {
		return false;
	}
}
