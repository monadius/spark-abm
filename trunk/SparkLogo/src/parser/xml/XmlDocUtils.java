package parser.xml;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Utilities for working with xml documents
 * @author Monad
 *
 */
public class XmlDocUtils {
	/**
	 * Returns a list of children with the specified name
	 * @param name
	 * @return
	 */
	public static ArrayList<Node> getChildrenByTagName(Node node, String name) {
		ArrayList<Node> list = new ArrayList<Node>();
		if (node == null)
			return list;

		for (Node child = node.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			if (child.getNodeName().equals(name))
				list.add(child);
		}

		return list;
	}
	
	
	/**
	 * Returns the first child node with the given name
	 * @param node
	 * @param name
	 * @return
	 */
	public static Node getChildByTagName(Node node, String name) {
		if (node == null)
			return null;
		
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeName().equals(name))
				return child;
		}
		
		return null;
	}
	
	
	/**
	 * Removes all children nodes with the specific name of the given node
	 * @param node
	 */
	public static void removeChildren(Node node, String name) {
		if (node == null)
			return;
		
		for (Node item : getChildrenByTagName(node, name)) {
			node.removeChild(item);
		}
	}
	
	
	/**
	 * Adds the attribute to the given node
	 * @param doc
	 * @param node
	 * @param attrName
	 * @param attrValue
	 */
	public static void addAttr(Document doc, Node node, String attrName, Object attrValue) {
		Node attr = doc.createAttribute(attrName);
		attr.setNodeValue(attrValue.toString());
		node.getAttributes().setNamedItem(attr);
	}

	
	
	/**
	 * Gets a string value of the given attribute
	 * @param node
	 * @param attrName
	 * @param defaultValue
	 * @return
	 */
	public static String getStringValue(Node node, String attrName, String defaultValue) {
		Node tmp;
		if (node == null)
			return defaultValue;
		
		String value = (tmp = node.getAttributes().getNamedItem(attrName)) != null ? 
				tmp.getNodeValue()
				: null;
				
		if (value == null)
			return defaultValue;
		
		return value;
	}

	
	/**
	 * Gets a boolean value of the given attribute
	 * @param node
	 * @param attrName
	 * @param defaultValue
	 * @return
	 */
	public static boolean getBooleanValue(Node node, String attrName, boolean defaultValue) {
		String value = getStringValue(node, attrName, null);
				
		if (value == null)
			return defaultValue;
		
		return Boolean.valueOf(value);
	}

	
	/**
	 * Gets an integer value of the given attribute
	 * @param node
	 * @param attrName
	 * @param defaultValue
	 * @return
	 */
	public static int getIntegerValue(Node node, String attrName, int defaultValue) {
		String value = getStringValue(node, attrName, null);
				
		if (value == null)
			return defaultValue;
		
		try {
			return Integer.valueOf(value);
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}


	/**
	 * Gets a float value of the given attribute
	 * @param node
	 * @param attrName
	 * @param defaultValue
	 * @return
	 */
	public static float getFloatValue(Node node, String attrName, float defaultValue) {
		String value = getStringValue(node, attrName, null);
				
		if (value == null)
			return defaultValue;
		
		try {
			return Float.valueOf(value);
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	

	/**
	 * Gets a double value of the given attribute
	 * @param node
	 * @param attrName
	 * @param defaultValue
	 * @return
	 */
	public static double getDoubleValue(Node node, String attrName, double defaultValue) {
		String value = getStringValue(node, attrName, null);
				
		if (value == null)
			return defaultValue;
		
		try {
			return Double.valueOf(value);
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
}

