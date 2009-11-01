package org.spark.runtime.internal.manager;

import java.util.LinkedList;

import org.spark.runtime.commands.ModelManagerCommand;


/**
 * Implements command manager.
 * All calls to methods are blocking
 * @author Monad
 *
 */
public class BlockingCommandManager extends CommandManager {
	private final LinkedList<ModelManagerCommand> buffer;
	
	public BlockingCommandManager() {
		buffer = new LinkedList<ModelManagerCommand>();
	}
	
	@Override
	public void sendCommand(ModelManagerCommand cmd) {
		synchronized (buffer) {
			buffer.add(cmd);
			buffer.notifyAll();
		}
	}
	
	
	@Override
	public boolean receiveCommands(ICommandExecutor executor) throws Exception {
		synchronized (buffer) {
			if (buffer.isEmpty())
			{
				try {
					buffer.wait();
				}
				catch (InterruptedException e) {
					// TODO: ?
					return false;
				}
			}
			
			while (!buffer.isEmpty()) {
				ModelManagerCommand cmd = buffer.poll();
				if (executor.execute(cmd))
					break;
			}
		}
		
		return true;
	}
	
	
	@Override
	public void clearCommands() {
		synchronized (buffer) {
			buffer.clear();
			buffer.notifyAll();
		}
	}

}
