package org.spark.runtime.commands;

import org.spark.core.SparkModel;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;
import org.spark.utils.RandomHelper;

/**
 * Command which sets the seed of the main random number generator
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_SetSeed extends ModelManagerCommand {
	private long seed;
	private boolean useTimeSeed;
	
	public Command_SetSeed(long seed, boolean useTimeSeed) {
		this.seed = seed;
		this.useTimeSeed = useTimeSeed;
	}
	

	/**
	 * Sets the seed in the current context
	 */
	public void execute(SparkModel model, AbstractSimulationEngine engine) {
		if (useTimeSeed)
			RandomHelper.setTimeSeed();
		else
			RandomHelper.setSeed((int) seed);
	}
}
