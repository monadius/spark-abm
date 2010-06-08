package org.spark.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.spark.gui.render.DataLayerWithColors;
import org.spark.math.Function;
import org.spark.math.Matrix;
import org.spark.space.BoundedSpace;
import org.spark.space.Space;
import org.spark.space.SpaceAgent;
import org.spark.utils.Vector;

/*
 * The basic implementation of the data layer interface
 * Values are stored inside cells of a grid of the given dimension 
 */
public class Grid implements AdvancedDataLayer, DataLayerWithColors {
	private static final long serialVersionUID = -1581695024221018527L;
	// Reference to the space object
	protected transient Space	space;
	// Dimension of the grid
	protected int xSize, ySize;
	// TODO: non-symmetric borders: left, right, top, bottom
	// Dimension of the border which is not processed by this Grid
	protected int xBorder, yBorder;
	// Topology of the grid
	protected boolean wrapX, wrapY;
	protected double xMin, xMax, yMin, yMax;

	// Auxiliary values for fast computations
	// The size of each rectangular grid cell
	protected double xStep, yStep;
	// The inverse of the grid cell size
	protected double invXStep, invYStep;
	
	// Data stored in the grid 
	protected double[][]	data;
	
	protected double[][]	readData;
	protected double[][]	writeData;
	
	// Auxiliary array for some computations
	private transient double[][]	dataCopy;
	
	private double colorScale = 10;
	
	
	protected Grid(Space space0, int xSize, int ySize) {
		assert(xSize > 0 && ySize > 0);
		assert(space0 != null);
		
		if (!(space0 instanceof BoundedSpace))
			throw new Error("Grid can be defined only for BoundedSpace");
		
		BoundedSpace space = (BoundedSpace) space0;

		this.space = space;
		this.xSize = xSize;
		this.ySize = ySize;
		
		xStep = space.getXSize() / xSize;
		yStep = space.getYSize() / ySize;
		invXStep = 1.0 / xStep;
		invYStep = 1.0 / yStep;
		
		this.wrapX = space.getWrapX();
		this.wrapY = space.getWrapY();
		this.xMin = space.getXMin();
		this.xMax = space.getXMax();
		this.yMin = space.getYMin();
		this.yMax = space.getYMax();
		
		
		data = new double[xSize][ySize];
		readData = data;
		writeData = data;
	}

	/**
	 * Sets the border sizes of the grid
	 * @param xBorder
	 * @param yBorder
	 */
	public void setBorders(int xBorder, int yBorder) {
		// TODO: better solution (for all function)
		if (xBorder < 0 ||
			yBorder < 0 ||
			2*xBorder > xSize ||
			2*yBorder > ySize)
			throw new Error("Border sizes are inappropriate");
			
		
		this.xBorder = xBorder;
		this.yBorder = yBorder;
	}
	
	

	
	/**
	 * Returns the data array. Use this method carefully
	 * @return
	 */
	public double[][] getData() {
		return data;
	}
	
	/**
	 * Restricts the x coordinate in accordance with grid topology
	 * @param x
	 * @return
	 */
	public int restrictX(int x) {
		if (wrapX) {
			if (x < 0) {
				x += xSize;
				if (x < 0) {
					x += ((-x - 1) / xSize + 1) * xSize;
				}
			}
			else if (x >= xSize) {
				x = x - xSize;
				if (x >= xSize) {
					x -= (x / xSize) * xSize;
				}
			}
			
		} else {
			if (x < 0) x = 0;
			else if (x >= xSize) x = xSize - 1;
		}
		
		return x;
	}

	/**
	 * Restricts the y coordinate in accordance with grid topology
	 * @param y
	 * @return
	 */
	public int restrictY(int y) {
		if (wrapY) {
			if (y < 0) {
				y += ySize;
				if (y < 0) {
					y += ((-y - 1) / ySize + 1) * ySize;
				}
			}
			else if (y >= ySize) {
				y = y - ySize;
				if (y >= ySize) {
					y -= (y / ySize) * ySize;
				}
			}
			
		} else {
			if (y < 0) y = 0;
			else if (y >= ySize) y = ySize - 1;
		}
		
		return y;
	}

