package org.spark.runtime.internal.manager;

import org.spark.runtime.commands.ModelManagerCommand;

/**
 * Executes a given command
 * @author Monad
 *
 */
public interface ICommandExecutor {
	/**
	 * Executes the given command.
	 * If the return value is true, then a command manager
	 * will not proceed with other commands in a command queue
	 * @param cmd
	 * @return
	 */
	public boolean execute(ModelManagerCommand cmd);
}
