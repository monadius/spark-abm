package org.spark.runtime.external.tools;

import java.io.File;
import java.util.ArrayList;

//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.util.Log;

import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.Parameter;
import org.spark.runtime.external.ParameterCollection;
import org.spark.runtime.external.batchrun.BatchRunController;
import org.spark.runtime.external.batchrun.BatchRunManager;
import org.spark.runtime.external.batchrun.ParameterSweep;
import org.spark.runtime.external.data.DataReceiver;
import org.sparkabm.runtime.internal.manager.ModelManager_Basic;
import org.sparkabm.utils.XmlDocUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Model description
 * 
 * @author Alexey
 * 
 */
class Model {
	private static final Logger logger = LogManager.getLogger();

	private final File modelFile;
	private final ArrayList<Batch> batches;

	/**
	 * Loads a model description from the given xml node
	 * 
	 * @param node
	 */
	public Model(Node node, File basePath) throws Exception {
		String path = XmlDocUtils.getValue(node, "path", null);

		if (path == null)
			throw new Exception(
					"Attribute 'path' does not exist for a 'model' node");

		modelFile = new File(basePath, path);
		if (!modelFile.exists())
			throw new Exception("The model file " + modelFile
					+ " does not exist");

		// Load batches
		ArrayList<Node> bb = XmlDocUtils.getChildrenByTagName(node, "batch");
		batches = new ArrayList<Batch>(bb.size());

		for (Node b : bb) {
			Batch batch = new Batch(b);
			batches.add(batch);
		}
	}

	/**
	 * Runs all batches for the model
	 */
	public void run() {
		logger.info("Model: " + modelFile);

		int i = 1;
		for (Batch batch : batches) {
			logger.info("Batch " + i + " of " + batches.size());

			// Load model
			Coordinator.getInstance().loadModel(modelFile);

			if (!Coordinator.getInstance().isModelLoaded()) {
				logger.error("Model cannot be loaded");
				break;
			}

			// Start a batch run
			batch.start();

			i++;
		}

	}

}

/**
 * Batch description
 * 
 * @author Alexey
 * 
 */
class Batch {
	private static final Logger logger = LogManager.getLogger();

	private final long ticks;
	private final int repetitions;
	private final boolean saveData;
	private final String dataFile;
	
	private final boolean finalSnapshot;
	private final int snapshotInterval;

	private final ArrayList<ParameterInfo> parameters;
	
	private final int dataLayerInterval;
	private final int dataLayerPrecision;
	private final boolean dataLayerOneFile;
	private final String[] dataLayerNames;

	private final Object lock = new Object();

	/**
	 * Description of a parameter
	 * 
	 * @author Alexey
	 * 
	 */
	private static class ParameterInfo {
		private final String name;
		private final double start;
		private final double end;
		private final double step;

		/**
		 * Loads a parameter from the given node
		 * 
		 * @param node
		 */
		public ParameterInfo(Node node) throws Exception {
			name = XmlDocUtils.getValue(node, "name", null);
			if (name == null)
				throw new Exception("Undefined parameter name");

			start = XmlDocUtils.getDoubleValue(node, "start", 0);
			end = XmlDocUtils.getDoubleValue(node, "end", 0);
			step = XmlDocUtils.getDoubleValue(node, "step", 1);
		}

		/**
		 * Adds the parameter to the sweep controller
		 * 
		 * @param sweep
		 */
		public void addParameter(ParameterSweep sweep) {
			ParameterCollection pc = Coordinator.getInstance().getParameters();
			Parameter p = pc.getParameter(name); 

			if (p == null) {
				logger.error("Undefined parameter " + name);
				return;
			}

			sweep.addParameter(p, start, end, step);
		}
	}

