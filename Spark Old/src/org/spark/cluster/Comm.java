package org.spark.cluster;

import java.io.IOException;


public abstract class Comm {
	private static Comm instance;
	
	
	public void finalizeMPI() {
	}
	
	
	public static void init(String[] args) throws IOException {
		// TODO: select a communicator type
//		instance = new CommFactory.PJCommFactory().createCommunicator(args);
//		ObjectBuf.factory = new BufferFactory.PJBufferFactory();
		
		instance = new CommFactory.P2PMPICommFactory().createCommunicator(args);
		ObjectBuf.factory = new BufferFactory.P2PMPIBufferFactory();

	}
	
	
	public static Comm world() {
		if (instance == null)
			throw new Error("Communicator is not initialized");
		
		return instance;
	}
	
	
	public abstract int rank();
	
	public abstract int size();
	
	
	
	public abstract void broadcast(int root, int tag, Buf data) throws IOException;
	
//	public abstract void scatter(int root, Buf[] srcBuffers, Buf dstBuffer) throws IOException;

	public abstract void scatter(int root, int tag, Buf[] srcBuffers, Buf dstBuffer) throws IOException;

	public abstract void gather(int root, int tag, Buf srcBuffer, Buf[] dstBuffers) throws IOException;
	
	public abstract void sendReceive(int toRank, int sendTag, Buf sendBuf, int fromRank, int recvTag, Buf recvBuf) throws IOException;
	
	public abstract void send(int toRank, int tag, Buf buffer) throws IOException;
	
//	public abstract void send(int toRank, Buf buffer) throws IOException;
	
//	public abstract void receive(int fromRank, Buf buffer) throws IOException;
	
	public abstract void receive(int fromRank, int tag, Buf buffer) throws IOException;
	
	public abstract void barrier() throws IOException;
}
