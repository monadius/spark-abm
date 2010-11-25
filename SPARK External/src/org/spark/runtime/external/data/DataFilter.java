package org.spark.runtime.external.data;

import java.util.ArrayList;

import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.data.DataRow;
import org.spark.runtime.external.Coordinator;

/**
 * Special data consumer which filters input data
 * @author Monad
 *
 */
public class DataFilter implements IDataConsumer {
	/* Target data consumer for which data is filtered */
	private final IDataConsumer target;
	
	/* Most recent local copy of a received data */
	private DataRow localCopy;
	
	/* Each data filter belongs to some group */
	private final String group;
	
	/* A set of descriptions of acceptable input data */
	private final ArrayList<DataCollectorDescription> inputDataList;
	
	/* Collection interval is the same for all input data values for the filter */
	private int collectionInterval;
	
	// TODO: for some data it is required to collect data only for the
	// given time intervals and also for the end of a simulation.
	// Make a flag for this type of collection or invent an universal
	// way for defining different collection conditions.
	// TODO: "on change" data collection for variables associated with parameters
	
	/* If true, then collectionInterval is used strictly */
	private boolean synchronizedCollection;
	
	
	/**
	 * Creates a default data filter which consumes any new data
	 * @param target
	 */
	public DataFilter(IDataConsumer target, String groupName) {
		this.target = target;
		this.group = groupName;
		this.inputDataList = new ArrayList<DataCollectorDescription>();
		this.collectionInterval = 1;
		this.synchronizedCollection = false;
	}
	
	
	/**
	 * Returns the group name of this data filter
	 * @return
	 */
	public String getGroupName() {
		return group;
	}
	
	
	/**
	 * Returns the data collection interval
	 * @return
	 */
	public int getCollectionInterval() {
		return collectionInterval;
	}
	
	
	/**
	 * Controls synchronization options
	 * @param flag
	 */
	public void setSynchronizedFlag(boolean flag) {
		this.synchronizedCollection = flag;
	}
	
	
	/**
	 * Sets the collection interval for all data collectors
	 * @param interval
	 * @return
	 */
	public synchronized void setInterval(int interval) {
		if (collectionInterval < 0)
			collectionInterval = 0;
		
		if (collectionInterval == interval)
			return;
		
		collectionInterval = interval;
		
		DataCollectorDescription[] dcds = new DataCollectorDescription[inputDataList.size()];
		dcds = inputDataList.toArray(dcds);
			
		Coordinator c = Coordinator.getInstance();
			
		// Remove old data collectors
		for (DataCollectorDescription dcd : inputDataList) {
			c.removeDataCollector(dcd);
		}

		inputDataList.clear();
			
		// Add new data collectors (with modified collection interval)
		for (int i = 0; i < dcds.length; i++) {
			int type = dcds[i].getType();
			String dataName = dcds[i].getDataName();
			DataCollectorDescription dcd = new DataCollectorDescription(type, dataName, collectionInterval);
				
			inputDataList.add(dcd);
			c.addDataCollector(dcd);
		}
	}
	
	
	/**
	 * Copies all parameters telling which data to collect from
	 * a given filter to this filter
	 * @param src
	 */
	public synchronized void copyDataParameters(DataFilter src) {
		if (collectionInterval < 0)
			collectionInterval = 0;

		// TODO: think about potential deadlocks
//		synchronized (src) {
			DataCollectorDescription[] dcds = new DataCollectorDescription[src.inputDataList.size()];
			dcds = src.inputDataList.toArray(dcds);
//		}
			
		Coordinator c = Coordinator.getInstance();
		
		// Remove old data collectors
		for (DataCollectorDescription dcd : inputDataList) {
			c.removeDataCollector(dcd);
		}

		inputDataList.clear();
			
		// Add new data collectors (from src filter)
		for (int i = 0; i < dcds.length; i++) {
			int type = dcds[i].getType();
			String dataName = dcds[i].getDataName();
			DataCollectorDescription dcd = new DataCollectorDescription(type, dataName, collectionInterval);
				
			inputDataList.add(dcd);
			c.addDataCollector(dcd);
		}		
	}
	
	
	/**
	 * Adds data for filtering
	 * @param dcd
	 */
	public synchronized void addData(int type, String dataName) {
		DataCollectorDescription dcd = new DataCollectorDescription(type, dataName, collectionInterval);
		
		if (inputDataList.contains(dcd))
			return;
		
		inputDataList.add(dcd);
		Coordinator.getInstance().addDataCollector(dcd);
	}
	
	
	/**
	 * Removes data
	 * @param type
	 * @param dataName
	 */
	public synchronized void removeData(int type, String dataName) {
		DataCollectorDescription dcd = new DataCollectorDescription(type, dataName, collectionInterval);
		
		if (inputDataList.contains(dcd)) {
			inputDataList.remove(dcd);
			Coordinator.getInstance().removeDataCollector(dcd);
		}
	}
	
	
	/**
	 * Removes all data
	 */
	public synchronized void removeAllData() {
		for (DataCollectorDescription dcd : inputDataList) {
			Coordinator.getInstance().removeDataCollector(dcd);
		}
		
		inputDataList.clear();
	}
	
	
	/**
	 * Returns a local copy of the most recently received data 
	 * @return
	 */
	public DataRow getLocalCopy() {
		return localCopy;
	}
	
	
	
	/**
	 * Main method
	 */
	public void consume(DataRow row) {
		// Test tick value
		if (synchronizedCollection && collectionInterval > 0) {
			long tick = row.getTime().getTick();
			if (tick % collectionInterval != 0)
				return;
		}
		
		synchronized (this) {
			// Test available data
			for (DataCollectorDescription dcd : inputDataList) {
				if (!row.contains(dcd.getType(), dcd.getDataName()))
					return;
			}
		}
		
		localCopy = row;
		target.consume(row);
	}
}
