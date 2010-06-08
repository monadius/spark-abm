package org.spark.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

class AgentSetArrayList<T extends Agent> implements AgentSet<T> {
	/* All agents */
	private Object[] agents;

	int capacity;
	int size;

	public AgentSetArrayList() {
		capacity = 10;
		size = 0;

		agents = new Object[10];
	}

	public void ensureCapacity(int minCapacity) {
		int oldCapacity = agents.length;
		if (minCapacity > oldCapacity) {
			int newCapacity = (oldCapacity * 3) / 2 + 1;
			if (newCapacity < minCapacity)
				newCapacity = minCapacity;
			agents = Arrays.copyOf(agents, newCapacity);
		}
	}

	public void addAll(AgentSet<T> set) {
		// TODO Auto-generated method stub

	}

	public void addAll(ArrayList<T> list) {
		// TODO Auto-generated method stub

	}

	public void addAll(T[] set) {
		// TODO Auto-generated method stub

	}

	public void remove(T agent) {
		// TODO Auto-generated method stub

	}

	public Iterator<T> iterator() {
		return null;
	}

	public void add(T agent) {

	}

	public void clear() {
		// TODO Auto-generated method stub
		
	}

}
