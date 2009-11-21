package org.spark.runtime.external;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.spark.core.ExecutionMode;
import org.spark.modelfile.ModelFileLoader;
import org.spark.runtime.commands.*;
import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.data.DataObject;
import org.spark.runtime.data.DataObject_State;
import org.spark.runtime.external.data.LocalDataReceiver;
import org.spark.runtime.external.gui.*;
import org.spark.runtime.external.gui.menu.SparkMenu;
import org.spark.runtime.external.gui.menu.StandardMenu;
import org.spark.runtime.external.render.DataLayerStyle;
import org.spark.runtime.external.render.Render;
import org.spark.runtime.internal.manager.IModelManager;
import org.spark.runtime.internal.manager.ModelManager_Basic;
import org.spark.utils.Vector;
import org.spark.utils.XmlDocUtils;

import static org.spark.utils.XmlDocUtils.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.spinn3r.log5j.Logger;

/**
 * Central external class which coordinates interactions between a model manager
 * and a data receiver
 * 
 * @author Monad
 * 
 */
public class Coordinator {
	/* Logger */
	private static final Logger logger = Logger.getLogger();

	/* A single instance of the class */
	private static Coordinator coordinator;

	/* Main model manager */
	private IModelManager modelManager;
	/* Main data receiver */
	private LocalDataReceiver receiver;

	/* Current directory */
	private File currentDir;

	/* Loaded model xml file */
	private File modelXmlFile;
	/* Loaded model xml document */
	private Document modelXmlDoc;

	/* Collection of proxy variables */
	private ProxyVariableCollection variables;
	/* Collection of parameters in a loaded model */
	private ParameterCollection parameters;
	
	/* Names of external methods */
	private final ArrayList<String> methods;
	
	/* Styles of all data layers */
	private final HashMap<String, DataLayerStyle> dataLayerStyles;
	/* Type names and names of agents */
	private final HashMap<String, String> agentTypesAndNames;

	/* Random generator properties (for the next run) */
	private long randomSeed;
	private boolean useTimeSeed = true;

	/* Observer parameters (for the next run) */
	private String observerName = null;
	private int executionMode = ExecutionMode.SERIAL_MODE;
	
	/* Initial delay time */
	private int delayTime;

	
	/*************** Configuration *****************/
	private final ConfigFile configFile;
	
	
	/*************** GUI ******************/
	private final WindowManager windowManager;
	
	/* List of all active renders */
	private final ArrayList<Render> renders;
	
	/**
	 * Private constructor
	 * 
	 * @param manager
	 * @param receiver
	 */
	private Coordinator(IModelManager manager, LocalDataReceiver receiver) {
		this.modelManager = manager;
		this.receiver = receiver;
		this.currentDir = new File(".");

		this.dataLayerStyles = new HashMap<String, DataLayerStyle>();
		this.agentTypesAndNames = new HashMap<String, String>();
		this.methods = new ArrayList<String>();
		
		this.windowManager = new Swing_WindowManager();
		SparkMenu mainMenu = StandardMenu.create(windowManager);
		windowManager.setMainMenu(mainMenu);
		
		this.renders = new ArrayList<Render>();
		
		this.configFile = new ConfigFile(mainMenu.getSubMenu("File"));
	}

	
	/**
	 * Initialization
	 */
	private void init() {
		// Initilize GUI
		SparkWindow mainWindow = windowManager.getMainWindow();
		mainWindow.setName("SPARK");
		
		SparkControlPanel controlPanel = new SparkControlPanel();
		mainWindow.addPanel(controlPanel, BorderLayout.NORTH);
		
		// Load config file
		configFile.readConfigFile();
	}

