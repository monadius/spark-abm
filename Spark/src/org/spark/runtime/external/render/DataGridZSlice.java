package org.spark.runtime.external.render;

import org.spark.runtime.data.DataObject_Grid;

/**
 * Represents a Z-slice of a 3-dimensional grid data
 * @author Alexey
 *
 */
public class DataGridZSlice extends DataObject_Grid {
	private static final long serialVersionUID = 1L;
	
	private final int z;
	
	/**
	 * Creates a Z-slice of the given grid data along the plane Z = z
	 * @param grid
	 * @param z
	 */
	public DataGridZSlice(DataObject_Grid grid, double z, double zMin, double zMax) throws Exception {
		// Create a shallow copy of the existing grid
		super(grid);
		
		if (grid.getZSize() <= 0)
			throw new Exception("Cannot take a slice of a 2-dimensional grid");

		// Compute the slize index
		double zStep = grid.getZStep();
		z -= zMin;
		z *= 1.0 / zStep;
		
		int zz = (int) Math.floor(z);
		if (zz < 0)
			zz = 0;
		else if (zz >= grid.getZSize())
			zz = grid.getZSize() - 1;
		
		this.z = zz;
	}
	
	
	@Override
	public double getValue(int x, int y) {
		return getValue(x, y, z);
	}
}
