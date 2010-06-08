package org.spark.utils;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Utilities for working with xml documents
 * @author Monad
 *
 */
public class XmlDocUtils {
	/**
	 * Loads an xml file
	 * @param fname
	 * @return
	 */
	public static Document loadXmlFile(String fname) {
		try {
			Document doc = DocumentBuilderFactory.newInstance()
								.newDocumentBuilder().parse(fname);
		
			return doc;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
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
	 * Removes the given attribute from the node
	 * @param doc
	 * @param node
	 * @param attrName
	 */
	public static void removeAttr(Node node, String attrName) {
		if (node.getAttributes().getNamedItem(attrName) != null)
			node.getAttributes().removeNamedItem(attrName);
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
	public static String getValue(Node node, String attrName, String defaultValue) {
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
		String value = getValue(node, attrName, null);
				
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
		String value = getValue(node, attrName, null);
				
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
		String value = getValue(node, attrName, null);
				
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
		String value = getValue(node, attrName, null);
				
		if (value == null)
			return defaultValue;
		
		try {
			return Double.valueOf(value);
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
	public static Vector getVectorValue(Node node, String attrName, String delim, Vector defaultValue) {
		String value = getValue(node, attrName, null);
				
		if (value == null)
			return defaultValue;
		
		String[] components = value.split(delim);
		
		try {
			switch (components.length) {
			case 0:
				return defaultValue;

			case 1:
				double v = Double.valueOf(components[0]);
				return new Vector(v);

			case 2:
				double v1 = Double.valueOf(components[0]);
				double v2 = Double.valueOf(components[1]);
				return new Vector(v1, v2, 0);
				
			default:
				double x = Double.valueOf(components[0]);
				double y = Double.valueOf(components[1]);
				double z = Double.valueOf(components[2]);
				return new Vector(x, y, z);
			}
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}

}
