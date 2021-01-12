package org.spark.runtime.data;

/**
 * Data for a grid (2d double array)
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class DataObject_Grid extends DataObject {
	private final int spaceIndex;
	private final double[] data;
	private final int n, m, k;
	private final double xStep, yStep, zStep;
	
	private transient Double min, max;
	

	
	/**
	 * Creates a shallow copy of the given grid data
	 * @param grid
	 */
	protected DataObject_Grid(DataObject_Grid grid) {
		this.spaceIndex = grid.spaceIndex;
		this.xStep = grid.xStep;
		this.yStep = grid.yStep;
		this.zStep = grid.zStep;
		this.n = grid.n;
		this.m = grid.m;
		this.k = grid.k;
		this.min = grid.min;
		this.max = grid.max;
		this.data = grid.data;
	}
	
	/**
	 * Creates a copy of the given data array
	 * @param data
	 */
	public DataObject_Grid(int spaceIndex, double[][] data, double xStep, double yStep) {
		this.spaceIndex = spaceIndex;
		this.xStep = xStep;
		this.yStep = yStep;
		this.zStep = 0;
		
		this.n = data.length;
		this.m = data[0].length;
		this.k = 0;
		
		this.data = new double[n * m];
		
		for (int i = 0, pos = 0; i < n; i++, pos += m) {
			System.arraycopy(data[i], 0, this.data, pos, m);
		}
	}

	
	/**
	 * Creates a copy of the given data array
	 * @param data
	 */
	public DataObject_Grid(int spaceIndex, double[][][] data, 
				double xStep, double yStep, double zStep) {
		this.spaceIndex = spaceIndex;
		this.xStep = xStep;
		this.yStep = yStep;
		this.zStep = zStep;
		
		this.n = data.length;
		this.m = data[0].length;
		this.k = data[0][0].length;
		
		this.data = new double[n * m * k];
		
		for (int i = 0, pos = 0; i < n; i++) {
			for (int j = 0; j < m; j++, pos += k) {
				System.arraycopy(data[i][j], 0, this.data, pos, k);
			}
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
	 * Returns a value at the position (x,y,z)
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public double getValue(int x, int y, int z) {
		return data[(x * m + y) * k + z];
	}
	
	
	/**
	 * Returns the minimum value in the grid
	 * @return
	 */
	public double getMin() {
		if (min == null) {
			int size = data.length;
			double m = data[0];
			
			for (int i = 1; i < size; i++) {
				double val = data[i];
				if (val < m)
					m = val;
			}
			
			min = m;
		}
		
		return min;
	}
	

	/**
	 * Returns the maximum value in the grid
	 * @return
	 */
	public double getMax() {
		if (max == null) {
			int size = data.length;
			double m = data[0];
			
			for (int i = 1; i < size; i++) {
				double val = data[i];
				if (val > m)
					m = val;
			}
			
			max = m;
		}
		
		return max;
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
	 * Returns grid's z-dimension
	 * @return
	 */
	public int getZSize() {
		return k;
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
	 * Returns grid's z step value
	 * @return
	 */
	public double getZStep() {
		return zStep;
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
