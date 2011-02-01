package org.spark.runtime.external.render;

import static org.spark.utils.XmlDocUtils.*;

import java.io.File;

import org.w3c.dom.Document;
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
	
	/* If true, then the sizes of space cells are computed automatically */
	public boolean autoSize;
	
	/* Sizes of space cells in pixels */
	public int cellXSize, cellYSize;
	
	
	/**
	 * A default constructor
	 * @param name
	 */
	private SpaceStyle(String name, boolean swapXY, boolean selected) {
		this.name = name;
		this.swapXY = swapXY;
		this.selected = selected;
		this.autoSize = true;
		this.cellXSize = 10;
		this.cellYSize = 10;
	}
	
	
	public SpaceStyle(String name) {
		this(name, false, true);
	}
	
	
	/**
	 * Creates an xml node for this space style
	 */
	public Node createNode(Document doc, File modelPath) {
		Node spaceNode = doc.createElement("spacestyle");

		addAttr(doc, spaceNode, "name", name);
		addAttr(doc, spaceNode, "swapXY", swapXY);
		addAttr(doc, spaceNode, "selected", selected);
		addAttr(doc, spaceNode, "auto-size", autoSize);
		addAttr(doc, spaceNode, "cell-xsize", cellXSize);
		addAttr(doc, spaceNode, "cell-ysize", cellYSize);

		return spaceNode;
	}
	
	/**
	 * Loads a space style from the given xml node
	 * @param node
	 * @return
	 */
	public static SpaceStyle load(Node node) {
		String name = getValue(node, "name", "space");
		boolean selected = getBooleanValue(node, "selected", true);
		boolean swapXY = getBooleanValue(node, "swapXY", false);
		
		boolean autoSize = getBooleanValue(node, "auto-size", true);
		int cellXSize = getIntegerValue(node, "cell-xsize", 10);
		int cellYSize = getIntegerValue(node, "cell-ysize", 10);
		
		SpaceStyle style = new SpaceStyle(name, swapXY, selected);
		style.autoSize = autoSize;
		style.cellXSize = cellXSize;
		style.cellYSize = cellYSize;
		
		return style;
	}
		
}
