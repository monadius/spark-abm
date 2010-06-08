package org.spark.test;

import java.io.File;
import java.io.PrintStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.spark.core.ExecutionMode;
import org.spark.runtime.commands.*;
import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.external.ParameterCollection;
import org.spark.runtime.internal.manager.IModelManager;
import org.spark.runtime.internal.manager.ModelManager_Basic;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class RunEngine2 {
	/* Model manager */
	private IModelManager modelManager;
	
	private ParameterCollection parameters;
	
	/* Model xml file */
//	private File xmlDocFile;

	/* Custom print stream */
	private PrintStream out;
	
	/**
	 * Creates a new run engine
	 * 
	 * @param out
	 */
	public RunEngine2(PrintStream out) {
		this.out = out;
		this.modelManager = new ModelManager_Basic();
	}

	
	/**
	 * Loads a specific model
	 * 
	 * @param modelFile
	 */
	public void loadModel(File modelFile) throws Exception {
		// Open xml file
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document xmlDoc = db.parse(modelFile);
//		xmlDocFile = modelFile;
		
		modelManager.sendCommand(new Command_LoadModel(modelFile, modelManager));
		modelManager.sendCommand(new Command_AddDPTest());

		NodeList list;
		
		/* Load parameters and variable sets */
		parameters = new ParameterCollection();

		list = xmlDoc.getElementsByTagName("parameterframe");
		if (list.getLength() >= 1) {
			parameters.loadParameters(list.item(0));
		}
		
//		ParameterCollection.sendUpdateCommands(modelManager);

		/* Collect agents */
		list = xmlDoc.getElementsByTagName("agent");
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			String name = node.getTextContent().trim();
			modelManager.sendCommand(new Command_AddDataCollector(
					new DataCollectorDescription(DataCollectorDescription.SPACE_AGENTS, name, 1)));
		}
		
		/* Collect variables */
		list = xmlDoc.getElementsByTagName("variable");
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			String name = node.getAttributes().getNamedItem("name").getNodeValue();
			modelManager.sendCommand(new Command_AddDataCollector(
					new DataCollectorDescription(DataCollectorDescription.VARIABLE, name, 1)));
		}
		

		out.println();
		out.println("MODEL: " + modelFile.getName());
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
		// Send initialization commands
		modelManager.sendCommand(new Command_SetSeed(seed, false));
		modelManager.sendCommand(new Command_Start(length, false, observerName, ExecutionMode.toString(mode)));

		// Log the run
		out.println("RUN: Observer = " + observerName + "; length = " + length
				+ "; seed = " + seed + "; mode = " + ExecutionMode.toString(mode)
				+ "; all data = " + saveAllData);
		
		modelManager.runOnce();

	}
	

}
