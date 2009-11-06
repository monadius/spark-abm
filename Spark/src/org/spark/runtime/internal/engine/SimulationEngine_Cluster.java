package org.spark.runtime.internal.engine;

import org.spark.cluster.ClusterManager;
import org.spark.core.SparkModel;
import org.spark.runtime.internal.manager.CommandQueue;

public class SimulationEngine_Cluster extends StandardSimulationEngine {

	public SimulationEngine_Cluster(SparkModel model, CommandQueue commandQueue) {
		super(model, commandQueue);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public void setup(String observerName, int executionMode) throws Exception {
		if (!ClusterManager.getInstance().isSlave()) {
			// Master node
			super.setup(observerName, executionMode);
			ClusterManager.getInstance().sendInitCommands(model);
		}
		else {
			// Slave node
			ClusterManager.getInstance().waitInitCommands(model);
		}
	}

}
