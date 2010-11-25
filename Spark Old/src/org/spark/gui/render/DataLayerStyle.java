package org.spark.gui.render;

import org.spark.utils.Vector;

public class DataLayerStyle {
	public String name;
	public Vector[][] gridGeometry;
	public double val1, val2;
	public Vector color1, color2;
	
	public DataLayerStyle() {
	}
	
	public DataLayerStyle(DataLayerStyle style) {
		this(style.name, style.val1, style.val2, style.color1, style.color2);
	}
	
	public DataLayerStyle(String name, double val1, double val2, Vector color1, Vector color2) {
		this.name = name;
		this.val1 = val1;
		this.val2 = val2;
		this.color1 = color1;
		this.color2 = color2;
	}
}
