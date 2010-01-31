package org.spark.runtime.external;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
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
	

	
	// TODO: getParameter(String name)
	// Problem: id contains variable's name also
	
	/**
	 * Returns an array containing all parameters in the collection
	 */
	public Parameter[] getParameters() {
		Parameter[] pars = new Parameter[parametersList.size()];
		return parametersList.toArray(pars);
	}
	
	
	/**
	 * Returns a parameter by its name
	 * @param name
	 * @return
	 */
	public Parameter getParameter(String name) {
		for (String key : parameters.keySet()) {
			String[] e = key.split(";");
			if (e.length == 2) {
				if (e[1].equals(name))
					return parameters.get(key);
			}
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
		NamedNodeMap attributes = node.getAttributes();
		Node tmp;

		// Read node attributes
		String variable = (tmp = attributes.getNamedItem("variable")) != null ? tmp.getNodeValue() : "???";
		String name = (tmp = attributes.getNamedItem("name")) != null ? tmp.getNodeValue() : null;
		String smin = (tmp = attributes.getNamedItem("min")) != null ? tmp.getNodeValue() : "0";
		String smax = (tmp = attributes.getNamedItem("max")) != null ? tmp.getNodeValue() : "1";
		String step = (tmp = attributes.getNamedItem("step")) != null ? tmp.getNodeValue() : "1";
		String svalues = (tmp = attributes.getNamedItem("values")) != null ? tmp.getNodeValue() : null;
		String widget = (tmp = attributes.getNamedItem("widget")) != null ? tmp.getNodeValue() : "Spinner";
		String defaultValue = (tmp = attributes.getNamedItem("default")) != null ? tmp.getNodeValue() : null;

		// Create a parameter
		Parameter par = new Parameter();
		
		// Add parameter to the table
		par.variable = Coordinator.getInstance().getVariable(variable);
		if (par.variable == null)
			throw new Exception("Variable " + variable + " is not defined");
		
		
		String id = variable + ";" + name;
		if (parameters.containsKey(id))
			throw new Exception("Parameter " + name + " for variable " + 
					variable + "is already defined");
		
		parameters.put(id, par);
		parametersList.add(par);
		
		// Set up the parameter
		par.variable.addChangeListener(par);

		if (name != null)
			par.name = name;
		else
			par.name = par.variable.getName();
		

		// Use specific list of values
		if (svalues != null) {
			String[] values = svalues.split(",");
			par.setValues(values);
			
			// TODO: better solution is required
			smin = "-1e1000";
			smax = "1e1000";
		}
		
		// Set up parameter's widget
		par.widgetName = widget;
		
		// Set up min/max values
		double min = Double.valueOf(smin);
		double max = Double.valueOf(smax);
		
		if (min > max)
			min = max;
		
		par.setMinimum(par.min = min);
		par.setMaximum(par.max = max);
		
		// Set up step
		par.setStepSize(par.step = Double.valueOf(step));
		
		if (par.step <= 0)
			par.setStepSize(par.step = 1);

		par.adjustFormat();
		
		// Assign the default value
		if (defaultValue != null)
			par.setValue(defaultValue);
/*		else {
			if (par.variable.getType() == Double.class)
				par.setValue(min);
			else if (par.variable.getType() == Integer.class)
				par.setValue((int) min);
		}
*/		
		// TODO: set initial value to be between 'min' and 'max'
		
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
