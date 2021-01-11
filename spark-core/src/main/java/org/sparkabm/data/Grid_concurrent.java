package org.spark.data;

import org.spark.space.Space;


/**
 * Implementation of the grid for the concurrent execution mode
 * @author Monad
 *
 */
public class Grid_concurrent extends Grid {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 7481145279817269519L;

	
	/**
	 * Basic constructor
	 * @param space
	 * @param xSize
	 * @param ySize
	 */
	protected Grid_concurrent(Space space, int xSize, int ySize) {
		super(space, xSize, ySize);
	}
	
	
	@Override
	public void beginStep() {
		writeData = new double[xSize][ySize];
		
		// Create a copy of the existing data
		for (int i = 0; i < xSize; i++) {
			System.arraycopy(data[i], 0, writeData[i], 0, ySize);
		}
		
		readData = data;
	}
	
	
	@Override
	public void endStep() {
		readData = data = writeData;
	}
}
