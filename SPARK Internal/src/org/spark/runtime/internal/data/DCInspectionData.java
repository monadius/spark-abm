package org.spark.runtime.internal.data;

import java.util.ArrayList;

import org.spark.core.Agent;
import org.spark.core.SparkModel;
import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.data.DataObject;
import org.spark.runtime.data.DataObject_Inspection;

/**
 * Collects data about specific type of space agents
 * 
 * @author Monad
 * 
 */
public class DCInspectionData extends DataCollector {
	// The name of this inspector
	protected String name;
	// The initial inspection position
	protected DataObject_Inspection.Parameters parameters;
	
	// The list of inspected agents
	protected ArrayList<? extends Agent> agents;

	/**
	 * Creates the data collector for the given type of space agents
	 * 
	 * @param typeName
	 */
	DCInspectionData(String name, DataObject_Inspection.Parameters pars) {
		this.name = name;
		this.parameters = pars;

		this.dataName = DataCollectorDescription
				.typeToString(DataCollectorDescription.INSPECTION_DATA)
				+ name;
	}

//	@SuppressWarnings("unchecked")
	@Override
	public DataObject collect0(SparkModel model) throws Exception {
		DataObject_Inspection data = new DataObject_Inspection();
		
		DataObject_Inspection.ObjectInformation info = new DataObject_Inspection.ObjectInformation("Agent1");
		info.addVariable("var1", "1424");
		info.addVariable("x", "name");
		
		data.addObject(info);
		data.addObject(info);
		
		return data;
	}

	@Override
	public void reset() {
		parameters = null;
		agents = null;
	}
}
