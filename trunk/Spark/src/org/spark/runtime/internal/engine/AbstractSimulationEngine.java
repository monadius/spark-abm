package org.spark.runtime.internal.engine;

import java.util.ArrayList;

import org.spark.core.SparkModel;
import org.spark.runtime.data.DataCollector;
import org.spark.runtime.data.DataProcessor;


/**
 * Abstract simulation engine
 * @author Monad
 */
public abstract class AbstractSimulationEngine {
	/* Model for which a simulation is running */
	protected SparkModel model;
	
	/* Number of ticks for a simulation */
	protected long simulationTime = Long.MAX_VALUE;
	
	/* All data collectors */
	protected final ArrayList<DataCollector> dataCollectors;
	/* All data processors */
	protected final ArrayList<DataProcessor> dataProcessors;
	
	
	/**
	 * Default constructor
	 * @param model
	 */
	public AbstractSimulationEngine(SparkModel model) {
		this.model = model;
		
		dataCollectors = new ArrayList<DataCollector>();
		dataProcessors = new ArrayList<DataProcessor>();
	}
	
	
	/**
	 * Sets the length of a simulation
	 * @param time
	 */
	public void setSimulationTime(long time) {
		if (time < 0)
			time = 0;
		
		this.simulationTime = time;
	}
	
	
	/**
	 * Adds the data collector dc
	 * @param dc
	 */
	public void addDataCollector(DataCollector dc) {
		dataCollectors.add(dc);
	}
	
	
	/**
	 * Adds the data processor dp
	 * @param dp
	 */
	public void addDataProcessor(DataProcessor dp) {
		dataProcessors.add(dp);
	}
	
	
	
	/**
	 * Sets up the model
	 */
	public abstract void setup(String observerName, int executionMode)
		throws Exception;
	
	/**
	 * Main simulation method
	 * @throws Exception
	 */
	public abstract void run() throws Exception;
}
