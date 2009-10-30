package org.spark.runtime.external.render;

import org.spark.utils.Vector;

/**
 * Describes a rendering style of a data layer 
 * @author Monad
 *
 */
public class DataLayerStyle {
	/* Data layer's name */
	public String name;
	
	/* Geometry of the data layer */
	public Vector[][] gridGeometry;
	
	/* Minimum and maximum values */
	public double val1, val2;
	
	/* Corresponding colors */
	public Vector color1, color2;
	
	
	/**
	 * Default constructor
	 */
	public DataLayerStyle() {
	}
	
	
	/**
	 * Copy constructor
	 * @param style
	 */
	public DataLayerStyle(DataLayerStyle style) {
		this(style.name, style.val1, style.val2, style.color1, style.color2);
	}
	
	
	/**
	 * Constructor
	 * @param name
	 * @param val1
	 * @param val2
	 * @param color1
	 * @param color2
	 */
	public DataLayerStyle(String name, double val1, double val2, Vector color1, Vector color2) {
		this.name = name;
		this.val1 = val1;
		this.val2 = val2;
		this.color1 = color1;
		this.color2 = color2;
	}
}