	/**
	 * Returns the grid x-coordinate corresponding to the space x-coordinate
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
	 * Returns the coordinates of the grid cell
	 * @param x
	 * @param y
	 * @return
	 */
	public Vector getCenter(int x, int y) {
		return new Vector(x*xStep + xMin + xStep/2,
				y*yStep + yMin + yStep/2, 0);
	}

	
	//**************************
	// Rendering implementation
	//**************************
	
	
	public Vector[][] getGeometry() {
		Vector[][] data = new Vector[xSize + 1][ySize + 1];
		
		for (int i = 0; i <= xSize; i++) {
			for (int j = 0; j <= ySize; j++) {
				data[i][j] = new Vector(xMin + i*xStep, yMin + j*yStep, 0); 
			}
		}
		
		return data;
	}
	
	
	private transient Vector[][] geometry2;
	
	public Vector[][] getGeometry2() {
		if (geometry2 != null)
			return geometry2;

		geometry2 = new Vector[xSize][ySize];

		double xStepHalf = xStep / 2;
		double yStepHalf = yStep / 2;
		
		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) {
				geometry2[i][j] = new Vector(xMin + i*xStep + xStepHalf, yMin + j*yStep + yStepHalf, 0);
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
	
	

	public Vector[][] getColors() {
		Vector[][] data = new Vector[xSize + 1][ySize + 1];
		double scale = 1.0 / colorScale;

		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) {
				data[i][j] = new Vector(this.readData[i][j] * scale, 0, 0);
// 				data[i][j] = f(i, j);
			}
			data[i][ySize] = new Vector(this.readData[i][ySize - 1] * scale, 0, 0);
//			data[i][ySize] = RED;
		}
		
		for (int j = 0; j < ySize; j++) {
			data[xSize][j] = new Vector(this.readData[xSize - 1][j] * scale, 0, 0);
//			data[xSize][j] = RED;
		}
		data[xSize][ySize] = new Vector(this.readData[xSize - 1][ySize - 1] * scale, 0, 0);
//		data[xSize][ySize] = RED;
		
