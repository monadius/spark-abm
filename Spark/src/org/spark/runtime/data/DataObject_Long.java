package org.spark.runtime.data;

@SuppressWarnings("serial")
public class DataObject_Long extends DataObject {
	private long value;
	
	public DataObject_Long(long val) {
		value = val;
	}
	
	
	public long getValue() {
		return value;
	}
	
	
	@Override
	public String toString() {
		return String.valueOf(value);
	}

}