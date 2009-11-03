package org.spark.runtime.data;

@SuppressWarnings("serial")
public class DataObject_Integer extends DataObject {
	private int value;
	
	public DataObject_Integer(int val) {
		value = val;
	}
	
	
	public int getValue() {
		return value;
	}
	
	
	@Override
	public String toString() {
		return String.valueOf(value);
	}

}
