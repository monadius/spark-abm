package org.spark.runtime.data;

import org.spark.core.SimulationTime;
import org.spark.core.SparkModel;

/**
 * Defines basic functionality for data collectors.
 * Each data collector is responsible for getting
 * some information about simulated model
 * @author Monad
 */
public abstract class DataCollector {
	/* Interval in ticks for data collection */
	private int collectionInterval;
	
	/* Name of the data collected by this collector */
	protected String dataName; 
	
	
	/**
	 * Creates a data collector with the specific data collection interval
	 * @param collectionInterval
	 */
	protected DataCollector(int collectionInterval) {
		if (collectionInterval < 1)
			collectionInterval = 1;
		
		this.collectionInterval = collectionInterval;
	}
	
	
	/**
	 * Creates a default data collector
	 */
	protected DataCollector() {
		this(1);
	}
	
	
	/**
	 * Sets the data collection interval
	 * @param interval
	 */
	public void setCollectionInterval(int interval) {
		this.collectionInterval = interval;
	}
	
	
	/**
	 * Collects data into the given data row
	 * @param row
	 * @throws Exception
	 */
	public final void collect(SparkModel model, DataRow row, SimulationTime time) throws Exception {
		if (dataName == null)
			throw new Exception("Name is not specified for the data collector");
		
		if (time.getTick() % collectionInterval == 0) {
			// The data is already collected
			if (row.data.containsKey(dataName))
				return;
			
			DataObject obj = collect0(model);
			row.data.put(dataName, obj);
		}
	}
	
	/**
	 * This method is called for collecting data
	 * @throws Exception
	 */
	public abstract DataObject collect0(SparkModel model) throws Exception;
}
