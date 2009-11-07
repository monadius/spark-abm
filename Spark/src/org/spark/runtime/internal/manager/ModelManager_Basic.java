package org.spark.runtime.internal.manager;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import org.spark.core.SparkModel;
import org.spark.core.SparkModelFactory;
import org.spark.runtime.commands.*;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;
import org.spark.runtime.internal.engine.StandardSimulationEngine;
import org.w3c.dom.Document;

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
	 * Returns the command queue of the model manager
	 * @return
	 */
	public CommandQueue getCommandQueue() {
		return commandQueue;
	}
	
	
	
	/**
	 * Returns the path derived from the 'path' attribute of the node and the
	 * path to the model xml file
	 * 
	 * @param node
	 * @return
	 */
/*	private File getPath(File rootPath, Node node) {
		if (rootPath == null || node == null)
			return null;

		NamedNodeMap attributes = node.getAttributes();
		Node tmp;

		String path = (tmp = attributes.getNamedItem("path")) != null ? tmp
				.getNodeValue() : null;

		if (path != null) {
			File path2 = new File(rootPath, path);
			return path2;
		}

		return null;
	}
*/
	
	/**
	 * Sets up the model class loader
	 * @param node
	 */
/*	private void setupClassPath(File rootPath, Node node) {
		classLoader = null;
		if (rootPath == null)
			return;

		File path = getPath(rootPath, node);

		try {
			if (path != null) {
				URI uri = path.toURI();
				classLoader = new URLClassLoader(new URL[] { uri.toURL() });
			}
		} catch (Exception e) {
			e.printStackTrace();
			classLoader = null;
		}

	}
*/
	
	
	/**
	 * Loads definitions of all agents
	 * @param node
	 */
/*	@SuppressWarnings("unchecked")
	private void loadAgents(Node root) throws Exception {
		ArrayList<Node> list = XmlDocUtils.getChildrenByTagName(root, "agents");
		// No agents
		if (list.size() < 1)
			return;

		list = XmlDocUtils.getChildrenByTagName(list.get(0), "agent");
		Class<? extends Agent> agentType;

		for (int i = 0; i < list.size(); i++) {
			NamedNodeMap attributes = list.get(i).getAttributes();
			Node tmp;

			// Agent's priority
			String sPriority = (tmp = attributes.getNamedItem("priority")) != null ?
					tmp.getNodeValue() :
					"1000";
			
			// Agent's step time
			String sTime = (tmp = attributes.getNamedItem("time")) != null ?
					tmp.getNodeValue() :
					"1";

			// Agent's class
			String className = list.get(i).getTextContent().trim();
			if (classLoader != null) {
				agentType = (Class<Agent>) classLoader
						.loadClass(className);
			} else {
				agentType = (Class<Agent>) Class
						.forName(className);
			}

			// Agent's time and priority
			RationalNumber time = RationalNumber.parse(sTime);
			int priority = Integer.parseInt(sPriority);  
			
			// Add agent's definition to the Observer
			model.AddAgentType(agentType, time, priority);
		}
		
	}
*/
	
	
	/**
	 * Loads a local model
	 * @param modelFile
	 */
	protected void loadLocalModel(Document xmlDoc, File path) throws Exception {
		model = null;
/*		String defaultObserver;
		int defaultExecutionMode;
		
		ArrayList<Node> nodes;
		Node root = xmlDoc.getFirstChild();
		
		if (root == null)
			throw new Error();
*/		
		/* Load tick size */
/*		NamedNodeMap attributes = root.getAttributes();
		Node tmp = attributes.getNamedItem("tick");
		RationalNumber tickTime;
		
		if (tmp != null) {
			tickTime = RationalNumber.parse(tmp.getNodeValue());
		}
		else {
			tickTime = RationalNumber.ONE;
		}
*/		
		/* Load model */
		// Load class path
/*		nodes = XmlDocUtils.getChildrenByTagName(root, "classpath");
		if (nodes.size() >= 1) {
			setupClassPath(path, nodes.get(0));
		}

		// Load main class
		nodes = XmlDocUtils.getChildrenByTagName(root, "setup");
		if (nodes.size() != 1)
			throw new Exception("The setup class must be uniquely specified");

		String setupName = nodes.get(0).getTextContent().trim();
		if (classLoader != null) {
			model = (SparkModel) classLoader.loadClass(setupName).newInstance();
		} else {
			model = (SparkModel) Class.forName(setupName).newInstance();
		}

		// Set model's tick time
		model.setTickTime(tickTime);
		
		defaultObserver = XmlDocUtils.getValue(nodes.get(0), "observer", "Observer1");
		defaultExecutionMode = ExecutionMode.parse(XmlDocUtils.getValue(nodes.get(0), "mode", "serial"));
		
		/* Load agents */
/*		loadAgents(xmlDoc);		

		/* Load variables */
/*		nodes = XmlDocUtils.getChildrenByTagName(root, "variables");
		if (nodes.size() > 0)
			nodes = XmlDocUtils.getChildrenByTagName(nodes.get(0), "variable");

		for (int i = 0; i < nodes.size(); i++) {
			ModelVariable.createVariable(model, nodes.get(i));
		}

*/
		model = SparkModelFactory.loadModel(xmlDoc, path);
		
		simEngine = new StandardSimulationEngine(model, commandQueue);
//		simEngine.setDefaultObserver(defaultObserver, defaultExecutionMode);
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
		if (cmd instanceof Command_LoadLocalModel) {
			Command_LoadLocalModel command = (Command_LoadLocalModel) cmd;
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(command.getModelPath());
			loadLocalModel(doc, command.getPath());
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
			logger.info("Executing command: " + cmd.getClass().getSimpleName());
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
