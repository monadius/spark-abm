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
	
	private String defaultObserverName = ObserverFactory.DEFAULT_OBSERVER_NAME;
	private int defaultExecutionMode = ExecutionMode.SERIAL_MODE;
	
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
	 * Sets the model observer
	 * @param observer
	 */
	final void setObserver(Observer observer) {
		this.modelObserver = observer;
		
		// Add definitions of agent types to the observer
		for (Observer.AgentType type : agentTypes) {
			observer.setAgentType(type.type, type.timeStep, type.priority);
		}
	}
	
	
	/**
	 * Sets default observer name and mode
	 * @param defaultObserverName
	 * @param defaultMode
	 */
	private final void setDefaultObserver(String defaultObserverName, int defaultMode) {
		this.defaultObserverName = defaultObserverName;
		this.defaultExecutionMode = defaultMode;
	}
	

	/**
	 * Returns default observer name
	 * @return
	 */
	public final String getDefaultObserverName() {
		return defaultObserverName;
	}
	
	
	/**
	 * Returns default execution mode
	 * @return
	 */
	public final int getDefaultExecutionMode() {
		return defaultExecutionMode;
	}
	
	
	/**
	 * Adds a method
	 * @param method
	 * @return
	 */
	private final boolean addMethod(ModelMethod method) {
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
	public final ModelMethod getMethod(String name) {
		return methods.get(name);
	}
	
	
	/**
	 * Returns an array of all model methods
	 * @return
	 */
	public final ModelMethod[] getMethods() {
		ModelMethod[] result = new ModelMethod[methods.size()];
		
		int i = 0;
		for (ModelMethod method : methods.values()) {
			result[i++] = method;
		}
		
		return result;
	}
	
	
	
	/**
	 * Synchronizes method calls
	 * @return true if any method was invoked
	 */
	public final boolean synchronizeMethods() {
		boolean flag = false;
		
		for (ModelMethod method : methods.values()) {
			try {
				flag |= method.synchronize(this);
			}
			catch (Exception e) {
				logger.error("Error during external method invokation: " + method.getName());
				logger.error(e.getMessage());
			}
		}
		
		return flag;
	}

	
	
	/**
	 * Returns a model variable by its name
	 * @param name
	 * @return null if there is no variable with the given name
	 */
	public final ModelVariable getVariable(String name) {
		return modelVariables.get(name);
	}
	
	
	/**
	 * Returns all model variables
	 * @return
	 */
	public final ModelVariable[] getVariables() {
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
	private final boolean addModelVariable(ModelVariable var) {
		if (modelVariables.containsKey(var.getName()))
			return false;
		
		modelVariables.put(var.getName(), var);
		return true;
	}
	
	
	/**
	 * Clears all model variables
	 */
/*	public void clearVariables() {
		modelVariables.clear();
	}*/
	
	
	
	/**
	 * Synchronizes values of all variables
	 */
	public final void synchronizeVariables() {
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
	 * Adds a definition of the agent type for the model
	 * @param type
	 * @param timeStep
	 * @param priority
	 */
	private final void addAgentType(Class<? extends Agent> type, RationalNumber timeStep, int priority) {
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
	private final void setTickTime(RationalNumber time) {
		tickTime = new RationalNumber(time);
	}
	
	
	/**
	 * Returns the model observer
	 * @return
	 */
	public final Observer getObserver() {
		return modelObserver;
	}
	
	
	/************ Main model methods ******************/
	
	
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
	
	
	/**
	 * A base class for any spark model factory which has access
	 * to private methods of the model class
	 * @author Monad
	 *
	 */
	public static abstract class SparkModelFactory {
		/* Model which is under construction */
		private SparkModel model;
		
		
		/**
		 * Internal initializer of the factory
		 * @param model
		 */
		protected void startConstruction(SparkModel model) {
			this.model = model;
		}
		
		
		/**
		 * Sets model's tick time
		 * @param model
		 * @param tickTime
		 */
		protected void setTickTime(RationalNumber tickTime) {
			model.setTickTime(tickTime);
		}
		
		/**
		 * Sets default observer name and mode
		 * @param defaultObserverName
		 * @param defaultMode
		 */
		protected void setDefaultObserver(String defaultObserverName, int defaultMode) {
			model.setDefaultObserver(defaultObserverName, defaultMode);
		}
		
		
		/**
		 * Adds a method
		 * @param method
		 * @return
		 */
		protected boolean addMethod(ModelMethod method) {
			return model.addMethod(method);
		}
		

		/**
		 * Adds the model variable into the collection of variables for the model
		 * @param var
		 */
		protected boolean addModelVariable(ModelVariable var) {
			return model.addModelVariable(var);
		}

		
		/**
		 * Adds a definition of the agent type for the model
		 * @param type
		 * @param timeStep
		 * @param priority
		 */
		protected void addAgentType(Class<? extends Agent> type, RationalNumber timeStep, int priority) {
			model.addAgentType(type, timeStep, priority);
		}

	}
	
}
