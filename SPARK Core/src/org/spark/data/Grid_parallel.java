package org.spark.data;

import org.spark.math.Function;
import org.spark.space.Space;
import org.spark.utils.Vector;

/**
 * Implementation of the grid for the parallel execution mode
 * @author Monad
 *
 */
public class Grid_parallel extends Grid_concurrent {
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
	protected Grid_parallel(Space space, int xSize, int ySize) {
		super(space, xSize, ySize);
	}
	

	// TODO: think about synchronization for this method
	public Vector[][] getColors(double val1, double val2, Vector color1, Vector color2) {
		return super.getColors(val1, val2, color1, color2);
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

		synchronized (writeData) {
			writeData[x][y] += value;
		}

		return readData[x][y];
	}
	
	
	public void setValue(Vector p, double value) {
		int x = findX(p.x);
		int y = findY(p.y);

		// FIXME: this function should not work in the parallel mode
		// or collision resolution should be implemented
		
		synchronized (writeData) {
			writeData[x][y] = value;
		}
	}
	
	
	public void setValue(double value) {
		synchronized (writeData) {
			for (int i = 0; i < xSize; i++)
				for (int j = 0; j < ySize; j++)
					writeData[i][j] = value;
		}
	}
	
	
	public void setValue(Function f) {
		Vector v = new Vector();
		v.x = xMin + xStep / 2;
		
		synchronized (writeData) {
			for (int i = 0; i < xSize; i++, v.x += xStep) {
				v.y = yMin + yStep / 2;

				for (int j = 0; j < ySize; j++, v.y += yStep) {
					writeData[i][j] = f.getValue(v);
				}
			}
		}
	}

	//************************************
	// AdvancedDataLayer interface implementation
	//************************************
	
	public void multiply(double value) {
		int xSize2 = xSize - xBorder;
		int ySize2 = ySize - yBorder;
		
		synchronized (writeData) {
			for (int i = xBorder; i < xSize2; i++)
				for (int j = yBorder; j < ySize2; j++)
					writeData[i][j] *= value;
		}
	}
	
	
	public void add(double value) {
		int xSize2 = xSize - xBorder;
		int ySize2 = ySize - yBorder;
		
		synchronized (writeData) {
			for (int i = xBorder; i < xSize2; i++)
				for (int j = yBorder; j < ySize2; j++)
					writeData[i][j] += value;
		}
	}
	
	
	
	public void setValue(int x, int y, double value) {
		synchronized (writeData) {
			writeData[x][y] = value;
		}
	}
	

	/**
	 * Does nothing
	 */
	public void beginStep() {
		// TODO: probably, we don't need to synchronize data here,
		// because this method is always called from the same thread
		// Synchronize all cached data
		synchronized (readData) {
			synchronized (writeData) {
				super.beginStep();
			}
		}
	}
	

	/**
	 * Does nothing
	 */
	public void endStep() {
		// Synchronize all cached data
		synchronized (readData) {
			synchronized (writeData) {
				super.endStep();
			}
		}
	}

}
