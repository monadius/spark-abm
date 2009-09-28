package org.spark.cluster;

import java.io.IOException;


abstract class CommFactory {
	public abstract Comm createCommunicator(String[] args) throws IOException;
	
	
	static class PJCommFactory extends CommFactory {
		@Override
		public Comm createCommunicator(String[] args) throws IOException {
			edu.rit.pj.Comm.init(args);

			return new PJComm(edu.rit.pj.Comm.world());
		}
	}
	
	
	static class P2PMPICommFactory extends CommFactory {
		@Override
		public Comm createCommunicator(String[] args) throws IOException {
			return new P2PMPIComm(args);
		}

	}
}
