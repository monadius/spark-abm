package org.spark.test.other;

import java.nio.ByteBuffer;

import javax.swing.JFrame;

import org.spark.cluster.ClusterManager;
import org.spark.cluster.Comm;
import org.spark.cluster.ObjectBuf;
import org.spark.core.ObserverFactory;

@SuppressWarnings("serial")
public class P2PMPITest extends JFrame {

	private int nextRank, prevRank;
	private int myRank;
	
	public P2PMPITest() {
		super("My Frame");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setVisible(true);

		Comm comm = ClusterManager.getInstance().getComm();
//		IntraComm comm = p2pmpi.mpi.MPI.COMM_WORLD;
		int size = comm.size();
		int rank = comm.rank();
		
		myRank = rank;
		nextRank = rank + 1;
		if (nextRank >= size)
			nextRank = 0;
		
		prevRank = rank - 1;
		if (prevRank < 0)
			prevRank = size - 1;
	}

	
	public void masterFunction() {
	}

	
/*	public void slaveFunction2(String[] args) {
		p2pmpi.mpi.MPI.Init(args);
		IntraComm comm = p2pmpi.mpi.MPI.COMM_WORLD;
		
		double[] buffer = new double[10];
		double[] buffer2 = new double[10];
		
		if (nextRank == myRank || prevRank == myRank) {
			System.out.println("At least two processes are required");
			System.exit(1);
		}
		
		for (long tick = 0; tick < 1000000000; tick++) {
//			ClusterManager.getAutoShutdown().reset();
			
			if (tick % 100 == 0) {
				System.out.println("Tick2 = " + tick);
			}

/*			double[] buffer = new double[10];
			for (int i = 0; i < 10; i++)
				buffer[i] = i;
			
			double[] buffer2 = new double[10];*/
			
/*			try {
				if (myRank % 2 == 0) {
					comm.Send(buffer, 0, 10, MPI.DOUBLE, nextRank, 0);
					comm.Recv(buffer2, 0, 10, MPI.DOUBLE, prevRank, 0);
				}
				else {
					comm.Recv(buffer2, 0, 10, MPI.DOUBLE, prevRank, 0);
					comm.Send(buffer, 0, 10, MPI.DOUBLE, nextRank, 0);
				}
				
//				comm.Barrier();
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit(2);
			}
		}
		
	}
*/	
	
	public void slaveFunction() {
		if (nextRank == myRank || prevRank == myRank) {
			System.out.println("At least two processes are required");
			System.exit(1);
		}
		
		for (long tick = 0; tick < 1000000000; tick++) {
			ClusterManager.getAutoShutdown().reset();
			
			if (tick % 100 == 0) {
				System.out.println("Tick = " + tick);
			}
			
//			Double[] buffer = new Double[10];
			Double buffer = new Double(1);
//			for (int i = 0; i < buffer.length; i++)
//				buffer[i] = new Double(i);
			
//			ObjectBuf<Double[]> buf1 = ObjectBuf.objectBuffer(buffer);
			ObjectBuf<Double> buf1 = ObjectBuf.buffer(buffer);
//			ObjectBuf<Double[]> buf2 = ObjectBuf.buffer();
			ObjectBuf<Double> buf2 = ObjectBuf.buffer();
			
			try {
				if (myRank % 2 == 0) {
					ClusterManager.getInstance().getComm().send(nextRank, 0, buf1);
					ClusterManager.getInstance().getComm().receive(prevRank, 0, buf2);
				}
				else {
					ClusterManager.getInstance().getComm().receive(prevRank, 0, buf2);
					ClusterManager.getInstance().getComm().send(nextRank, 0, buf1);
				}
				ClusterManager.getInstance().getComm().barrier();
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit(2);
			}
		}
	}

	
	public void testByteBuffer() {
		for (int i = 0; i < 1000000; i++) {
			ByteBuffer buf = ByteBuffer.allocateDirect(8192);
			buf.put((byte)32);
			
			if (i % 1000 == 0) {
				System.out.println("i = " + i);
			}
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		ObserverFactory.create(null, "org.spark.core.Observer1", 0);
		ClusterManager.init(args);

		P2PMPITest test = new P2PMPITest();

		ClusterManager.getAutoShutdown().start(5000);
		
//		if (ClusterManager.getInstance().isSlave())
			test.slaveFunction();
//		test.testByteBuffer();
//		else
//			test.masterFunction();
	}

}
