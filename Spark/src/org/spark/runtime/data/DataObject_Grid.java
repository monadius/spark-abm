package org.spark.runtime.data;

/**
 * Data for a grid (2d double array)
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class DataObject_Grid extends DataObject {
	private int spaceIndex;
	private double[] data;
	private int n, m;
	private double xStep, yStep;
	
	
	/**
	 * Creates a copy of the given data array
	 * @param data
	 */
	DataObject_Grid(int spaceIndex, double[][] data, double xStep, double yStep) {
		this.spaceIndex = spaceIndex;
		this.xStep = xStep;
		this.yStep = yStep;
		
		this.n = data.length;
		this.m = data[0].length;
		
		this.data = new double[n * m];
		
		for (int i = 0, pos = 0; i < n; i++, pos += m) {
			System.arraycopy(data[i], 0, data, pos, m);
		}
	}
	
	
	/**
	 * Returns a value at the position (x, y)
	 * @param x
	 * @param y
	 * @return
	 */
	public double getValue(int x, int y) {
		return data[x * m + y];
	}
	
	
	/**
	 * Returns grid's x-dimension
	 * @return
	 */
	public int getXSize() {
		return n;
	}
	
	
	/**
	 * Returns grid's y-dimension
	 * @return
	 */
	public int getYSize() {
		return m;
	}
	
	
	/**
	 * Returns grid's x step value
	 * @return
	 */
	public double getXStep() {
		return xStep;
	}
	
	
	/**
	 * Returns grid's y step value
	 * @return
	 */
	public double getYStep() {
		return yStep;
	}
	
	
	/**
	 * Returns index of a space for which data is defined
	 * @return
	 */
	public int getSpaceIndex() {
		return spaceIndex;
	}
	
	
	@Override
	public String toString() {
		double sum = 0;
		
		for (int i = 0; i < data.length; i++)
			sum += data[i];
		
		return String.valueOf(sum);
	}

}
