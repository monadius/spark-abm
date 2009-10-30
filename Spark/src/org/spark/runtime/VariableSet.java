package org.spark.runtime;

import java.util.ArrayList;
import java.util.HashMap;

import org.spark.runtime.internal.ModelVariable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Represents a set of values of selected variables
 * @author Monad
 */
public class VariableSet {
//	private static final Logger logger = Logger.getLogger();
	
	/* Auxiliary inner class */
	private static class VariableValue {
		/* Variable */
		public ModelVariable var;
		/* Variable's value in the string format */
		public String svalue;
		
		public VariableValue(ModelVariable var, String svalue) {
			this.var = var;
			this.svalue = svalue;
		}
		
		public VariableValue(ModelVariable var) {
			this.var = var;
			Object val = var.getValue();
			svalue = String.valueOf(val);
		}
	}
	
	/* Name of the set */
	private String name;
	
	/* List of all variables in the set */
	private final ArrayList<VariableValue> vars = new ArrayList<VariableValue>();
	
	
	/**
	 * Returns set's name
	 * @return
	 */
	public String getName() {
		return name;
	}
	

	/**
	 * Internal constructor
	 */
	VariableSet(String name) {
		this.name = name;
	}
	
	
	/**
	 * Internal method for adding new variable-value pairs
	 * @param var
	 * @param svalue
	 */
	void addVariable(ModelVariable var, String svalue) {
		vars.add(new VariableValue(var, svalue));
	}
	
	
	/**
	 * Adds/removes variables from the set to make
	 * it contain only parameters
	 */
	public void synchronizeWithParameters() {
		Parameter_Old[] pars = ParameterFactory_Old.getParameters();
		
		// Create a temporary table
		HashMap<String, VariableValue> table = new HashMap<String, VariableValue>();
		for (VariableValue vv : vars) {
			table.put(vv.var.getName(), vv);
		}
		
		// Clear list
		vars.clear();
		
		for (int i = 0; i < pars.length; i++) {
			Parameter_Old p = pars[i];
			
			String varName = p.variable.getName();
			
			// If a variable is already in the set,
			// then do nothing
			if (table.containsKey(varName)) {
				vars.add(table.get(varName));
				continue;
			}
			
			// Otherwise, create a new variable-value pair
			// for the parameter
			VariableValue vv = new VariableValue(p.variable);
			vars.add(vv);
		}
	}

	
	/**
	 * Assigns variable values according to the set's values
	 */
	public void loadVariablesFromSet() throws Exception {
		for (VariableValue vv : vars) {
			// TODO: be careful with null values (svalue == "null" or "" or null)
			vv.var.setValue(vv.svalue);
		}
	}
	
	

	/**
	 * Saves current values of variables into the set
	 */
	public void saveVariablesIntoSet() {
		for (VariableValue vv : vars) {
			// TODO: be careful with null values
			String sval = String.valueOf(vv.var.getValue());
			vv.svalue = sval;
		}
	}
	
	
	/**
	 * Saves current variable values into an xml document
	 * @throws Exception
	 */
	// TODO: method for loading data from an xml node also should
	// be inside this class (not inside a factory class)
	public Node createXML(Document doc) {
		Node root = doc.createElement("variable-set");
		
		Node attr = doc.createAttribute("name");
		attr.setNodeValue(name);
		root.getAttributes().setNamedItem(attr);
		
		for (int i = 0; i < vars.size(); i++) {
			VariableValue vv = vars.get(i);
			
			Node child = doc.createElement("variable");
			
			attr = doc.createAttribute("name");
			attr.setNodeValue(vv.var.getName());
			child.getAttributes().setNamedItem(attr);
			
			attr = doc.createAttribute("value");
			attr.setNodeValue(vv.svalue);
			child.getAttributes().setNamedItem(attr);
			
			root.appendChild(child);
		}
		
		return root;
	}
	
	
	/**
	 * Returns names of all variables in the set separated by commas
	 * @return
	 */
	public String getVariableNames() {
		String str = "";

		for (int i = 0; i < vars.size(); i++) {
			VariableValue vv = vars.get(i);
			
			str += vv.var.getName();
			if (i < vars.size() - 1) {
				str += ",";
			}
		}
		
		return str;
	}



	/**
	 * Returns values of all variables in the set separated by commas
	 * @return
	 */
	public String getVariableValues() {
		String str = "";

		for (int i = 0; i < vars.size(); i++) {
			VariableValue vv = vars.get(i);
			
			str += vv.svalue;
			if (i < vars.size() - 1) {
				str += ",";
			}
		}
		
		return str;
	}
}
