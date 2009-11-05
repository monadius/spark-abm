package org.spark.runtime.external;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JDialog;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.spark.core.ExecutionMode;
import org.spark.runtime.commands.*;
import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.external.data.DataFilter;
import org.spark.runtime.external.data.LocalDataReceiver;
import org.spark.runtime.external.gui.MainWindow;
import org.spark.runtime.external.gui.ParameterPanel;
import org.spark.runtime.external.gui.RenderFrame;
import org.spark.runtime.external.render.DataLayerStyle;
import org.spark.runtime.external.render.Render;
import org.spark.runtime.internal.manager.IModelManager;
import org.spark.runtime.internal.manager.ModelManager_Basic;
import org.spark.utils.Vector;

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

	/* Main window */
	private MainWindow mainWindow;
	
	private final ArrayList<JDialog> frames = new ArrayList<JDialog>();
	
	/**
	 * Private constructor
	 * 
	 * @param manager
	 * @param receiver
	 */
	private Coordinator(IModelManager manager, LocalDataReceiver receiver) {
		this.modelManager = manager;
		this.receiver = receiver;
		this.currentDir = null;
		
		dataLayerStyles = new HashMap<String, DataLayerStyle>();
		agentTypesAndNames = new HashMap<String, String>();
		
		mainWindow = new MainWindow();
		mainWindow.setVisible(true);
		
		mainWindow.setBounds(10, 10, 600, 700);
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
	 * Initial properties of random generator for the next simulation
	 * @return
	 */
	public long getRandomSeed() {
		return randomSeed;
	}
	
	
	public boolean getTimeSeedFlag() {
		return useTimeSeed;
	}
	
	
	public void setRandomSeed(long randomSeed, boolean useTimeSeed) {
		this.randomSeed = randomSeed;
		this.useTimeSeed = useTimeSeed;
	}
	
	
	public HashMap<String, DataLayerStyle> getDataLayerStyles() {
		return dataLayerStyles;
	}
	
	
	/**
	 * Sets parameters of the observer (for the next run)
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
	 * @param varName
	 * @param newValue
	 */
	public void changeVariable(String varName, Object newValue) {
		modelManager.sendCommand(new Command_SetVariableValue(varName, newValue));
	}
	
	
	/**
	 * Adds a data collector
	 * @param dcd
	 */
	public void addDataCollector(DataCollectorDescription dcd) {
		modelManager.sendCommand(new Command_AddDataCollector(dcd));
	}
	
	
	/**
	 * Removes a data collector
	 * @param dcd
	 */
	public void removeDataCollector(DataCollectorDescription dcd) {
		modelManager.sendCommand(new Command_RemoveDataCollector(dcd));
	}
	
	
	/**
	 * Returns a variable by its name
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
		
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document xmlDoc = db.parse(modelFile);
		currentDir = modelFile.getParentFile();

		modelManager.sendCommand(
				new Command_LoadLocalModel(modelFile, currentDir));
		modelManager.sendCommand(new Command_AddLocalDataSender(receiver));
//		modelManager.sendCommand(new Command_AddDataCollector(
//				new DataCollectorDescription(DataCollectorDescription.SPACES, null, 1)));

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

		
		/* Load parameter panel */
		list = xmlDoc.getElementsByTagName("parameterframe");
		if (list.getLength() >= 1) {
			// TODO: only one parameter frame should be specified
//			frame = new ParameterFrame(nodes.item(0), mainFrame);
//			frames.add(frame);
			
			JDialog dialog = new ParameterPanel(list.item(0), mainWindow);
			dialog.setVisible(true);
			frames.add(dialog);
		}
		

//		parameters.sendUpdateCommands(modelManager);

		/* Collect agents */
		agentTypesAndNames.clear();
		
		list = xmlDoc.getElementsByTagName("agent");
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			String typeName = node.getTextContent().trim();
			String name = getValue(node, "name", null);
//			modelManager.sendCommand(new Command_AddDataCollector(
//					new DataCollectorDescription(DataCollectorDescription.SPACE_AGENTS, typeName, 1)));
			
			agentTypesAndNames.put(typeName, name);
		}

		/* Collect variables */
//		list = xmlDoc.getElementsByTagName("variable");
//		for (int i = 0; i < list.getLength(); i++) {
//			Node node = list.item(i);
//			String name = node.getAttributes().getNamedItem("name")
//					.getNodeValue();
			
//			modelManager.sendCommand(new Command_AddDataCollector(
//					new DataCollectorDescription(DataCollectorDescription.VARIABLE, name, 1)));
//		}
		
		
		/* Load data layer styles */
		dataLayerStyles.clear();
		
		list = xmlDoc.getElementsByTagName("datalayer");
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			loadDataLayer(node);
			
