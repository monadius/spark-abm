package org.spark.runtime.external;

import java.util.ArrayList;

import org.spark.utils.XmlDocUtils;
import org.w3c.dom.Node;

/**
 * Collection of all external methods available in the user interface
 * @author Alexey
 *
 */
public class MethodCollection {
	/**
	 * Names of all available methods
	 */
	private final ArrayList<String> names;
	
	
	/**
	 * Default constructor
	 */
	public MethodCollection() {
		names = new ArrayList<String>();
	}


	/**
	 * Returns a list of names of all available methods
	 * @return
	 */
	public ArrayList<String> getNames() {
		return names;
	}
	
	
	/**
	 * Loads methods from the given xml node
	 * @param node
	 */
	public void loadMethods(Node node) {
		ArrayList<Node> methods = XmlDocUtils.getChildrenByTagName(node, "method");
		
		for (Node method : methods) {
			String name = XmlDocUtils.getValue(method, "name", null);

			if (name != null)
				names.add(name);
		}
	}
}
