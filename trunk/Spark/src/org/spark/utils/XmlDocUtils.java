package org.spark.utils;

import java.util.ArrayList;

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

		for (Node child = node.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			if (child.getNodeName().equals(name))
				list.add(child);
		}

		return list;
	}

}
