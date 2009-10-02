/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package org.spark.core;

import java.util.ArrayList;
import java.util.HashMap;

import com.spinn3r.log5j.Logger;

/**
 * Implementation of the abstract context.
 * Agents are sorted by types and all agents of the same type
 * are stored in the array list
 */
class Observer1 extends ObserverImpl {
	private static final Logger logger = Logger.getLogger();
	// All agents and their types
	private final HashMap<Class<? extends Agent>, ArrayList<Agent>>	agents;
	// List of all types of agents
	private final ArrayList<Class<? extends Agent>> listOfTypes;
	private final HashMap<Class<? extends Agent>, Boolean> staticFlags;
	// For statistics
	private final HashMap<Class<? extends Agent>, Long> statistics;
	

	/**
	 * The default constructor
	 */
	Observer1() {
		logger.info("Creating Observer1");
		agents = new HashMap<Class<? extends Agent>, ArrayList<Agent>>();
		listOfTypes = new ArrayList<Class<? extends Agent>>();
		staticFlags = new HashMap<Class<? extends Agent>, Boolean>();
		statistics = new HashMap<Class<? extends Agent>, Long>();
	}
	
	/**
	 * Removes all agents, data layers and the space from the context
	 */
	public synchronized void clear() {
		agents.clear();
		listOfTypes.clear();
		staticFlags.clear();
	}
	
	
	/**
	 * Removes all agents
	 */
	public synchronized void clearAgents() {
		agents.clear();
		listOfTypes.clear();
		staticFlags.clear();
	}
	
	
	/**
	 * Adds the agent into the context
	 * @param agent
	 */
	protected synchronized void addAgent(Agent agent, Class<? extends Agent> cl) {
		ArrayList<Agent> list = agents.get(cl);

		if (list != null) {
			list.add(agent);
		} else {
			list = new ArrayList<Agent>(100);
			list.add(agent);
			agents.put(cl, list);
			listOfTypes.add(cl);
			statistics.put(cl, 0l);

			observer.setAgentType(cl);
			
			AgentAnnotation note = cl.getAnnotation(AgentAnnotation.class);
			staticFlags.put(cl, note != null ? note.Static() : false);
		}
	}
	

	protected synchronized void addAllAgents(ArrayList<Agent> newAgents, Class<? extends Agent> cl) {
		ArrayList<Agent> list = agents.get(cl);
		
		if (list == null) {
			list = new ArrayList<Agent>(100);
			agents.put(cl, list);
			listOfTypes.add(cl);
			statistics.put(cl, 0l);

			observer.setAgentType(cl);
			
			AgentAnnotation note = cl.getAnnotation(AgentAnnotation.class);
			staticFlags.put(cl, note != null ? note.Static() : false);
		}
		
		list.addAll(newAgents);
	}

	
	/**
	 * Removes the agent from the context
	 * @param agent
	 */
	// TODO: remove synchronization
	protected synchronized boolean removeAgent(Agent agent) {
		Class<? extends Agent> cl = agent.getClass();

		ArrayList<Agent> list = agents.get(cl);
		
		if (list != null) {
			return list.remove(agent);
		}
		
		return false;
	}
	
	/**
	 * Returns the number of agents of a specific type
	 */
	public synchronized int getAgentsNumber(Class<? extends Agent> type) {
		ArrayList<Agent> list = agents.get(type);
		
		if (list != null)
			return list.size();
		else
			return 0;
	}
	
	
	/**
	 * Returns the number of agents derived from a specific type
	 */
	public synchronized int getAgentsNumberOfKind(Class<? extends Agent> kind) {
		int n = 0;
		for (Class<? extends Agent> type : agents.keySet()) {
			if (derived(type, kind)) {
				n += agents.get(type).size();
			}
		}
		
		return n;
	}
	

