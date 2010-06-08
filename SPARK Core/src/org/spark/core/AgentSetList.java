package org.spark.core;

import java.util.ArrayList;
import java.util.Iterator;


class AgentSetList<T extends Agent> implements AgentSet<T> {
	@SuppressWarnings("serial")
	private static class DummyAgent extends Agent 
	{
		public int number = 0;
		public DummyAgent() {
			super(0);
			next = prev = this;
		}
	}
	
	DummyAgent tmp;
	
	public AgentSetList() {
		tmp = new DummyAgent();
	}

	
	public void add(T agent) {
		tmp.number++;
		
		agent.prev = tmp;
		agent.next = tmp.next;
		tmp.next.prev = agent;
		tmp.next = agent;		
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
		if (agent.next == null)
		
		tmp.number--;
		agent.prev.next = agent.next;
		agent.next.prev = agent.prev;
	}

	public Iterator<T> iterator() {
		return new MyIterator();
	}

	public void clear() {
		// TODO Auto-generated method stub
		
	}

	
	class MyIterator implements Iterator<T> {
		Agent current;

		public MyIterator() {
			current = tmp;
		}
		
		public boolean hasNext() {
			return current.next != tmp;
		}

		@SuppressWarnings("unchecked")
		public T next() {
			return (T) current.next;
		}

		public void remove() {
			throw new Error("Removing is not allowed");
		}
		
	}
}