	/**
	 * Creates a coordinator
	 * 
	 * @param manager
	 * @param receiver
	 */
	public static void init(IModelManager manager, LocalDataReceiver receiver) {
		if (coordinator != null) {
			logger.error("Coordinator is already created");
			throw new Error("Illegal operation");
		}

		coordinator = new Coordinator(manager, receiver);
		coordinator.init();
	}

	
	/**
	 * Returns the instance of the class
	 * 
	 * @return
	 */
	public static Coordinator getInstance() {
		return coordinator;
	}
	
	
	/**
	 * Returns true if a model is loaded
	 * @return
	 */
	public boolean isModelLoaded() {
		return modelXmlFile != null;
	}
	
	
	/**
	 * Returns the most recently received data object of the given type
	 * @param type
	 * @param name
	 * @return
	 */
	public DataObject getMostRecentData(int type, String name) {
		return receiver.getMostRecentData(type, name);
	}
	
	
	/**
	 * Returns the initial state of the current simulation
	 * @return
	 */
	public DataObject_State getInitialState() {
		return receiver.getInitialState();
	}
	

	/**
	 * Initial properties of random generator for the next simulation
	 * 
	 * @return
	 */
	public long getRandomSeed() {
		if (receiver.getInitialState() != null)
			return receiver.getInitialState().getSeed();
		
		return randomSeed;
	}

	public boolean getTimeSeedFlag() {
		return useTimeSeed;
	}

	public void setRandomSeed(long randomSeed, boolean useTimeSeed) {
		this.randomSeed = randomSeed;
		this.useTimeSeed = useTimeSeed;
	}

	
	/**
	 * Returns visual styles for data layers
	 * @return
	 */
	public HashMap<String, DataLayerStyle> getDataLayerStyles() {
		return dataLayerStyles;
	}
	

	/**
	 * Sets parameters of the observer (for the next run)
	 * 
	 * @param observerName
	 * @param executionMode
	 */
	public void setObserver(String observerName, int executionMode) {
		this.observerName = observerName;
		this.executionMode = executionMode;
	}

	public String getObserverName() {
		return observerName;
	}

	public int getExecutionMode() {
		return executionMode;
	}

	/**
	 * Sends a command for changing value of the given variable
	 * 
	 * @param varName
	 * @param newValue
	 */
	public void changeVariable(String varName, Object newValue) {
		modelManager
				.sendCommand(new Command_SetVariableValue(varName, newValue));
	}
	
	
	/**
	 * Invokes the given external model method
	 * @param methodName
	 */
	public void invokeMethod(String methodName) {
		modelManager.sendCommand(new Command_InvokeMethod(methodName));
	}
	

	/**
	 * Adds a data collector
	 * 
	 * @param dcd
	 */
	public void addDataCollector(DataCollectorDescription dcd) {
		modelManager.sendCommand(new Command_AddDataCollector(dcd));
	}

	/**
	 * Removes a data collector
	 * 
	 * @param dcd
	 */
	public void removeDataCollector(DataCollectorDescription dcd) {
		modelManager.sendCommand(new Command_RemoveDataCollector(dcd));
	}
	
	
	/**
	 * Sets a delay time for a simulation
	 * @param time
	 */
	public void setSimulationDelayTime(int time) {
		this.delayTime = time;
		
		if (modelXmlFile != null && time >= 0) {
			modelManager.sendCommand(new Command_SetDelay(time));
			receiver.setCollectionInterval("render", 1);
		}
		
		if (time < 0) {
			receiver.setCollectionInterval("render", -time);
			if (modelXmlFile != null)
				modelManager.sendCommand(new Command_SetDelay(0));
		}
	}
	

	/**
	 * Returns a variable by its name
	 * 
	 * @param varName
	 * @return
	 */
	public ProxyVariable getVariable(String varName) {
		if (variables == null)
			return null;

		return variables.getVariable(varName);
	}

	/**
	 * Returns a collection of all parameters
	 * 
	 * @return
	 */
	public ParameterCollection getParameters() {
		return parameters;
	}

	/**
	 * Returns the current directory (the base directory of a loaded model)
	 * 
	 * @return
	 */
	public synchronized File getCurrentDir() {
		return currentDir;
	}

