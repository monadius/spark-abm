package org.spark.runtime.internal.manager;

import java.util.LinkedList;

import org.spark.runtime.commands.ModelManagerCommand;


/**
 * Implements command manager.
 * All calls to methods are non-blocking
 * @author Monad
 *
 */
public class NonBlockingCommandManager extends CommandManager {
	private final LinkedList<ModelManagerCommand> buffer;
	private volatile boolean isEmpty;
	
	public NonBlockingCommandManager() {
		buffer = new LinkedList<ModelManagerCommand>();
		isEmpty = true;
	}
	
	@Override
	public void sendCommand(ModelManagerCommand cmd) {
		synchronized (buffer) {
			buffer.add(cmd);
			isEmpty = false;
		}
	}
	
	
	@Override
	public boolean receiveCommands(ICommandExecutor executor) throws Exception {
		if (isEmpty)
			return false;
		
		synchronized (buffer) {
			while (!buffer.isEmpty()) {
				ModelManagerCommand cmd = buffer.poll();
				if (executor.execute(cmd))
					break;
			}
			
			if (buffer.isEmpty())
				isEmpty = true;
		}
		
		return true;
	}

}
