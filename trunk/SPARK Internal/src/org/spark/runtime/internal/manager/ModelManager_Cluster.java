package org.spark.runtime.internal.manager;

import java.io.File;
import java.util.HashSet;

import org.spark.cluster.ClusterManager;
import org.spark.cluster.Comm;
import org.spark.cluster.ObjectBuf;
import org.spark.core.SparkModel;
import org.spark.runtime.commands.*;
import org.spark.runtime.internal.SparkModelXMLFactory;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;
import org.spark.runtime.internal.engine.SimulationEngine_Cluster;
import org.w3c.dom.Document;

import com.spinn3r.log5j.Logger;


/**
 * A model manager for the cluster version of SPARK
 * @author Monad
 */
public class ModelManager_Cluster extends ModelManager_Basic {
	/* Logger */
	private static final Logger logger = Logger.getLogger();


	/**
	 * A special command which tells slaves to wait for load model commands
	 * @author Monad
	 */
	@SuppressWarnings("serial")
	private static class Command_LoadModelOnSlave extends ModelManagerCommand {
		@Override
		public void execute(SparkModel model, AbstractSimulationEngine engine)
				throws Exception {
		}
	}
	
	
	@SuppressWarnings("serial")
	private static class Command_StartOnSlave extends Command_Start {
		public Command_StartOnSlave(Command_Start originalCommand) {
			super(originalCommand);
		}
	}

	@SuppressWarnings("serial")
	private static class Command_StopOnSlave extends ModelManagerCommand {
		@Override
		public void execute(SparkModel model, AbstractSimulationEngine engine)
				throws Exception {
		}
	}

	
	@SuppressWarnings("serial")
	private static class Command_PauseResumeOnSlave extends ModelManagerCommand {
		@Override
		public void execute(SparkModel model, AbstractSimulationEngine engine)
				throws Exception {
		}
	}

	
	/**
	 * Creates a model manager for the current node in a cluster
	 * @param args
	 */
	public ModelManager_Cluster(String[] args) throws Exception {
		logger.info("Initializing the cluster manager...");
		ClusterManager.init(args);
		logger.info("Done");
		
		// Create a special MPI command receiver for all slaves
		if (ClusterManager.getInstance().isSlave()) {
			createMPICommandReceiver();
		}
	}
	
	
	
	/**
	 * Creates a special MPI command receiver
	 */
	private void createMPICommandReceiver() {
		new Thread(new Runnable() {
			public void run() {
				Comm c = ClusterManager.getInstance().getComm();
				ObjectBuf<ModelManagerCommand> cmdBuf = ObjectBuf.buffer();
				
				while (true) {
					try {
						// Receive a command
						c.broadcast(0, ClusterManager.CMD_DATA, cmdBuf);
					}
					catch (Exception e) {
						logger.error(e);
					}
				
					// Put the received command into the command queue
					sendCommand(cmdBuf.get(0));
				}
			}
		}).start();
	}
	
	
	// TODO: change type to Command_Cluster
	private void broadcastCommand(ModelManagerCommand cmd) throws Exception {
		Comm c = ClusterManager.getInstance().getComm();
		ObjectBuf<ModelManagerCommand> cmdBuf = ObjectBuf.buffer(cmd);
		
		c.broadcast(0, ClusterManager.CMD_DATA, cmdBuf);
	}
	
	

	// Sets of acceptable commands
	private final static HashSet<Class<? extends ModelManagerCommand>> masterAcceptedCommands =
		new HashSet<Class<? extends ModelManagerCommand>>();

	private final static HashSet<Class<? extends ModelManagerCommand>> slaveAcceptedCommands =
		new HashSet<Class<? extends ModelManagerCommand>>();

	
	static {
		masterAcceptedCommands.add(Command_AddDataCollector.class);
		masterAcceptedCommands.add(Command_AddDataProcessor.class);
		masterAcceptedCommands.add(Command_AddLocalDataSender.class);
		masterAcceptedCommands.add(Command_LoadModel.class);
		masterAcceptedCommands.add(Command_PauseResume.class);
		masterAcceptedCommands.add(Command_RemoveDataCollector.class);
		masterAcceptedCommands.add(Command_SetSeed.class);
		masterAcceptedCommands.add(Command_SetVariableValue.class);
		masterAcceptedCommands.add(Command_Start.class);
		masterAcceptedCommands.add(Command_Stop.class);
		
		slaveAcceptedCommands.add(Command_AddDataCollector.class);
		slaveAcceptedCommands.add(Command_AddDataProcessor.class);
		slaveAcceptedCommands.add(Command_AddLocalDataSender.class);
		slaveAcceptedCommands.add(Command_LoadModelOnSlave.class);
		slaveAcceptedCommands.add(Command_PauseResumeOnSlave.class);
		slaveAcceptedCommands.add(Command_RemoveDataCollector.class);
		slaveAcceptedCommands.add(Command_SetSeed.class);
		slaveAcceptedCommands.add(Command_SetVariableValue.class);
		slaveAcceptedCommands.add(Command_StartOnSlave.class);
		slaveAcceptedCommands.add(Command_StopOnSlave.class);
	}
	
	

	/**
	 * Accepts cluster specific commands
	 */
	@Override
	protected void acceptCommand(ModelManagerCommand cmd) throws Exception {
		if (!ClusterManager.getInstance().isSlave()) {
			// Master node commands
			
			// Ignore some commands
			if (!masterAcceptedCommands.contains(cmd)) {
				logger.error("Received an unacceptable command: " + cmd);
				return;
			}
			
			// Load model
			if (cmd instanceof Command_LoadModel) {
				Command_LoadModel loadCommand = (Command_LoadModel) cmd;

				File tmpDir = new File("tmp");
				loadModelMaster(loadCommand.getModelDocument(), loadCommand.getRootPath(tmpDir));
				return;
			}
			
			// Start model
			if (cmd instanceof Command_Start) {
				// Broadcast this command
				Command_Start command = (Command_Start) cmd;
				broadcastCommand(new Command_StartOnSlave(command));
				super.acceptCommand(cmd);
				return;
			}
		}
		else {
			// Slave node commands

			// Ignore some commands
			if (!slaveAcceptedCommands.contains(cmd)) {
				logger.error("Received an unacceptable command: " + cmd);
				return;
			}
			
			if (cmd instanceof Command_LoadModelOnSlave) {
				loadModelSlave();
				return;
			}
			
			// Ignore this command on slaves
			if (cmd instanceof Command_LoadModel) {
				return;
			}
		}
		
		super.acceptCommand(cmd);
	}
	
	

	/**
	 * Loads a model on a master node
	 * @param xmlModelFile
	 */
	private void loadModelMaster(Document xmlDoc, File rootPath) throws Exception {
		// Load model locally
		model = SparkModelXMLFactory.loadModel(xmlDoc, rootPath);
		
		// Create a simulation engine
		simEngine = new SimulationEngine_Cluster(model, commandQueue);
		
		// Broadcast the model description to all slaves
		broadcastCommand(new Command_LoadModelOnSlave());
		ClusterManager.getInstance().sendModelDescription(xmlDoc);
	}
	
	
	
	/**
	 * Loads a model on a slave node 
	 */
	private void loadModelSlave() throws Exception {
		Document doc = ClusterManager.getInstance().receiveModelDescription();
		model = SparkModelXMLFactory.loadModel(doc, null);

		// Create a simulation engine
		simEngine = new SimulationEngine_Cluster(model, commandQueue);
	}
}
