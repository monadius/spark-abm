package org.spark.runtime.external;

import java.util.HashMap;

import org.spark.utils.XmlDocUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.spinn3r.log5j.Logger;

/**
 * Class for managing and creating sets of variables
 * @author Monad
 *
 */
public class VariableSetFactory {
	private static final Logger logger = Logger.getLogger();
	
	/* Table containing all variable sets */
	private static HashMap<String, VariableSet> variableSets = 
		new HashMap<String, VariableSet>();
	
	
	/**
	 * Returns names of all variable sets
	 * @return
	 */
	public static String[] getNames() {
		String[] names = new String[variableSets.size()];
		
		int n = 0;
		for (VariableSet set : variableSets.values()) {
			names[n++] = set.getName();
		}
		
		return names;
	}
	
	/**
	 * Returns a variable set with the given name or creates
	 * a new variable set if it does not exist
	 * @param name
	 * @return
	 */
	public static VariableSet getVariableSet(String name) {
		VariableSet set = variableSets.get(name);
		
		if (set != null)
			return set;

		set = new VariableSet(name);
		set.synchronizeWithParameters(Coordinator.getInstance().getParameters());
		
		variableSets.put(name, set);
		
		return set;
	}
	
	
	/**
	 * Loads all variable sets from a given xml-node
	 * @param parent
	 */
	public static void loadVariableSets(Node parent) {
		clear();
		NodeList list = parent.getChildNodes();
		
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			
			try {
				if (node.getNodeName().equals("variable-set"))
					createVariableSet(node);
			}
			catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage());
			}
		}		
	}
	
	
	/**
	 * Creates an empty variable set
	 * @return
	 */
	public static VariableSet createVariableSet(String name) {
		VariableSet set = new VariableSet(name);
		return set;
	}
	
	
	/**
	 * Clears the table of variable sets
	 */
	public static void clear() {
		variableSets.clear();
	}
	
	
	/**
	 * Creates a new variable set based on a given xml-node.
	 * @param node could be null in which case a completely new
	 * variable set based on the parameters is created.
	 * @return
	 */
	public static VariableSet createVariableSet(Node node) throws Exception {
		if (node == null) {
			return null;
		}

		NamedNodeMap attributes = node.getAttributes();
		Node tmp;

		String name = (tmp = attributes.getNamedItem("name")) != null ? tmp.getNodeValue() : "???";
		
		if (variableSets.containsKey(name)) {
			throw new Exception("Variable set " + name + " is already defined");
		}

		VariableSet set = new VariableSet(name);
		variableSets.put(name, set);
		
		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node2 = nodes.item(i);

			if (node2.getNodeName().equals("variable")) {
				attributes = node2.getAttributes();

				name = (tmp = attributes.getNamedItem("name")) != null ? tmp.getNodeValue() : "???";
				String svalue = (tmp = attributes.getNamedItem("value")) != null ? tmp.getNodeValue() : "0";

				ProxyVariable var = Coordinator.getInstance().getVariable(name);
				
				// TODO: do we need to throw an exception?
				// Or just ignore it
				if (var == null) {
					logger.error("Variable " + name + " is not found");
					continue;
				}

				set.addVariable(var, svalue);
			}
		}
		
		// TODO: we need this synchronization to avoid
		// errors after removing/adding parameters
		set.synchronizeWithParameters(Coordinator.getInstance().getParameters());
		
		return set;
	}
	
	
	/**
	 * Saves the current variable sets in an xml-document
	 * @param doc
	 * @param root
	 */
	public static void saveXML(Document doc, Node root) {
		XmlDocUtils.removeChildren(root, "variable-set");
		
		for (VariableSet set : variableSets.values()) {
			Node node = set.createXML(doc);
			root.appendChild(node);
		}
	}

	
}

