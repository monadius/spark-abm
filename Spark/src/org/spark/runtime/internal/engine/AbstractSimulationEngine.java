package org.spark.runtime.internal.engine;

import java.util.ArrayList;

import org.spark.core.ExecutionMode;
import org.spark.core.SparkModel;
import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.internal.data.DataCollectorManager;
import org.spark.runtime.internal.data.DataProcessor;
import org.spark.runtime.internal.manager.CommandQueue;


/**
 * Abstract simulation engine
 * @author Monad
 */
public abstract class AbstractSimulationEngine {
	/* Model manager's command queue associated with the simulation engine */
	protected final CommandQueue commandQueue;
	
	/* Model for which a simulation is running */
	protected final SparkModel model;
	
	/* Default observer parameters */
	protected String defaultObserverName = "Observer1";
	protected int defaultExecutionMode = ExecutionMode.SERIAL_MODE;
	
	/* Number of ticks for a simulation */
	protected long simulationTime = Long.MAX_VALUE;
	
	/* Manager for data collectors */
	protected final DataCollectorManager dataCollectors;
	
	/* All data processors */
	protected final ArrayList<DataProcessor> dataProcessors;
	
	
	/**
	 * Default constructor
	 * @param model
	 */
	public AbstractSimulationEngine(SparkModel model, CommandQueue commandQueue) {
		this.model = model;
		this.commandQueue = commandQueue;
		
		dataCollectors = new DataCollectorManager();
		dataProcessors = new ArrayList<DataProcessor>();
	}
	
	
	/**
	 * Sets the default observer parameters
	 * @param observerName
	 * @param executionMode
	 */
	public void setDefaultObserver(String observerName, int executionMode) {
		this.defaultObserverName = observerName;
		if (ExecutionMode.isMode(executionMode))
			this.defaultExecutionMode = executionMode;
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
	 * Adds a data collector described by dcd
	 * @param dc
	 */
	public void addDataCollector(DataCollectorDescription dcd) {
		dataCollectors.addCollector(dcd);
	}
	
	
	/**
	 * Removes the given data collector
	 * @param dcd
	 */
	public void removeDataCollector(DataCollectorDescription dcd) {
		dataCollectors.removeCollector(dcd);
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
	public abstract void run(boolean pausedFlag) throws Exception;
	
}
