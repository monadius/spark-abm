package org.spark.test.other;

public class Array2D {
	private double[]	data;
	@SuppressWarnings("unused")
	private int			sizeX, sizeY;
	
	public Array2D(int sizeX, int sizeY) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		
		this.data = new double[sizeX * sizeY];
	}
	
	
	public double get(int x, int y) {
		return data[y * sizeX + x];
	}
	
	
	public void set(int x, int y, double val) {
		data[y * sizeX + x] = val;
	}
}
