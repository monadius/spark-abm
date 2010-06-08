/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */

package org.spark.data;

import org.spark.core.Observer;
import org.spark.gui.render.DataLayerWithColors;
import org.spark.math.Function;
import org.spark.math.Matrix;
import org.spark.space.BoundedSpace3d;
import org.spark.space.Space;
import org.spark.space.SpaceAgent;
import org.spark.utils.Vector;

/*
 * The basic implementation of the data layer interface
 * Values are stored inside cells of a grid of the given dimension 
 */
public class Grid3d implements AdvancedDataLayer, DataLayerWithColors {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -6864232284995812231L;
	// Reference to the space object
	protected BoundedSpace3d space;
	// Dimension of the grid
	protected int xSize, ySize, zSize;
	// TODO: non-symmetric borders: left, right, top, bottom
	// Dimension of the border which is not processed by this Grid
	protected int xBorder, yBorder, zBorder;
	// Topology of the grid
	protected boolean wrapX, wrapY, wrapZ;
	protected double xMin, xMax, yMin, yMax, zMin, zMax;

	// Auxiliary values for fast computations
	// The size of each rectangular grid cell
	protected double xStep, yStep, zStep;
	// The inverse of the grid cell size
	protected double invXStep, invYStep, invZStep;

	// Data stored in the grid
	protected double[][][] data;
	// Auxiliary array for some computations
	private double[][][] dataCopy;

	/**
	 * Creates the xSize by ySize grid
	 * 
	 * @param xSize
	 * @param ySize
	 */
	public Grid3d(int xSize, int ySize, int zSize) {
		this(Observer.getDefaultSpace(), xSize, ySize, zSize);
	}

	/**
	 * Sets the border sizes of the grid
	 * 
	 * @param xBorder
	 * @param yBorder
	 */
	public void setBorders(int xBorder, int yBorder, int zBorder) {
		// TODO: better solution (for all function)
		if (xBorder < 0 || yBorder < 0 || zBorder < 0 || 2 * xBorder > xSize
				|| 2 * yBorder > ySize || 2 * zBorder > zSize)
			throw new Error("Border sizes are inappropriate");

		this.xBorder = xBorder;
		this.yBorder = yBorder;
		this.zBorder = zBorder;
	}

	public Grid3d(Space space0, int xSize, int ySize, int zSize) {
		assert (xSize > 0 && ySize > 0 && zSize > 0);
		assert (space0 != null);

		if (!(space0 instanceof BoundedSpace3d))
			throw new Error("Grid3d can be defined only for BoundedSpace3d");
		
		BoundedSpace3d space = (BoundedSpace3d) space0;
		
		this.space = space;
		this.xSize = xSize;
		this.ySize = ySize;
		this.zSize = zSize;

		xStep = space.getXSize() / xSize;
		yStep = space.getYSize() / ySize;
		zStep = space.getZSize() / zSize;
		invXStep = 1.0 / xStep;
		invYStep = 1.0 / yStep;
		invZStep = 1.0 / zStep;

		this.wrapX = space.getWrapX();
		this.wrapY = space.getWrapY();
		this.wrapZ = space.getWrapZ();
		this.xMin = space.getXMin();
		this.xMax = space.getXMax();
		this.yMin = space.getYMin();
		this.yMax = space.getYMax();
		this.zMin = space.getZMin();
		this.zMax = space.getZMax();

		data = new double[xSize][ySize][zSize];
	}

	/**
	 * Returns the data array. Use this method carefully
	 * 
	 * @return
	 */
	public double[][][] getData() {
		return data;
	}

	/**
	 * Restricts the x coordinate in accordance with grid topology
	 * 
	 * @param x
	 * @return
	 */
	public int restrictX(int x) {
		if (wrapX) {
			if (x < 0) {
				x += xSize;
				if (x < 0) {
					x += (((-x) / xSize) + 1) * xSize;
				}
			} else if (x >= xSize) {
				x = x - xSize;
				if (x >= xSize) {
					x -= (x / xSize) * xSize;
				}
			}

		} else {
			if (x < 0)
				x = 0;
			else if (x >= xSize)
				x = xSize - 1;
		}

		return x;
	}

	/**
	 * Restricts the y coordinate in accordance with grid topology
	 * 
	 * @param y
	 * @return
	 */
	public int restrictY(int y) {
		if (wrapY) {
			if (y < 0) {
				y += ySize;
				if (y < 0) {
					y += (((-y) / ySize) + 1) * ySize;
				}
			} else if (y >= ySize) {
				y = y - ySize;
				if (y >= ySize) {
					y -= (y / ySize) * ySize;
				}
			}

		} else {
			if (y < 0)
				y = 0;
			else if (y >= ySize)
				y = ySize - 1;
		}

		return y;
	}

