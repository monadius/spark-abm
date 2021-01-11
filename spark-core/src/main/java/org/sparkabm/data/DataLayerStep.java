package org.spark.data;

import java.io.Serializable;

public interface DataLayerStep extends Serializable {
	
	public double step(long tick, int x, int y, double value);
}
