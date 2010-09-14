package org.spark.runtime.data;

@SuppressWarnings("serial")
public class DataObject_Double extends DataObject {
	private double value;
	
	public DataObject_Double(double val) {
		value = val;
	}
	
	
	public double getValue() {
		return value;
	}
	
	
	@Override
	public String toString() {
		return String.valueOf(value);
	}

}