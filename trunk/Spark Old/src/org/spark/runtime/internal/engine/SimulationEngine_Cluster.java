package org.spark.runtime.internal.engine;

import org.spark.cluster.ClusterCommand;
import org.spark.cluster.ClusterManager;
import org.spark.core.Observer;
import org.spark.core.SparkModel;
import org.spark.data.GridCommunicator;
import org.spark.math.RationalNumber;
import org.spark.runtime.internal.manager.CommandQueue;
import org.spark.space.GlobalSpace;

import com.spinn3r.log5j.Logger;

/**
 * Simulation engine for a cluster
 * @author Monad
 *
 */
public class SimulationEngine_Cluster extends StandardSimulationEngine {
	private final static Logger logger = Logger.getLogger();

	/**
	 * Default constructor
	 * @param model
	 * @param commandQueue
	 */
	public SimulationEngine_Cluster(SparkModel model, CommandQueue commandQueue) {
		super(model, commandQueue);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	/**
	 * Cluster setup method
	 */
	public void setup(String observerName, String executionMode) throws Exception {
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
	
	
	@Override
	/**
	 * Cluster main step method
	 */
	protected boolean mainStep(RationalNumber tickTime, long tick) {
		if (ClusterManager.getInstance().isSlave())
			return slaveMainStep(tickTime, tick);
		
		/* Main step on a master */
		ClusterCommand cmd = new ClusterCommand();
		if (tick % 10 == 0)
			cmd.addAction(ClusterCommand.GET_AGENTS_DATA);
		cmd.addAction(ClusterCommand.BARRIER);
		try {
			ClusterCommand.broadcastAndExecute(cmd);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	
	/**
	 * Slave's main step
	 * @param tickTime
	 * @param tick
	 * @return
	 */
	protected boolean slaveMainStep(RationalNumber tickTime, long tick) {
		/* Main step on a slave */
		int rank = ClusterManager.getInstance().getComm().rank();
		
		if (rank > 0) {
			try {
				ClusterCommand.broadcastAndExecute(null);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		logger.debug("model.begin");
		model.begin(tick);
		
		logger.debug("processAllAgents");
		Observer.getInstance().processAllAgents(tickTime);
		logger.debug("processAllDataLayers");
		Observer.getInstance().processAllDataLayers(tick);
		
		logger.debug("model.end");
		model.end(tick);

		// TODO: put it in the right place
		GridCommunicator gridCommunicator = ClusterManager.getInstance().getGridCommunicator();
		if (gridCommunicator != null) {
			logger.debug("Tick: %d; Rank: %d; calling gridCommunicator.synchronizeBorders", tick, rank);
			gridCommunicator.synchronizeBorders();
		}
		
		GlobalSpace globalSpace = ClusterManager.getInstance().getGlobalSpace();
		if (globalSpace != null) {
			logger.debug("Tick: %d; Rank: %d; calling globalSpace.sendReceiveAgents()", tick, rank);
			globalSpace.sendReceiveAgents();

			logger.debug("Tick: %d; Rank: %d; calling globalSpace.synchronizeBorders()", tick, rank);
			globalSpace.synchronizeBorders();
		}

		
		return false;
	}

}
