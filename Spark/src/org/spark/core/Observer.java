/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */

package org.spark.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;

import org.spark.data.DataLayer;
import org.spark.math.RationalNumber;
import org.spark.space.Space;
import org.spark.utils.RandomHelper;

import com.spinn3r.log5j.Logger;

/**
 * Basic class for representing the context. All agents should belong to the
 * context. This class is abstract. There are several other classes implementing
 * all methods of the generic context.
 * 
 * @see org.spark.core.Observer1
 * @see org.spark.core.Observer2
 */
public final class Observer {
	// Log
	private static final Logger logger = Logger.getLogger();

	/**
	 * Priority constants
	 */
	public static final int HIGH_PRIORITY = 0;
	public static final int LOW_PRIORITY = 1000;
	
	// Instance
	// TODO: should be removed later
	static volatile Observer instance;
	
	
	// Implementation of main methods
//	private static volatile ObserverImpl impl;
	private ObserverImpl impl;
	// Mode execution flag
//	private static volatile boolean serialMode;
	// TODO: make it final and add constructor
	// Observers should be created for each loaded model
	private int executionMode;

	/* True when inside setup method */
	private boolean setupFlag;
	
	
	/**
	 * Returns true if setup method is executed
	 * @return
	 */
	public boolean getSetupFlag() {
		return setupFlag;
	}
	
	
	/**
	 * Description of time properties of an agent type
	 * @author Monad
	 */
	static class AgentType implements Comparable<AgentType> {
		/* Type */
		public Class<? extends Agent> type;
		/* Time step */
		public RationalNumber timeStep;
		/* Priority */
		public int priority;
		
		
		/**
		 * Default constructor
		 * @param type
		 * @param timeStep
		 * @param priority
		 */
		public AgentType(Class<? extends Agent> type, RationalNumber timeStep, int priority) {
			this.type = type;
			this.timeStep = new RationalNumber(timeStep);
			this.priority = priority;
		}
		
		/**
		 * Compares priorities: low number means high priority
		 * If priorities are equal, then use lexical ordering
		 */
		public int compareTo(AgentType b) {
			// b has higher priority
			if (b.priority < priority)
				return 1;
			
			// b has lower priority
			if (b.priority > priority)
				return -1;

			// Use lexical ordering
			return type.getName().compareTo(b.type.getName());
		}
	}
	
	
	// All registered agent types
	private final HashMap<Class<? extends Agent>, AgentType> agentTypes;
	
	
	/**
	 * Represents a time event for the given type of agents
	 * @author Monad
	 *
	 */
	private static class AgentTime implements Comparable<AgentTime> {
		public RationalNumber time;
		public AgentType type;

		/**
		 * Default constructor
		 * @param type
		 */
		public AgentTime(AgentType type) {
			this.type = type;
			this.time = type.timeStep;
		}

		public int compareTo(AgentTime t) {
			return time.compareTo(t.time);
		}
		
		
		public void advanceTime() {
			if (type.timeStep.compareTo(RationalNumber.ZERO) <= 0)
				time.add(RationalNumber.ONE);
			else
				time.add(type.timeStep);
		}
		
	}
	
	// The queue of agent actions
	private final PriorityQueue<AgentTime> actionQueue;
	
	// Simulation time
	private final SimulationTime time;

	/* List of all spaces */
	private final ArrayList<Space> spacesList;
	/* Hash map of all spaces */
	private final HashMap<String, Space> spacesMap;

	// Default space
	private Space defaultSpace;

	// TODO: be careful with agents which are added and then removed in the
	// same step
	// New agents queue
	// TODO: ObserverParallel can use ParrallelArray for new agents also
	// TODO: maybe ArrayList is better?
	private final HashMap<Class<? extends Agent>, ArrayList<Agent>> newAgents;
	private final ArrayList<Agent> removedQueue;