	/**
	 * Restricts the z coordinate in accordance with grid topology
	 * 
	 * @param z
	 * @return
	 */
	public int restrictZ(int z) {
		if (wrapZ) {
			if (z < 0) {
				z += zSize;
				if (z < 0) {
					z += (((-z) / zSize) + 1) * zSize;
				}
			} else if (z >= zSize) {
				z = z - zSize;
				if (z >= zSize) {
					z -= (z / zSize) * zSize;
				}
			}

		} else {
			if (z < 0)
				z = 0;
			else if (z >= zSize)
				z = zSize - 1;
		}

		return z;
	}

	/**
	 * Returns the grid x-coordinate corresponding to the space x-coordinate
	 * 
	 * @param x
	 * @return
	 */
	public int findX(double x) {
		x -= xMin;
		x *= invXStep;

		int xx = (int) Math.floor(x);
		return restrictX(xx);
	}

	/**
	 * Returns the grid y-coordinate corresponding to the space y-coordinate
	 * 
	 * @param y
	 * @return
	 */
	public int findY(double y) {
		y -= yMin;
		y *= invYStep;

		int yy = (int) Math.floor(y);
		return restrictY(yy);
	}

	/**
	 * Returns the grid z-coordinate corresponding to the space z-coordinate
	 * 
	 * @param z
	 * @return
	 */
	public int findZ(double z) {
		z -= zMin;
		z *= invZStep;

		int zz = (int) Math.floor(z);
		return restrictZ(zz);
	}

	/**
	 * Returns the coordinates of the grid cell
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public Vector getCenter(int x, int y, int z) {
		return new Vector(x * xStep + xMin + xStep / 2, y * yStep + yMin
				+ yStep / 2, z * zStep + zMin + zStep / 2);
	}

	// **************************
	// Rendering implementation
	// **************************

	// TODO: flexible interface for geometry, not just an array
	public Vector[][] getGeometry() {
		Vector[][][] data = new Vector[xSize + 1][ySize + 1][zSize + 1];

		for (int i = 0; i <= xSize; i++)
			for (int j = 0; j <= ySize; j++)
				for (int k = 0; k <= zSize; k++) {
					data[i][j][k] = new Vector(xMin + i * xStep, yMin + j
							* yStep, zMin + k * zStep);
				}

		// throw new Error("Method is not completely implemented");

		return data[0];
	}

	private Vector[][] geometry2;

	public Vector[][] getGeometry2() {
		if (geometry2 != null)
			return geometry2;

		geometry2 = new Vector[xSize][ySize];

		double xStepHalf = xStep / 2;
		double yStepHalf = yStep / 2;

		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) {
				geometry2[i][j] = new Vector(xMin + i * xStep + xStepHalf, yMin
						+ j * yStep + yStepHalf, 0);
			}
		}

		return geometry2;
	}

	public double getXStep() {
		return xStep;
	}

	public double getYStep() {
		return yStep;
	}

	public double getZStep() {
		return zStep;
	}

	private Vector[][] colors = null;

	public Vector[][] getColors(double val1, double val2, Vector color1,
			Vector color2) {
		if (colors == null) {
			colors = new Vector[xSize + 1][ySize + 1];

			for (int i = 0; i <= xSize; i++)
				for (int j = 0; j <= ySize; j++)
					colors[i][j] = new Vector();
		}

		if (Math.abs(val1 - val2) < 1e-3)
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
				t = data[i][j][0];
				x = a1 + b1 * t;
				y = a2 + b2 * t;
				z = a3 + b3 * t;

				colors[i][j].set(x, y, z);
			}

			t = data[i][ySize - 1][0];
			x = a1 + b1 * t;
			y = a2 + b2 * t;
			z = a3 + b3 * t;

			colors[i][ySize].set(x, y, z);
		}

		for (int j = 0; j < ySize; j++) {
			t = data[xSize - 1][j][0];
			x = a1 + b1 * t;
			y = a2 + b2 * t;
			z = a3 + b3 * t;

			colors[xSize][j].set(x, y, z);
		}

		t = data[xSize - 1][ySize - 1][0];
		x = a1 + b1 * t;
		y = a2 + b2 * t;
		z = a3 + b3 * t;

		colors[xSize][ySize].set(x, y, z);

		return colors;
	}

	// ************************************
	// DataLayer interface implementation
	// ************************************

	public double getValue(Vector p) {
		return data[findX(p.x)][findY(p.y)][findZ(p.z)];
	}

	public double addValue(Vector p, double value) {
		return data[findX(p.x)][findY(p.y)][findZ(p.z)] += value;
	}

	public void setValue(Vector p, double value) {
		data[findX(p.x)][findY(p.y)][findZ(p.z)] = value;
	}

	public void setValue(double value) {
		for (int i = 0; i < xSize; i++)
			for (int j = 0; j < ySize; j++)
				for (int k = 0; k < zSize; k++)
					data[i][j][k] = value;
	}

	/**
	 * Generalization of 2d function
	 * 
	 * @param p
	 * @return
	 */
	public Vector getUphillDirection(Vector p) {
		int x = findX(p.x);
		int y = findY(p.y);
		int z = findZ(p.z);

		double v = data[x][y][z];
		int x1 = 0, y1 = 0, z1 = 0;

		for (int k = -1; k <= 1; k++)
			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {
					int xx = x + i;
					int yy = y + j;
					int zz = z + k;

					if (wrapX) {
						if (xx < 0)
							xx = xSize - 1;
						else if (xx >= xSize)
							xx = 0;
					} else {
						if (xx < 0)
							xx = 0;
						else if (xx >= xSize)
							xx = xSize - 1;
					}

					if (wrapY) {
						if (yy < 0)
							yy = ySize - 1;
						else if (yy >= ySize)
							yy = 0;
					} else {
						if (yy < 0)
							yy = 0;
						else if (yy >= ySize)
							yy = ySize - 1;
					}
					
					if (wrapZ) {
						if (zz < 0)
							zz = zSize - 1;
						else if (zz >= zSize)
							zz = 0;
					} else {
						if (zz < 0)
							zz = 0;
						else if (zz >= zSize)
							zz = zSize - 1;
					}


					if (data[xx][yy][zz] > v) {
						x1 = i;
						y1 = j;
						z1 = k;
						v = data[xx][yy][zz];
					}
				}
			}

