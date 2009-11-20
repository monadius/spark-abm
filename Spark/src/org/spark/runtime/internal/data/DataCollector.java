package org.spark.runtime.internal.data;

import org.spark.core.SimulationTime;
import org.spark.core.SparkModel;
import org.spark.runtime.data.DataObject;
import org.spark.runtime.data.DataRow;

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
	
	/* Indicates whether the collector is active or not */
	protected boolean active;
	
	
	/**
	 * Creates a default data collector
	 */
	protected DataCollector() {
		collectionInterval = 1;
		dataName = null;
		active = true;
	}
	
	
	/**
	 * Returns true if the collector is active
	 * @return
	 */
	public boolean isActive() {
		return active;
	}
	
	
	/**
	 * Makes the collector inactive
	 */
	public void deactivate() {
		active = false;
	}
	
	
	/**
	 * Sets the data collection interval
	 * @param interval
	 */
	public void setCollectionInterval(int interval) {
		if (interval < 0)
			interval = 0;
		
		this.collectionInterval = interval;
	}
	
	
	/**
	 * Collects data into the given data row
	 * @param row
	 * @throws Exception
	 */
	public final void collect(SparkModel model, DataRow row, SimulationTime time, boolean specialCollection) throws Exception {
		if (dataName == null)
			throw new Exception("Name is not specified for the data collector");

		// If the interval == 0, then do only special collections
		if (collectionInterval == 0 && !specialCollection)
			return;
		
		if (specialCollection || time.getTick() % collectionInterval == 0) {
			// The data is already collected
			if (row.contains(dataName))
				return;
			
			DataObject obj = collect0(model);
			row.addDataObject(dataName, obj);
		}
	}
	
	/**
	 * This method is called for collecting data
	 * @throws Exception
	 */
	public abstract DataObject collect0(SparkModel model) throws Exception;
	
	
	/**
	 * Resets all cached values inside the data collector
	 */
	public abstract void reset();
}
