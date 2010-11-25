package org.spark.runtime.external.render;

import static org.spark.utils.XmlDocUtils.*;

import org.w3c.dom.Node;


/**
 * Represents visualization properties of a space
 * @author Monad
 *
 */
public class SpaceStyle {
	/* The name of a space */
	public String name;
	
	// TODO: make other rotations available
	public boolean swapXY;
	
	
	/* Indicates whether this space is selected or not */
	public boolean selected;
	
	
	/**
	 * A default constructor
	 * @param name
	 */
	public SpaceStyle(String name, boolean swapXY, boolean selected) {
		this.name = name;
		this.swapXY = swapXY;
		this.selected = selected;
	}
	
	
	
	/**
	 * Loads a space style from the given xml node
	 * @param node
	 * @return
	 */
	public static SpaceStyle load(Node node) {
		String name = getValue(node, "name", "space");
		boolean selected = getBooleanValue(node, "selected", false);
		boolean swapXY = getBooleanValue(node, "swapXY", false);
		
		return new SpaceStyle(name, swapXY, selected);
	}
		
}
