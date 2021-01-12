package org.sparkabm.runtime.commands;

import org.sparkabm.core.SparkModel;
import org.sparkabm.runtime.data.DataCollectorDescription;
import org.sparkabm.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Tells to remove a data collector
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_RemoveDataCollector extends ModelManagerCommand {
	private DataCollectorDescription dcd;
	
	
	public Command_RemoveDataCollector(DataCollectorDescription dcd) {
		this.dcd = dcd;
	}


	@Override
	public void execute(SparkModel model, AbstractSimulationEngine engine)
			throws Exception {
		engine.removeDataCollector(dcd);
	}
	
	@Override
	public String toString() {
		return "RemoveDataCollector: " + dcd.toString();
	}
}
