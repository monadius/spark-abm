package org.spark.gui.render;

import org.spark.utils.Vector;

public interface DataLayerWithColors {
	public Vector[][] getColors(double val1, double val2, Vector color1, Vector color2);
	public Vector[][] getGeometry();
	public Vector[][] getGeometry2();
	
	public double getXStep();
	public double getYStep();
}
