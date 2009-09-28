package org.spark.cluster;

import java.io.IOException;

import com.spinn3r.log5j.Logger;

import p2pmpi.mpi.MPI;

public class P2PMPIComm extends Comm {
	private static final Logger logger = Logger.getLogger();
	private p2pmpi.mpi.IntraComm comm;

	
	P2PMPIComm(String[] args) {
		logger.info("P2PMPI Comm is initilized");
		MPI.Init(args);
		comm = MPI.COMM_WORLD;
	}
	
	
	@Override
	public void finalizeMPI() {
		logger.info("MPI is being finalized");
		MPI.Finalize();
	}
	
	
	@Override
	protected void finalize() throws Throwable {
		finalizeMPI();
		super.finalize();
	}
	

	@Override
	public int rank() {
		return comm.Rank();
	}

	
	@Override
	public int size() {
		return comm.Size();
	}


	
	@Override
	public void broadcast(int root, int tag, Buf data) throws IOException {
		// FIXME: no tagged version?
		logger.debug("Comm Rank: %d broadcast, Tag: %d", comm.Rank(), tag);
		comm.Bcast(data.getBuffer(), 0, 1, MPI.OBJECT, root);
	}


//	@Override
	public void receive(int fromRank, Buf buffer) throws IOException {
		receive(fromRank, 0, buffer);
	}

	@Override
	public void receive(int fromRank, int tag, Buf buffer) throws IOException {
		logger.debug("Comm Rank: %d receive(tag = %d, fromRank = %d)", comm.Rank(), tag, fromRank);
		comm.Recv(buffer.getBuffer(), 0, 1, MPI.OBJECT, fromRank, tag);
	}

	@SuppressWarnings("unchecked")
//	@Override
	public void scatter(int root, Buf[] srcBuffers, Buf dstBuffer)
			throws IOException {
		
		Object[] sendBuffer = null;
		
		if (srcBuffers != null) {
			sendBuffer = new Object[srcBuffers.length];
		
			for (int i = 0; i < srcBuffers.length; i++)
				sendBuffer[i] = ((ObjectBuf) srcBuffers[i]).get(0);
		}

		logger.debug("Comm Rank: %d scatter", comm.Rank());
		comm.Scatter(sendBuffer, 0, 1, MPI.OBJECT, dstBuffer.getBuffer(), 0, 1, MPI.OBJECT, root);
	}
	
	
	@Override
	public void gather(int root, int tag, Buf srcBuffer, Buf[] dstBuffers) {
		Object[] recvBuffer = null;
		
		if (dstBuffers != null) {
			recvBuffer = new Object[dstBuffers.length];
		}
		
		logger.debug("Comm Rank: %d gather, Tag: %d", comm.Rank(), tag);
		// TODO: no tagged version?
		comm.Gather(srcBuffer.getBuffer(), 0, 1, MPI.OBJECT, recvBuffer, 0, 1, MPI.OBJECT, root);
		
		if (dstBuffers != null) {
			for (int i = 0; i < dstBuffers.length; i++) {
				((Object[]) dstBuffers[i].getBuffer())[0] = recvBuffer[i];
			}
		}
	}
	

	@Override
	public void scatter(int root, int tag, Buf[] srcBuffers, Buf dstBuffer)
			throws IOException {
		// TODO: no tagged version?
		scatter(root, srcBuffers, dstBuffer);
	}

	@Override
	public void send(int toRank, int tag, Buf buffer) throws IOException {
		logger.debug("Comm Rank: %d send(tag = %d, toRank = %d)", comm.Rank(), tag, toRank);
		comm.Send(buffer.getBuffer(), 0, 1, MPI.OBJECT, toRank, tag);
	}

//	@Override
	public void send(int toRank, Buf buffer) throws IOException {
		send(toRank, 0, buffer);
	}

	@Override
	public void sendReceive(int toRank, int sendTag, Buf sendBuf, int fromRank,
			int recvTag, Buf recvBuf) throws IOException {
		logger.debug("CommRank: %d sendReceive(toRank = %d, sendTag = %d, " +
				"fromRank = %d, recvTag = %d)", comm.Rank(), toRank, sendTag, fromRank, recvTag);

		comm.Sendrecv(sendBuf.getBuffer(), 0, 1, MPI.OBJECT, toRank, sendTag, recvBuf.getBuffer(), 0, 1, MPI.OBJECT, fromRank, recvTag);
	}
	
	
	public void barrier() {
		logger.debug("CommRank: %d barrier()", comm.Rank());
		comm.Barrier();
	}

}
