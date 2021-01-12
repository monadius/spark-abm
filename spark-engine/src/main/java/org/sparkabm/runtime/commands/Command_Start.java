package org.sparkabm.runtime.commands;

import org.sparkabm.core.SparkModel;
import org.sparkabm.runtime.internal.engine.AbstractSimulationEngine;

/**
 * A command for starting a simulation
 * @author Monad
 */
@SuppressWarnings("serial")
public class Command_Start extends ModelManagerCommand {
	private long simulationTime;
	private boolean paused;
	private String observerName;
	private String executionMode;
	
	
	public Command_Start(long simulationTime, boolean paused, String observerName, String mode) {
		this.simulationTime = simulationTime;
		this.paused = paused;
		this.observerName = observerName;
		this.executionMode = mode;
	}
	
	
	/**
	 * A copy constructor
	 * @param cmd
	 */
	public Command_Start(Command_Start cmd) {
		this(cmd.simulationTime, cmd.paused, cmd.observerName, cmd.executionMode);
	}
	

	/**
	 * Executes the command on the given simulation engine
	 * @param engine
	 */
	public void execute(SparkModel model, AbstractSimulationEngine engine) throws Exception {
		engine.setSimulationTime(simulationTime);
		engine.setup(observerName, executionMode);
	}
	
	
	public boolean getPausedFlag() {
		return paused;
	}
	
	
	@Override
	public String toString() {
		return "Start[time = " + simulationTime + 
			"; paused = " + paused + 
			"; observer = " + observerName + 
			"; mode = " + executionMode +
			"]";
	}

}