	/**
	 * Loads the given model file
	 * 
	 * @param file
	 */
	public synchronized void loadModel(File modelFile) throws Exception {
		try {
			if (modelXmlFile != null)
				unloadModel();

			Document xmlDoc = ModelFileLoader.loadModelFile(modelFile);
			currentDir = modelFile.getParentFile();
			
			ModelFileLoader.saveModelFile(xmlDoc, new File("test.xml"));

			modelManager.sendCommand(new Command_LoadLocalModel(modelFile,
					currentDir));
			modelManager.sendCommand(new Command_AddLocalDataSender(receiver));
			// modelManager.sendCommand(new Command_AddDataCollector(
			// new DataCollectorDescription(DataCollectorDescription.SPACES,
			// null, 1)));

			NodeList list;

			/* Load variables (for parameters) */
			list = xmlDoc.getElementsByTagName("variables");
			if (list.getLength() >= 1) {
				variables = new ProxyVariableCollection(list.item(0));
				variables.registerVariables(receiver);
			}

			/* Load parameters and variable sets */
			parameters = new ParameterCollection();
			list = xmlDoc.getElementsByTagName("parameterframe");
			if (list.getLength() >= 1) {
				parameters.loadParameters(list.item(0));
			}

			list = xmlDoc.getElementsByTagName("variable-sets");
			if (list.getLength() >= 1) {
				VariableSetFactory.loadVariableSets(list.item(0));
			}
			
			/* Load methods */
			methods.clear();
			
			list = xmlDoc.getElementsByTagName("method");
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				String name = XmlDocUtils.getValue(node, "name", null);
				String methodName = XmlDocUtils.getValue(node, "method", null);
				
				if (methodName == null)
					continue;
				
				if (name == null)
					name = methodName;
				
				methods.add(name);
			}

			/* Collect agents */
			agentTypesAndNames.clear();

			list = xmlDoc.getElementsByTagName("agent");
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				String typeName = node.getTextContent().trim();
				String name = getValue(node, "name", null);

				agentTypesAndNames.put(typeName, name);
			}

			/* Load data layer styles */
			dataLayerStyles.clear();

			list = xmlDoc.getElementsByTagName("datalayer");
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				loadDataLayer(node);