	// for some internal statistics
	private long statTime;

	
	/**
	 * Returns the current simulation time
	 * @return
	 */
	public SimulationTime getSimulationTime() {
		return new SimulationTime(time);
	}
	
	
	/**
	 * Returns the current time value
	 * @return
	 */
	public RationalNumber getTime() {
		return time.getTime();
	}

	
	/**
	 * Returns the current execution mode
	 * @return
	 */
	public int getExecutionMode() {
		return executionMode;
	}
	
	
	/**
	 * Returns the current simulation tick
	 * @return
	 */
	public long getSimulationTick() {
		return time.getTick();
	}
	
	
	/**
	 * Advances simulation tick by one
	 */
	public void advanceSimulationTick() {
		time.advanceTick();
	}

	
	/**
	 * Returns true if serial mode is on
	 * @return
	 */
	public boolean isSerial() {
		return setupFlag || (executionMode == ExecutionMode.SERIAL_MODE);
	}

	

	/**
	 * Internal constructor
	 */
	Observer(ObserverImpl implementation, int executionMode) {
		this.impl = implementation;
		impl.setObserver(this);
		
		newAgents = new HashMap<Class<? extends Agent>, ArrayList<Agent>>();
		removedQueue = new ArrayList<Agent>(100);
		
		actionQueue = new PriorityQueue<AgentTime>(20);
		time = new SimulationTime();
		
		agentTypes = new HashMap<Class<? extends Agent>, AgentType>();

		spacesList = new ArrayList<Space>();
		spacesMap = new HashMap<String, Space>();

		if (ExecutionMode.isMode(executionMode)) {
			this.executionMode = executionMode;
		}
		else
		{
			this.executionMode = ExecutionMode.SERIAL_MODE;
		}
		
		RandomHelper.reset(executionMode == ExecutionMode.PARALLEL_MODE);
		
		logger.debug("Observer is created. Execution mode: " + ExecutionMode.toString(executionMode));
	}

	/**
	 * Gets the instance of the observer
	 * 
	 * @return observer an instance of the observer
	 */
	public static Observer getInstance() {
		return instance;
	}

	/**
	 * @deprecated Gets the instance of the space
	 * @return space a current model space
	 */
	public static Space getSpace() {
		return getDefaultSpace();
	}

	/**
	 * Gets the instance of a default space
	 * 
	 * @return
	 */
	public static Space getDefaultSpace() {
		if (instance != null)
			return instance.defaultSpace;

		return null;
	}

	/**
	 * Returns the instance of space with a given name
	 * 
	 * @param name
	 * @return
	 */
	public static Space getSpace(String name) {
		if (instance != null) {
			return instance.spacesMap.get(name);
		}

		return null;
	}

	/**
	 * Clears the context by removing all agents, data layers, etc.
	 */
/*	public void clear() {
		newAgents.clear();

		defaultSpace = null;
		spacesList.clear();
		spacesMap.clear();

		actionQueue.clear();
		time.reset();
		
		agentTypes.clear();
		
		impl.clear();

		logger.debug("Resetting random generator");
		RandomHelper.reset();
	}
*/	
	
	/**
	 * Resets the Observer
	 * Does not affect the information about agent types
	 */
	public void reset() {
		newAgents.clear();
		
		defaultSpace = null;
		spacesList.clear();
		spacesMap.clear();
		
		actionQueue.clear();
		time.reset();
		
		impl.clear();
		
		RandomHelper.reset(executionMode == ExecutionMode.PARALLEL_MODE);
	}

	/**
	 * Removes all agents from the context
	 */
	public void clearAgents() {
		newAgents.clear();
		impl.clearAgents();
	}
	
	
	/**
	 * Initializes the action queue
	 */
	private void initializeActionQueue() {
		actionQueue.clear();
		
		for (AgentType type : agentTypes.values()) {
			AgentTime t = new AgentTime(type);
			t.time = time.getTime().add(type.timeStep);
			actionQueue.add(t);
		}
	}
	
	
	/**
	 * Creates a definition of the specific type of agents
	 * @param type
	 * @param time
	 * @param priority
	 */
	synchronized void setAgentType(Class<? extends Agent> type, RationalNumber timeStep, int priority) {
		AgentType t = agentTypes.get(type);
		
		if (priority < HIGH_PRIORITY)
			priority = HIGH_PRIORITY;
		else if (priority > LOW_PRIORITY)
			priority = LOW_PRIORITY;
		
		if (t == null) {
			t = new AgentType(type, timeStep, priority);
			agentTypes.put(type, t);
			
			AgentTime tt = new AgentTime(t);
			tt.time = time.getTime().add(timeStep);
			
			actionQueue.add(tt);
		}
		else {
			t.timeStep = timeStep;
			t.priority = priority;
		}
	}
	
	
	/**
	 * Creates a definition of the specific type of agents
	 * with default parameters
	 * @param type
	 */
	synchronized void setAgentType(Class<? extends Agent> type) {
		AgentType t = agentTypes.get(type);
		
		if (t == null) {
			t = new AgentType(type, RationalNumber.ONE, LOW_PRIORITY);
			agentTypes.put(type, t);
			
			AgentTime tt = new AgentTime(t);
			tt.time = time.getTime().add(RationalNumber.ONE);
			
			actionQueue.add(tt);
		}
	}

