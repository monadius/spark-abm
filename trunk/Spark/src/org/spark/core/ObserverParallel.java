/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package org.spark.core;

import java.util.ArrayList;
import java.util.HashMap;
//import jsr166y.forkjoin.*;

import com.spinn3r.log5j.Logger;

import extra166y.ParallelArray;
import extra166y.Ops.Procedure;

/**
 * Implementation of the abstract context.
 * Agents are sorted by types and all agents of the same type
 * are stored in the array list
 */
class ObserverParallel extends ObserverImpl {
	private static final Logger logger = Logger.getLogger();

	// All agents
	private final HashMap<Class<? extends Agent>, ParallelArray<Agent>>	agents;

	//TODO: change
	private final static int AMOUNT_Agents = 1000;
	
	/**
	 * The default constructor
	 */
	ObserverParallel() {
		logger.info("Creating ObserverParallel");
		agents = new HashMap<Class<? extends Agent>, ParallelArray<Agent>>();
//		agents = ParallelArray.createEmpty(AMOUNT_Agents, Agent.class, ParallelArray.defaultExecutor());
	}
	
	
	@Override
	public int filterExecutionMode(int mode) {
		return ExecutionMode.PARALLEL_MODE;
	}
	
	
	/**
	 * Removes all agents, data layers and the space from the context
	 */
	@Override
	public synchronized void clear() {
		agents.clear();
	}
	
	
	/**
	 * Removes all agents
	 */
	@Override
	public synchronized void clearAgents() {
		agents.clear();
	}

	
	
	/**
	 * Adds the agent into the context
	 * @param agent
	 */
	@Override
	protected void addAgent(Agent agent, Class<? extends Agent> cl) {
		ParallelArray<Agent> list = agents.get(cl);

		if (list != null) {
			list.asList().add(agent);
		} else {
			list = ParallelArray.createEmpty(AMOUNT_Agents, Agent.class, ParallelArray.defaultExecutor());			
			list.asList().add(agent);

			agents.put(cl, list);
			observer.setAgentType(cl);
		}
	}
	
	
	@Override
	protected void addAllAgents(ArrayList<Agent> newAgents, Class<? extends Agent> cl) {
		ParallelArray<Agent> list = agents.get(cl);
		
		if (list == null) {
			list = ParallelArray.createEmpty(AMOUNT_Agents, Agent.class, ParallelArray.defaultExecutor());

			agents.put(cl, list);
			observer.setAgentType(cl);
		}
		
		list.asList().addAll(newAgents);
	}
	

	/**
	 * Removes the agent from the context
	 * @param agent
	 */
	@Override
	protected boolean removeAgent(Agent agent) {
		Class<? extends Agent> cl = agent.getClass();

		ParallelArray<Agent> list = agents.get(cl);
		
		if (list != null) {
			return list.asList().remove(agent);
		}
		
		return false;
	}
	
	
	@Override
	public synchronized int getAgentsNumber(final Class<? extends Agent> type) {
		ParallelArray<Agent> list = agents.get(type);
		if (list == null)
			return 0;
		
		return list.size();
	}
	
	
	@Override
	public synchronized int getAgentsNumberOfKind(final Class<? extends Agent> kind) {
		int n = 0;
		for (Class<? extends Agent> type : agents.keySet()) {
			if (derived(type, kind)) {
				n += agents.get(type).size();
			}
		}
		
		return n;
	}

	/**
	 * @deprecated
	 * Processes all agents
	 * @param tick
	 */
	@Override
	public synchronized void processAllAgents(final long tick) {
		// TODO: can anything be done with static agents?
/*		ParallelArray<Agent> TempAgent = agents.withMapping(
				new Op<Agent, Agent>() {
					public Agent op(Agent agent) {
						if (agent == null || agent.isDead())
							return null;
						agent.step(tick);

						// TODO: now this line is ineffective in a parallel mode
						if (agent.isDead())
							return null;
						
						return agent;
					}
				}).all();
		
		if (tick % 10 == 0) {
			if (TempAgent.withFilter(new Predicate<Agent>() {
				public boolean op(Agent arg0) {
					return arg0 == null;
				}
			}).size() > 0.1 * AMOUNT_Agents)
				TempAgent.removeNulls();
		}

		agents = TempAgent;
*/	
	}
	
	
	@Override
	public void processAgents(Class<? extends Agent> type, final SimulationTime time) {
		ParallelArray<Agent> list = agents.get(type);
		
		if (list == null)
			return;
		
		list.apply(new Procedure<Agent>() {
			public void op(Agent agent) {
				agent.step(time);
			}
		});
	}


	/**
	 * Prints statistic in the following form:
	 * for each type of agents the number of milliseconds
	 * required to process all agents of the given time is printed
	 */
	@Override
	public void printStatistics() {
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized <T extends Agent> T[] getAgents(final Class<T> type) {
		ParallelArray<Agent> list = agents.get(type);
		
		if (list == null)
			return null;

		T[] tmp = (T[])new Agent[list.size()];
		
		return list.asList().toArray(tmp);
	}
	
	
	
	public synchronized Agent[] getAgents() {
		throw new Error("Not implemented");
	}
}
