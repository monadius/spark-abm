package org.spark.runtime.external.render;

import org.spark.runtime.data.DataObject_Grid;
import org.spark.utils.Vector;

/**
 * Auxiliary class for converting grid data into colors
 * @author Monad
 *
 */
public class GridGraphics {
	public static Vector[][] getColors(DataObject_Grid data, double val1, double val2, Vector color1, Vector color2) {
		int xSize = data.getXSize();
		int ySize = data.getYSize();
		
		Vector[][] colors = new Vector[xSize][ySize];
			
		if (Math.abs(val1 - val2) < 1e-6)
			val2 = val1 + 1;
		
		double x1, y1, z1;
		double x2, y2, z2;
		double a1, b1, a2, b2, a3, b3;
		
		x1 = color1.x; y1 = color1.y; z1 = color1.z;
		x2 = color2.x; y2 = color2.y; z2 = color2.z;
		
		b1 = (x2 - x1) / (val2 - val1);
		a1 = x1 - b1 * val1;

		b2 = (y2 - y1) / (val2 - val1);
		a2 = y1 - b2 * val1;

		b3 = (z2 - z1) / (val2 - val1);
		a3 = z1 - b3 * val1;

		double x, y, z, t;
		
		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) {
				t = data.getValue(i, j);
				x = a1 + b1 * t;
				y = a2 + b2 * t;
				z = a3 + b3 * t;
				
				colors[i][j] = new Vector(x, y, z);
			}
		}
			
		return colors;
	}
}