	/**
	 * @deprecated Sets up the space for the model. This function should be
	 *             called before all other steps of the model initialization. It
	 *             can be called only once
	 */
	public void setSpace(Space space) {
		addSpace("space", space);
		setDefaultSpace("space");
	}

	/**
	 * Adds a new space to the context
	 * 
	 * @param space
	 */
	public <T extends Space> T addSpace(String name, T space) {
		if (spacesMap.containsKey(name))
			throw new Error("Space " + name + " is already defined");

		int index = spacesList.size();
		space.setIndex(index);
		
		spacesList.add(space);
		spacesMap.put(name, space);

		if (defaultSpace == null)
			defaultSpace = space;

		return space;
	}

	/**
	 * Returns names of all spaces in a model
	 * 
	 * @return
	 */
	public String[] getSpaceNames() {
		String[] tmp = new String[0];
		return spacesMap.keySet().toArray(tmp);
	}

	/**
	 * Returns the name associated with a given space
	 * 
	 * @param space
	 * @return null if no such space found
	 */
	public String getSpaceName(Space space) {
		for (String name : spacesMap.keySet()) {
			if (space == spacesMap.get(name))
				return name;
		}

		return null;
	}

	/**
	 * Sets a default space for spatial operations
	 * 
	 * @param name
	 */
	public void setDefaultSpace(String name) {
		Space space = spacesMap.get(name);

		if (space != null)
			defaultSpace = space;
	}

	/**
	 * @deprecated Adds a new data layer to a default space
	 * @param name
	 *            a name of a new data layer
	 * @param data
	 *            a data layer itself
	 */
	public <T extends DataLayer> T addDataLayer(String name, T data) {
		if (data.getSpace() != defaultSpace)
			throw new Error("A data layer " + name + " is associated with "
					+ data.getSpace() + ", so it cannot be added into "
					+ defaultSpace);

		return defaultSpace.addDataLayer(name, data);
	}

	/**
	 * @deprecated Use space.getDataLayer(name) or findDataLayer(name) instead.
	 *             Gets the specific data layer
	 * @param name
	 *            a name of the data layer
	 * @return
	 */
	public DataLayer getDataLayer(String name) {
		return findDataLayer(name);
	}

	/**
	 * Finds the specific data layer in a model
	 * 
	 * @param name
	 * @return null if no data layer was found
	 */
	public DataLayer findDataLayer(String name) {
		for (Space space : spacesList) {
			DataLayer data = space.getDataLayer(name);

			if (data != null)
				return data;
		}

		return null;
	}

	/**
	 * Adds the agent into the context
	 * 
	 * @param agent
	 *            an agent to be added to the context
	 */
	protected void addAgent(Agent agent) {
		Class<? extends Agent> cl = agent.getClass();

		if (setupFlag) {
			impl.addAgent(agent, cl);
			return;
		}
		
		switch (executionMode) {
		// Serial Mode
		case ExecutionMode.SERIAL_MODE:
			impl.addAgent(agent, cl);
			break;
			
		// Concurrent Mode
		case ExecutionMode.CONCURRENT_MODE:
			ArrayList<Agent> list = newAgents.get(cl);

			if (list != null) {
				list.add(agent);
			} else {
				list = new ArrayList<Agent>(100);
				list.add(agent);
				newAgents.put(cl, list);
			}
			break;

		// Parallel Mode
		case ExecutionMode.PARALLEL_MODE:
			synchronized (newAgents) {
				list = newAgents.get(cl);

				if (list != null) {
					list.add(agent);
				} else {
					list = new ArrayList<Agent>(100);
					list.add(agent);
					newAgents.put(cl, list);
				}
			}
			break;
			
		}
	}

