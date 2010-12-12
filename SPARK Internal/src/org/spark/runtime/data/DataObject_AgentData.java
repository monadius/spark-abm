package org.spark.runtime.data;

import java.util.HashMap;

import org.spark.core.Agent;
import org.spark.core.AgentData;

/**
 * Special data of agents
 * @author Alexey
 *
 */
public class DataObject_AgentData extends DataObject {
	private static final long serialVersionUID = 1L;

	// All string values for each agent
	private HashMap<String, String>[] stringVals;
	
	// The number of entries
	private int counter;
	private int n;
	
	
	/**
	 * Creates a data object for the given number of space agents
	 * @param agentsNumber
	 */
	@SuppressWarnings("unchecked")
	public DataObject_AgentData(int agentsNumber) {
		if (agentsNumber < 0)
			agentsNumber = 0;
		
		if (agentsNumber > 0) {
			stringVals = new HashMap[agentsNumber];
		}
		
		n = agentsNumber;
		counter = 0;
	}
	
	
	/**
	 * Empty protected constructor
	 */
	protected DataObject_AgentData() {
	}
	
	
	/**
	 * Adds agent's parameters into the data object
	 * @param position
	 * @param r
	 * @param color
	 * @param shape
	 */
	public void addAgent(Agent agent) {
		// Cannot hold any more agents
		if (counter >= n)
			return;
		
		AgentData data = agent.getData();
		if (data == null) {
			// TODO: is it possible that the data is null for some agents and is not null for others?
//			counter++;
			return;
		}
		
		stringVals[counter] = data.getStringVals();		
		
		
		counter++;
	}
	
	
	/**
	 * Returns the total number of agents in the data object
	 * @return
	 */
	public int getTotalNumber() {
		return counter;
	}
	

	/**
	 * Returns the values of the variable called name of the agent
	 * defined by the given index
	 * @param index
	 * @param name
	 * @return
	 */
	public String getStringVal(int index, String name) {
		HashMap<String,String> vals = stringVals[index];
		if (vals == null)
			return null;
		
		return vals.get(name);
	}
}
