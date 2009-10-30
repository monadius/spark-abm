package org.spark.runtime.internal.manager;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import org.spark.core.Agent;
import org.spark.core.SparkModel;
import org.spark.math.RationalNumber;
import org.spark.runtime.commands.Command_LoadLocalModel;
import org.spark.runtime.commands.Command_Start;
import org.spark.runtime.commands.Command_String;
import org.spark.runtime.commands.ModelManagerCommand;
import org.spark.runtime.internal.ModelVariable;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;
import org.spark.runtime.internal.engine.StandardSimulationEngine;
import org.spark.utils.XmlDocUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.spinn3r.log5j.Logger;

/**
 * A simple implementation of the basic model manager
 * @author Monad
 *
 */
public class SimpleModelManager extends BasicModelManager {
	/* Logger */
	private static final Logger logger = Logger.getLogger();
	
	
	/* Command manager */
	private final CommandManager commandManager = new BlockingCommandManager();
	
	/* If true, then the execution stops */
	private boolean exitFlag = false;
	
	/* If true, then the simulation should be started */
	private boolean startFlag = false;
	
	/* Simulation engine */
	private AbstractSimulationEngine simEngine;
	
	/* Custom class loader */
	private ClassLoader classLoader;
	
	/* Model itself */
	private SparkModel model;
	
	
	/**
	 * Default constructor
	 * @param autoExit
	 */
	public SimpleModelManager() {
	}

	
	/**
	 * Returns the path derived from the 'path' attribute of the node and the
	 * path to the model xml file
	 * 
	 * @param node
	 * @return
	 */
	private File getPath(File rootPath, Node node) {
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

	
	/**
	 * Sets up the model class loader
	 * @param node
	 */
	private void setupClassPath(File rootPath, Node node) {
		classLoader = null;

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

	
	
	/**
	 * Loads definitions of all agents
	 * @param node
	 */
	@SuppressWarnings("unchecked")
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

	
	
	/**
	 * Loads a local model
	 * @param modelFile
	 */
	public void loadLocalModel(Document xmlDoc, File path) throws Exception {
		model = null;
		
		ArrayList<Node> nodes;
		Node root = xmlDoc.getFirstChild();
		
		/* Load tick size */
		NamedNodeMap attributes = root.getAttributes();
		Node tmp = attributes.getNamedItem("tick");
		RationalNumber tickTime;
		
		if (tmp != null) {
			tickTime = RationalNumber.parse(tmp.getNodeValue());
		}
		else {
			tickTime = RationalNumber.ONE;
		}
		
		/* Load model */
		// Load class path
		nodes = XmlDocUtils.getChildrenByTagName(root, "classpath");
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

		model.setTickTime(tickTime);
		
		/* Load agents */
		loadAgents(xmlDoc);		

		/* Load variables */
		nodes = XmlDocUtils.getChildrenByTagName(root, "variables");
		if (nodes.size() > 0)
			nodes = XmlDocUtils.getChildrenByTagName(nodes.get(0), "variable");

		for (int i = 0; i < nodes.size(); i++) {
			ModelVariable.createVariable(model, nodes.get(i));
		}

		/* Load parameters and variable sets */
/*		ParameterFactory.clear();

		list = xmlDoc.getElementsByTagName("parameterframe");
		if (list.getLength() >= 1) {
			ParameterFactory.loadParameters(list.item(0));
		}

		VariableSetFactory.clear();

		list = xmlDoc.getElementsByTagName("variable-sets");
		if (list.getLength() >= 1) {
			VariableSetFactory.loadVariableSets(list.item(0));
		}
*/
		
		simEngine = new StandardSimulationEngine(model);
	}
	

	@Override
	public void sendCommand(ModelManagerCommand cmd) {
		commandManager.sendCommand(cmd);
	}
	
	
	@Override
	protected void acceptCommand(ModelManagerCommand cmd) throws Exception {
		/* Command_String */
		if (cmd instanceof Command_String) {
			String command = ((Command_String) cmd).getCommand().intern();
//			String arg = null;
			
//			if (cmd instanceof Command_StringWithArgument)
//				arg = ((Command_StringWithArgument) cmd).getArgument();
			
			if (command == "exit") {
				exitFlag = true;
			}
			
			return;
		}
		
		/* Command LoadLocalModel */
		if (cmd instanceof Command_LoadLocalModel) {
			Command_LoadLocalModel command = (Command_LoadLocalModel) cmd;
			loadLocalModel(command.getXmlDoc(), command.getPath());
			
			return;
		}
		
		/* Standard commands */
		if (model == null || simEngine == null)
			throw new Exception("Model is not loaded");
		
		cmd.execute(model, simEngine);
		
		/* Start command */
		if (cmd instanceof Command_Start) {
			startFlag = true;
		}
	}
	
	
	/**
	 * Main command loop
	 */
	public void run() {
		while (true) {
			runOnce();
			
			if (exitFlag)
				break;
		}
		
		exitFlag = false;
		startFlag = false;
	}
	
	
	
	/**
	 * Executes all received commands
	 */
	public void runOnce() {
		try {
			// Receive and preprocess commands
			commandManager.receiveCommands(this);
				
			if (startFlag) {
				simEngine.run();
				startFlag = false;
			}
		}
		catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		
	}

}
