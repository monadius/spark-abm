package org.spark.runtime.external;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.spark.utils.XmlDocUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.spinn3r.log5j.Logger;

/**
 * Class for creating and managing model's parameters
 * @author Monad
 *
 */
// TODO: make all functionality non-static
public class ParameterCollection {
	private static final Logger logger = Logger.getLogger();
	
	/* All model parameters */
	protected final HashMap<String, Parameter> parameters = 
		new HashMap<String, Parameter>();
	
	/* A list of all parameters for preserving their order */
	protected final ArrayList<Parameter> parametersList =
		new ArrayList<Parameter>();
	

	/**
	 * Returns an array containing all parameters in the collection
	 */
	public Parameter[] getParameters() {
		Parameter[] pars = new Parameter[parametersList.size()];
		return parametersList.toArray(pars);
	}
	
	
	/**
	 * Returns the number of parameters in the collection
	 */
	public int size() {
		return parametersList.size();
	}
	
	
	/**
	 * Returns a parameter by its name
	 * @param name
	 * @return
	 */
	public Parameter getParameter(String name) {
		for (String key : parameters.keySet()) {
			if (key.equals(name))
				return parameters.get(key);
		}
		
		return null;
	}
	
	
	/**
	 * Loads all parameters from the given parent xml-node
	 * @param parent
	 * @throws Exception
	 */
	public void loadParameters(Node parent) {
		clear();
		NodeList list = parent.getChildNodes();
		
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			
			try {
				if (node.getNodeName().equals("parameter"))
					createParameter(node);
			}
			catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage());
			}
		}
	}
	
	
	/**
	 * Clears the table of loaded parameters
	 */
	public void clear() {
		parameters.clear();
		parametersList.clear();
	}
	
	
	/**
	 * Creates a new parameter from the specific xml-node
	 * @param node
	 * @return
	 */
	public Parameter createParameter(Node node) throws Exception {
		// Read node attributes
		String variableRef = XmlDocUtils.getValue(node, "variable", "???");
		String name = XmlDocUtils.getValue(node, "name", null);
		double min = XmlDocUtils.getDoubleValue(node, "min", 0.0);
		double max = XmlDocUtils.getDoubleValue(node, "max", 0.0);
		double step = XmlDocUtils.getDoubleValue(node, "step", 1.0);
		String svalues = XmlDocUtils.getValue(node, "values", null);
		String defaultValue = XmlDocUtils.getValue(node, "default", null); 

		if (min > max)
			min = max;
		
		if (step <= 0)
			step = 1.0;
		
		// Get the corresponding variable
		ProxyVariable variable = Coordinator.getInstance().getVariable(variableRef);
		if (variable == null)
			throw new Exception("Variable " + variable + " is not defined for the parameter " + name);

		if (name == null) {
			name = variable.getName();
		}
		
		if (parameters.containsKey(name))
			throw new Exception("Parameter " + name + " is already defined");

		// Create a parameter
		Parameter par = new Parameter();
		par.init(name, variable, min, max, step);
		
		// Adds the parameter to the table and to the list
		parameters.put(name, par);
		parametersList.add(par);

		// Use a list of values
		if (svalues != null) {
			String[] values = svalues.split(",");
			par.setValues(values);
		}

		// Assign the default value
		if (defaultValue != null)
			par.setValue(defaultValue);
		
		return par;
	}
	
	
	/**
	 * Saves the parameters into an xml-document
	 * @param doc
	 * @param parent
	 */
	public static void saveXML(Document doc, Node parent) {
		// TODO: implement
	}
	
	
	/**
	 * Saves values of parameters into the given output stream
	 * @param out
	 */
	public synchronized void saveParameters(PrintStream out) {
		out.println("\"Parameters\"");
		
		int n = parametersList.size();
		for (Parameter p : parametersList) {
			out.print(p.getName());
			n -= 1;
			if (n > 0)
				out.print(',');
		}
		
		out.println();
		n = parametersList.size();
		for (Parameter p : parametersList) {
			out.print(p.getValue());
			n -= 1;
			if (n > 0)
				out.print(',');
		}
		
		out.println();
	}

}
