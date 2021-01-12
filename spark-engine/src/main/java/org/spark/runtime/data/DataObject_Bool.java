package org.spark.runtime.data;

@SuppressWarnings("serial")
public class DataObject_Bool extends DataObject {
	private boolean value;
	
	public DataObject_Bool(boolean val) {
		value = val;
	}
	
	
	public boolean getValue() {
		return value;
	}
	
	
	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