				// String name = getValue(node, "name", null);
				// TODO: space name should be also specified somehow
				// modelManager.sendCommand(new Command_AddDataCollector(
				// new
				// DataCollectorDescription(DataCollectorDescription.DATA_LAYER,
				// name, 1)));
			}

			this.modelXmlFile = modelFile;
			this.modelXmlDoc = xmlDoc;
			
			Node root = xmlDoc.getFirstChild();
			Node interfaceNode = XmlDocUtils.getChildByTagName(root, "interface");
			
			if (interfaceNode != null) {
				loadInterface(interfaceNode);
			}
			
			configFile.addRecentProject(modelFile);
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
		
	/**
	 * Loads interface from the given interface node
	 * @param interfaceNode
	 */
	private void loadInterface(Node interfaceNode) {
		XML_WindowsLoader.loadWindows(windowManager, interfaceNode);
		
		/* Load view panels */
		ArrayList<Node> list = XmlDocUtils.getChildrenByTagName(interfaceNode, "renderframe");
		for (Node render : list) {
			new SparkViewPanel(windowManager, render, Render.JAVA_2D_RENDER);
		}
		
		Node mainWindowRender = XmlDocUtils.getChildByTagName(interfaceNode, "mainframe");
		if (mainWindowRender != null) {
			new SparkViewPanel(windowManager, mainWindowRender, Render.JAVA_2D_RENDER);
		}
		
		/* Load the parameter panel */
		Node parameterNode = XmlDocUtils.getChildByTagName(interfaceNode, "parameterframe");
		if (parameterNode != null) {
			new SparkParameterPanel(windowManager, parameterNode);
		}
		
		
		/* Load charts */
		list = XmlDocUtils.getChildrenByTagName(interfaceNode, "chart");
		for (Node chart : list) {
			SparkChartPanel chartPanel = new SparkChartPanel(windowManager, chart);
			receiver.addDataConsumer(chartPanel.getDataFilter());
		}
		
		/* Load methods */
		Node methodsNode = XmlDocUtils.getChildByTagName(interfaceNode, "methods-panel");
		if (methodsNode != null) {
			new SparkMethodPanel(windowManager, methodsNode, methods);
		}
	}
	

	/**
	 * Starts the loaded model
	 * 
	 * @param observerName
	 * @param executionMode
	 */
	public synchronized void startLoadedModel() {
		if (modelXmlDoc == null)
			return;

		setSimulationDelayTime(delayTime);
		modelManager.sendCommand(new Command_SetSeed(randomSeed, useTimeSeed));
		modelManager.sendCommand(new Command_Start(Long.MAX_VALUE, true,
				observerName, executionMode));
	}
	

	/**
	 * Pauses/resumes the simulation
	 */
	public synchronized void pauseResumeLoadedModel() {
		if (modelXmlDoc == null)
			return;

		modelManager.sendCommand(new Command_PauseResume());
	}
	

	/**
	 * Unloads the current model
	 */
	public synchronized void unloadModel() {
		if (modelXmlDoc == null)
			return;

		modelManager.sendCommand(new Command_Stop());

		modelXmlDoc = null;
		modelXmlFile = null;

		renders.clear();
		receiver.removeAllConsumers();

		windowManager.disposeAll();
	}
	

	/**
	 * Loads data layer styles
	 * 
	 * @param node
	 */
	private void loadDataLayer(Node node) {
		String name = getValue(node, "name", null);
		double val1 = getDoubleValue(node, "val1", 0);
		double val2 = getDoubleValue(node, "val2", 0);
		Vector color1 = getVectorValue(node, "color1", ";", new Vector(0));
		Vector color2 = getVectorValue(node, "color2", ";", new Vector(1, 0, 0));

		dataLayerStyles.put(name, new DataLayerStyle(name, val1, val2, color1,
				color2));
		// dataLayerStyleNodes.put(name, node);
	}
	

	/**
	 * Creates a renderer of the given type from the given xml-node with
	 * parameters
	 * 
	 * @param node
	 * @param renderType
	 * @return
	 */
	public Render createRender(Node node, int renderType) {
		int interval = (delayTime < 0) ? -delayTime : 1;
		
		Render render = Render.createRender(node, renderType, interval, dataLayerStyles,
				agentTypesAndNames, currentDir);

		render.updateDataFilter();
		receiver.addDataConsumer(render.getDataFilter());
		
		renders.add(render);

		return render;
	}
	
	
	/**
	 * Invokes the update method for all active renders
	 */
	public void updateAllRenders() {
		for (Render render : renders) {
			render.update();
		}
	}
	

	/**
	 * Test main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// The first thing to do is to set up the logger
		try {
			if (new File("spark.log4j.properties").exists()) {
				PropertyConfigurator.configure("spark.log4j.properties");
			} else {
				BasicConfigurator.configure();
				logger
						.error("File spark.log4j.properties is not found: using default output streams for log information");
			}
		} catch (Exception e) {
			e.printStackTrace();
			BasicConfigurator.configure();
		}

		ModelManager_Basic manager = new ModelManager_Basic();
		LocalDataReceiver receiver = new LocalDataReceiver();

		new Thread(manager).start();

		Coordinator.init(manager, receiver);

		/*
		 * Coordinator c = Coordinator.getInstance(); c.loadModel(newFile(
		 * "c:/help/alexey/my new projects/eclipse projects/spark/tests/models/rsv/RSVModel.xml"
		 * )); c.startLoadedModel(); Thread.sleep(100); // c.loadModel(newFile(
		 * "c:/help/alexey/my new projects/eclipse projects/spark/tests/models/rsv/RSVModel.xml"
		 * )); c.loadModel(newFile(
		 * "c:/help/alexey/my new projects/eclipse projects/spark/tests/models/basic/CreateDieA.xml"
		 * )); c.startLoadedModel();
		 */
		// c.setRandomSeed(0, false);
		// c.setObserver("Observer2", ExecutionMode.CONCURRENT_MODE);
		// c.startLoadedModel();
		// manager.sendCommand(new Command_SetSeed(0, false));
		// manager.sendCommand(new Command_Start(1000, "Observer1",
		// ExecutionMode.SERIAL_MODE));
		// manager.runOnce();
		// Thread.sleep(1);
		// manager.sendCommand(new Command_Start(1000, "Observer2",
		// ExecutionMode.CONCURRENT_MODE));
		// c.startLoadedModel();
	}
}
