package org.spark.runtime.internal.data;

import java.util.ArrayList;
import java.util.HashMap;

import org.spark.runtime.data.DataCollectorDescription;


/**
 * Manages data collectors
 * @author Monad
 *
 */
public class DataCollectorManager {
	/**
	 * Adds a reference counter for each data collector
	 * @author Monad
	 */
	private static class DataCollectorInfo {
		public DataCollector dataCollector;
		public int refCounter;
		
		public DataCollectorInfo(DataCollector dc) {
			this.dataCollector = dc;
			this.refCounter = 1;
		}
	}
	
	/* Table of all data collectors */
	private final HashMap<DataCollectorDescription, DataCollectorInfo> dataCollectors;
	
	/* List of active data collectors */
	private final ArrayList<DataCollector> activeCollectors;
	
	
	/**
	 * Default constructor
	 */
	public DataCollectorManager() {
		dataCollectors = new HashMap<DataCollectorDescription, DataCollectorInfo>();
		activeCollectors = new ArrayList<DataCollector>();
	}
	
	
	
	/**
	 * Adds a data collector defined by the provided description 
	 * @param dcd
	 */
	public void addCollector(DataCollectorDescription dcd) {
		if (dataCollectors.containsKey(dcd)) {
			// Increment the reference counter
			DataCollectorInfo info = dataCollectors.get(dcd);
			info.refCounter++;
			
			return;
		}
		
		// Create a new data collector
		DataCollector dc = null;
		
		switch (dcd.getType()) {
		case DataCollectorDescription.VARIABLE:
			dc = new DCVariable(dcd.getDataName());
			break;
			
		case DataCollectorDescription.DATA_LAYER:
			dc = new DCDataLayer(dcd.getDataName());
			break;
			
		case DataCollectorDescription.SPACE_AGENTS:
			dc = new DCSpaceAgents(dcd.getDataName());
			break;
			
		case DataCollectorDescription.SPACES:
			dc = new DCSpaces();
			break;
		}

		dc.setCollectionInterval(dcd.getInterval());
		
		// Add the created data collector to the table and to the list
		dataCollectors.put(dcd, new DataCollectorInfo(dc));
		activeCollectors.add(dc);
	}
	
	
	/**
	 * Removes the given data collector
	 * @param dcd
	 */
	public void removeCollector(DataCollectorDescription dcd) {
		if (dataCollectors.containsKey(dcd)) {
			DataCollectorInfo info = dataCollectors.get(dcd);
			info.refCounter--;
			
			if (info.refCounter <= 0) {
				activeCollectors.remove(info.dataCollector);
				dataCollectors.remove(dcd);
			}
		}
	}
	
	
	/**
	 * Returns all active data collectors
	 * @return
	 */
	public ArrayList<DataCollector> getActiveCollectors() {
		return activeCollectors;
	}
}
