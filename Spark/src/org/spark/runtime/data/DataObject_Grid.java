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
	
	
	/**
	 * Creates a copy of the given data array
	 * @param data
	 */
	DataObject_Grid(int spaceIndex, double[][] data) {
		this.spaceIndex = spaceIndex;
		
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
