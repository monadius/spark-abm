package org.spark.runtime.internal;

import java.util.ArrayList;

import org.spark.core.Observer;
import org.spark.core.SimulationTime;
import org.spark.core.SparkModel;
import org.spark.math.RationalNumber;
import org.spark.runtime.data.BadDataSourceException;
import org.spark.runtime.data.DataCollector;
import org.spark.runtime.data.DataProcessor;
import org.spark.runtime.data.DataRow;


/**
 * Abstract simulation engine
 * @author Monad
 */
public abstract class AbstractSimulationEngine {
	protected SparkModel model;
	
	
	protected ArrayList<DataCollector> dataCollectors;
	protected ArrayList<DataProcessor> dataProcessors;
	
	
	public AbstractSimulationEngine(SparkModel model) {
		this.model = model;
	}
	

	public void run() throws Exception {
		if (model == null)
			throw new Exception("Model is not loaded");
		
		long tick = Observer.getInstance().getSimulationTick();
		
		long length = 1;
		RationalNumber tickTime = new RationalNumber(1);

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
				
				// model.synchronizeMethods();
				model.synchronizeVariables();
				
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
				
				for (DataProcessor processor : dataProcessors) {
					try {
						processor.processDataRow(row);
					}
					catch (Exception e) {
						// TODO: process this exception
					}
				}
				
				// Process data: save, send, etc.

				Observer.getInstance().advanceSimulationTick();
				tick = Observer.getInstance().getSimulationTick();
			}

		} catch (Exception e) {
			throw e;
		}
	}

}