		return new Vector(x1, y1, z1);
	}
	
	
	
	/**
	 * Returns a "smooth" gradient at the given point
	 * @param p
	 * @return
	 */
	public Vector getSmoothGradient(Vector p) {
		int x = findX(p.x);
		int y = findY(p.y);
		int z = findZ(p.z);
		
		int x0 = restrictX(x - 1);
		int x1 = restrictX(x + 1);
		int y0 = restrictY(y - 1);
		int y1 = restrictY(y + 1);
		int z0 = restrictZ(z - 1);
		int z1 = restrictZ(z + 1);
		
		double dx = data[x1][y][z] - data[x0][y][z];
		double dy = data[x][y1][z] - data[x][y0][z];
		double dz = data[x][y][z1] - data[x][y][z0];
		
		return new Vector(dx / (2 * xStep), dy / (2 * yStep), dz / (2 * zStep));
	}
	

	public Vector getGradient(Vector p) {
		int x = findX(p.x);
		int y = findY(p.y);
		int z = findZ(p.z);

		double v = data[x][y][z];
		int x1 = 0, y1 = 0, z1 = 0;

		for (int i = -1; i <= 1; i++)
			for (int j = -1; j <= 1; j++)
				for (int k = -1; k <= 1; k++) {
					int xx = x + i;
					int yy = y + j;
					int zz = z + k;

					if (wrapX) {
						if (xx < 0)
							xx = xSize - 1;
						else if (xx >= xSize)
							xx = 0;
					} else {
						if (xx < 0)
							xx = 0;
						else if (xx >= xSize)
							xx = xSize - 1;
					}

					if (wrapY) {
						if (yy < 0)
							yy = ySize - 1;
						else if (yy >= ySize)
							yy = 0;
					} else {
						if (yy < 0)
							yy = 0;
						else if (yy >= ySize)
							yy = ySize - 1;
					}
					
					if (wrapZ) {
						if (zz < 0)
							zz = zSize - 1;
						else if (zz >= zSize)
							zz = 0;
					} else {
						if (zz < 0)
							zz = 0;
						else if (zz >= zSize)
							zz = zSize - 1;
					}

					if (data[xx][yy][zz] > v) {
						x1 = i;
						y1 = j;
						z1 = k;
						v = data[xx][yy][zz];
					}
				}

		double dv = v - data[x][y][z];
		return new Vector(x1 * dv, y1 * dv, z1 * dv);
	}

	public double addValue(SpaceAgent agent, double value) {
		return addValue(agent.getPosition(), value);
	}

	public double getValue(SpaceAgent agent) {
		return getValue(agent.getPosition());
	}

	public void setValue(SpaceAgent agent, double value) {
		setValue(agent.getPosition(), value);
	}

	public double getTotalNumber() {
		double v = 0;

		int xSize2 = xSize - xBorder;
		int ySize2 = ySize - yBorder;
		int zSize2 = zSize - zBorder;

		for (int i = xBorder; i < xSize2; i++)
			for (int j = yBorder; j < ySize2; j++)
				for (int k = zBorder; k < zSize2; k++)
					v += data[i][j][k];

		return v;
	}

	public void setValue(Function f) {
		Vector v = new Vector();
		v.x = xMin + xStep / 2;

		for (int i = 0; i < xSize; i++, v.x += xStep) {
			v.y = yMin + yStep / 2;

			for (int j = 0; j < ySize; j++, v.y += yStep) {
				v.z = zMin + zStep / 2;

				for (int k = 0; k < zSize; k++, v.z += zStep)
					data[i][j][k] = f.getValue(v);
			}
		}
	}

	// ************************************
	// AdvancedDataLayer interface implementation
	// ************************************

	public void multiply(double value) {
		int xSize2 = xSize - xBorder;
		int ySize2 = ySize - yBorder;
		int zSize2 = zSize - zBorder;

		for (int i = xBorder; i < xSize2; i++)
			for (int j = yBorder; j < ySize2; j++)
				for (int k = zBorder; k < zSize2; k++)
					data[i][j][k] *= value;
	}

	public void add(double value) {
		int xSize2 = xSize - xBorder;
		int ySize2 = ySize - yBorder;
		int zSize2 = zSize - zBorder;

		for (int i = xBorder; i < xSize2; i++)
			for (int j = yBorder; j < ySize2; j++)
				for (int k = zBorder; k < zSize2; k++)
					data[i][j][k] += value;
	}

	// TODO: implement with 3d matrix
	public void convolution(Matrix m) {
		if (true)
			throw new Error("Not implemented for 3d grid");
/*		int rows = m.getRowsNumber();
		int cols = m.getColsNumber();
		if ((rows & 1) == 0 || (cols & 1) == 0)
			throw new Error("The matrix should be of odd order");
		int r2 = rows / 2;
		int c2 = cols / 2;
		double[][] mData = m.getData();

		if (dataCopy == null)
			dataCopy = new double[xSize][ySize][zSize];
		else {
			for (int i = 0; i < xSize; i++)
				for (int j = 0; j < ySize; j++)
					dataCopy[i][j][0] = 0;
		}

		for (int j = 0; j < ySize; j++)
			for (int i = 0; i < xSize; i++) {
				double v = 0;
				for (int jj = -c2, jm = 0; jj <= c2; jj++, jm++)
					for (int ii = -r2, im = 0; ii <= r2; ii++, im++) {
						int x = i + ii;
						int y = j + jj;

						if (x < 0)
							x += xSize;
						else if (x >= xSize)
							x -= xSize;

						if (y < 0)
							y += ySize;
						else if (y >= ySize)
							y -= ySize;

						v += mData[im][jm] * data[x][y][0];
					}

				dataCopy[i][j][0] = v;
			}

		double[][][] temp = data;
		data = dataCopy;
		dataCopy = temp;*/
	}

	Matrix diffusion = new Matrix(3, 3);

	public void diffuse2(double p) {
		double q = p / 8;
		diffusion.set(0, 0, q);
		diffusion.set(1, 0, q);
		diffusion.set(2, 0, q);
		diffusion.set(0, 1, q);
		diffusion.set(2, 1, q);
		diffusion.set(0, 2, q);
		diffusion.set(1, 2, q);
		diffusion.set(2, 2, q);
		diffusion.set(1, 1, 1 - p);

		convolution(diffusion);
	}

	public void diffuse(double p) {
		assert (0 <= p && p <= 1);

		double q = p / 26;
		// TODO: better implementation with borders (and without them)
		int xSize2 = xSize - xBorder;
		int ySize2 = ySize - yBorder;
		int zSize2 = zSize - zBorder;

		// Create a temporary buffer for diffused data
		if (dataCopy == null)
			dataCopy = new double[xSize][ySize][zSize];
		else {
			// Fill it with zeros
			for (int i = xBorder; i < xSize2; i++)
				for (int j = yBorder; j < ySize2; j++)
					for (int k = zBorder; k < zSize2; k++)
						dataCopy[i][j][k] = 0;

			// TODO: implement this for all borders
			// The border values should be unchanged
			/*
			 * for (int i = 0; i < xBorder; i++) { for (int j = 0; j < ySize;
			 * j++) { for (int k = 0; k < zSize; k++) { dataCopy[i][j] =
			 * data[i][j]; dataCopy[i + xSize2][j] = data[i + xSize2][j]; } } }
			 * 
			 * for (int i = 0; i < xSize; i++) { for (int j = 0; j < yBorder;
			 * j++) { dataCopy[i][j] = data[i][j]; dataCopy[i][j + ySize2] =
			 * data[i][j + ySize2]; } }
			 */

		}

		for (int x = xBorder; x < xSize2; x++) {
			for (int y = yBorder; y < ySize2; y++) {
				for (int z = yBorder; z < zSize2; z++) {
					double v = data[x][y][z] * (1 - p);

					for (int i = -1; i <= 1; i++)
						for (int j = -1; j <= 1; j++)
							for (int k = -1; k <= 1; k++) {
								if (i == 0 && j == 0 && k == 0)
									continue;

								int xx = x + i;
								int yy = y + j;
								int zz = z + k;

								if (xx < 0) {
//									if (wrapX)
										xx = xSize - 1;
//									else
//										continue;
								}
								else if (xx >= xSize) {
//									if (wrapX)
										xx = 0;
//									else
//										continue;
								}

								if (yy < 0) {
//									if (wrapY)
										yy = ySize - 1;
//									else
//										continue;
								}
								else if (yy >= ySize) {
//									if (wrapY)
										yy = 0;
//									else
//										continue;
								}

								if (zz < 0) {
//									if (wrapZ)
										zz = zSize - 1;
//									else
//										continue;
								}
								else if (zz >= zSize) {
//									if (wrapZ)
										zz = 0;
//									else
//										continue;
								}

								v += data[xx][yy][zz] * q;
							}

					dataCopy[x][y][z] = v;
				}
			}
		}

		double[][][] temp = data;
		data = dataCopy;
		dataCopy = temp;
	}

	// TODO: zMin and zMax should be arguments
	public double getTotalNumber(double xMin, double xMax, double yMin,
			double yMax) {

		int x0 = findX(xMin);
		int y0 = findY(yMin);
		int z0 = findZ(zMin);
		int x1 = findX(xMax);
		int y1 = findY(yMax);
		int z1 = findZ(zMax);

		double val = 0;

		for (int i = x0; i <= x1; i++)
			for (int j = y0; j <= y1; j++)
				for (int k = z0; k <= z1; k++)
					val += data[i][j][k];

		return val;
	}

	public double getTotalNumber(DataLayer filter, double val) {
		double v = 0;

		// TODO: other cases
		if (filter instanceof Grid3d) {
			Grid3d filterGrid = (Grid3d) filter;

			// TODO: xMin, step are also important
			if (filterGrid.xSize == this.xSize
					&& filterGrid.ySize == this.ySize
					&& filterGrid.zSize == this.zSize) {
				double[][][] filterData = filterGrid.data;

				for (int i = 0; i < xSize; i++)
					for (int j = 0; j < ySize; j++) {
						for (int k = 0; k < zSize; k++) {
							if (filterData[i][j][k] == val)
								v += data[i][j][k];
						}
					}
			}
		}

		return v;
	}

	public void process(long tick) {
	}

	// TODO z should be an argument
	public double getValue(int x, int y) {
		return data[x][y][0];
	}

	// TODO: z should be an argument
	public void setValue(int x, int y, double value) {
		data[x][y][0] = value;
	}

	
	public double getMax() {
		double max = data[0][0][0];
		
		for (int k = 0; k < zSize; k++)
		for (int i = 0; i < xSize; i++)
			for (int j = 0; j < ySize; j++) {
				if (data[i][j][k] > max)
					max = data[i][j][k];
			}
		
		return max;
	}


	public double getMin() {
		double min = data[0][0][0];
		
		for (int k = 0; k < zSize; k++)
		for (int i = 0; i < xSize; i++)
			for (int j = 0; j < ySize; j++) {
				if (data[i][j][k] < min)
					min = data[i][j][k];
			}
		
		return min;
	}

	
	public Space getSpace() {
		return space;
	}
	
	
	
	/**
	 * Does nothing
	 */
	public void beginStep() {
	}
	

	/**
	 * Does nothing
	 */
	public void endStep() {
	}
}
