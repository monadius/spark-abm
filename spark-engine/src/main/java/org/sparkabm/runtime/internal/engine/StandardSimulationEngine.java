package org.sparkabm.runtime.internal.engine;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sparkabm.core.ExecutionMode;
import org.sparkabm.core.Observer;
import org.sparkabm.core.ObserverFactory;
import org.sparkabm.math.SimulationTime;
import org.sparkabm.core.SparkModel;
import org.sparkabm.math.RationalNumber;
import org.spark.runtime.commands.*;
import org.sparkabm.runtime.commands.*;
import org.sparkabm.runtime.data.DataObject_State;
import org.sparkabm.runtime.data.DataRow;
import org.sparkabm.runtime.internal.data.BadDataSourceException;
import org.sparkabm.runtime.internal.data.DataCollector;
import org.sparkabm.runtime.internal.data.DataProcessor;
import org.sparkabm.runtime.internal.manager.CommandQueue;
import org.sparkabm.utils.FileUtils;


/**
 * A standard simulation engine
 * @author Monad
 *
 */
public class StandardSimulationEngine extends AbstractSimulationEngine {
	private static final Logger logger = LogManager.getLogger();
	
	/* Simulation flags */
	private boolean pausedFlag = false;
	private boolean stopFlag = false;
	
	/* Time when a simulation was started */
	private long startSimulationTime;
	
	/**
	 * Default constructor
	 * @param model
	 */
	public StandardSimulationEngine(SparkModel model, CommandQueue commandQueue) {
		super(model, commandQueue);
	}
	
	
	/**
	 * Initializes the model
	 * 
	 * @param seed
	 * @param observerName
	 * @param serialMode
	 */
	@Override
	public void setup(String observerName, String mode)
			throws Exception {
		// Close all open files
		FileUtils.closeAllOpenFiles();
		
		int executionMode = model.getDefaultExecutionMode();
		if (mode != null)
			executionMode = ExecutionMode.parse(mode);
		
		// Use default parameters if observerName == null
		if (observerName == null) {
			observerName = model.getDefaultObserverName();
			executionMode = model.getDefaultExecutionMode();
		}
		
		if (!ExecutionMode.isMode(executionMode))
			executionMode = model.getDefaultExecutionMode();
		
		// Set observer
		ObserverFactory.create(model, observerName, executionMode);

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
	protected boolean mainStep(RationalNumber tickTime, long tick) {
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
	private void processData(boolean paused, boolean specialFlag, 
			int flags) throws Exception {
		// Synchronize variables and methods
		model.synchronizeVariables();
		model.synchronizeMethods();

		// Get simulation time
		SimulationTime time = model.getObserver().getSimulationTime();
		
		if (paused)
			flags |= DataObject_State.PAUSED_FLAG;
		
		// Create a data row
		DataRow row = new DataRow(time, flags, startSimulationTime);
		ArrayList<DataCollector> collectors = dataCollectors.getActiveCollectors();
		
		// Collect all data
		for (DataCollector collector : collectors) {
			if (!collector.isActive())
				continue;
			
			try {
				collector.collect(model, row, time, specialFlag);
			}
			catch (BadDataSourceException e) {
				logger.error(e);
				collector.deactivate();
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
	 * Receives and processes commands
	 */
	private boolean processCommands() {
		// No commands
		if (commandQueue.peek() == null)
			return false;
		
		// Process all received commands
		while (true) {
			ModelManagerCommand cmd = commandQueue.peek();
		
			// No commands
			if (cmd == null)
				break;
		
//			logger.debug("Executing command: " + cmd.toString());

			// Process some commands in a special way
			
			// Exit
			if (cmd instanceof Command_Exit) {
				stopFlag = true;
				break;
			}
			
			// Load model
			if (cmd instanceof Command_LoadModel) {
				stopFlag = true;
				break;
			}
			
			// Start
			if (cmd instanceof Command_Start) {
				stopFlag = true;
				break;
			}
			
			
			// Regular commands
			cmd = commandQueue.take();
			
			// Stop
			if (cmd instanceof Command_Stop) {
				stopFlag = true;
				break;
			}

			// Pause/resume
			if (cmd instanceof Command_PauseResume) {
				pausedFlag = !pausedFlag;
			}
		
			// Standard execution for all other commands
			try {
				cmd.execute(model, this);
			}
			catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
		}
		
		// There were some commands
		return true;
	}
	
	
	/**
	 * Process paused state
	 */
	private void processPause() throws Exception {
		if (pausedFlag) {
			processData(true, true, 0);
		}
		
		// TODO: process exceptions properly
		while (pausedFlag) {
		// Update data after each received command
			if (processCommands()) {
				processData(true, true, 0);
			}
				
			if (stopFlag)
				break;
				
			Thread.sleep(1);
		}
	}
	

	/**
	 * Main model simulation method
	 */
	@Override
	public void run(boolean pausedFlag) throws Exception {
		if (model == null)
			throw new Exception("Model is not loaded");
		
		this.pausedFlag = pausedFlag;
		this.startSimulationTime = System.currentTimeMillis();
		long tick = model.getObserver().getSimulationTick();
		long length = this.simulationTime;
		RationalNumber tickTime = model.getTickTime();
		
		try {
			// Process data before simulation steps
			processData(this.pausedFlag, true, DataObject_State.INITIAL_STATE_FLAG);

			/* Main process */
			while (tick < length) {
				// Receive and process commands
				processCommands();
				processPause();

				if (stopFlag)
					break;

				// Get the time at the start of the next tick
				long tickStartTime = System.currentTimeMillis();
				
				// Make one simulation step
				if (mainStep(tickTime, tick))
					break;

				// Process data
				processData(false, false, 0);

				// Make a delay
				if (delayTime > 0) {
					try {
						Thread.sleep(delayTime);
					}
					catch (InterruptedException e) {
						stopFlag = true;
					}
				}

				// The length of the tick (including the above delay)
				long tickTotalTime = System.currentTimeMillis() - tickStartTime;

				// Try to follow the given frequency
				if (frequency > 0) {
					// TODO: use floating point to compute the delay more accurately,
					// accumulate errors for ticks in some variable
					long delay = 1000 / frequency;
					if (tickTotalTime < delay) {
						try {
							Thread.sleep(delay - tickTotalTime);
						}
						catch (InterruptedException e) {
							logger.error(e);
							stopFlag = true;
						}
					}
				}
				

				// Advance simulation time
				model.getObserver().advanceSimulationTick();
				tick = model.getObserver().getSimulationTick();
			}
			
			// Process all data one more time before the simulation stops
			int flags = DataObject_State.FINAL_STATE_FLAG;
			if (stopFlag)
				flags |= DataObject_State.TERMINATED_FLAG;
			
			processData(false, true, flags);

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
			FileUtils.closeAllOpenFiles();
			stopFlag = false;
		}
	}


}
