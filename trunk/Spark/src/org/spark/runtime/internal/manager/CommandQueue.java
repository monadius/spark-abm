package org.spark.runtime.internal.manager;

import org.spark.runtime.commands.ModelManagerCommand;

/**
 * Manages commands received and sent by a model manager
 * @author Monad
 *
 */
public abstract class CommandQueue {
	public abstract void sendCommand(ModelManagerCommand cmd);
	
	/**
	 * Returns true if some commands were received
	 * @param manager
	 * @return
	 * @throws Exception
	 */
	public abstract boolean receiveCommands(ICommandExecutor executor) throws Exception;
	
	
	/**
	 * Removes all commands from a command queue
	 */
	public abstract void clearCommands();
}