	/**
	 * Adds an agent into the context instantly without any waiting queues
	 * 
	 * @param agent
	 */
	void addAgentInstantly(Agent agent) {
		impl.addAgent(agent, agent.getClass());
	}

	/**
	 * Removes the agent from the context
	 * 
	 * @param agent
	 */
	protected void removeAgent(Agent agent) {
		if (setupFlag) {
			Class<? extends Agent> cl = agent.getClass();

			agent.dead = true;

			// TODO: removeAgent(agent, cl)
			if (impl.removeAgent(agent))
				return;

			ArrayList<Agent> list = newAgents.get(cl);
			if (list != null) {
				list.remove(agent);
			}

			return;
		}
		
		switch (executionMode) {
		case ExecutionMode.SERIAL_MODE:
			Class<? extends Agent> cl = agent.getClass();

			agent.dead = true;

			// TODO: removeAgent(agent, cl)
			if (impl.removeAgent(agent))
				return;

			ArrayList<Agent> list = newAgents.get(cl);
			if (list != null) {
				list.remove(agent);
			}
			break;
			
		case ExecutionMode.CONCURRENT_MODE:
			removedQueue.add(agent);
			break;
			
		case ExecutionMode.PARALLEL_MODE:
			synchronized (removedQueue) {
				removedQueue.add(agent);
			}
		}
	}

	/**
	 * Returns the number of specific agents
	 * 
	 * @param type
	 *            a type of agents for which the number is retrieved
	 * @return the number of agents of the given type
	 */
	public int getAgentsNumber(Class<? extends Agent> type) {
		return impl.getAgentsNumber(type);
	}

	/**
	 * Returns the number of agents derived from a specific type
	 * 
	 * @param type
	 * @return
	 */
	public int getAgentsNumberOfKind(Class<? extends Agent> type) {
		return impl.getAgentsNumberOfKind(type);
	}

	/**
	 * Processes a queue of newly added agents
	 */
	private synchronized void processNewAgents() {
		for (Class<? extends Agent> cl : newAgents.keySet()) {
			ArrayList<Agent> newList = newAgents.get(cl);

			impl.addAllAgents(newList, cl);
			newList.clear();
		}
	}

	/**
	 * Processes a queue of removed agents
	 */
	private synchronized void processRemovedAgents() {
		for (int i = 0; i < removedQueue.size(); i++) {
			Agent agent = removedQueue.get(i);
			// It is possible that an agent is added to the queue twice
			// Avoid that by checking the dead flag
			if (agent.dead)
				continue;
			
			agent.dead = true;

			if (impl.removeAgent(agent))
				continue;

			ArrayList<Agent> list = newAgents.get(agent.getClass());
			if (list != null) {
				list.remove(agent);
			}
		}

		removedQueue.clear();
	}

	/**
	 * @deprecated
	 * Processes all agents
	 * 
	 * @param tick
	 *  			a number representing time passed since the model start
	 */
	public void processAllAgents(long tick) {
		// TODO: execution mode works here
		impl.processAllAgents(tick);

		long start = System.currentTimeMillis();
		processRemovedAgents();
		processNewAgents();

		for (Space space : spacesList) {
			space.processNodes();
		}

		long end = System.currentTimeMillis();

		statTime += end - start;
	}
	
	
	
	private ArrayList<AgentType> tempTypeList = new ArrayList<AgentType>();
	
