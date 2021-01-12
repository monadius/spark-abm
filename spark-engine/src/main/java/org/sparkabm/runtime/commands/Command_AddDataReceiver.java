package org.sparkabm.runtime.commands;

import org.sparkabm.core.SparkModel;
import org.sparkabm.runtime.data.AbstractDataReceiver;
import org.sparkabm.runtime.internal.data.LocalDataSender;
import org.sparkabm.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Adds a local data sender to the list of data processors
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_AddDataReceiver extends LocalCommand {
	// Data receiver is never serialized
	private transient AbstractDataReceiver receiver;
	
	public Command_AddDataReceiver(AbstractDataReceiver receiver) {
		this.receiver = receiver;
	}
	
	
	public void execute(SparkModel model, AbstractSimulationEngine engine) {
		engine.addDataProcessor(new LocalDataSender(receiver));
	}
}