//			String name = getValue(node, "name", null);
			// TODO: space name should be also specified somehow
//			modelManager.sendCommand(new Command_AddDataCollector(
//					new DataCollectorDescription(DataCollectorDescription.DATA_LAYER, name, 1)));
		}
		
		
		/* Load render frames */
		list = xmlDoc.getElementsByTagName("renderframe");

		for (int i = 0; i < list.getLength(); i++) {
			JDialog dialog = new RenderFrame(list.item(i), mainWindow, Render.JAVA_2D_RENDER);
			dialog.setVisible(true);
			frames.add(dialog);
		}		
		
		
		/* Main frame */
		list = xmlDoc.getElementsByTagName("mainframe");
		Node node = list.item(0);
		
		DataFilter df = new DataFilter(mainWindow);
//		df.setInterval(100);
//		df.setSynchronizedFlag(true);
		receiver.addDataConsumer(df);
		mainWindow.setupRender(node);
		
		this.modelXmlFile = modelFile;
		this.modelXmlDoc = xmlDoc;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Starts the loaded model
	 * @param observerName
	 * @param executionMode
	 */
	public synchronized void startLoadedModel() {
		if (modelXmlDoc == null)
			return;

		// TODO: verify that the model is not running now
		modelManager.sendCommand(new Command_SetSeed(randomSeed, useTimeSeed));
		modelManager.sendCommand(new Command_Start(Long.MAX_VALUE, true,
				observerName, executionMode));
	}
	
	
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
		
		receiver.removeAllConsumers();
		for (JDialog frame : frames) {
			frame.dispose();
		}
		
		frames.clear();
	}
	
	
	/**
	 * Loads data layer styles
	 * @param node
	 */
	private void loadDataLayer(Node node) {
		String name = getValue(node, "name", null);
		double val1 = getDoubleValue(node, "val1", 0);
		double val2 = getDoubleValue(node, "val2", 0);
		Vector color1 = getVectorValue(node, "color1", ";", new Vector(0));
		Vector color2 = getVectorValue(node, "color2", ";", new Vector(1, 0, 0));
		
		dataLayerStyles.put(name, new DataLayerStyle(name, val1, val2,
					color1, color2));
//		dataLayerStyleNodes.put(name, node);
	}
	
	
	
	/**
	 * Creates a renderer of the given type from the given xml-node
	 * with parameters
	 * @param node
	 * @param renderType
	 * @return
	 */
	public Render createRender(Node node, int renderType) {
		Render render = Render.createRender(node, renderType, 
				dataLayerStyles, agentTypesAndNames, currentDir);

		render.updateDataFilter();
		receiver.addDataConsumer(render.getDataFilter());
		
		return render;
	}
	
	
	/**
	 * Test main method
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// The first thing to do is to set up the logger
		try {
			if (new File("spark.log4j.properties").exists()) {
				PropertyConfigurator.configure("spark.log4j.properties");
			} else {
				BasicConfigurator.configure();
				logger.error("File spark.log4j.properties is not found: using default output streams for log information");
			}
		} catch (Exception e) {
			e.printStackTrace();
			BasicConfigurator.configure();
		}

		
		ModelManager_Basic manager = new ModelManager_Basic();
		LocalDataReceiver receiver = new LocalDataReceiver();
		
		new Thread(manager).start();
		
		Coordinator.init(manager, receiver);
		
/*		Coordinator c = Coordinator.getInstance();
		c.loadModel(new File("c:/help/alexey/my new projects/eclipse projects/spark/tests/models/rsv/RSVModel.xml"));
		c.startLoadedModel();
		Thread.sleep(100);
//		c.loadModel(new File("c:/help/alexey/my new projects/eclipse projects/spark/tests/models/rsv/RSVModel.xml"));
		c.loadModel(new File("c:/help/alexey/my new projects/eclipse projects/spark/tests/models/basic/CreateDieA.xml"));
		c.startLoadedModel();
*/	
//		c.setRandomSeed(0, false);
//		c.setObserver("Observer2", ExecutionMode.CONCURRENT_MODE);
//		c.startLoadedModel();
//		manager.sendCommand(new Command_SetSeed(0, false));
//		manager.sendCommand(new Command_Start(1000, "Observer1", ExecutionMode.SERIAL_MODE));
//		manager.runOnce();
//		Thread.sleep(1);
//		manager.sendCommand(new Command_Start(1000, "Observer2", ExecutionMode.CONCURRENT_MODE));
//		c.startLoadedModel();
	}
}
