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
	protected final ArrayList<IDataConsumer> consumers;
	

	
	/**
	 * Default protected constructor
	 */
	protected DataReceiver() {
		consumers = new ArrayList<IDataConsumer>();
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
	 * Adds the new data consumer
	 * @param consumer
	 */
	public synchronized void addDataConsumer(IDataConsumer consumer) {
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
