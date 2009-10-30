package org.spark.runtime.external;

import java.awt.BorderLayout;
import java.io.File;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.spark.core.ExecutionMode;
import org.spark.runtime.commands.*;
import org.spark.runtime.external.data.LocalDataReceiver;
import org.spark.runtime.external.render.DataLayerStyle;
import org.spark.runtime.external.render.Render;
import org.spark.runtime.internal.manager.BasicModelManager;
import org.spark.runtime.internal.manager.SimpleModelManager;
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

	private BasicModelManager modelManager;
	private LocalDataReceiver receiver;
	private File currentDir;
	
	private final HashMap<String, DataLayerStyle> dataLayerStyles;
	private final HashMap<String, String> agentTypesAndNames;

	/**
	 * Private constructor
	 * 
	 * @param manager
	 * @param receiver
	 */
	private Coordinator(BasicModelManager manager, LocalDataReceiver receiver) {
		this.modelManager = manager;
		this.receiver = receiver;
		this.currentDir = null;
		
		dataLayerStyles = new HashMap<String, DataLayerStyle>();
		agentTypesAndNames = new HashMap<String, String>();
	}

	/**
	 * Creates a coordinator
	 * 
	 * @param manager
	 * @param receiver
	 */
	public static void init(BasicModelManager manager, LocalDataReceiver receiver) {
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
	 * Returns the current directory (the base directory of a loaded model)
	 * 
	 * @return
	 */
	public File getCurrentDir() {
		return currentDir;
	}

	/**
	 * Loads the given model file
	 * 
	 * @param file
	 */
	public void loadModel(File modelFile) throws Exception {
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document xmlDoc = db.parse(modelFile);
		currentDir = modelFile.getParentFile();

		modelManager.sendCommand(
				new Command_LoadLocalModel(xmlDoc, currentDir));
		modelManager.sendCommand(new Command_AddLocalDataSender(receiver));
		modelManager.sendCommand(new Command_AddDCSpaces(1));

		NodeList list;

		/* Load parameters and variable sets */
		ParameterFactory.clear();

		list = xmlDoc.getElementsByTagName("parameterframe");
		if (list.getLength() >= 1) {
			ParameterFactory.loadParameters(list.item(0));
		}

		ParameterFactory.sendUpdateCommands(modelManager);

		/* Collect agents */
		agentTypesAndNames.clear();
		
		list = xmlDoc.getElementsByTagName("agent");
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			String typeName = node.getTextContent().trim();
			String name = getValue(node, "name", null);
			modelManager.sendCommand(new Command_AddDCSpaceAgents(typeName, 1));
			
			agentTypesAndNames.put(typeName, name);
		}

		/* Collect variables */
		list = xmlDoc.getElementsByTagName("variable");
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			String name = node.getAttributes().getNamedItem("name")
					.getNodeValue();
			modelManager.sendCommand(new Command_AddDCVariable(name, 1));
		}
		
		
		/* Load data layer styles */
		dataLayerStyles.clear();
		
		list = xmlDoc.getElementsByTagName("datalayer");
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			loadDataLayer(node);
		}
		
		
		/* Main frame */
		list = xmlDoc.getElementsByTagName("mainframe");
		Node node = list.item(0);
		
		
		JFrame frame = new TestWindow();
		Render render = createRender(node, Render.JAVA_2D_RENDER);
		
		frame.getContentPane().add(render.getCanvas(), BorderLayout.CENTER);
		frame.pack();
		frame.setBounds(10, 10, 500, 500);
		frame.setVisible(true);
 
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
	
	
	private Render createRender(Node node, int renderType) {
		Render render = Render.createRender(node, renderType, 
				dataLayerStyles, agentTypesAndNames, currentDir);
		
		receiver.addDataConsumer(render);
		
		return render;
	}
	
	
	
	class TestWindow extends JFrame {
		public TestWindow() {
			setTitle("Test");
			setDefaultCloseOperation(EXIT_ON_CLOSE);
		}
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

		
		SimpleModelManager manager = new SimpleModelManager();
		LocalDataReceiver receiver = new LocalDataReceiver();
		
		new Thread(manager).start();
		
		Coordinator.init(manager, receiver);
		
		Coordinator c = Coordinator.getInstance();
		c.loadModel(new File("c:/help/alexey/my new projects/eclipse projects/spark/tests/models/rsv/RSVModel.xml"));
		
		manager.sendCommand(new Command_SetSeed(0, false));
		manager.sendCommand(new Command_Start(1000, "Observer1", ExecutionMode.SERIAL_MODE));
//		manager.runOnce();
		Thread.sleep(1);
		manager.sendCommand(new Command_Start(1000, "Observer2", ExecutionMode.CONCURRENT_MODE));
	}
}
