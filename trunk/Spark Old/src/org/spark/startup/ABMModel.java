package org.spark.startup;

public interface ABMModel {
	public void setup();
	
	public boolean begin(long tick);
	
	public boolean end(long tick);
}