	/**
	 * Fills in the type list
	 * @param t1
	 */
	private void fillTypeList(RationalNumber t1) {
		tempTypeList.clear();
		
		// Take the first time event from the queue
		AgentTime t = actionQueue.peek();
		
		// No time events
		if (t == null) {
			time.setTime(t1);
			return;
		}

		RationalNumber t0 = new RationalNumber(t.time);
		
		// It's not time yet
		if (t0.compareTo(t1) > 0) {
			time.setTime(t1);
			return;
		}
		
		time.setTime(t0);
		
		// Add the first type into the list
		tempTypeList.add(t.type);

		// Advance time for the first time event and return it to the queue
		actionQueue.poll();
		t.advanceTime();
		actionQueue.add(t);

		// Get time events with the same time
		while (true) {
			t = actionQueue.peek();
			
			if (t == null)
				break;
			
			int c = t.time.compareTo(t0);

			if (c > 0) {
				break;
			}
			
			if (c < 0) {
				throw new Error("Error in action queue");
			}
			
			if (c == 0) {
				t = actionQueue.poll();
				tempTypeList.add(t.type);
				
				t.advanceTime();
				actionQueue.add(t);
			}
		}
		
	}
	/**
	 * Processes all agents in the time interval [t, t + dt]
	 * where t is the current time
	 * @param dt
	 */
	public void processAllAgents(RationalNumber dt) {
		RationalNumber t1 = time.getTime().add(dt);
		
		while (true) {
			fillTypeList(t1);
			
			if (tempTypeList.size() == 0) {
				break;
			}
			
			AgentType[] types = new AgentType[tempTypeList.size()];
			types = tempTypeList.toArray(types);
			
			// Sort types according to their priorities
			Arrays.sort(types);
			
			// Begin step for all data layers
			for (Space space : spacesList) {
				space.dataLayersBeginStep();
			}
			
			// Process all agents
			for (int i = 0; i < types.length; i++) {
				impl.processAgents(types[i].type, time);
			}
			
			// Post process for agents
			processRemovedAgents();
			processNewAgents();

			for (Space space : spacesList) {
				space.processNodes();
			}
			
			// End step for all data layers
			for (Space space : spacesList) {
				space.dataLayersEndStep();
			}
		}
		
		// TODO:
		// Ignore static agents
		// Make steps and collect statistics for each agent type separately
	}
	
	
	/**
	 * Call before model setup method
	 */
	public void beginSetup() {
		time.reset();
		setupFlag = true;
	}

	/**
	 * Call after model setup method
	 */
	public void finalizeSetup() {
		// TODO: next line are not required
		// because setup always works in the serial mode
		processRemovedAgents();
		processNewAgents();

		for (Space space : spacesList) {
			space.processNodes();
		}
		
		time.reset();
		initializeActionQueue();

		setupFlag = false;
	}

	/**
	 * Processes all data layers
	 * 
	 * @param tick
	 *            a number representing time passed since the model start
	 */
	public void processAllDataLayers(long tick) {
		// TODO: execution modes
		for (Space space : spacesList) {
			space.processAllDataLayers(tick);
		}
	}

	// TODO: should be protected or remove this method at all
	// it is used only in the render and internally by the observer
	/**
	 * Returns a set of specific agents
	 * 
	 * @param type
	 *            a type of agents to be returned
	 * @return a set of all agents of the given type
	 */
	public <T extends Agent> T[] getAgents(Class<T> type) {
		return impl.getAgents(type);
	}

	/**
	 * @deprecated Returns all agents
	 * @return
	 */
	public Agent[] getAgents() {
		return impl.getAgents();
	}

	/**
	 * Returns a list of agents of a specific type
	 * 
	 * @param <T>
	 * @param type
	 * @return
	 */
	public <T extends Agent> ArrayList<T> getAgentsList(Class<T> type) {
		return impl.getAgentsList(type);
	}

	/**
	 * Returns all agents derived from a specific type
	 * 
	 * @param <T>
	 * @param type
	 * @return
	 */
	public <T extends Agent> ArrayList<T> getAgentsListOfKind(Class<T> type) {
		return impl.getAgentsListOfKind(type);
	}

	/**
	 * @deprecated Returns the names of data layers in all spaces
	 * @return names of all data layers
	 */
	public String[] getDataLayers() {
		ArrayList<String> names = new ArrayList<String>();
		String[] tmp = new String[0];

		for (Space space : spacesList) {
			tmp = space.getDataLayerNames();
			if (tmp == null)
				continue;

			for (int i = 0; i < tmp.length; i++)
				names.add(tmp[i]);
		}

		return names.toArray(tmp);
	}

