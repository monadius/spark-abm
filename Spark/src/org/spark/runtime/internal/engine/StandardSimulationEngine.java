package org.spark.runtime.internal.engine;

import java.util.ArrayList;

import org.spark.core.ExecutionMode;
import org.spark.core.Observer;
import org.spark.core.ObserverFactory;
import org.spark.core.SimulationTime;
import org.spark.core.SparkModel;
import org.spark.math.RationalNumber;
import org.spark.runtime.commands.*;
import org.spark.runtime.data.DataRow;
import org.spark.runtime.internal.data.BadDataSourceException;
import org.spark.runtime.internal.data.DataCollector;
import org.spark.runtime.internal.data.DataProcessor;
import org.spark.runtime.internal.manager.IModelManager;
import org.spark.runtime.internal.manager.ICommandExecutor;
import org.spark.runtime.internal.manager.CommandQueue_NonBlocking;

import com.spinn3r.log5j.Logger;

/**
 * A standard simulation engine
 * @author Monad
 *
 */
public class StandardSimulationEngine extends AbstractSimulationEngine {
	private static final Logger logger = Logger.getLogger();
	
	/* Non-blocking command manager */
	private CommandQueue_NonBlocking commandManager;
	
	private boolean pausedFlag = false;
	private boolean stopFlag = false;
	
	/**
	 * Default constructor
	 * @param model
	 */
	public StandardSimulationEngine(SparkModel model, IModelManager manager) {
		super(model);
		this.commandManager = new CommandQueue_NonBlocking();
	}
	
	
	/**
	 * Sends a command to the engine
	 */
	public void sendCommand(ModelManagerCommand cmd) {
		commandManager.sendCommand(cmd);
	}

	
	/**
	 * Initializes the model
	 * 
	 * @param seed
	 * @param observerName
	 * @param serialMode
	 */
	@Override
	public void setup(String observerName, int executionMode)
			throws Exception {
		// Use default parameters if observerName == null
		if (observerName == null) {
			observerName = defaultObserverName;
			executionMode = defaultExecutionMode;
		}
		
		if (!ExecutionMode.isMode(executionMode))
			executionMode = defaultExecutionMode;
		
		// Set observer
		ObserverFactory.create(model, "org.spark.core." + observerName, executionMode);

		// First, synchronize variables and methods
		model.synchronizeVariables();
		model.synchronizeMethods();

		// Setup is processed in serial mode always
		model.getObserver().beginSetup();
		model.setup();
		model.getObserver().finalizeSetup();

		// Synchronize variables right after setup method
		model.synchronizeVariables();
	}
	
	
	// TODO: think about 'stateName', or 'stateFile' argument ('stateStream')
	public void loadState(String stateName, String observerName, int executionMode) {
		// Remark: it is possible to load a state and run a simulation for
		// an arbitrary observer, because a state does not depend on an observer
		// Relevant variable from the observer class are: simulationTime,
		// actionQueue, agentTypes, etc.
	}
	
	
	/**
	 * Main simulation step
	 * @param tickTime
	 * @param tick
	 */
	private boolean mainStep(RationalNumber tickTime, long tick) {
		if (model.begin(tick)) {
			return true;
		}
		Observer.getInstance().processAllAgents(tickTime);
		Observer.getInstance().processAllDataLayers(tick);

		if (model.end(tick)) {
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * Processes all data
	 * @param time
	 */
	private void processData(boolean paused, boolean specialFlag) throws Exception {
		// Synchronize variables and methods
		model.synchronizeVariables();
		model.synchronizeMethods();

		// Get simulation time
		SimulationTime time = model.getObserver().getSimulationTime();
		
		// Create a data row
		DataRow row = new DataRow(time, paused);
		ArrayList<DataCollector> collectors = dataCollectors.getActiveCollectors();
		
		// Collect all data
		for (DataCollector collector : collectors) {
			try {
				collector.collect(model, row, time, specialFlag);
			}
			catch (BadDataSourceException e) {
				// TODO: remove 'collector' from the list
				// logger.debug(e);
			}
		}
		
		// Process collected data
		for (DataProcessor processor : dataProcessors) {
			try {
				processor.processDataRow(row);
			}
			catch (Exception e) {
				// TODO: process this exception
			}
		}
		
	}
	
	
	/**
	 * Engine's command executor class
	 * @author Monad
	 *
	 */
	private class CommandExecutor implements ICommandExecutor {
		public boolean execute(ModelManagerCommand cmd) {
			logger.debug("Executing command: " + cmd.getClass().getSimpleName());
			
			if (cmd instanceof Command_PauseResume) {
				pausedFlag = !pausedFlag;
				return false;
			}
			
			if (cmd instanceof Command_SetVariableValue) {
				try {
					cmd.execute(model, StandardSimulationEngine.this);
				}
				catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}
			}
			
			if (cmd instanceof Command_Stop) {
				stopFlag = true;
				return true;
			}
			
			// Standard execution for all other commands
			try {
				cmd.execute(model, StandardSimulationEngine.this);
			}
			catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

			return false;
		}
	}
	
	
	private CommandExecutor executor = new CommandExecutor();
	

	/**
	 * Receives and processes commands
	 */
	private void processCommands() throws Exception {
		commandManager.receiveCommands(executor);
		
		// TODO: process exceptions properly
		
		if (pausedFlag) {
			while (pausedFlag) {
				// Update data after each received command
				if (commandManager.receiveCommands(executor)) {
					processData(true, true);
				}
				
				if (stopFlag)
					break;
				
				Thread.sleep(1);
			}
		}
		
		
	}
	

	/**
	 * Main model simulation method
	 */
	@Override
	public void run(boolean pausedFlag) throws Exception {
		// TODO: find a correct solution of the double stop command problem
		commandManager.clearCommands();
		
		if (model == null)
			throw new Exception("Model is not loaded");
		
		this.pausedFlag = pausedFlag;
		long tick = model.getObserver().getSimulationTick();
		long length = this.simulationTime;
		RationalNumber tickTime = model.getTickTime();
		
		try {
			// Process data before simulation steps
			processData(this.pausedFlag, true);

			/* Main process */
			while (tick < length) {
				// Receive and process commands
				processCommands();
				if (stopFlag)
					break;

				// Make one simulation step
				if (mainStep(tickTime, tick))
					break;

				// Process data
				processData(false, false);

				// Advance simulation time
				model.getObserver().advanceSimulationTick();
				tick = model.getObserver().getSimulationTick();
			}
			
			// Process all data one more time before complete simulation stop
			processData(false, true);

			
			// Finalize data processing
			ArrayList<DataCollector> collectors = dataCollectors.getActiveCollectors();

			for (DataCollector dc : collectors) {
				dc.reset();
			}
			
			for (DataProcessor dp : dataProcessors) {
				dp.finalizeProcessing();
			}

		} catch (Exception e) {
			throw e;
		}
		finally {
			stopFlag = false;
		}
	}


}
