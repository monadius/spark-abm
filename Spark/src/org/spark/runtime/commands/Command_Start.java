package org.spark.runtime.commands;

import org.spark.core.SparkModel;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;

/**
 * A command for starting a simulation
 * @author Monad
 */
@SuppressWarnings("serial")
public class Command_Start extends ModelManagerCommand {
	private long simulationTime;
	private String observerName;
	private int executionMode;
	
	
	public Command_Start(long simulationTime, String observerName, int mode) {
		this.simulationTime = simulationTime;
		this.observerName = observerName;
		this.executionMode = mode;
	}
	

	/**
	 * Executes the command on the given simulation engine
	 * @param engine
	 */
	public void execute(SparkModel model, AbstractSimulationEngine engine) throws Exception {
		engine.setSimulationTime(simulationTime);
		engine.setup(observerName, executionMode);
	}
}
