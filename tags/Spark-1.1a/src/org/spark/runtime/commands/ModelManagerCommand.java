package org.spark.runtime.commands;

import java.io.Serializable;

import org.spark.core.SparkModel;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;

/**
 * All interactions with a model manager are via commands
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public abstract class ModelManagerCommand implements Serializable {
	/**
	 * Command execution method
	 * @param model
	 * @param engine
	 */
	public abstract void execute(SparkModel model, AbstractSimulationEngine engine)
		throws Exception;
}
