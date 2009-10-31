package org.spark.runtime.internal.engine;

import org.spark.core.ExecutionMode;
import org.spark.core.Observer;
import org.spark.core.ObserverFactory;
import org.spark.core.SimulationTime;
import org.spark.core.SparkModel;
import org.spark.math.RationalNumber;
import org.spark.runtime.data.BadDataSourceException;
import org.spark.runtime.data.DataCollector;
import org.spark.runtime.data.DataProcessor;
import org.spark.runtime.data.DataRow;

/**
 * A standard simulation engine
 * @author Monad
 *
 */
public class StandardSimulationEngine extends AbstractSimulationEngine {
	/**
	 * Default constructor
	 * @param model
	 */
	public StandardSimulationEngine(SparkModel model) {
		super(model);
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
	private void processData(boolean paused) throws Exception {
		// Synchronize variables and methods
		model.synchronizeVariables();
		model.synchronizeMethods();

		// Get simulation time
		SimulationTime time = model.getObserver().getSimulationTime();
		
		// Create a data row
		DataRow row = new DataRow(time, paused);
		
		// Collect all data
		for (DataCollector collector : dataCollectors) {
			try {
				collector.collect(model, row, time);
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
	 * Main model simulation method
	 */
	@Override
	public void run() throws Exception {
		if (model == null)
			throw new Exception("Model is not loaded");
		
		long tick = model.getObserver().getSimulationTick();
		
		long length = this.simulationTime;
		RationalNumber tickTime = model.getTickTime();
		
		try {
			// Process data before simulation steps
			processData(false);

			/* Main process */
			while (tick < length) {
				// TODO: Get commands
				// Process commands
				
				// Make one simulation step
				if (mainStep(tickTime, tick))
					break;

				// Process data
				processData(false);

				// Advance simulation time
				model.getObserver().advanceSimulationTick();
				tick = model.getObserver().getSimulationTick();
			}
			
			// Finalize data processing
			for (DataProcessor dp : dataProcessors) {
				dp.finalizeProcessing();
			}

		} catch (Exception e) {
			throw e;
		}
	}


}
