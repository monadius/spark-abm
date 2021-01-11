package org.spark.data;

import org.spark.core.ExecutionMode;
import org.spark.core.Observer;
import org.spark.space.BoundedSpace;
import org.spark.space.BoundedSpace3d;
import org.spark.space.Space;

/**
 * Creates instances of Grid and derived class
 * based on the current execution mode
 * @author Monad
 *
 */
public class GridFactory {
	/**
	 * Creates a 2d-grid of the specific size in the default space
	 * @param xSize
	 * @param ySize
	 * @return
	 */
	public static Grid createGrid(int xSize, int ySize) {
		return createGrid(Observer.getDefaultSpace(), xSize, ySize);
	}
	
	
	/**
	 * Creates a 3d-grid of the specific size in the default space
	 * @param xSize
	 * @param ySize
	 * @param zSize
	 * @return
	 */
	public static Grid3d createGrid(int xSize, int ySize, int zSize) {
		return createGrid(Observer.getDefaultSpace(), xSize, ySize, zSize);
	}


	/**
	 * Creates a 2d-grid of the specific size in the default space
	 * @param xSize
	 * @param ySize
	 * @return
	 */
	public static Grid createGrid(Space space, int xSize, int ySize) {
		if (!(space instanceof BoundedSpace)) {
			throw new Error("A 2d-grid cannot be created in the given space: " + space);
		}
		
		int mode = Observer.getInstance().getExecutionMode();
		
		switch (mode) {
		case ExecutionMode.SERIAL_MODE:
			return new Grid(space, xSize, ySize);
			
		case ExecutionMode.CONCURRENT_MODE:
			return new Grid_concurrent(space, xSize, ySize);
			
		case ExecutionMode.PARALLEL_MODE:
			return new Grid_parallel(space, xSize, ySize);
		}
		
		throw new Error("Grid cannot be created in the current execution mode: " + ExecutionMode.toString(mode));
	}
	
	
	/**
	 * Creates a 3d-grid of the specific size in the default space
	 * @param xSize
	 * @param ySize
	 * @param zSize
	 * @return
	 */
	public static Grid3d createGrid(Space space, int xSize, int ySize, int zSize) {
		if (!(space instanceof BoundedSpace3d)) {
			throw new Error("A 3d-grid cannot be created in the given space: " + space);
		}
		
		int mode = Observer.getInstance().getExecutionMode();
		
		switch (mode) {
		case ExecutionMode.SERIAL_MODE:
			return new Grid3d(space, xSize, ySize, zSize);
			
//		case ExecutionMode.CONCURRENT_MODE:
//			return new Grid3d_concurrent(space, xSize, ySize);
			
//		case ExecutionMode.PARALLEL_MODE:
//			return new Grid3d_parallel(space, xSize, ySize);
		}
		
		throw new Error("Grid cannot be created in the current execution mode: " + ExecutionMode.toString(mode));
	}
}
