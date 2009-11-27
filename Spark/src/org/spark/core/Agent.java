/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package org.spark.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;


// TODO: die immediately and die via adding to die queue

/**
 * Represents an abstract agent
 */
public abstract class Agent implements Serializable {
	/**
	 * Default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	// Internal private variable which assigns the unique id to each agent
	// TODO: Is it useful?
	private int id = ++idCounter;
	private static int idCounter = 0;
	
	// True when agent is dead. Can be accessed using isDead() method.
	boolean dead = false;

	// TODO: is there a better way? (without introducing new variable inside agent class)
	// "deep" serialization means that the agent during serialization automatically
	// removed from the observer and space and then completely restored during
	// deserealization
	protected boolean deepSerialization = false;
	
	// Used by Observer2 to create a linked list of agents
	// TODO: restore after serialization somehow, need to check the observer type (maybe)
	transient Agent	prev, next;
	
	
	// TODO: there are problems with links and spatial parallelization,
	// so links list is transient
	transient ArrayList<Link> links = null;
	
	/**
	 * Adds a link to the list of links
	 * @param link
	 */
	void addLink(Link link) {
		if (links == null) {
			links = new ArrayList<Link>();
			links.add(link);
			return;
		}
		
		if (links.contains(link))
			return;
		
		links.add(link);
	}
	
	/**
	 * Removes a link from the list of links
	 * @param link
	 */
	void removeLink(Link link) {
		if (links == null)
			return;
		
		links.remove(link);
	}
	
	
	/**
	 * Returns all links connected to the agent 
	 * @return
	 */
	public ArrayList<Link> getLinks() {
		if (links == null)
			return null;
		
		ArrayList<Link> copy = new ArrayList<Link>(links);
		return copy;
	}
	
	
	/**
	 * Returns a link connecting two agents
	 * @param a
	 * @return null if no such a link exists
	 */
	public Link getFirstConnection(Agent a) {
//		if (a == this)
//			return null;
		if (links == null)
			return null;
		
		for (Link link : links) {
			if (link.isConnectedTo(a))
				return link;
		}
		
		return null;
	}

	
	/**
	 * Returns a link of the given type connecting two agents
	 * @param a
	 * @return null if no such a link exists
	 */
	@SuppressWarnings("unchecked")
	public <T extends Link> T getFirstConnection(Agent a, Class<T> linkType) {
//		if (a == this)
//			return null;
		if (links == null)
			return null;
		
		for (Link link : links) {
			if (link.getClass() == linkType)
				if (link.isConnectedTo(a))
					return (T) link;
		}
		
		return null;
	}

	
	/**
	 * Returns all links of the given type
	 * @param <T>
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Link> ArrayList<T> getLinks(Class<T> type) {
		if (links == null)
			return null;
		
		ArrayList<T> list = new ArrayList<T>(links.size());
		
		for (Link link : links) {
			if (link.getClass() == type)
				list.add((T) link);
		}
		
		return list;
	}
	
	
	
	/**
	 * Changes the type of the serialization
	 * Use with caution!
	 * @param deepSerialization
	 */
	public void setDeepSerialization(boolean deepSerialization) {
		this.deepSerialization = deepSerialization;
	}
	
	/**
	 * Checks whether the agent is dead
	 * @return true if the agent is dead and false otherwise
	 */
	public boolean isDead() {
		return dead;
	}
	
	/**
	 * Default protected constructor.
	 * Automatically adds the agent to the context.
	 */
	protected Agent() {
		Observer.getInstance().addAgent(this);
	}
	
	// A trick to use this constructor internally only
	// for initialization without adding to Observer's list of agents
	Agent(int trick) {}
	
	/**
	 * Removes the agent from context
	 */
	public void die() {
		if (dead) return;
//		dead = true;
		
		if (links != null) {
			for (Link link : links) {
				link.removeEnd(this);
			}
			links = null;
		}
		
		Observer.getInstance().removeAgent(this);
	}
	
	/**
	 * @deprecated
	 * This is the main function of each agent. Any derived class
	 * should override this function. All work of agent is done inside
	 * this function
	 * @param tick a number of ticks passed since the model setup
	 */
	public void step(long tick) {
		
	}
	
	/**
	 * This is the main function of each agent. Any derived class
	 * should override this function. All work of agent is done inside
	 * this function
	 * @param time represents the simulation time
	 */	
	public void step(SimulationTime time) {
		
	}
	
	
	/**
	 * Gives some information about agent. Specifically, the class of the agent
	 * and its id
	 */
	public String toString() {
		return this.getClass().getSimpleName() + id;
	}

	// TODO: better implementation required
	/**
	 * Returns id of the agent.
	 */
	public int hashCode() {
		return id;
	}
	
	
	/**
	 * Custom deserialization is needed.
	 */
	private void readObject(ObjectInputStream ois) throws IOException,
			ClassNotFoundException {
//		System.out.println("Reading agent");

		ois.defaultReadObject();


		// restore the agent in the observer
		if (deepSerialization) {
			Observer.getInstance().addAgentInstantly(this);
			// automatic reset
			deepSerialization = false;
		}
	}

	/**
	 * Custom serialization is needed.
	 */
	private void writeObject(ObjectOutputStream oos) throws IOException {
//		System.out.println("Writing agent");

		oos.defaultWriteObject();

		// TODO: instantly
		// remove the agent from the observer instantly
		if (deepSerialization) {
			die();
			// automatic reset
			deepSerialization = false;
		}
	}

}
