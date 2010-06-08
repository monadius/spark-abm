package org.spark.cluster;

public abstract class ObjectBuf<T> extends Buf {
	static BufferFactory factory;
	
	public static <T> ObjectBuf<T> buffer(T data) {
		return factory.buffer(data);
	}
	
	
	public static <T> ObjectBuf<T> buffer() {
		return factory.buffer();
	}
	
	
	public static <T> ObjectBuf<T[]> objectBuffer(T[] data) {
		return factory.objectBuffer(data);
	}
	
	
	public abstract T get(int position);
}
