package org.spark.runtime.internal.manager;

import org.spark.runtime.commands.ModelManagerCommand;

/**
 * Very basic definition of a model manager
 * @author Monad
 *
 */
public abstract class BasicModelManager implements Runnable {
	/**
	 * Sends the command to the model manager
	 * @param cmd
	 */
	public abstract void sendCommand(ModelManagerCommand cmd);
	
	
	/**
	 * Called for each received command
	 * @param cmd
	 */
	protected abstract void acceptCommand(ModelManagerCommand cmd) throws Exception;
	
	
	/**
	 * Executes all commands sent to the manager and exits
	 */
	public abstract void runOnce();
}
