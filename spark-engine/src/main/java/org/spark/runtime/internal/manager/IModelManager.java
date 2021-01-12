package org.spark.runtime.internal.manager;

import java.io.File;

import org.spark.runtime.commands.FileTransfer;
import org.spark.runtime.commands.ModelManagerCommand;
import org.w3c.dom.Node;

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

	/**
	 * Creates a file transfer object for the manager
	 * @param filesNode
	 * @param xmlModelPath
	 * @return
	 */
	public FileTransfer createFileTransfer(Node filesNode, File xmlModelPath);
}
