package org.spark.runtime.internal.manager;

import java.io.File;

import org.spark.core.SparkModel;
import org.spark.runtime.commands.*;
import org.spark.runtime.internal.SparkModelXMLFactory;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;
import org.spark.runtime.internal.engine.StandardSimulationEngine;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.spinn3r.log5j.Logger;

/**
 * A simple implementation of the basic model manager
 * @author Monad
 *
 */
public class ModelManager_Basic implements IModelManager {
	/* Logger */
	private static final Logger logger = Logger.getLogger();
	
	
	/* Command queue */
	protected final CommandQueue commandQueue;
	
	/* If true, then the model manager stops */
	private boolean exitFlag;
	
	/* Simulation engine */
	protected AbstractSimulationEngine simEngine;
	
	/* Model itself */
	protected SparkModel model;
	
	
	/**
	 * Default constructor
	 * @param autoExit
	 */
	public ModelManager_Basic() {
		commandQueue = new CommandQueue();
		exitFlag = false;
		simEngine = null;
		model = null;
	}
	
	
	/**
	 * Returns null
	 */
	public FileTransfer createFileTransfer(Node filesNode, File xmlModelFile) {
		return null;
//		return new FileTransfer(filesNode, xmlModelFile.getParentFile());
	}
	
	
	/**
	 * Returns the command queue of the model manager
	 * @return
	 */
	public CommandQueue getCommandQueue() {
		return commandQueue;
	}
	
	
	/**
	 * Puts received commands into the command queue
	 */
	public final void sendCommand(ModelManagerCommand cmd) {
		commandQueue.put(cmd);
	}
	
	

	/**
	 * Executes the given command
	 * @param cmd
	 * @throws Exception
	 */
	protected void acceptCommand(ModelManagerCommand cmd) throws Exception {
		/* Exit command */
		if (cmd instanceof Command_Exit) {
			exitFlag = true;
			return;
		}
		
		/* Command LoadLocalModel */
		if (cmd instanceof Command_LoadModel) {
			Command_LoadModel command = (Command_LoadModel) cmd;
			Document doc = command.getModelDocument(); 
			
			File tmpDir = new File("tmp");
			model = SparkModelXMLFactory.loadModel(doc, command.getRootPath(tmpDir));
			simEngine = new StandardSimulationEngine(model, commandQueue);
			
			return;
		}
		
		/* Standard commands */
		if (model == null || simEngine == null)
			throw new Exception("Model is not loaded");
		
		cmd.execute(model, simEngine);
		
		/* Start command */
		if (cmd instanceof Command_Start) {
			boolean pausedFlag = ((Command_Start) cmd).getPausedFlag();
			simEngine.run(pausedFlag);
			return;
		}
	}
	
	
	/**
	 * Main command loop
	 */
	public final void run() {
		while (true) {
			executeNextCommand();
			
			if (exitFlag)
				break;
		}
		
		exitFlag = false;
	}
	
	
	/**
	 * Executes all commands in the queue and exits.
	 */
	public final void runOnce() {
		while (true) {
			ModelManagerCommand cmd = commandQueue.peek();
			if (cmd == null)
				break;
			
			executeNextCommand();
			
			if (exitFlag)
				break;
		}
		
		exitFlag = false;
	}
	
	
	
	/**
	 * Executes one received command.
	 * If there are no commands, then waits for a new command
	 */
	private void executeNextCommand() {
		try {
			// Receive and process commands
			ModelManagerCommand cmd = commandQueue.takeBlocking();
			logger.debug("Executing command: " + cmd.toString());
			acceptCommand(cmd);
		}
		catch (InterruptedException ie) {
			// Exit on interruption
			logger.info("Interrupted");
			exitFlag = true;
		}
		catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		
	}

}
