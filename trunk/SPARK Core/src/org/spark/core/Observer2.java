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
 * Basic class for representing the context.
 * Agents are sorted by types and agents of the same type are stored
 * in a double linked list.
 * This context is efficient when agents are removed (die) from 
 * the context often.
 */
// FIXME: Observer2 and Observer1 yields distinct results
// The reason: Observer2 adds new agents at the beginning of the list,
// meanwhile Observer1 adds new agents at the end
class Observer2 extends ObserverImpl {
	private final static Logger logger = Logger.getLogger();
	// All agents and their types
	private final HashMap<Class<? extends Agent>, DummyAgent>	agentsMap;
	private final ArrayList<DummyAgent> agentsList; 

	@SuppressWarnings("serial")
	private static class DummyAgent extends Agent 
	{
		public int number;
		public DummyAgent() { super(0); }
	}

	
	/**
	 * The default constructor
	 */
	Observer2() {
		logger.info("Creating Observer2");
		agentsMap = new HashMap<Class<? extends Agent>, DummyAgent>();
		agentsList = new ArrayList<DummyAgent>();
	}
	
	@Override
	public void clear() {
		agentsMap.clear();
		agentsList.clear();
	}
	
	/**
	 * Removes all agents
	 */
	@Override
	public synchronized void clearAgents() {
		agentsMap.clear();
		agentsList.clear();
	}

	
	/**
	 * Adds the agent into the context
	 * @param agent
	 * @param cl
	 */
	@Override
	public synchronized void addAgent(Agent agent, Class<? extends Agent> cl) {
		DummyAgent tmp = agentsMap.get(cl);
		if (tmp != null) {
			tmp.number++;
			agent.prev = tmp;
			agent.next = tmp.next;
			tmp.next.prev = agent;
			tmp.next = agent;
		}
		else {
			tmp = new DummyAgent();
			tmp.number = 1;
			tmp.next = agent;
			tmp.prev = agent;
			agent.next = tmp;
			agent.prev = tmp;
			agentsMap.put(cl, tmp);
			agentsList.add(tmp);
			
			observer.setAgentType(cl);
		}
	}
	
	
	/**
	 * Removes the agent from the context
	 * @param agent
	 */
	@Override
	public synchronized boolean removeAgent(Agent agent) {
		if (agent.next == null)
			return false;
		
		DummyAgent tmp = agentsMap.get(agent.getClass());
		tmp.number--;
		agent.prev.next = agent.next;
		agent.next.prev = agent.prev;
		
		agent.next = null;
		
		return true;
	}
	
	
	@Override
	public synchronized int getAgentsNumber(Class<? extends Agent> type) {
		DummyAgent tmp = agentsMap.get(type);
		if (tmp != null)
			return tmp.number;
		else
			return 0;
	}
	
	/**
	 * Returns the number of agents derived from a specific type
	 */
	public synchronized int getAgentsNumberOfKind(Class<? extends Agent> kind) {
		int n = 0;
		for (Class<? extends Agent> type : agentsMap.keySet()) {
			if (derived(type, kind)) {
				n += agentsMap.get(type).number;
			}
		}
		
		return n;
	}
	

	@SuppressWarnings("unchecked")
	Class<? extends Agent>[] ctmp = new Class[0];
	/**
	 * @deprecated
	 * Processes all agents
	 */
	@Override
	public void processAllAgents(long tick) {
		// TODO: static agents
//		final Class<? extends Agent>[] types = agents2.keySet().toArray(ctmp);
//		if (types == null) return;
		
//		int k = types.length;
		int k = agentsList.size(); 
		for (int j = 0; j < k; j++) {
//			DummyAgent first = agents2.get(types[j]);
			DummyAgent first = agentsList.get(j);

			for (Agent agent = first.next; first != agent; agent = agent.next) {
				if (agent.isDead()) continue;
				agent.step(tick);
			}
		}
	}
	
	
	@Override
	public void processAgents(Class<? extends Agent> type, SimulationTime time) {
		DummyAgent first = agentsMap.get(type);
		if (first == null)
			return;

		for (Agent agent = first.next; first != agent; agent = agent.next) {
			if (agent.isDead()) continue;
			agent.step(time);
		}
	}

	
	
	Agent[] tmp = new Agent[0];
	@SuppressWarnings("unchecked")
	@Override
	public synchronized <T extends Agent> T[] getAgents(Class<T> type) {
		DummyAgent tmp = agentsMap.get(type);
		if (tmp == null) return null;

		Agent[] agents = new Agent[tmp.number];
		int i = 0;
		for (Agent next = tmp.next; next != tmp; next = next.next) {
			if (i >= tmp.number) {
				throw new Error();
			}

			agents[i++] = next;
		}
		
		return (T[]) agents;		
	}
	
	
	@Override
	public synchronized Agent[] getAgents() {
		ArrayList<Agent> all = new ArrayList<Agent>();
		
		for (DummyAgent tmp : agentsMap.values()) {
			for (Agent next = tmp.next; next != tmp; next = next.next) {
				all.add(next);
			}
		}
		
		return all.toArray(tmp);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Agent> ArrayList<T> getAgentsList(Class<T> type) {
		DummyAgent tmp = agentsMap.get(type);
		if (tmp == null)
			return null;
			
		ArrayList all = new ArrayList(tmp.number);
		
		for (Agent next = tmp.next; next != tmp; next = next.next) {
			all.add(next);
		}
		
		return all;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Agent> ArrayList<T> getAgentsListOfKind(Class<T> kind) {
		ArrayList<DummyAgent> types = new ArrayList<DummyAgent>();
		
		int n = 0;
		for (Class<? extends Agent> type : agentsMap.keySet()) {
			if (derived(type, kind)) {
				DummyAgent tmp = agentsMap.get(type);
				n += tmp.number;
				types.add(tmp);
			}
		}
		
		ArrayList all = new ArrayList(n);
		
		for (int i = 0; i < types.size(); i++) {
			DummyAgent tmp = types.get(i);
			
			for (Agent next = tmp.next; next != tmp; next = next.next) {
				all.add(next);
			}
		}
		
		return all;
	}
	
	@Override
	protected synchronized void addAllAgents(ArrayList<Agent> newAgents,
			Class<? extends Agent> cl) {
		int n = newAgents.size();
		for (int i = 0; i < n; i++) {
			addAgent(newAgents.get(i), cl);
		}
	}

}
