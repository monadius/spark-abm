package org.spark.test.other;

import org.spark.core.Observer;
import org.spark.core.ObserverFactory;
import org.spark.data.Grid;
import org.spark.data.GridFactory;
import org.spark.space.StandardSpace;

public class GridTest {
	public static void main(String[] args) throws Exception {
		ObserverFactory.create(null, "org.spark.core.Observer1", 0);
		Observer.getInstance().addSpace("space", new StandardSpace(0, 5, 0, 20, true, true));
		Grid grid = GridFactory.createGrid(5, 20);
		
		long start = System.currentTimeMillis();
		
		for (int t = 0; t < 5000000; t++) {
			grid.findY((double) t);
		}
		
		long end = System.currentTimeMillis();
		System.out.println(end - start);
	}
}
