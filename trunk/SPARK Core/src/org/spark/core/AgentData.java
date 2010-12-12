package org.spark.core;

import java.util.HashMap;

/**
 * Class which contains data collected from an agent
 * @author Alexey
 *
 */
public class AgentData {
	// String values
	private final HashMap<String, String> stringVals;
	
	/**
	 * Default constructor
	 */
	public AgentData() {
		stringVals = new HashMap<String, String>();
	}
	
	
	/**
	 * Adds a string value
	 * @param name
	 * @param val
	 */
	public void add(String name, String val) {
		stringVals.put(name, val);
	}
	
	/**
	 * String value
	 * @return
	 */
	public String getStringVal(String name) {
		return stringVals.get(name);
	}
	
	
	/**
	 * String values
	 * @return
	 */
	public HashMap<String,String> getStringVals() {
		return stringVals; 
	}
}
