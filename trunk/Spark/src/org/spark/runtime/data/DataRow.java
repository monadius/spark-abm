package org.spark.runtime.data;

import java.io.Serializable;
import java.util.HashMap;

import org.spark.core.SimulationTime;
import org.spark.utils.RandomHelper;


/**
 * Represents a row containing all collected data objects
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public final class DataRow implements Serializable {
	/* Simulation's state is defined for each data row */
	private DataObject_State state;
	
	/* All data */
	HashMap<String, DataObject> data;
	
	
	/**
	 * Creates a data row for the given simulation time value
	 * @param time
	 */
	public DataRow(SimulationTime time, boolean paused) {
		data = new HashMap<String, DataObject>();
		state = new DataObject_State(time, RandomHelper.getSeed(), paused);
	}
	
	
	
	/**
	 * Each row has a time value
	 * @return
	 */
	public SimulationTime getTime() {
		return state.getSimulationTime();
	}
	
	
	/**
	 * Returns simulation's state
	 * @return
	 */
	public DataObject_State getState() {
		return state;
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
	public DataObject_Grid getGrid(String dataLayerName) {
//		String name = "$data-layer:" + spaceName + ":" + dataLayerName;
		String name = "$data-layer:" + dataLayerName;
		
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
