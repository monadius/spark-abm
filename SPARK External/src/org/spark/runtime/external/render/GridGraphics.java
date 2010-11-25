package org.spark.runtime.external.render;

import org.spark.runtime.data.DataObject_Grid;
import org.spark.utils.Vector;

/**
 * Auxiliary class for converting grid data into colors
 * 
 * @author Monad
 * 
 */
public class GridGraphics {
	/**
	 * Computes colors based on the given data object
	 * 
	 * @param data
	 * @param val1
	 * @param val2
	 * @param color1
	 * @param color2
	 * @return
	 */
	public static Vector[][] getColors(DataObject_Grid data,
			DataLayerStyle style) {
		double val1 = style.getVal1();
		double val2 = style.getVal2();
		Vector color1 = style.getColor1();
		Vector color2 = style.getColor2();

		int xSize = data.getXSize();
		int ySize = data.getYSize();

		Vector[][] colors = new Vector[xSize][ySize];

		if (style.sortValues()) {
			// Use full interpolation
			for (int i = 0; i < xSize; i++) {
				for (int j = 0; j < ySize; j++) {
					double value = data.getValue(i, j);
					colors[i][j] = style.getColor(value);
				}
			}
		} else {
			// Use simple two-color interpolation
			if (Math.abs(val1 - val2) < 1e-6)
				val2 = val1 + 1;

			double x1, y1, z1;
			double x2, y2, z2;
			double a1, b1, a2, b2, a3, b3;

			x1 = color1.x;
			y1 = color1.y;
			z1 = color1.z;
			x2 = color2.x;
			y2 = color2.y;
			z2 = color2.z;

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
		}

		return colors;
	}

	/**
	 * Computes vertex data from the given grid data
	 * 
	 * @param data
	 * @param xMin
	 * @param yMin
	 * @return
	 */
	public static Vector[][] getGeometry(DataObject_Grid data, double xMin,
			double yMin) {
		int xSize = data.getXSize();
		int ySize = data.getYSize();
		double xStep = data.getXStep();
		double yStep = data.getYStep();

		Vector[][] geometry = new Vector[xSize][ySize];

		double xStepHalf = xStep / 2;
		double yStepHalf = yStep / 2;

		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) {
				geometry[i][j] = new Vector(xMin + i * xStep + xStepHalf, yMin
						+ j * yStep + yStepHalf, 0);
			}
		}

		return geometry;
	}
}
