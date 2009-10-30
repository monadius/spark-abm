package org.spark.runtime.commands;

import org.spark.core.SparkModel;
import org.spark.runtime.data.LocalDataSender;
import org.spark.runtime.external.data.LocalDataReceiver;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Adds a local data sender to the list of data processors
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_AddLocalDataSender extends LocalCommand {
	private LocalDataReceiver receiver;
	
	public Command_AddLocalDataSender(LocalDataReceiver receiver) {
		this.receiver = receiver;
	}
	
	
	public void execute(SparkModel model, AbstractSimulationEngine engine) {
		engine.addDataProcessor(new LocalDataSender(receiver));
	}
}
