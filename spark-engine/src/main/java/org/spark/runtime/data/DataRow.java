package org.spark.runtime.data;

import java.io.Serializable;
import java.util.HashMap;

import static org.spark.runtime.data.DataCollectorDescription.*;

import org.sparkabm.math.RandomHelper;
import org.sparkabm.math.SimulationTime;


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
	 */
	public DataRow(SimulationTime time, int flags, long startTime) {
		data = new HashMap<String, DataObject>();
		state = new DataObject_State(time, RandomHelper.getSeed(), flags, startTime);
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
	 * Returns special data of the given agent type 
	 * @param typeName
	 * @return
	 */
	public DataObject_AgentData getAgentData(String typeName) {
		String name = STR_AGENT_DATA + typeName;
		
		DataObject obj = data.get(name);
		if (obj == null)
			return null;
		else
			return (DataObject_AgentData) obj;
	}
	
	
	/**
	 * Returns the inspection data with the given name
	 * @param name
	 * @return
	 */
	public DataObject_Inspection getInspectionData(String name) {
		String dataName = STR_INSPECTION_DATA + name;
		
		DataObject obj = data.get(dataName);
		if (obj == null)
			return null;
		else
			return (DataObject_Inspection) obj;
	}
	
	
	/**
	 * Returns the number of the given agents
	 * @param typeName
	 * @return
	 */
	public Integer getNumberOfAgents(String typeName) {
		String name = STR_NUMBER_OF_AGENTS + typeName;
		DataObject obj = data.get(name);
		if (obj == null)
			return null;
		else
			return ((DataObject_Integer) obj).getValue();
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
