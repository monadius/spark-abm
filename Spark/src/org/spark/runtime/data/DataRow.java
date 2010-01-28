package org.spark.runtime.data;

import java.io.Serializable;
import java.util.HashMap;

import static org.spark.runtime.data.DataCollectorDescription.*;

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
	private HashMap<String, DataObject> data;
	
	
	/**
	 * Creates a data row for the given simulation time value
	 * @param time
	 */
	public DataRow(SimulationTime time, int flags) {
		data = new HashMap<String, DataObject>();
		state = new DataObject_State(time, RandomHelper.getSeed(), flags);
	}
	
	
	/**
	 * Tells whether the row contains the given item
	 * @param name
	 * @return
	 */
	public boolean contains(int type, String dataName) {
		String name = DataCollectorDescription.typeToString(type);
		
		if (dataName != null)
			name += dataName;
		
		return data.containsKey(name);
	}
	
	
	public boolean contains(String fullName) {
		return data.containsKey(fullName);
	}
	
	
	/**
	 * Returns the given named object
	 * @param name
	 * @return
	 */
	public DataObject get(int type, String dataName) {
		String name = DataCollectorDescription.typeToString(type);
		
		if (dataName != null)
			name += dataName;

		return data.get(name);
	}
	
	
	public DataObject get(String fullName) {
		return data.get(fullName);
	}
	
	
	/**
	 * Puts the given data object into the row
	 * @param name
	 * @param object
	 */
	public void addDataObject(String name, DataObject object) {
		data.put(name, object);
	}
	
	
	/**
	 * Returns all names of items in the row
	 * @return
	 */
	public String[] getNames() {
		String[] names = new String[data.size()];
		names = data.keySet().toArray(names);
		
		return names;
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
		DataObject obj = data.get(STR_SPACES);
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
		String name = STR_DATA_LAYER + dataLayerName;
		
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
		String name = STR_SPACE_AGENTS + typeName;
		
		DataObject obj = data.get(name);
		if (obj == null)
			return null;
		else
			return (DataObject_SpaceAgents) obj;
	}
	
	
	/**
	 * Returns a double value for the given variable
	 * @param varName
	 * @return
	 */
	public Double getVarDoubleValue(String varName) {
		String name = STR_VARIABLE + varName;
		
		DataObject obj = data.get(name);
		if (obj == null)
			return null;
		else if (obj instanceof DataObject_Double)
			return ((DataObject_Double) obj).getValue();
		else
			return null;
	}
	
	
	/**
	 * Returns an integer value for the given variable
	 * @param varName
	 * @return
	 */
	public Integer getVarIntegerValue(String varName) {
		String name = STR_VARIABLE + varName;
		
		DataObject obj = data.get(name);
		if (obj == null)
			return null;
		else if (obj instanceof DataObject_Integer)
			return ((DataObject_Integer) obj).getValue();
		else
			return null;
	}


	/**
	 * Returns a boolean value for the given variable
	 * @param varName
	 * @return
	 */
	public Boolean getVarBooleanValue(String varName) {
		String name = STR_VARIABLE + varName;
		
		DataObject obj = data.get(name);
		if (obj == null)
			return null;
		else if (obj instanceof DataObject_Bool)
			return ((DataObject_Bool) obj).getValue();
		else
			return null;
	}
}