	@SuppressWarnings("unchecked")
	Class<? extends Agent>[] ctmp = new Class[0];
	/**
	 * @deprecated
	 */
	public void processAllAgents(long tick) {
//		final Class<? extends Agent>[] types = agents.keySet().toArray(ctmp);
//		if (types == null) return;
		
//		int k = types.length;
		int k = listOfTypes.size();

		for (int j = 0; j < k; j++) {
			Class<? extends Agent> type = listOfTypes.get(j);
//			if (staticFlags.get(types[j]))
			if (staticFlags.get(type))
				continue;
			
//			Agent[] agents = getAgents(types[j]);
			Agent[] agents = getAgents(type);
			int n = agents.length;
			
			long start = System.currentTimeMillis();
			
			for (int i = 0; i < n; i++) {
				if (agents[i].isDead()) continue;
				agents[i].step(tick);
			}
			
			long end = System.currentTimeMillis();
//			long time = statistics.get(types[j]);
//			statistics.put(types[j], time + end - start);
			long time = statistics.get(type);
			statistics.put(type, time + end - start);
		}
		
	}
	

	@Override
	public void processAgents(Class<? extends Agent> type, SimulationTime time) {
//		if (staticFlags.get(type))
//			continue;
			
			Agent[] agents = getAgents(type);
			if (agents == null)
				return;
			
			int n = agents.length;
			
//			long start = System.currentTimeMillis();
			
			for (int i = 0; i < n; i++) {
				if (agents[i].isDead()) continue;
				agents[i].step(time);
			}
			
//			long end = System.currentTimeMillis();
//			long time = statistics.get(type);
//			statistics.put(type, time + end - start);
	}

	
	
	// A trick used to avoid explicit cast operation
	Agent[] tmp = new Agent[0];
	@SuppressWarnings("unchecked")
	public synchronized <T extends Agent> T[] getAgents(Class<T> type) {
		if (agents.containsKey(type)) {
			ArrayList<T> set = (ArrayList<T>) agents.get(type);
			T[] tmp = (T[]) new Agent[set.size()];
			return agents.get(type).toArray(tmp);
		}
		else
			return null;
	}
	
	
	@SuppressWarnings("unchecked")
	public synchronized <T extends Agent> ArrayList<T> getAgentsList(Class<T> type) {
		if (agents.containsKey(type))
			return (ArrayList<T>) agents.get(type);
		else
			return null;
	}
	
	
	@SuppressWarnings("unchecked")
	public synchronized <T extends Agent> ArrayList<T> getAgentsListOfKind(Class<T> kind) {
		ArrayList<ArrayList<Agent>> types = new ArrayList<ArrayList<Agent>>();
		
		int n = 0;
		for (Class<? extends Agent> type : agents.keySet()) {
			if (derived(type, kind)) {
				ArrayList<Agent> list = agents.get(type);
				n += list.size();
				types.add(list);
			}
		}
		
		if (types.size() == 0)
			return null;
		
		if (types.size() == 1)
			return (ArrayList<T>) types.get(0);
		
		ArrayList<Agent> allAgents = new ArrayList<Agent>(n);
		for (int i = 0; i < types.size(); i++) {
			allAgents.addAll(types.get(i));
		}
		
		return (ArrayList<T>) allAgents;
	}
	
	
	public synchronized Agent[] getAgents() {
		ArrayList<Agent> all = new ArrayList<Agent>();
		
		for (ArrayList<Agent> list : agents.values()) {
			all.addAll(list);
		}
		
		return all.toArray(tmp);
	}
	
	
	/**
	 * Prints statistic in the following form:
	 * for each type of agents the number of milliseconds
	 * required to process all agents of the given time is printed
	 */
	public void printStatistics() {
		final Class<? extends Agent>[] types = statistics.keySet().toArray(ctmp); 
		if (types == null) return;
		
		int k = types.length;
		for (int j = 0; j < k; j++) {
			String name = types[j].getSimpleName();
			long time = statistics.get(types[j]);
			
			logger.debug(name + ": " + time);
			System.err.println(name + ": " + time);
		}
		
	}

}