	/**
	 * Creates a batch from the given xml node
	 * 
	 * @param node
	 */
	public Batch(Node node) {
		ticks = XmlDocUtils.getIntegerValue(node, "ticks", 100);
		repetitions = XmlDocUtils.getIntegerValue(node, "repetitions", 1);
		
		saveData = XmlDocUtils.getBooleanValue(node, "save-data", true);
		dataFile = XmlDocUtils.getValue(node, "data-file", "data");

		finalSnapshot = XmlDocUtils.getBooleanValue(node, "final-snapshot", true);
		snapshotInterval = XmlDocUtils.getIntegerValue(node, "snapshot-interval", 0);

		// Load parameters
		ArrayList<Node> pars = XmlDocUtils.getChildrenByTagName(node,
				"parameter");
		parameters = new ArrayList<ParameterInfo>(pars.size());

		for (Node p : pars) {
			try {
				ParameterInfo pInfo = new ParameterInfo(p);
				parameters.add(pInfo);
			} catch (Exception e) {
				logger.error(e);
			}
		}
		
		// Load data layers
		Node grids = XmlDocUtils.getChildByTagName(node, "datalayers");
		
		if (grids != null) {
			dataLayerInterval = XmlDocUtils.getIntegerValue(grids, "interval", 1);
			dataLayerPrecision = XmlDocUtils.getIntegerValue(grids, "precision", 5);
			dataLayerOneFile = XmlDocUtils.getBooleanValue(grids, "one-file", true);
			
			String text = grids.getTextContent();
			String[] tmp = text.split(",");
			dataLayerNames = new String[tmp.length];

			for (int i = 0; i < tmp.length; i++) {
				dataLayerNames[i] = tmp[i].trim();
			}
		}
		else {
			dataLayerInterval = 1;
			dataLayerPrecision = 5;
			dataLayerOneFile = true;
			dataLayerNames = new String[0];
		}
	}

	/**
	 * Starts the batch run process
	 */
	public void start() {
		// Create parameter sweep controller
		ParameterSweep sweep = new ParameterSweep();

		for (ParameterInfo pInfo : parameters) {
			pInfo.addParameter(sweep);
		}

		// Create batch run controller
		BatchRunController batchRunController = new BatchRunController(
				repetitions, ticks, dataFile);
		batchRunController.setSaveDataFlag(saveData);
		batchRunController.setSaveFinalSnapshotsFlag(finalSnapshot);

		batchRunController.setParameterSweepController(sweep);
		// batchRunController.setDataAnalyzer(dataAnalyzer, varName);

		batchRunController.initOutputFolder(Coordinator.getInstance()
				.getCurrentDir());
		
		// Set up data layers
		batchRunController.setDataLayers(dataLayerNames, 
				dataLayerInterval, dataLayerPrecision, dataLayerOneFile);

		// Change parameters before setup method is called
		sweep.setInitialValuesAndAdvance();

		BatchRunManager batchManager = new BatchRunManager(batchRunController,
				null);
		batchManager.addBatchRunEnded(new BatchRunManager.BatchRunEnded() {
			@Override
			public void finished(int flag) {
				synchronized (lock) {
					lock.notifyAll();
				}
			}
		});


		final boolean saveSnapshots = snapshotInterval > 0;

		synchronized (lock) {
			// Start a batch run process
			batchManager.start(saveSnapshots, snapshotInterval);

			// Block until finished
			try {
				lock.wait();
			} catch (InterruptedException e) {
				logger.error("Interrupted");
			}
		}

	}

}

/**
 * A command line tool for running batch run processes without any user
 * interface
 * 
 * @author Monad
 * 
 */
public class BatchRunner {
	/* Logger */
	private static final Logger logger = LogManager.getLogger();

	/**
	 * Test main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("Usage: spark-batches batchfile.xml");
			return;
		}

		// The first thing to do is to set up the logger
		// TODO: logger
//		try {
//			if (new File("BatchRunner.properties").exists()) {
//				PropertyConfigurator.configure("BatchRunner.properties");
//			} else {
//				BasicConfigurator.configure();
//				logger
//						.error("File BatchRunner.properties is not found: using default output streams for log information");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			BasicConfigurator.configure();
//		}

		try {
			// Initialize main objects
			ModelManager_Basic manager = new ModelManager_Basic();
			DataReceiver receiver = new DataReceiver();

			new Thread(manager).start();

			Coordinator.init(manager, receiver, true);

			// Load xml document
			Document doc = XmlDocUtils.loadXmlFile(args[0]);
			if (doc == null)
				return;

			File basePath = new File(args[0]).getParentFile();

			Node root = doc.getFirstChild();
			if (root == null)
				return;

			// Load model descriptions
			ArrayList<Node> modelNodes = XmlDocUtils.getChildrenByTagName(root,
					"model");
			ArrayList<Model> models = new ArrayList<Model>(modelNodes.size());

			for (Node node : modelNodes) {
				try {
					Model model = new Model(node, basePath);
					models.add(model);
				} catch (Exception e) {
					logger.error(e);
				}
			}

			// Run batches for all models
			for (Model model : models) {
				model.run();
			}
		} catch (Exception e) {
			Log.error(e);
		} finally {
			// FIXME: make a good exit without sleeping for finishing the work
			Thread.sleep(1000);
			System.exit(0);
		}
	}
}
