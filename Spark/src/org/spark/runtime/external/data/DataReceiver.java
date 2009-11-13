package org.spark.runtime.external.data;

import java.util.ArrayList;

import org.spark.runtime.data.DataObject;
import org.spark.runtime.data.DataRow;

import com.spinn3r.log5j.Logger;

/**
 * Base data receiver class
 * @author Monad
 *
 */
public abstract class DataReceiver {
	private static final Logger logger = Logger.getLogger();
	
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
		int n = consumers.size();
		
		for (int i = 0; i < n; i++) {
			try {
				consumers.get(i).consume(row);
			}
			catch (Exception e) {
				logger.error(e);
			}
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
	
	
	/**
	 * Returns a copy of the most recently received data object 
	 * @param type
	 * @param name
	 * @return
	 */
	public synchronized DataObject getMostRecentData(int type, String name) {
		long tick = -1;
		DataObject result = null;
		
		for (DataFilter filter : consumers) {
			DataRow copy = filter.getLocalCopy();
			if (copy == null)
				continue;

			DataObject o = copy.get(type, name);
			if (o == null)
				continue;
			
			if (copy.getState().getTick() > tick) {
				result = o;
				tick = copy.getState().getTick();
			}
		}
		
		return result;
	}
}
