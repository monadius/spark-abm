package org.spark.runtime.internal.manager;

import org.spark.runtime.commands.ModelManagerCommand;

/**
 * Very basic definition of a model manager
 * @author Monad
 *
 */
public interface IModelManager extends Runnable {
	/**
	 * Sends the command to the model manager
	 * @param cmd
	 */
	public void sendCommand(ModelManagerCommand cmd);
	
	
	/**
	 * Executes all commands sent to the manager and exits
	 */
	public void runOnce();
}