		return data;
	}
	
	private transient Vector[][] colors = null;
	
	public Vector[][] getColors(double val1, double val2, Vector color1, Vector color2) {
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
				t = readData[i][j];
				x = a1 + b1 * t;
				y = a2 + b2 * t;
				z = a3 + b3 * t;
				
				colors[i][j].set(x, y, z);
			}
			
			t = readData[i][ySize - 1];
			x = a1 + b1 * t;
			y = a2 + b2 * t;
			z = a3 + b3 * t;

			colors[i][ySize].set(x, y, z);
		}
		
		for (int j = 0; j < ySize; j++) {
			t = readData[xSize - 1][j];
			x = a1 + b1 * t;
			y = a2 + b2 * t;
			z = a3 + b3 * t;

			colors[xSize][j].set(x, y, z);
		}
		
		t = readData[xSize - 1][ySize - 1];
		x = a1 + b1 * t;
		y = a2 + b2 * t;
		z = a3 + b3 * t;

		colors[xSize][ySize].set(x, y, z);
		
		return colors;
	}
	
	
	
	
	//************************************
	// DataLayer interface implementation
	//************************************
	
	
	public double getValue(Vector p) {
		return readData[findX(p.x)][findY(p.y)];
	}
	
	
	public double addValue(Vector p, double value) {
		int x = findX(p.x);
		int y = findY(p.y);
		
		writeData[x][y] += value;
		return readData[x][y];
	}
	
	
	public void setValue(Vector p, double value) {
		// FIXME: this function should not work in the parallel mode
		// or collision resolution should be implemented
		writeData[findX(p.x)][findY(p.y)] = value;
	}
	
	
	public void setValue(double value) {
		for (int i = 0; i < xSize; i++)
			for (int j = 0; j < ySize; j++)
				writeData[i][j] = value;
	}
	
	
	/**
	 * Returns a "smooth" gradient at the given point
	 * @param p
	 * @return
	 */
	public Vector getSmoothGradient(Vector p) {
		int x = findX(p.x);
		int y = findY(p.y);
		
		int x0 = restrictX(x - 1);
		int x1 = restrictX(x + 1);
		int y0 = restrictY(y - 1);
		int y1 = restrictY(y + 1);
		
		double dx = readData[x1][y] - readData[x0][y];
		double dy = readData[x][y1] - readData[x][y0];
		
		return new Vector(dx / (2 * xStep), dy / (2 * yStep), 0);
	}
	
	
	public Vector getGradient(Vector p) {
		int x = findX(p.x);
		int y = findY(p.y);
		
		double v = readData[x][y];
		int x1 = 0, y1 = 0;
		
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				int xx = x + i;
				int yy = y + j;
			
				if (wrapX) {
					if (xx < 0) xx = xSize - 1;
					else if (xx >= xSize) xx = 0;
				}
				else {
					if (xx < 0) xx = 0;
					else if (xx >= xSize) xx = xSize - 1;
				}

				if (wrapY) {
					if (yy < 0) yy = ySize - 1;
					else if (yy >= ySize) yy = 0;
				}
				else {
					if (yy < 0) yy = 0;
					else if (yy >= ySize) yy = ySize - 1;
				}
				
				if (readData[xx][yy] > v) {
					x1 = i;
					y1 = j;
					v = readData[xx][yy];
				}
			}
		}
		
		double dv = v - readData[x][y];
		return new Vector(x1*dv, y1*dv, 0);
	}
	
	/**
	 * Returns the uphill direction in the NetLogo sense
	 * @param p
	 * @return
	 */
	public Vector getUphillDirection(Vector p) {
		int x = findX(p.x);
		int y = findY(p.y);
		
		double v = readData[x][y];
		int x1 = 0, y1 = 0;
		
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				int xx = x + i;
				int yy = y + j;
			
				if (wrapX) {
					if (xx < 0) xx = xSize - 1;
					else if (xx >= xSize) xx = 0;
				}
				else {
					if (xx < 0) xx = 0;
					else if (xx >= xSize) xx = xSize - 1;
				}

				if (wrapY) {
					if (yy < 0) yy = ySize - 1;
					else if (yy >= ySize) yy = 0;
				}
				else {
					if (yy < 0) yy = 0;
					else if (yy >= ySize) yy = ySize - 1;
				}
				
				if (readData[xx][yy] > v) {
					x1 = i;
					y1 = j;
					v = readData[xx][yy];
				}
			}
		}
		
		return new Vector(x1, y1, 0);
	}
	
	
	public double addValue(SpaceAgent agent, double value) {
		if (agent.getNode().getSpace() == space)
			return addValue(agent.getPosition(), value);
		else
			return 0;
	}


	public double getValue(SpaceAgent agent) {
		if (agent.getNode().getSpace() == space)
			return getValue(agent.getPosition());
		else
			return 0;
	}


	public void setValue(SpaceAgent agent, double value) {
		if (agent.getNode().getSpace() == space)
			setValue(agent.getPosition(), value);		
	}
	
	
	public double getTotalNumber() {
		double v = 0;
		
		int xSize2 = xSize - xBorder;
		int ySize2 = ySize - yBorder;
		
		for (int i = xBorder; i < xSize2; i++)
			for (int j = yBorder; j < ySize2; j++)
				v += readData[i][j];
		
		return v;
	}

	
	public void setValue(Function f) {
		Vector v = new Vector();
		v.x = xMin + xStep / 2;
		
		for (int i = 0; i < xSize; i++, v.x += xStep) {
			v.y = yMin + yStep / 2;

			for (int j = 0; j < ySize; j++, v.y += yStep) {
				writeData[i][j] = f.getValue(v);
			}
		}
	}

	//************************************
	// AdvancedDataLayer interface implementation
	//************************************
	
	public void multiply(double value) {
		int xSize2 = xSize - xBorder;
		int ySize2 = ySize - yBorder;
		
		for (int i = xBorder; i < xSize2; i++)
			for (int j = yBorder; j < ySize2; j++)
				writeData[i][j] *= value;
	}
	
	
	public void add(double value) {
		int xSize2 = xSize - xBorder;
		int ySize2 = ySize - yBorder;
		
		for (int i = xBorder; i < xSize2; i++)
			for (int j = yBorder; j < ySize2; j++)
				writeData[i][j] += value;
	}
	
	
	public void convolution(Matrix m) {
		int rows = m.getRowsNumber();
		int cols = m.getColsNumber();
		if ((rows & 1) == 0 || (cols & 1) == 0)
			throw new Error("The matrix should be of odd order");
		int r2 = rows / 2;
		int c2 = cols / 2;
		double[][] mData = m.getData();
		
		if (dataCopy == null)
			dataCopy = new double[xSize][ySize];
		else {
			for (int i = 0; i < xSize; i++)
				for (int j = 0; j < ySize; j++)
					dataCopy[i][j] = 0;
		}
		
		for (int j = 0; j < ySize; j++)
			for (int i = 0; i < xSize; i++) {
				double v = 0;
				for (int jj = -c2, jm = 0; jj <= c2; jj++, jm++)
					for (int ii = -r2, im = 0; ii <= r2; ii++, im++) {
						int x = i + ii;
						int y = j + jj;
						
						if (x < 0) x += xSize;
						else if (x >= xSize) x -= xSize;
						
						if (y < 0) y += ySize;
						else if (y >= ySize) y -= ySize;
						
						v += mData[im][jm] * readData[x][y];
					}
				
				dataCopy[i][j] = v;
			}

		double[][]	temp = data;
		data = dataCopy;
		readData = writeData = data;
		dataCopy = temp;
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
		assert(0 <= p && p <= 1);
		
		double q = p / 8;
		// TODO: better implementation with borders (and without them)
		int xSize2 = xSize - xBorder;
		int ySize2 = ySize - yBorder;
		
		// Create a temporary buffer for diffused data
		if (dataCopy == null)
			dataCopy = new double[xSize][ySize];
		else {
			// Fill it with zeros
			for (int i = xBorder; i < xSize2; i++)
				for (int j = yBorder; j < ySize2; j++)
					dataCopy[i][j] = 0;
			
			// The border values should be unchanged
			for (int i = 0; i < xBorder; i++) {
				for (int j = 0; j < ySize; j++) {
					dataCopy[i][j] = data[i][j];
					dataCopy[i + xSize2][j] = data[i + xSize2][j];
				}
			}

			for (int i = 0; i < xSize; i++) {
				for (int j = 0; j < yBorder; j++) {
					dataCopy[i][j] = data[i][j];
					dataCopy[i][j + ySize2] = data[i][j + ySize2];
				}
			}

		}
			
		
		for (int x = xBorder; x < xSize2; x++) {
			for (int y = yBorder; y < ySize2; y++) {
				double v = data[x][y] * (1 - p);
//				dataCopy[x][y] += data[x][y] * (1 - p);
				
				for (int i = -1; i <= 1; i++)
					for (int j = -1; j <= 1; j++) {
						if (i == 0 && j == 0) continue;
						
						int xx = x + i;
						int yy = y + j;
					
						if (xx < 0) xx = xSize - 1;
						else if (xx >= xSize) xx = 0;

						if (yy < 0) yy = ySize - 1;
						else if (yy >= ySize) yy = 0;
						
//						dataCopy[xx][yy] += data[x][y] * q;
						v += data[xx][yy] * q;
					}
				
				dataCopy[x][y] = v;
			}
		}
		
		double[][]	temp = data;
		data = dataCopy;
		// TODO: is it correct?
		readData = writeData = data;
		dataCopy = temp;
	}
	

	public double getTotalNumber(double xMin, double xMax, double yMin, double yMax) {
		int x0 = findX(xMin);
		int y0 = findY(yMin);
		int x1 = findX(xMax);
		int y1 = findY(yMax);

		double val = 0;
		
		for (int i = x0; i <= x1; i++)
			for (int j = y0; j <= y1; j++)
				val += readData[i][j];
		
		return val;
	}
	
	
	public double getTotalNumber(DataLayer filter, double val) {
		double v = 0;
		
		// TODO: other cases
		if (filter instanceof Grid) {
			Grid filterGrid = (Grid) filter;
			
			// TODO: xMin, step are also important
			if (filterGrid.xSize == this.xSize && filterGrid.ySize == this.ySize) {
				double[][] filterData = filterGrid.readData;
				
				for (int i = 0; i < xSize; i++)
					for (int j = 0; j < ySize; j++) {
						if (filterData[i][j] == val)
							v += readData[i][j];
					}
			}
		}
		
		return v;
	}


	public void process(long tick) {
	}

	
	public double getValue(int x, int y) {
		return readData[x][y];
	}

	
	public void setValue(int x, int y, double value) {
		writeData[x][y] = value;
	}
	

	public double getMax() {
		double max = readData[0][0];
		
		for (int i = 0; i < xSize; i++)
			for (int j = 0; j < ySize; j++) {
				if (readData[i][j] > max)
					max = readData[i][j];
			}
		
		return max;
	}


	public double getMin() {
		double min = readData[0][0];
		
		for (int i = 0; i < xSize; i++)
			for (int j = 0; j < ySize; j++) {
				if (readData[i][j] < min)
					min = readData[i][j];
			}
		
		return min;
	}

	
	public Space getSpace() {
		return space;
	}

	
	// FIXME: remove
	public void setSpace(Space space) {
		this.space = space;
	}

	/**
	 * Custom deserialization is needed.
	 */
	private void readObject(ObjectInputStream ois) throws IOException,
			ClassNotFoundException {

		ois.defaultReadObject();
	}

	/**
	 * Custom serialization is needed.
	 */
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
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
