package org.spark.runtime.external.data;

import java.util.ArrayList;

import org.spark.runtime.data.DataRow;

/**
 * Base data receiver class
 * @author Monad
 *
 */
public abstract class DataReceiver {
	/* List of all data consumers */
	protected final ArrayList<DataFilter> consumers;
	

	
	/**
	 * Default protected constructor
	 */
	protected DataReceiver() {
		consumers = new ArrayList<DataFilter>();
	}
	
	
	/**
	 * Invoked whenever new data is received
	 * @param row
	 */
	public synchronized void receive(DataRow row) {
		for (IDataConsumer consumer : consumers) {
			consumer.consume(row);
		}
	}
	
	
	/**
	 * Sets collection intervals for all filters in the given group
	 * @param groupName
	 */
	public synchronized void setCollectionInterval(String groupName, int interval) {
		for (DataFilter filter : consumers) {
			if (filter.getGroupName().equals(groupName))
				filter.setInterval(interval);
		}
	}
	
	
	/**
	 * Adds the new data consumer
	 * @param consumer
	 */
	public synchronized void addDataConsumer(DataFilter consumer) {
		consumers.add(consumer);
	}
	
	
	/**
	 * Removes the given data consumer
	 * @param consumer
	 */
	public synchronized void removeDataConsumer(IDataConsumer consumer) {
		consumers.remove(consumer);
	}
	
	
	/**
	 * Removes all data consumers
	 */
	public synchronized void removeAllConsumers() {
		consumers.clear();
	}
}
