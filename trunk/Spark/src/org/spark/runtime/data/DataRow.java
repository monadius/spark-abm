package org.spark.runtime.data;

import java.io.Serializable;
import java.util.HashMap;

import org.spark.core.SimulationTime;


/**
 * Represents a row containing all collected data objects
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public final class DataRow implements Serializable {
	/* All data */
	HashMap<String, DataObject> data;
	
	
	/**
	 * Creates a data row for the given simulation time value
	 * @param time
	 */
	public DataRow(SimulationTime time) {
		data = new HashMap<String, DataObject>();
		data.put("$time", new DataObject_Time(time));
	}
	
	
	
	/**
	 * Each row has a time value
	 * @return
	 */
	public SimulationTime getTime() {
		DataObject_Time time = (DataObject_Time) data.get("$time");
		return time.getSimulationTime();
	}
	
	
	/**
	 * Returns information about spaces
	 * @return
	 */
	public DataObject_Spaces getSpaces() {
		DataObject obj = data.get("$spaces");
		if (obj == null)
			return null;
		else
			return (DataObject_Spaces) obj;
	}
	
	
	/**
	 * Returns information about the given grid
	 * @param spaceName
	 * @param gridName
	 * @return
	 */
	public DataObject_Grid getGrid(String spaceName, String dataLayerName) {
		String name = "$data-layer:" + spaceName + ":" + dataLayerName;
		
		DataObject obj = data.get(name);
		if (obj == null)
			return null;
		else
			return (DataObject_Grid) obj;
	}
	
	
	/**
	 * Returns information about the given space agents
	 * @param typeName
	 * @return
	 */
	public DataObject_SpaceAgents getSpaceAgents(String typeName) {
		String name = "$agents:" + typeName;
		
		DataObject obj = data.get(name);
		if (obj == null)
			return null;
		else
			return (DataObject_SpaceAgents) obj;
	}
}
