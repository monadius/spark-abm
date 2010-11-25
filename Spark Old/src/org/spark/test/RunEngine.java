package org.spark.test;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.spark.core.Agent;
import org.spark.core.ExecutionMode;
import org.spark.core.Observer;
import org.spark.core.ObserverFactory;
import org.spark.core.SparkModel;
import org.spark.math.RationalNumber;
import org.spark.modelfile.ModelFileLoader;
import org.spark.runtime.AbstractModelManager;
import org.spark.runtime.ParameterFactory_Old;
import org.spark.runtime.VariableSetFactory;
import org.spark.runtime.internal.SparkModelXMLFactory;
import org.spark.utils.RandomHelper;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

class RunEngine extends AbstractModelManager {
	/* Custom class loader */
//	private ClassLoader classLoader;
	
	/* Model xml file */
//	private File xmlDocFile;
	
	/* Model itself */
	private SparkModel model;
	
	/* The length of one tick */
	private RationalNumber tickTime;
	
	/* Types of agents in the model */
	private Class<Agent>[] agentTypes;
	
	/* Custom print stream */
	private PrintStream out;
	
	/* Data collected in the simulation */
	private Dataset data;

	/**
	 * Creates a new run engine
	 * 
	 * @param out
	 */
	public RunEngine(PrintStream out) {
		this.out = out;
	}

	@Override
	public SparkModel getModel() {
		return model;
	}

	/**
	 * Returns the path derived from the 'path' attribute of the node and the
	 * path to the model xml file
	 * 
	 * @param node
	 * @return
	 */
/*	private File getPath(Node node) {
		if (xmlDocFile == null || node == null)
			return null;

		NamedNodeMap attributes = node.getAttributes();
		Node tmp;

		String path = (tmp = attributes.getNamedItem("path")) != null ? tmp
				.getNodeValue() : null;

		if (path != null) {
			File dir = xmlDocFile.getParentFile();
			File path2 = new File(dir, path);
			return path2;
		}

		return null;
	}
*/
	/**
	 * Sets up the model class loader
	 * 
	 * @param node
	 */
/*	private void setupClassPath(Node node) {
		classLoader = null;

		File path = getPath(node);

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
	 * Returns a list of children with the specified name
	 * 
	 * @param name
	 * @return
	 */
/*	private ArrayList<Node> getChildrenByTagName(Node node, String name) {
		ArrayList<Node> list = new ArrayList<Node>();

		for (Node child = node.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			if (child.getNodeName().equals(name))
				list.add(child);
		}

		return list;
	}
*/
	
