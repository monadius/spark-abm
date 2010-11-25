package org.spark.cluster;

import java.io.IOException;

import com.spinn3r.log5j.Logger;


class PJComm extends Comm {
	private edu.rit.pj.Comm comm;
	private static final Logger logger = Logger.getLogger(); 
	
	PJComm(edu.rit.pj.Comm comm) {
		this.comm = comm;
	}

	
	@Override
	public int rank() {
		return comm.rank();
	}
	
	@Override
	public int size() {
		return comm.size();
	}
	
	
	@Override
	public void broadcast(int root, int tag, Buf data) throws IOException {
		logger.debug("Broadcast Root = %d", comm.rank());
		comm.broadcast(root, tag, (edu.rit.mp.Buf) data.getBuffer());
	}


//	@Override
	public void receive(int fromRank, Buf buffer) throws IOException {
		comm.receive(fromRank, (edu.rit.mp.Buf) buffer.getBuffer());		
	}

	@Override
	public void receive(int fromRank, int tag, Buf buffer) throws IOException {
		comm.receive(fromRank, tag, (edu.rit.mp.Buf) buffer.getBuffer());
	}

//	@Override
	public void scatter(int root, Buf[] srcBuffers, Buf dstBuffer)
			throws IOException {
		scatter(root, 0, srcBuffers, dstBuffer);
	}

	@Override
	public void scatter(int root, int tag, Buf[] srcBuffers, Buf dstBuffer)
			throws IOException {
		if (srcBuffers == null) {
			comm.scatter(root, null, (edu.rit.mp.Buf) dstBuffer.getBuffer());
			return;
		}

		edu.rit.mp.Buf[] srcarray = new edu.rit.mp.Buf[srcBuffers.length];
		
		for (int i = 0; i < srcBuffers.length; i++)
			srcarray[i] = (edu.rit.mp.Buf) srcBuffers[i].getBuffer();
		
		comm.scatter(root, tag, srcarray, (edu.rit.mp.Buf) dstBuffer.getBuffer());
	}
	
	
	@Override
	public void gather(int root, int tag, Buf srcBuffer, Buf[] dstBuffers) 
			throws IOException{
		if (dstBuffers == null) {
			comm.gather(root, tag, (edu.rit.mp.Buf) srcBuffer.getBuffer(), null);
			return;
		}
		
		edu.rit.mp.Buf[] dstarray = new edu.rit.mp.Buf[dstBuffers.length];
		
		for (int i = 0; i < dstBuffers.length; i++) {
			dstarray[i] = (edu.rit.mp.Buf) dstBuffers[i].getBuffer();
		}
		
		comm.gather(root, tag, (edu.rit.mp.Buf) srcBuffer.getBuffer(), dstarray);
	}
	

	@Override
	public void send(int toRank, int tag, Buf buffer) throws IOException {
		comm.send(toRank, tag, (edu.rit.mp.Buf) buffer.getBuffer());
	}

//	@Override
	public void send(int toRank, Buf buffer) throws IOException {
		comm.send(toRank, (edu.rit.mp.Buf) buffer.getBuffer());
	}

	@Override
	public void sendReceive(int toRank, int sendTag, Buf sendBuf, int fromRank,
			int recvTag, Buf recvBuf) throws IOException {
		comm.sendReceive(toRank, sendTag, (edu.rit.mp.Buf) sendBuf.getBuffer(),
				fromRank, recvTag, (edu.rit.mp.Buf) recvBuf.getBuffer());
	}
	
	
	@Override
	public void barrier() throws IOException {
		comm.barrier();
	}



}
