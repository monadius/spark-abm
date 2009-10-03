package org.spark.data;

import org.spark.core.ExecutionMode;
import org.spark.core.Observer;
import org.spark.space.Space;

/**
 * Creates instances of Grid and derived class
 * based on the current execution mode
 * @author Monad
 *
 */
public class GridFactory {
	/**
	 * Creates a grid of the specific size in the default space
	 * @param xSize
	 * @param ySize
	 * @return
	 */
	public static Grid createGrid(int xSize, int ySize) {
		return createGrid(Observer.getDefaultSpace(), xSize, ySize);
	}


	/**
	 * Creates a grid of the specific size in the default space
	 * @param xSize
	 * @param ySize
	 * @return
	 */
	public static Grid createGrid(Space space, int xSize, int ySize) {
		int mode = Observer.getInstance().getExecutionMode();
		
		switch (mode) {
		case ExecutionMode.SERIAL_MODE:
			return new Grid(space, xSize, ySize);
			
		case ExecutionMode.CONCURRENT_MODE:
			return new Grid_concurrent(space, xSize, ySize);
		}
		
		throw new Error("Grid cannot be created in the current execution mode: " + ExecutionMode.toString(mode));
	}
}
