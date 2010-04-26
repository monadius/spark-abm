package org.spark.runtime.external;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import org.spark.modelfile.ModelFileLoader;
import org.spark.runtime.commands.*;
import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.data.DataObject;
import org.spark.runtime.data.DataObject_State;
import org.spark.runtime.external.data.DataFilter;
import org.spark.runtime.external.data.DataReceiver;
import org.spark.runtime.external.data.DataSetTmp;
import org.spark.runtime.external.data.LocalDataReceiver;
import org.spark.runtime.external.gui.*;
import org.spark.runtime.external.gui.menu.SparkMenu;
import org.spark.runtime.external.gui.menu.StandardMenu;
import org.spark.runtime.external.render.DataLayerStyle;
import org.spark.runtime.external.render.Render;
import org.spark.runtime.internal.manager.IModelManager;
import org.spark.runtime.internal.manager.ModelManager_Basic;
import org.spark.utils.XmlDocUtils;

import static org.spark.utils.XmlDocUtils.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

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

	/**************** Two main components ********************/
	/* Main model manager */
	private IModelManager modelManager;
	/* Main data receiver */
	private LocalDataReceiver receiver;

	/**************** Files **********************************/
	/* Current directory */
	private File currentDir;
	
	/* Stack of output directories */
	private final Stack<File> outputDir;

	/* Loaded model xml file */
	private File modelXmlFile;
	/* Loaded model xml document */
	private Document modelXmlDoc;

	/*************** Collections *****************************/
	/* Collection of proxy variables */
	private ProxyVariableCollection variables;
	/* Collection of parameters in a loaded model */
	private ParameterCollection parameters;
	/* Collection of external methods */
	private MethodCollection methods;
	
	/* Styles of all data layers */
	private final HashMap<String, DataLayerStyle> dataLayerStyles;
	private final HashMap<String, Node> dataLayerStyleNodes;
	/* Type names and names of agents */
	private final HashMap<String, String> agentTypesAndNames;

	/********************* Model properties ********************/
	/* Random generator properties (for the next run) */
	private long randomSeed;
	private boolean useTimeSeed = true;

	/* Observer parameters (for the next run) */
	private String observerName = null;
	private String executionMode = "serial";
	
	/* Initial delay time */
	private int delayTime;
	
	/* Data set */
	private DataSetTmp dataSet;

	
	/*************** Configuration *****************/
	private final Configuration configuration;
	
	
	/*************** GUI ******************/
	/* If true, then no GUI elements are created */
	private final boolean noGUI;
	
	/* Main window manager */
	private final WindowManager windowManager;
	
	/* Control panel */
	private SparkControlPanel controlPanel;
	
	/* List of all active renders */
	private final ArrayList<Render> renders;
	
	/**
	 * Private constructor
	 * 
	 * @param manager
	 * @param receiver
	 */
	private Coordinator(IModelManager manager, LocalDataReceiver receiver, boolean noGUI) {
		this.modelManager = manager;
		this.receiver = receiver;
		this.currentDir = new File(".");
		this.outputDir = new Stack<File>();

		this.dataLayerStyles = new HashMap<String, DataLayerStyle>();
		this.dataLayerStyleNodes = new HashMap<String, Node>();
		this.agentTypesAndNames = new HashMap<String, String>();
		
		this.noGUI = noGUI;
		this.renders = new ArrayList<Render>();
		
		if (noGUI) {
			this.windowManager = null;
			this.configuration = new Configuration(null);
		}
		else {
			this.windowManager = new Swing_WindowManager();
			SparkMenu mainMenu = StandardMenu.create(windowManager);
			windowManager.setMainMenu(mainMenu);
		
			this.configuration = new Configuration(mainMenu.getSubMenu("File"));
		}
	}

	
	/**
	 * Initialization
	 */
	private void init() {
		if (!noGUI) {
			// Initilize GUI
			SparkWindow mainWindow = windowManager.getMainWindow();
			mainWindow.setName("SPARK");
		
			controlPanel = new SparkControlPanel();
			mainWindow.addPanel(controlPanel, BorderLayout.NORTH);
		}
		
		// Load config file
		configuration.readConfigFile();
	}

	/**
	 * Creates a coordinator
	 * 
	 * @param manager
	 * @param receiver
	 * @param noGUI
	 */
	public static void init(IModelManager manager, LocalDataReceiver receiver, boolean noGUI) {
		if (coordinator != null) {
			logger.error("Coordinator is already created");
			throw new Error("Illegal operation");
		}

		coordinator = new Coordinator(manager, receiver, noGUI);
		coordinator.init();
	}
	
	
	/**
	 * Creates a coordinator with GUI
	 * 
	 * @param manager
	 * @param receiver
	 */
	public static void init(IModelManager manager, LocalDataReceiver receiver) {
		init(manager, receiver, false);
	}
	
	
	/**
	 * Disposes the coordinator: saves configuration changes, etc.
	 */
	public static void dispose() {
		try {
			if (coordinator != null) {
				// TODO: wait until the simulation is completely stopped
				coordinator.unloadModel();
				coordinator.configuration.saveConfigFile();
			}
		}
		catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
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
	 * Returns configuration
	 * @return
	 */
	public Configuration getConfiguration() {
		return configuration;
	}
	
	
	/**
	 * Returns the window manager
	 * @return
	 */
	public WindowManager getWindowManager() {
		return windowManager;
	}
	
	
	/**
	 * Returns the data receiver
	 * @return
	 */
	public DataReceiver getDataReceiver() {
		return receiver;
	}
	
	
	/**
	 * Returns true if a model is loaded
	 * @return
	 */
	public synchronized boolean isModelLoaded() {
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
	 * Returns the current data set
	 * @return
	 */
	public DataSetTmp getDataSet() {
		return dataSet;
	}
	

	/**
	 * Initial properties of random generator for the next simulation
	 * 
	 * @return
	 */
	public synchronized long getRandomSeed() {
		if (receiver.getInitialState() != null)
			return receiver.getInitialState().getSeed();
		
		return randomSeed;
	}

	public synchronized boolean getTimeSeedFlag() {
		return useTimeSeed;
	}

	public synchronized void setRandomSeed(long randomSeed, boolean useTimeSeed) {
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
	public synchronized void setObserver(String observerName, String executionMode) {
		this.observerName = observerName;
		this.executionMode = executionMode;
	}

	public synchronized String getObserverName() {
		return observerName;
	}

	public synchronized String getExecutionMode() {
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
	public synchronized void setSimulationDelayTime(int time) {
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
	 * Returns all available variables
	 * @return
	 */
	public ProxyVariableCollection getVariables() {
		return variables;
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
	 * Returns the output directory
	 * @return
	 */
	public synchronized File getOutputDir() {
		if (outputDir.empty())
			return getCurrentDir();
		else
			return outputDir.peek();
	}
	
	
	/**
	 * Adds an output directory to the stack
	 * @param file
	 * @return
	 */
	public synchronized void pushOutputDir(File file) {
		outputDir.push(file);
	}
	
	
	/**
	 * Removes the last added output directory
	 */
	public synchronized void popOutputDir() {
		if (!outputDir.empty())
			outputDir.pop();
	}
	
	

	/**
	 * Loads the given model file
	 * 
	 * @param file
	 */
	public synchronized void loadModel(File modelFile) {
		try {
			if (modelXmlFile != null)
				unloadModel();

			Document xmlDoc = ModelFileLoader.loadModelFile(modelFile);
			currentDir = modelFile.getParentFile();
			
//			ModelFileLoader.saveModelFile(xmlDoc, new File("test.xml"));

			modelManager.sendCommand(new Command_LoadModel(modelFile,
					modelManager));
			modelManager.sendCommand(new Command_AddLocalDataSender(receiver));

			// Root node
			Node root = xmlDoc.getFirstChild();
			
			Node modelNode = XmlDocUtils.getChildByTagName(root, "model");
			Node interfaceNode = XmlDocUtils.getChildByTagName(root, "interface");
			
			ArrayList<Node> list;

			/* Load variables (for parameters) */
			list = XmlDocUtils.getChildrenByTagName(modelNode, "variables");
			if (list.size() >= 1) {
				variables = new ProxyVariableCollection(list.get(0));
				variables.registerVariables(receiver);
			}

			/* Load parameters and variable sets */
			parameters = new ParameterCollection();
			list = XmlDocUtils.getChildrenByTagName(interfaceNode, "parameterframe");
			if (list.size() >= 1) {
				parameters.loadParameters(list.get(0));
			}

			list = XmlDocUtils.getChildrenByTagName(interfaceNode, "variable-sets");
			if (list.size() >= 1) {
				VariableSetFactory.loadVariableSets(list.get(0));
			}
			
			/* Load methods */
			methods = new MethodCollection();
			list = XmlDocUtils.getChildrenByTagName(modelNode, "methods");
			if (list.size() >= 1) {
				methods.loadMethods(list.get(0));
			}

			
			/* Collect agents */
			agentTypesAndNames.clear();
			
			Node agents = XmlDocUtils.getChildByTagName(modelNode, "agents");
			if (agents != null) {
				list = XmlDocUtils.getChildrenByTagName(agents, "agent");
				for (int i = 0; i < list.size(); i++) {
					Node node = list.get(i);
					String typeName = node.getTextContent().trim();
					String name = getValue(node, "name", null);

					agentTypesAndNames.put(typeName, name);
				}
			}


			/* Load data layer styles */
			dataLayerStyles.clear();
			dataLayerStyleNodes.clear();

			Node dataLayersNode = XmlDocUtils.getChildByTagName(interfaceNode, "data-layers");
			list = XmlDocUtils.getChildrenByTagName(dataLayersNode, "datalayer");
			for (int i = 0; i < list.size(); i++) {
				Node node = list.get(i);
				loadDataLayer(node);
			}

			this.modelXmlFile = modelFile;
			this.modelXmlDoc = xmlDoc;
			
			
			if (interfaceNode != null) {
				loadInterface(interfaceNode);
			}
			
			configuration.addRecentProject(modelFile);
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
		
	/**
	 * Loads interface from the given interface node
	 * @param interfaceNode
	 */
	private void loadInterface(Node interfaceNode) {
		// TODO: load data set properly
		Node datasetNode = XmlDocUtils.getChildByTagName(interfaceNode, "dataset");
		if (datasetNode != null) {
			dataSet = new DataSetTmp(datasetNode);
			receiver.addDataConsumer(dataSet.getDataFilter());
		}
		
		
		if (windowManager == null)
			return;
		
		XML_WindowsLoader.loadWindows(windowManager, interfaceNode);
		
		/* Load view panels */
		ArrayList<Node> list = XmlDocUtils.getChildrenByTagName(interfaceNode, "renderframe");
		for (Node render : list) {
			new SparkViewPanel(windowManager, render, configuration.getRenderType());
		}
		
		Node mainWindowRender = XmlDocUtils.getChildByTagName(interfaceNode, "mainframe");
		if (mainWindowRender != null) {
			new SparkViewPanel(windowManager, mainWindowRender, configuration.getRenderType());
		}
		
		/* Load the parameter panel */
		Node parameterNode = XmlDocUtils.getChildByTagName(interfaceNode, "parameterframe");
		if (parameterNode != null) {
			new SparkParameterPanel(windowManager, parameterNode);
		}
		
		
		/* Load charts */
		list = XmlDocUtils.getChildrenByTagName(interfaceNode, "chart");
		// TODO: ad hoc implementation of multiple plots in one window
		HashMap<String, SparkChartPanel> chartPanels = new HashMap<String, SparkChartPanel>();
		
		for (Node chart : list) {
			String name = XmlDocUtils.getValue(chart, "name", null);
			if (name != null) {
				if (chartPanels.containsKey(name)) {
					chartPanels.get(name).addChart(chart);
					continue;
				}
			}
			
			SparkChartPanel chartPanel = new SparkChartPanel(windowManager, chart);
			receiver.addDataConsumer(chartPanel.getDataFilter());
			
			if (name != null)
				chartPanels.put(name, chartPanel);
		}
		
		/* Load methods */
		Node methodsNode = XmlDocUtils.getChildByTagName(interfaceNode, "methods-panel");
		if (methodsNode != null) {
			new SparkMethodPanel(windowManager, methodsNode, methods.getNames());
		}

		/* Load a data set panel */
		datasetNode = XmlDocUtils.getChildByTagName(interfaceNode, "dataset");
		if (datasetNode != null) {
			new SparkDatasetPanel(windowManager, datasetNode, dataSet);
		}

		
		/* Set up control panel */
		receiver.addDataConsumer(new DataFilter(controlPanel, "state"));
	}
	

	/**
	 * Starts the loaded model
	 * 
	 * @param observerName
	 * @param executionMode
	 */
	public synchronized void startLoadedModel(long simulationTime, boolean paused) {
		if (modelXmlDoc == null)
			return;

		setSimulationDelayTime(delayTime);
		modelManager.sendCommand(new Command_SetSeed(randomSeed, useTimeSeed));
		modelManager.sendCommand(new Command_Start(simulationTime, paused,
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

		// Stop a simulation
		stopSimulation();

		// Save GUI changes
		saveGUIChanges();

		// Clear variables
		dataSet = null;
		modelXmlDoc = null;
		modelXmlFile = null;

		renders.clear();
		receiver.removeAllConsumers();

		if (windowManager != null)
			windowManager.disposeAll();
	}
	
	
	/**
	 * Stops a simulation
	 */
	public synchronized void stopSimulation() {
		modelManager.sendCommand(new Command_Stop());
	}
	

	/**
	 * Loads data layer styles
	 * 
	 * @param node
	 */
	private void loadDataLayer(Node node) {
		DataLayerStyle style = DataLayerStyle.LoadXml(node);
		String name = style.getName();
		
		dataLayerStyles.put(name, style); 
		dataLayerStyleNodes.put(name, node);
	}
	
	
	/**
	 * Called whenever a model is unloading
	 * @throws Exception
	 */
	public void saveGUIChanges() {
		if (noGUI)
			return;
		
		if (modelXmlDoc == null || modelXmlFile == null)
			return;
		
		try {
			// Save data layers
			saveDataLayerStyles(modelXmlDoc);

			Node interfaceNode = XmlDocUtils.getChildByTagName(modelXmlDoc.getFirstChild(), "interface");
			XML_WindowsLoader.saveWindows(windowManager, modelXmlDoc, interfaceNode, modelXmlFile);

			
			ModelFileLoader.saveModelFile(modelXmlDoc, modelXmlFile);
//			ModelFileLoader.saveModelFile(modelXmlDoc, new File("test2.xml"));
		}
		catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	/**
	 * Saves changes of data layers
	 */
	private void saveDataLayerStyles(Document doc) {
		for (String name : dataLayerStyles.keySet()) {
			DataLayerStyle style = dataLayerStyles.get(name);
			Node node = dataLayerStyleNodes.get(name);
			
			style.SaveXml(doc, node);
		}
	}
	

	/**
	 * Creates a renderer of the given type from the given xml-node with
	 * parameters
	 * 
	 * @param node
	 * @param renderType
	 * @return
	 */
	public synchronized Render createRender(Node node, int renderType) {
		if (noGUI)
			return null;
		
		int interval = (delayTime < 0) ? -delayTime : 1;
		
		Render render = Render.createRender(node, renderType, interval, dataLayerStyles,
				agentTypesAndNames, currentDir);

		render.updateDataFilter();
		render.register(receiver);
		
		renders.add(render);

		return render;
	}
	
	
	/**
	 * Invokes the update method for all active renders
	 */
	public synchronized void updateAllRenders() {
		if (noGUI)
			return;
		
		for (Render render : renders) {
			render.update();
		}
	}
	
	
	/**
	 * Returns an array of all renders
	 */
	public synchronized Render[] getRenders() {
		Render[] result = new Render[renders.size()];
		return renders.toArray(result);
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

		Coordinator.init(manager, receiver, false);
		
		if (args.length == 1) {
			final String modelPath = args[0];
			
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						Coordinator.getInstance().loadModel(new File(modelPath));
						Coordinator.getInstance().startLoadedModel(Long.MAX_VALUE, true);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}

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
