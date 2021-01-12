package org.spark.runtime.data;

import java.io.Serializable;
import java.util.ArrayList;

import org.sparkabm.math.Vector;

/**
 * Contains information about agents
 */
@SuppressWarnings("serial")
public class DataObject_Inspection extends DataObject {
	/**
	 * Parameters for creating a data inspection collector
	 */
	public static class Parameters implements Serializable {
		public final String spaceName;
		public final Vector position;

		// Constructor
		public Parameters(String spaceName, Vector position) {
			this.spaceName = spaceName;
			this.position = position;
		}
	}
	
	/**
	 * Contains information about one object
	 */
	public static class ObjectInformation implements Serializable {
		// The name of the object
		public final String objectName;
		
		// All variables with values
		public final ArrayList<String> varNames;
		public final ArrayList<String> varValues;
		
		/**
		 * Default constructor
		 */
		public ObjectInformation(String objectName) {
			this.objectName = objectName;
			this.varNames = new ArrayList<String>();
			this.varValues = new ArrayList<String>();
		}
		
		/**
		 * Adds a variable with its value
		 */
		public void addVariable(String varName, String value) {
			if (varName == null)
				return;
			
			if (value == null)
				value = "null";
		
			varNames.add(varName);
			varValues.add(value);
		}
		
		
		@Override
		public String toString() {
			return objectName;
		}
	}
	
	
	// List of all objects
	private final ArrayList<ObjectInformation> objects;  
	
	
	/**
	 * Default constructor
	 */
	public DataObject_Inspection() {
		objects = new ArrayList<ObjectInformation>();
	}
	
	
	/**
	 * Adds an object
	 * @param obj
	 */
	public void addObject(ObjectInformation obj) {
		if (obj == null)
			return;
		
		objects.add(obj);
	}
	
	
	/**
	 * Returns objects
	 * @return
	 */
	public ArrayList<ObjectInformation> getObjects() {
		return objects;
	}
}