	/**
	 * Loads the definition of all agents
	 * @param node
	 */
/*	@SuppressWarnings("unchecked")
	private void loadAgents(Document xmlDoc) throws Exception {
		NodeList list = xmlDoc.getElementsByTagName("agent");
		agentTypes = new Class[list.getLength()];

		for (int i = 0; i < list.getLength(); i++) {
			NamedNodeMap attributes = list.item(i).getAttributes();
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
			String className = list.item(i).getTextContent().trim();
			if (classLoader != null) {
				agentTypes[i] = (Class<Agent>) classLoader
						.loadClass(className);
			} else {
				agentTypes[i] = (Class<Agent>) Class
						.forName(className);
			}

			// Agent's time and priority
			RationalNumber time = RationalNumber.parse(sTime);
			int priority = Integer.parseInt(sPriority);  
			
			// Add agent's definition to the Observer
			model.AddAgentType(agentTypes[i], time, priority);
		}
		
	}
*/
	
	
	/**
	 * Loads a specific model
	 * 
	 * @param modelFile
	 */
	public void loadModel(File modelFile) throws Exception {
		model = null;
		data = null;
		agentTypes = null;
		
		// Open xml file
		Document xmlDoc = ModelFileLoader.loadModelFile(modelFile); 
			
//		xmlDocFile = modelFile;
		
		model = SparkModelXMLFactory.loadModel(xmlDoc, modelFile.getParentFile());

/*		ArrayList<Node> nodes;
		NodeList list;
		Node root = xmlDoc.getFirstChild();
		
		/* Load tick size */
/*		NamedNodeMap attributes = root.getAttributes();
		Node tmp = attributes.getNamedItem("tick");
		if (tmp != null) {
			tickTime = RationalNumber.parse(tmp.getNodeValue());
		}
		else {
			tickTime = RationalNumber.ONE;
		}


		/* Load model */
		// Load class path
/*		nodes = getChildrenByTagName(root, "classpath");
		if (nodes.size() >= 1) {
			setupClassPath(nodes.get(0));
		}

		// Load main class
		nodes = getChildrenByTagName(root, "setup");
		if (nodes.size() != 1)
			throw new Exception("The setup class must be uniquely specified");

		String setupName = nodes.get(0).getTextContent().trim();
		if (classLoader != null) {
			model = (SparkModel) classLoader.loadClass(setupName).newInstance();
		} else {
			model = (SparkModel) Class.forName(setupName).newInstance();
		}
		
		/* Load agents */
/*		loadAgents(xmlDoc);		

		/* Load variables */
/*		nodes = getChildrenByTagName(root, "variables");
		if (nodes.size() > 0)
			nodes = getChildrenByTagName(nodes.get(0), "variable");

		for (int i = 0; i < nodes.size(); i++) {
			ModelVariable.createVariable(model, nodes.get(i));
		}

		/* Load parameters and variable sets */
		ParameterFactory_Old.clear();

		NodeList list = xmlDoc.getElementsByTagName("parameterframe");
		if (list.getLength() >= 1) {
			ParameterFactory_Old.loadParameters(model, list.item(0));
		}

		VariableSetFactory.clear();

		list = xmlDoc.getElementsByTagName("variable-sets");
		if (list.getLength() >= 1) {
			VariableSetFactory.loadVariableSets(model, list.item(0));
		}

		out.println();
		out.println("MODEL: " + modelFile.getName() + "; MainClass = "
				+ model.getClass().getCanonicalName());
	}
	
	
	/**
	 * Annuls all static fields in the loaded model
	 */
	@SuppressWarnings("unused")
	private void annulStaticFields() {
		if (model == null)
			return;
		
		Field[] fields = model.getClass().getFields();
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers()) &&
				!Modifier.isFinal(field.getModifiers())) {
				
				try {
					Object val = field.get(null);
					if (val == null)
						continue;
					
					if (val instanceof Number)
						field.set(null, (Number) 0);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}
	

	/**
	 * Initializes the model
	 * 
	 * @param seed
	 * @param observerName
	 * @param serialMode
	 */
	private void setupModel(long seed, String observerName, int mode)
			throws Exception {
		// Set random generator's seed
		RandomHelper.setSeed((int) seed);
		// Set observer
		ObserverFactory.create(model, "org.spark.core." + observerName, mode);

		/* Setup the model */
		// FIXME: not the best solution
		// What happens if there is a static initializer?
//		annulStaticFields();
		
		// First, synchronize model parameters
		model.synchronizeVariables();
		model.synchronizeVariables();

		// Setup is processed in serial mode always
		Observer.getInstance().beginSetup();
		model.setup();
		Observer.getInstance().finalizeSetup();

		// Synchronize variables right after setup method
		model.synchronizeVariables();
	}
	

	/**
	 * Runs a loaded model for 'length' ticks starting from the given random
	 * seed and with the given observer and in the given mode
	 * 
	 * @param length
	 * @param seed
	 * @param observerName
	 * @param serialMode
	 */
	public void run(long length, long seed, String observerName,
			int mode, boolean saveAllData) throws Exception {
		if (model == null)
			throw new Exception("Model is not loaded");
		
		// Reset collected data
		data = null;
		
		// Initialize the model
		setupModel(seed, observerName, mode);
		// Initialize the data set
		data = new Dataset((int) length, agentTypes, model.getVariables());
		
		// Save initial values of all variables
		data.update(-1);

		// Start collecting time statistics
		final long start = System.currentTimeMillis();

		// Log the run
		out.println("RUN: Observer = " + observerName + "; length = " + length
				+ "; seed = " + seed + "; mode = " + ExecutionMode.toString(mode)
				+ "; all data = " + saveAllData);

		long tick = Observer.getInstance().getSimulationTick();

		try {
			/* Main process */
			while (tick < length) {
				
				if (model.begin(tick)) {
					out.println("BREAK: model.begin(" + tick + ")");
					break;
				}
				Observer.getInstance().processAllAgents(tickTime);
//				Observer.getInstance().processAllAgents(tick);
				Observer.getInstance().processAllDataLayers(tick);

				if (model.end(tick)) {
					out.println("BREAK: model.end(" + tick + ")");
					break;
				}

				if (saveAllData) {
					model.synchronizeVariables();
					data.update(tick);
				}

				Observer.getInstance().advanceSimulationTick();
				tick = Observer.getInstance().getSimulationTick();
			}
		} catch (Exception e) {
			out.println("EXCEPTION: " + e.getMessage());
			throw e;
		} finally {
			// Save time statistics
			final long end = System.currentTimeMillis();
			final long time = end - start;

			out.println("END: time = " + time);
			
			// Save final data values
			model.synchronizeVariables();
			data.update(tick);
		}
	}
	

	/**
	 * Saves the collected data in text and binary files
	 * @param fname
	 */
	public void saveData(String fname) {
		if (data == null)
			return;
		
		File text = new File(fname + ".csv");
		File bin = new File(fname + ".dat");
		
		try {
			data.saveAsText(text);
			data.saveAsBinary(bin);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Compares two data sets
	 * @param data
	 * @return
	 */
	public boolean compareData(Dataset data) {
		if (data == null)
			return false;
		
		return this.data.equals(data); 
	}
}
