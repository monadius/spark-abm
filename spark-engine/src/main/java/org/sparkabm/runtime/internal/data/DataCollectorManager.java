package org.sparkabm.runtime.internal.data;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sparkabm.runtime.data.DataCollectorDescription;
import org.sparkabm.runtime.data.DataObject_Inspection;

/**
 * Manages data collectors
 * @author Monad
 *
 */
public class DataCollectorManager {
	private static final Logger logger = LogManager.getLogger();
	
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
		Object parameters = dcd.getParameters();
		
		switch (dcd.getType()) {
		// Variable
		case DataCollectorDescription.VARIABLE:
			dc = new DCVariable(dcd.getDataName());
			break;
			
		// Data layer
		case DataCollectorDescription.DATA_LAYER:
			dc = new DCDataLayer(dcd.getDataName());
			break;
		
		// Space agents
		case DataCollectorDescription.SPACE_AGENTS:
			dc = new DCSpaceAgents(dcd.getDataName());
			break;
			
		// Agent data
		case DataCollectorDescription.AGENT_DATA:
			dc = new DCAgentData(dcd.getDataName());
			break;

		// Spaces
		case DataCollectorDescription.SPACES:
			dc = new DCSpaces();
			break;
			
		// Number of agents
		case DataCollectorDescription.NUMBER_OF_AGENTS:
			dc = new DCNumberOfAgents(dcd.getDataName());
			break;
			
		// Inspection data
		case DataCollectorDescription.INSPECTION_DATA:
			if (parameters == null || !(parameters instanceof DataObject_Inspection.Parameters))
				break;
			
			dc = new DCInspectionData(dcd.getDataName(), (DataObject_Inspection.Parameters) parameters);
			break;
		}

		if (dc == null) {
			logger.error("Unresolved data type: " + dcd);
			return;
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
