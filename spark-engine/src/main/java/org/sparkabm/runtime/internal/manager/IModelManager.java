package org.sparkabm.runtime.internal.manager;

import java.io.File;

import org.sparkabm.runtime.commands.FileTransfer;
import org.sparkabm.runtime.commands.ModelManagerCommand;
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
