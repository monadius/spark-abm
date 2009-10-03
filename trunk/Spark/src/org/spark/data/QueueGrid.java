package org.spark.data;

import org.spark.core.Observer;
import org.spark.space.Space;

public class QueueGrid extends Grid {

	private static final long serialVersionUID = -6354479163943029232L;
	private int currentLine = 0;
	
	public QueueGrid(int xSize, int ySize) {
		super(Observer.getDefaultSpace(), ySize, xSize);
	}
	
	public QueueGrid(Space space, int xSize, int ySize) {
		super(space, ySize, xSize);
	}
	
	
	public void addLineWithShift(double[] line) {
		if (line != null) {
			if (currentLine < xSize) {
				data[currentLine++] = line;
			} else {
				for (int i = 1; i < xSize; i++)
					data[i - 1] = data[i];
				data[xSize - 1] = line;
			}
		}
	}
	
	

}
