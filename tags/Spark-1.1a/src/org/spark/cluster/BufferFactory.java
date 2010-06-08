package org.spark.cluster;

abstract class BufferFactory {
	abstract public <T> ObjectBuf<T> buffer(T data);
	
	abstract public <T> ObjectBuf<T> buffer();
	
	abstract public <T> ObjectBuf<T[]> objectBuffer(T[] data);
	
	/**
	 * Produces pj buffers
	 * @author Monad
	 *
	 */
	static class PJBufferFactory extends BufferFactory {
		@Override
		public <T> ObjectBuf<T> buffer(T data) {
			return new PJObjectBuffer<T>(edu.rit.mp.ObjectBuf.buffer(data));
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> ObjectBuf<T> buffer() {
			return new PJObjectBuffer<T>((edu.rit.mp.ObjectBuf<T>) edu.rit.mp.ObjectBuf.buffer());
		}

		@Override
		public <T> ObjectBuf<T[]> objectBuffer(T[] data) {
			return new PJObjectBuffer<T[]>(edu.rit.mp.ObjectBuf.objectBuffer(data));
		}

	}
	
	
	/**
	 * Produces raw buffers
	 * @author Monad
	 *
	 */
	static class P2PMPIBufferFactory extends BufferFactory {

		@Override
		public <T> ObjectBuf<T> buffer(T data) {
			return new P2PMPIObjectBuffer<T>(data);
		}

		@Override
		public <T> ObjectBuf<T> buffer() {
			return new P2PMPIObjectBuffer<T>();
		}

		@Override
		public <T> ObjectBuf<T[]> objectBuffer(T[] data) {
			return new P2PMPIObjectBuffer<T[]>(data);
		}
		
	}
	
	
	/**
	 * Aggregates pj ObjectBuf class
	 * @author Monad
	 *
	 * @param <T>
	 */
	static class PJObjectBuffer<T> extends ObjectBuf<T> {
		edu.rit.mp.ObjectBuf<T> buffer;

		PJObjectBuffer(edu.rit.mp.ObjectBuf<T> buffer) {
			this.buffer = buffer;
		}
		
		@Override
		public T get(int position) {
			return buffer.get(position);
		}

		@Override
		public Object getBuffer() {
			return buffer;
		}
		
	}
	
	
	/**
	 * Aggregates raw objects
	 * @author Monad
	 *
	 * @param <T>
	 */
	static class P2PMPIObjectBuffer<T> extends ObjectBuf<T> {
		private T[] buffer;
		
		@SuppressWarnings("unchecked")
		P2PMPIObjectBuffer() {
			// FIXME: create a buffer
			buffer = (T[]) new Object[1];
		}
		
		@SuppressWarnings("unchecked")
		P2PMPIObjectBuffer(T data) {
			buffer = (T[]) new Object[1];
			buffer[0] = data;
		}
		
		@Override
		public T get(int position) {
			return buffer[position];
		}

		@Override
		public Object getBuffer() {
			return buffer;
		}
		
	}
}
