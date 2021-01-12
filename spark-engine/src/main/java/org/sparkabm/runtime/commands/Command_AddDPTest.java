package org.sparkabm.runtime.commands;

import org.sparkabm.core.SparkModel;
import org.sparkabm.runtime.internal.data.TestDataProcessor;
import org.sparkabm.runtime.internal.engine.AbstractSimulationEngine;

@SuppressWarnings("serial")
public class Command_AddDPTest extends ModelManagerCommand {
	public void execute(SparkModel model, AbstractSimulationEngine engine) {
		engine.addDataProcessor(new TestDataProcessor());
	}

}
