/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package org.spark.core;

import java.util.ArrayList;
//import jsr166y.forkjoin.*;

import com.spinn3r.log5j.Logger;

import extra166y.ParallelArray;
import extra166y.Ops.Op;
import extra166y.Ops.Predicate;

/**
 * Implementation of the abstract context.
 * Agents are sorted by types and all agents of the same type
 * are stored in the array list
 */
class ObserverParallel extends ObserverImpl {
	private static final Logger logger = Logger.getLogger();
	// All agents
	private ParallelArray<Agent>	agents;
	// New agent queue
//	private ParallelArray<Agent>	newAgents;
	// For statistics
//	private HashMap<Class<? extends Agent>, Long> statistics;
	
	//TODO change
	private final static int AMOUNT_Agents = 1000;
	
	/**
	 * The default constructor
	 */
	ObserverParallel() {
		logger.info("Creating ObserverParallel");
		agents = ParallelArray.createEmpty(AMOUNT_Agents, Agent.class, ParallelArray.defaultExecutor());
//		newAgents = ParallelArray.createEmpty(AMOUNT_Agents, Agent.class, ParallelArray.defaultExecutor());
		
		//agents2 = new HashMap<Class<? extends Agent>, ArrayList<Agent>>();
//		statistics = new HashMap<Class<? extends Agent>, Long>();
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
		// TODO: find better solution
		agents = ParallelArray.createEmpty(AMOUNT_Agents, Agent.class, ParallelArray.defaultExecutor());
//		newAgents = ParallelArray.createEmpty(AMOUNT_Agents, Agent.class, ParallelArray.defaultExecutor());
		
	}
	
	
	/**
	 * Removes all agents
	 */
	@Override
	public synchronized void clearAgents() {
		// TODO: find better solution
		agents = ParallelArray.createEmpty(AMOUNT_Agents, Agent.class, ParallelArray.defaultExecutor());
	}

	
	
	/**
	 * Adds the agent into the context
	 * @param agent
	 */
	@Override
	protected void addAgent(Agent agent, Class<? extends Agent> cl) {
		synchronized (agents) {
			agents.asList().add(agent);
		}
	}
	
	
	@Override
	protected void addAllAgents(ArrayList<Agent> newAgents, Class<? extends Agent> cl) {
		// TODO: better implementation later when ArrayList will be
		// no longer used
		synchronized (agents) {
			agents.asList().addAll(newAgents);
		}
	}
	
	
	/**
	 * Adds all new agents
	 */
//	private void processNewAgents() {
//		agents.addAll(newAgents);
//		newAgents.asList().clear();
//	}
	
	
	
	/**
	 * Removes the agent from the context
	 * @param agent
	 */
	@Override
	protected boolean removeAgent(Agent agent) {
		// TODO: still need to do something, probably
		// Be sure that only function die() may remove agents
		return false;
	}
	
	
	@Override
	public synchronized int getAgentsNumber(final Class<? extends Agent> type) {
		int n = agents.withFilter(new Predicate<Agent>()
				{
					public boolean op(Agent arg0) {					
						return (arg0 == null) ? false : arg0.getClass()==type;
					}			
		}).size();
		
		return n;
	}
	
	
	@Override
	public synchronized int getAgentsNumberOfKind(final Class<? extends Agent> kind) {
		int n = agents.withFilter(new Predicate<Agent>()
				{
					public boolean op(Agent arg0) {					
						return (arg0 == null) ? false : kind.isInstance(arg0);
					}			
		}).size();
		
		return n;
	}

	/**
	 * Processes all agents
	 * @param tick
	 */
	@Override
	public synchronized void processAllAgents(final long tick) {
		// TODO: can anything be done with static agents?
		ParallelArray<Agent> TempAgent = agents.withMapping(
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
	
	}
	
	
	@Override
	public void processAgents(Class<? extends Agent> type, SimulationTime time) {
		// TODO: implement
		throw new RuntimeException("Not implemented");
	}


	/**
	 * Prints statistic in the following form:
	 * for each type of agents the number of milliseconds
	 * required to process all agents of the given time is printed
	 */
	@Override
	public void printStatistics() {
	}
	
	
	// A trick used to avoid explicit cast operation
	Agent[] tmp = new Agent[0];
	@SuppressWarnings("unchecked")
	@Override
	public synchronized <T extends Agent> T[] getAgents(final Class<T> type) {
		return (T[]) agents.withFilter(new Predicate<Agent>()
				{
					public boolean op(Agent arg0) {
						return (arg0 == null) ? false : arg0.getClass()==type;
					}			
		}).all().asList().toArray(tmp);
	}
	
	
	
	@Override
	public synchronized Agent[] getAgents() {
		// FIXME: dead agents are returned as well if they have died
		// during the current step.
		// They are marked as dead only after processing all agents
		// when the removedQueue is processed.
		// This function is called by the render in synchronized mode
		// before agents take their steps so freshly dead agents are not
		// removed yet.
		agents.removeNulls();
		return agents.asList().toArray(tmp);
	}
}
