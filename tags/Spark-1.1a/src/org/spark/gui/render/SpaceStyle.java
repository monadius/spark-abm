package org.spark.gui.render;

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
	
	
	/**
	 * A default constructor
	 * @param name
	 */
	public SpaceStyle(String name) {
		this.name = name;
	}
}