	/**
	 * Prints performance statistics
	 */
	public void printStatistics() {
		logger.debug("Additional time: %d", statTime);
		System.err.println("Additional time: " + statTime);
		impl.printStatistics();
	}

	/**
	 * Writes the current state of the model into the output stream
	 * 
	 * @param model
	 * @param out
	 */
	// FIXME: serialize time and actionQueue with agentTypes
	// FIXME: save observer itself (executionMode, etc.) and its implementation (type)
	public synchronized void serializeState(SparkModel model, OutputStream out)
			throws Exception {
		// TODO: save simulation time
		if (newAgents.size() != 0 || removedQueue.size() != 0)
			throw new Exception(
					"Could not serialize model during agent's processing");

		// Create an object output stream
		ObjectOutputStream oos = new ObjectOutputStream(out);

		// Static model variables
		Field[] fields = model.getClass().getFields();
		if (fields == null)
			fields = new Field[0];

		oos.writeInt(fields.length);
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers())) {
				Object val = field.get(model);

				// TODO: val should be primitive or String

				oos.writeObject(field.getName());
				oos.writeObject(val);
			} else {
				throw new Exception(
						"Only static fields are allowed in a model class");
			}
		}

		// serialMode
		oos.writeInt(executionMode);

		// spacesList
		// spacesMap
		String defaultSpaceName = null;

		oos.writeInt(spacesMap.keySet().size());

		for (String spaceName : spacesMap.keySet()) {
			Space space = spacesMap.get(spaceName);

			if (space == defaultSpace)
				defaultSpaceName = spaceName;

			oos.writeObject(spaceName);
			space.serializeSpace(oos);
		}

		// defaultSpace
		oos.writeObject(defaultSpaceName);

		// Agents
		impl.serializeAgents(oos);
	}

	/**
	 * Loads the model state from the input stream
	 * 
	 * @param model
	 * @param in
	 */
	// TODO: make static + load observer itself and its implementation
	public static synchronized void loadState(SparkModel model, InputStream in,
			ClassLoader cl) throws Exception {
		// TODO: load simulation time
		// TODO: save/load states on a specific thread only
		throw new Error("Not implemented");
/*
		if (newAgents.size() != 0 || removedQueue.size() != 0)
			throw new Exception(
					"Could not load model state during agent's processing");

		// Clear everything
		clear();

		// Create an object input stream
		MyObjectInputStream ois = new MyObjectInputStream(in);
		ois.cl = cl;

		// Read global parameters
		int n = ois.readInt();

		for (int i = 0; i < n; i++) {
			String fieldName = (String) ois.readObject();
			Object value = ois.readObject();
			Field field = model.getClass().getField(fieldName);

			if (!Modifier.isFinal(field.getModifiers()))
				model.getClass().getField(fieldName).set(model, value);
		}

		// serialMode
//		setExecutionMode(ois.readInt());

		// spacesList
		// spacesMap
		n = ois.readInt();

		for (int i = 0; i < n; i++) {
			String spaceName = (String) ois.readObject();
			Space space = Space.loadSpace(ois);
			if (space == null)
				throw new Exception("Space " + spaceName
						+ " could not be loaded");

			addSpace(spaceName, space);
		}

		String defaultSpaceName = (String) ois.readObject();
		setDefaultSpace(defaultSpaceName);

		// Agents
		ois.setUserClass(true);
		impl.loadAgents(ois);*/
	}

	public static class MyObjectInputStream extends ObjectInputStream {
		protected ClassLoader cl = null;
		protected boolean userClass = false;

		public MyObjectInputStream(InputStream in) throws Exception {
			super(in);
		}

		public void setUserClass(boolean flag) {
			userClass = flag;
		}

		@Override
		protected Class<?> resolveClass(ObjectStreamClass desc)
				throws IOException, ClassNotFoundException {
			if (cl != null && userClass) {
				String name = desc.getName();
				return cl.loadClass(name);
			} else {
				return super.resolveClass(desc);
			}
		}
	}
}
