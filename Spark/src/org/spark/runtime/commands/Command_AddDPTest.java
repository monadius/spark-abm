package org.spark.runtime.commands;

import org.spark.core.SparkModel;
import org.spark.runtime.internal.data.TestDataProcessor;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;

@SuppressWarnings("serial")
public class Command_AddDPTest extends ModelManagerCommand {
	public void execute(SparkModel model, AbstractSimulationEngine engine) {
		engine.addDataProcessor(new TestDataProcessor());
	}

}
