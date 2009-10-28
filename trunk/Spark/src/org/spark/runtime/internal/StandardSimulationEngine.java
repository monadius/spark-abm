package org.spark.runtime.internal;

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
		// Set observer
		ObserverFactory.create(model, "org.spark.core." + observerName, executionMode);

		// First, synchronize variables and methods
		model.synchronizeVariables();
		model.synchronizeMethods();

		// Setup is processed in serial mode always
		Observer.getInstance().beginSetup();
		model.setup();
		Observer.getInstance().finalizeSetup();

		// Synchronize variables right after setup method
		model.synchronizeVariables();
	}
	

	/**
	 * Main model simulation method
	 */
	@Override
	public void run() throws Exception {
		if (model == null)
			throw new Exception("Model is not loaded");
		
		long tick = Observer.getInstance().getSimulationTick();
		
		long length = this.simulationTime;
		RationalNumber tickTime = model.getTickTime();

		try {
			/* Main process */
			while (tick < length) {
		
				// Get commands
				// Process commands
				
				if (model.begin(tick)) {
					break;
				}
				Observer.getInstance().processAllAgents(tickTime);
				Observer.getInstance().processAllDataLayers(tick);

				if (model.end(tick)) {
					break;
				}

				// Synchronize variables and methods
				model.synchronizeVariables();
				model.synchronizeMethods();
				
				// Begin data collection
				SimulationTime time = Observer.getInstance().getSimulationTime();
				DataRow row = new DataRow(time);
				
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


				// Advance simulation time
				Observer.getInstance().advanceSimulationTick();
				tick = Observer.getInstance().getSimulationTick();
			}

		} catch (Exception e) {
			throw e;
		}
	}


}
