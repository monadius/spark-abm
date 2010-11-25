package org.spark.test.other;

import p2pmpi.mpi.*;

public class SimpleP2PMPITest {
	public static void main(String[] args) {
		MPI.Init(args);
		
		while (true) {
			MPI.COMM_WORLD.Barrier();
		}
	}
}
