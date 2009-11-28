package org.spark.runtime.external;

import java.util.ArrayList;
import java.util.HashMap;

import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.external.data.DataFilter;
import org.spark.runtime.external.data.DataReceiver;
import org.spark.utils.XmlDocUtils;
import org.w3c.dom.Node;

/**
 * Collection of all proxy variables
 * @author Monad
 *
 */
public class ProxyVariableCollection {
	private final HashMap<String, ProxyVariable> variables;
	
	
	/**
	 * Creates a collection of proxy variables for all variables
	 * inside the given node
	 * @param node
	 */
	public ProxyVariableCollection(Node node) {
		variables = new HashMap<String, ProxyVariable>();
		ArrayList<Node> list = XmlDocUtils.getChildrenByTagName(node, "variable");
		
		for (Node varNode : list) {
			ProxyVariable var = ProxyVariable.loadVariable(varNode);
			variables.put(var.getName(), var);
		}
	}
	
	
	
	/**
	 * Returns a variable by its name
	 * @param name
	 * @return
	 */
	public ProxyVariable getVariable(String name) {
		return variables.get(name);
	}
	
	
	/**
	 * Returns all variables in the collection
	 * @return
	 */
	public ProxyVariable[] getVariables() {
		ProxyVariable[] result = new ProxyVariable[variables.size()];
		
		int i = 0;
		for (ProxyVariable var : variables.values()) {
			result[i++] = var;
		}
		
		return result;
	}
	
	
	/**
	 * Register variables as data consumers
	 * @param receiver
	 */
	public void registerVariables(DataReceiver receiver) {
		for (ProxyVariable var : variables.values()) {
			DataFilter df = new DataFilter(var, "variable");
			df.addData(DataCollectorDescription.VARIABLE, var.getName());
			receiver.addDataConsumer(df);
		}
	}
}
