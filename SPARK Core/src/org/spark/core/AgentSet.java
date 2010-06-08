package org.spark.core;

import java.util.ArrayList;

/**
 * An abstract representation of an agent set.
 * An agent set is a set of agents. 
 * @author Monad
 * @param <T>
 */
public interface AgentSet<T extends Agent> extends Iterable<T> {
	public void add(T agent);
	public void remove(T agent);
	
	public void clear();
	
	public void addAll(AgentSet<T> set);
	public void addAll(ArrayList<T> list);
	public void addAll(T[] set);
	
	
}
