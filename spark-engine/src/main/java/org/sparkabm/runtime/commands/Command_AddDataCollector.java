package org.sparkabm.runtime.commands;

import org.sparkabm.core.SparkModel;
import org.sparkabm.runtime.data.DataCollectorDescription;
import org.sparkabm.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Tells to add a data collector
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_AddDataCollector extends ModelManagerCommand {
	private DataCollectorDescription dcd;
	
	
	public Command_AddDataCollector(DataCollectorDescription dcd) {
		this.dcd = dcd;
	}


	@Override
	public void execute(SparkModel model, AbstractSimulationEngine engine)
			throws Exception {
		engine.addDataCollector(dcd);
	}
	
	
	@Override
	public String toString() {
		return "AddDataCollector: " + dcd.toString();
	}
}
