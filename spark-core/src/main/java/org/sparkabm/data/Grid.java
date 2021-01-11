package org.sparkabm.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.sparkabm.math.Function;
import org.sparkabm.math.Matrix;
import org.sparkabm.space.BoundedSpace;
import org.sparkabm.space.Space;
import org.sparkabm.space.SpaceAgent;
import org.sparkabm.math.Vector;

/*
 * The basic implementation of the data layer interface
 * Values are stored inside cells of a grid of the given dimension 
 */
public class Grid implements AdvancedDataLayer {
	private static final long serialVersionUID = -1581695024221018527L;
	// Reference to the space object
	protected transient Space	space;
	// Dimension of the grid
	protected int xSize, ySize;
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
	 * Returns the data array. Use this method carefully
	 * @return
	 */
	public double[][] getData() {
		return data;
	}
	
	
	public int getXSize() {
		return xSize;
	}
	
	
	public int getYSize() {
		return ySize;
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

	
	
	
	public double getXStep() {
		return xStep;
	}
	
	
	public double getYStep() {
		return yStep;
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
		
		for (int i = 0; i < xSize; i++) {
			double[] data = readData[i];
			for (int j = 0; j < ySize; j++)
				v += data[j];
		}
		
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
		for (int i = 0; i < xSize; i++) {
			double[] data = writeData[i];
			for (int j = 0; j < ySize; j++)
				data[j] *= value;
		}
	}
	
	
	public void add(double value) {
		for (int i = 0; i < xSize; i++) {
			double[] data = writeData[i];
			for (int j = 0; j < ySize; j++)
				data[j] += value;
		}
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
	
	
	/**
	 * Diffusion with a mask.
	 * No diffusion to all cells with a positive mask value.
	 */
	public void diffuse(double p, Grid mask) {
		if (mask.xSize != xSize || mask.ySize != ySize)
			return;
		
		if (dataCopy == null)
			dataCopy = new double[xSize][ySize];
		
		double[][] maskData = mask.getData();
		double q = p / 8;
		
		for (int x = 0; x < xSize; x++) {
			for (int y = 0; y < ySize; y++) {
				if (maskData[x][y] > 0.0) {
					// No diffusion
					dataCopy[x][y] = 0.0;
					continue;
				}
				
				// n is the number of good neighbors
				int n = 0;
				double v = 0;
				
				for (int i = -1; i <= 1; i++) {
					for (int j = -1; j <= 1; j++) {
						int x1 = x + i;
						int y1 = y + j;
						
						// Bound x1
						if (x1 < 0) {
							if (wrapX)
								x1 = xSize - 1;
							else
								continue;
						}
						else if (x1 == xSize) {
							if (wrapX)
								x1 = 0;
							else
								continue;
						}
						
						// Bound y1
						if (y1 < 0) {
							if (wrapY)
								y1 = ySize - 1;
							else
								continue;
						}
						else if (y1 == ySize) {
							if (wrapY)
								y1 = 0;
							else
								continue;
						}
						
						if (maskData[x1][y1] > 0.0)
							continue;
						
						n++;
						v += data[x1][y1];
					} // j
				} // i
				
				double pp = 1 - n * q;
				dataCopy[x][y] = data[x][y] * pp + v * q;
			} // y
		} // x
		
		double[][] temp = data;
		data = dataCopy;
		readData = writeData = data;
		dataCopy = temp;
	}
	
	
	/**
	 * Diffusion operation
	 */
	public void diffuse(double p) {
		if (dataCopy == null)
			dataCopy = new double[xSize][ySize];
/*		else {
			// Fill it with zeros
			for (int i = 0; i < xSize; i++) {
				double[] tmp = dataCopy[i];
				for (int j = 0; j < ySize; j++)
					tmp[j] = 0;
			}
		}
*/		
		if (wrapX) {
			if (wrapY)
				diffuseTT(p);
//				old_diffusion(p);
			else
				diffuseTF(p);
		}
		else {
			if (wrapY)
				diffuseFT(p);
			else
				diffuseFF(p);
		}
	}
	
	
	
	/**
	 * Diffusion operation for the FF-topology
	 */
	protected void diffuseFF(final double p) {
		final int xSize2 = xSize - 1;
		final int ySize2 = ySize - 1;
		final double p2 = 1 - p;
		final double q = p / 8.0;

		// TODO: it is assumed that xSize >= 2 and ySize >= 2
		
		// Diffusion for corners
		// (0,0)
		double pp = p2 + 5 * q;
		dataCopy[0][0] = data[0][0] * pp +
			q * (data[1][0] + data[0][1] + data[1][1]);
		
		// (xSize2,0)
		dataCopy[xSize2][0] = data[xSize2][0] * pp +
			q * (data[xSize2 - 1][0] + data[xSize2 - 1][1] + data[xSize2][1]);

		// (xSize2,ySize2)
		dataCopy[xSize2][ySize2] = data[xSize2][ySize2] * pp +
			q * (data[xSize2 - 1][ySize2] + data[xSize2 - 1][ySize2 - 1] + data[xSize2][ySize2 - 1]);
		
		// (0,ySize2)
		dataCopy[0][ySize2] = data[0][ySize2] * pp +
			q * (data[1][ySize2] + data[1][ySize2 - 1] + data[0][ySize2 - 1]);
		

		// Diffusion for borders
		// Left, x = 0
		pp = p2 + 3 * q; 

		double[] data1 = data[0];
		double[] data2 = data[1];
		double[] r = dataCopy[0];
		for (int y = 1; y < ySize2; y++) {
			r[y] = data1[y] * pp +
				q * (data1[y - 1] + data1[y + 1] + 
					 data2[y - 1] + data2[y] + data2[y + 1]);
		}

		// Right, x = xSize2
		data1 = data[xSize2];
		data2 = data[xSize2 - 1];
		r = dataCopy[xSize2];
		for (int y = 1; y < ySize2; y++) {
			r[y] = data1[y] * pp +
				q * (data1[y - 1] + data1[y + 1] +
					 data2[y - 1] + data2[y] + data2[y + 1]);
		}
		
		// Bottom, y = 0
		for (int x = 1; x < xSize2; x++) {
			dataCopy[x][0] = data[x][0] * pp +
				q * (data[x - 1][0] + data[x + 1][0] +
					 data[x - 1][1] + data[x][1] + data[x + 1][1]);
		}
		
		// Top, y = ySize2
		for (int x = 1; x < xSize2; x++) {
			dataCopy[x][ySize2] = data[x][ySize2] * pp +
				q * (data[x - 1][ySize2] + data[x + 1][ySize2] +
				     data[x - 1][ySize2 - 1] + data[x][ySize2 - 1] + data[x + 1][ySize2 - 1]);
		}

		
		// Diffusion for the center
		for (int x = 1; x < xSize2; x++) {
			double[] data0 = data[x - 1];
			data1 = data[x];
			data2 = data[x + 1];
			r = dataCopy[x];
			
			for (int y = 1; y < ySize2; y++) {
				r[y] = data1[y] * p2 +
						q * (data0[y - 1] + data0[y] + data0[y + 1] +
							 data1[y - 1] + data1[y + 1] +
							 data2[y - 1] + data2[y] + data2[y + 1]);
			}
		}
		
		double[][]	temp = data;
		data = dataCopy;
		// TODO: is it correct?
		readData = writeData = data;
		dataCopy = temp;
	}
	
	
	/**
	 * Diffusion operation for the FT-topology
	 */
	protected void diffuseFT(final double p) {
		final int xSize2 = xSize - 1;
		final int ySize2 = ySize - 1;
		final double p2 = 1 - p;
		final double q = p / 8.0;

		// TODO: it is assumed that xSize >= 2 and ySize >= 2
		
		// Diffusion for corners
		// (0,0)
		double pp = p2 + 3 * q;
		dataCopy[0][0] = data[0][0] * pp +
			q * (data[1][0] + data[0][1] + data[1][1] + 
				 data[0][ySize2] + data[1][ySize2]);
		
		// (xSize2,0)
		dataCopy[xSize2][0] = data[xSize2][0] * pp +
			q * (data[xSize2 - 1][0] + data[xSize2 - 1][1] + data[xSize2][1] +
				 data[xSize2 - 1][ySize2] + data[xSize2][ySize2]);

		// (xSize2,ySize2)
		dataCopy[xSize2][ySize2] = data[xSize2][ySize2] * pp +
			q * (data[xSize2 - 1][ySize2] + data[xSize2 - 1][ySize2 - 1] + data[xSize2][ySize2 - 1] +
				 data[xSize2 - 1][0] + data[xSize2][0]);	
					
		// (0,ySize2)
		dataCopy[0][ySize2] = data[0][ySize2] * pp +
			q * (data[1][ySize2] + data[1][ySize2 - 1] + data[0][ySize2 - 1] +
				 data[0][0] + data[1][0]);
		

		// Diffusion for borders
		// Left, x = 0
		pp = p2 + 3 * q; 

		double[] data1 = data[0];
		double[] data2 = data[1];
		double[] r = dataCopy[0];
		for (int y = 1; y < ySize2; y++) {
			r[y] = data1[y] * pp +
				q * (data1[y - 1] + data1[y + 1] + 
					 data2[y - 1] + data2[y] + data2[y + 1]);
		}

		// Right, x = xSize2
		data1 = data[xSize2];
		data2 = data[xSize2 - 1];
		r = dataCopy[xSize2];
		for (int y = 1; y < ySize2; y++) {
			r[y] = data1[y] * pp +
				q * (data1[y - 1] + data1[y + 1] +
					 data2[y - 1] + data2[y] + data2[y + 1]);
		}
		
		// Bottom, y = 0
		for (int x = 1; x < xSize2; x++) {
			dataCopy[x][0] = data[x][0] * p2 +
				q * (data[x - 1][0] + data[x + 1][0] +
					 data[x - 1][1] + data[x][1] + data[x + 1][1] +
					 data[x - 1][ySize2] + data[x][ySize2] + data[x + 1][ySize2]);
		}
		
		// Top, y = ySize2
		for (int x = 1; x < xSize2; x++) {
			dataCopy[x][ySize2] = data[x][ySize2] * p2 +
				q * (data[x - 1][ySize2] + data[x + 1][ySize2] +
				     data[x - 1][ySize2 - 1] + data[x][ySize2 - 1] + data[x + 1][ySize2 - 1] +
				     data[x - 1][0] + data[x][0] + data[x + 1][0]);
		}

		
		// Diffusion for the center
		for (int x = 1; x < xSize2; x++) {
			double[] data0 = data[x - 1];
			data1 = data[x];
			data2 = data[x + 1];
			r = dataCopy[x];
			
			for (int y = 1; y < ySize2; y++) {
				r[y] = data1[y] * p2 +
						q * (data0[y - 1] + data0[y] + data0[y + 1] +
							 data1[y - 1] + data1[y + 1] +
							 data2[y - 1] + data2[y] + data2[y + 1]);
			}
		}
		
		double[][]	temp = data;
		data = dataCopy;
		// TODO: is it correct?
		readData = writeData = data;
		dataCopy = temp;
	}
	
	
	/**
	 * Diffusion operation for the TF-topology
	 */
	protected void diffuseTF(final double p) {
		final int xSize2 = xSize - 1;
		final int ySize2 = ySize - 1;
		final double p2 = 1 - p;
		final double q = p / 8.0;

		// TODO: it is assumed that xSize >= 2 and ySize >= 2
		
		// Diffusion for corners
		// (0,0)
		double pp = p2 + 3 * q;
		dataCopy[0][0] = data[0][0] * pp +
			q * (data[1][0] + data[0][1] + data[1][1] + 
				 data[xSize2][0] + data[xSize2][1]);
		
		// (xSize2,0)
		dataCopy[xSize2][0] = data[xSize2][0] * pp +
			q * (data[xSize2 - 1][0] + data[xSize2 - 1][1] + data[xSize2][1] +
				 data[0][0] + data[0][1]);

		// (xSize2,ySize2)
		dataCopy[xSize2][ySize2] = data[xSize2][ySize2] * pp +
			q * (data[xSize2 - 1][ySize2] + data[xSize2 - 1][ySize2 - 1] + data[xSize2][ySize2 - 1] +
				 data[0][ySize2 - 1] + data[0][ySize2]);	
					
		// (0,ySize2)
		dataCopy[0][ySize2] = data[0][ySize2] * pp +
			q * (data[1][ySize2] + data[1][ySize2 - 1] + data[0][ySize2 - 1] +
				 data[xSize2][ySize2 - 1] + data[xSize2][ySize2]);
		

		// Diffusion for borders
		// Left, x = 0
		pp = p2 + 3 * q; 

		double[] data0 = data[xSize2];
		double[] data1 = data[0];
		double[] data2 = data[1];
		double[] r = dataCopy[0];
		for (int y = 1; y < ySize2; y++) {
			r[y] = data1[y] * p2 +
				q * (data0[y - 1] + data0[y] + data0[y + 1] +
					 data1[y - 1] + data1[y + 1] + 
					 data2[y - 1] + data2[y] + data2[y + 1]);
		}

		// Right, x = xSize2
		data0 = data[xSize2 - 1];
		data1 = data[xSize2];
		data2 = data[0];
		r = dataCopy[xSize2];
		for (int y = 1; y < ySize2; y++) {
			r[y] = data1[y] * p2 +
				q * (data0[y - 1] + data0[y] + data0[y + 1] +
					 data1[y - 1] + data1[y + 1] +
					 data2[y - 1] + data2[y] + data2[y + 1]);
		}
		
		// Bottom, y = 0
		for (int x = 1; x < xSize2; x++) {
			dataCopy[x][0] = data[x][0] * pp +
				q * (data[x - 1][0] + data[x + 1][0] +
					 data[x - 1][1] + data[x][1] + data[x + 1][1]);
		}
		
		// Top, y = ySize2
		for (int x = 1; x < xSize2; x++) {
			dataCopy[x][ySize2] = data[x][ySize2] * pp +
				q * (data[x - 1][ySize2] + data[x + 1][ySize2] +
				     data[x - 1][ySize2 - 1] + data[x][ySize2 - 1] + data[x + 1][ySize2 - 1]);
		}

		
		// Diffusion for the center
		for (int x = 1; x < xSize2; x++) {
			data0 = data[x - 1];
			data1 = data[x];
			data2 = data[x + 1];
			r = dataCopy[x];
			
			for (int y = 1; y < ySize2; y++) {
				r[y] = data1[y] * p2 +
						q * (data0[y - 1] + data0[y] + data0[y + 1] +
							 data1[y - 1] + data1[y + 1] +
							 data2[y - 1] + data2[y] + data2[y + 1]);
			}
		}
		
		double[][]	temp = data;
		data = dataCopy;
		// TODO: is it correct?
		readData = writeData = data;
		dataCopy = temp;
	}
	
	
	
	/**
	 * Diffusion operation for the TT-topology
	 */
	protected void diffuseTT(final double p) {
		final int xSize2 = xSize - 1;
		final int ySize2 = ySize - 1;
		final double p2 = 1 - p;
		final double q = p / 8.0;

		// TODO: it is assumed that xSize >= 2 and ySize >= 2
		
		// Diffusion for corners
		// (0,0)
		dataCopy[0][0] = p2 * data[0][0] + 
			q *	(data[1][0] + data[1][1] + data[0][1] + data[xSize2][1] +
				 data[xSize2][0] + data[xSize2][ySize2] + data[0][ySize2] + data[1][ySize2]);
		
		// (xSize2,0)
		dataCopy[xSize2][0] = p2 * data[xSize2][0] + 
			q *	(data[0][0] + data[0][1] + data[xSize2][1] + data[xSize2 - 1][1] +
				 data[xSize2 - 1][0] + data[xSize2 - 1][ySize2] + data[xSize2][ySize2] + data[0][ySize2]);
		
		// (0,ySize2)
		dataCopy[0][ySize2] = p2 * data[0][ySize2] + 
			q *	(data[1][ySize2] + data[1][0] + data[0][0] + data[xSize2][0] +
				 data[xSize2][ySize2] + data[xSize2][ySize2 - 1] + data[0][ySize2 - 1] + data[1][ySize2 - 1]);
		
		// (xSize2,ySize2)
		dataCopy[xSize2][ySize2] = p2 * data[xSize2][ySize2] + 
			q *	(data[0][ySize2] + data[0][0] + data[xSize2][0] + data[xSize2 - 1][0] +
				 data[xSize2 - 1][ySize2] + data[xSize2 - 1][ySize2 - 1] + 
				 data[xSize2][ySize2 - 1] + data[0][ySize2 - 1]);

		// Diffusion for borders
		// Left, x = 0
		
		double[] data0 = data[xSize2];
		double[] data1 = data[0];
		double[] data2 = data[1];
		double[] r = dataCopy[0];
		for (int y = 1; y < ySize2; y++) {
			r[y] = p2 * data1[y] + 
						q * (data0[y - 1] + data0[y] + data0[y + 1] +
							 data1[y - 1] + data1[y + 1] +
							 data2[y - 1] + data2[y] + data2[y + 1]);
		}

		// Right, x = xSize2
		data0 = data[xSize2 - 1];
		data1 = data[xSize2];
		data2 = data[0];
		r = dataCopy[xSize2];
		for (int y = 1; y < ySize2; y++) {
			r[y] = p2 * data1[y] + 
						q * (data0[y - 1] + data0[y] + data0[y + 1] +
							 data1[y - 1] + data1[y + 1] +
							 data2[y - 1] + data2[y] + data2[y + 1]);
		}
		
		// Bottom, y = 0
		for (int x = 1; x < xSize2; x++) {
			dataCopy[x][0] = p2 * data[x][0] + 
				q * (data[x - 1][ySize2] + data[x - 1][0] + data[x - 1][1] +
					 data[x][ySize2] + data[x][1] +
					 data[x + 1][ySize2] + data[x + 1][0] + data[x + 1][1]);
		}
		
		// Top, y = ySize2
		for (int x = 1; x < xSize2; x++) {
			dataCopy[x][ySize2] = p2 * data[x][ySize2] + 
				q * (data[x - 1][ySize2 - 1] + data[x - 1][ySize2] + data[x - 1][0] +
					 data[x][ySize2 - 1] + data[x][0] +
					 data[x + 1][ySize2 - 1] + data[x + 1][ySize2] + data[x + 1][0]);
		}
		

		// Diffusion for the center
		for (int x = 1; x < xSize2; x++) {
			data0 = data[x - 1];
			data1 = data[x];
			data2 = data[x + 1];
			r = dataCopy[x];
			
			for (int y = 1; y < ySize2; y++) {
				r[y] = p2 * data1[y] +
						q * (data0[y - 1] + data0[y] + data0[y + 1] +
							 data1[y - 1] + data1[y + 1] +
							 data2[y - 1] + data2[y] + data2[y + 1]);
			}
		}
		
		double[][]	temp = data;
		data = dataCopy;
		// TODO: is it correct?
		readData = writeData = data;
		dataCopy = temp;
	}
	
	
	
	
		
	
	protected void old_diffusion(double p) {
		double q = p / 8.0;
		
		for (int x = 0; x < xSize; x++) {
			for (int y = 0; y < ySize; y++) {
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
	
	
	public void addValue(int x, int y, double value) {
		writeData[x][y] += value;
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
