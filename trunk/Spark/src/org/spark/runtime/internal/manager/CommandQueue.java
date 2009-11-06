package org.spark.runtime.internal.manager;

import java.util.LinkedList;

import org.spark.runtime.commands.ModelManagerCommand;

/**
 * Manages commands received and sent by a model manager
 * @author Monad
 *
 */
public final class CommandQueue {
	private final LinkedList<ModelManagerCommand> buffer;
	
	/**
	 * Default constructor
	 */
	public CommandQueue() {
		buffer = new LinkedList<ModelManagerCommand>();
	}

	/**
	 * Puts a command into queue
	 * @param cmd
	 */
	public void put(ModelManagerCommand cmd) {
		synchronized (buffer) {
			buffer.add(cmd);
			buffer.notifyAll();
		}
	}
	
	
	/**
	 * Returns the first command from the queue and does not remove it from the queue
	 * @return null if the queue is empty
	 */
	public ModelManagerCommand peek() {
		synchronized (buffer) {
			return buffer.peek();
		}
	}
	
	
	/**
	 * Returns the first command from the queue and removes it
	 * @return null if the queue is empty
	 */
	public ModelManagerCommand take() {
		synchronized (buffer) {
			return buffer.poll();
		}
	}
	
	
	
	/**
	 * Returns the first command from the queue. If there are no commands,
	 * then waits until the queue is not empty 
	 * @return
	 */
	public ModelManagerCommand takeBlocking() throws InterruptedException {
		synchronized (buffer) {
			while (buffer.isEmpty()) {
				buffer.wait();
			}
			
			return buffer.poll();
		}
	}

	
	/**
	 * Clears the queue
	 */
	public void clear() {
		synchronized (buffer) {
			buffer.clear();
			buffer.notifyAll();
		}
	}

	
	
	/**
	 * Executes all commands in the queue
	 * @param executor
	 * @throws InterruptedException
	 */
	public void executeCommandsBlocking(ICommandExecutor executor) throws InterruptedException {
		synchronized (buffer) {
			while (buffer.isEmpty()) {
				buffer.wait();
			}
			
			while (!buffer.isEmpty()) {
				ModelManagerCommand cmd = buffer.poll();
				if (executor.execute(cmd))
					break;
			}
		}
	}
	
	
}
