package org.spark.runtime.external.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import org.spark.runtime.data.DataRow;
import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.data.DataFilter;
import org.spark.runtime.external.data.DataReceiver;
import org.spark.runtime.external.data.IDataConsumer;
import org.spark.runtime.internal.manager.ModelManager_Basic;

import org.spark.utils.FileUtils;
import org.spark.utils.XmlDocUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.spinn3r.log5j.Logger;

/**
 * Auxiliary output stream class
 * 
 * @author Monad
 * 
 */
class MyStream extends OutputStream {
	private FileOutputStream fos;

	public MyStream(File logFile) throws IOException {
		fos = new FileOutputStream(logFile, true);
	}

	@Override
	public void write(int b) throws IOException {
		System.out.write(b);
		fos.write(b);
	}

	@Override
	public void close() throws IOException {
		fos.close();
	}

}

/**
 * Represents a number of test cases for a given model
 * 
 * @author Monad
 */
class ModelTest implements IDataConsumer {
	// The model description file
	private final File modelFile;
	// List of all tests for this model
	private final ArrayList<TestCase> testCases;
	// Index of the current test
	private int currentTest;
	
	// Data filter for controlling the simulation process
	private final DataFilter dataFilter;
	
	// Synchronization lock
	private final Object lock = new Object();
	

	/**
	 * Constructs a model test from the given xml node
	 * 
	 * @param xmlNode
	 */
	public ModelTest(Node node, File basePath) throws Exception {
		// Load attributes with default values
		String model = XmlDocUtils.getValue(node, "model", null);
		if (model == null)
			throw new Exception(
					"'model' attribute should be specified for all tests");

		this.modelFile = new File(basePath, model);
		if (!modelFile.exists())
			throw new Exception("Model file '" + modelFile.getAbsolutePath() + "' does not exist");

		// Load test cases (runs)
		this.testCases = new ArrayList<TestCase>();
		ArrayList<Node> list = XmlDocUtils.getChildrenByTagName(node, "run");

		for (Node n : list) {
			// Create a test case
			TestCase testCase = new TestCase(n);
			testCases.add(testCase);
		}
		
		this.dataFilter = new DataFilter(this, "state");
	}

	/**
	 * Runs all tests for the given model
	 * 
	 * @param engine
	 */
	public void run() throws Exception {
		Coordinator c = Coordinator.getInstance();
		c.loadModel(modelFile);
		
		c.getDataReceiver().addDataConsumer(dataFilter);
		currentTest = 0;
		
		if (testCases.size() == 0 || !c.isModelLoaded()) {
			stop();
			return;
		}
		
		TestEngine.getLog().println();
		TestEngine.getLog().println("MODEL: " + modelFile.getName());

		
		synchronized (lock) {
			testCases.get(currentTest).start();
		
			// Wait until all simulations end
			lock.wait();
		}
	}
	
	
	/**
	 * Stops the testing process
	 */
	private void stop() {
		Coordinator c = Coordinator.getInstance();
		c.getDataReceiver().removeDataConsumer(dataFilter);
		c.unloadModel();
		
		synchronized (lock) {
			lock.notifyAll();
		}
	}
		
	


	@Override
	/**
	 * Method for controlling the simulation process
	 */
	public void consume(DataRow row) {
		// We are interested in final states only
		if (row.getState().isFinalState()) {
			// Proceed to the next test
			
			long elapsedTime = row.getState().getElapsedTime();
			TestEngine.getLog().println("END: time = " + elapsedTime);
			
			currentTest++;
			if (currentTest >= testCases.size()) {
				// Stop the testing process
				stop();
				return;
			}

			// Begin a new simulation
			testCases.get(currentTest).start();
		}
	}
	
	
	/**
	 * Represents a test case for the model test
	 * 
	 * @author Monad
	 */
	class TestCase {
		private final long length;
		private final long seed;
		private final String mode;
		private final boolean saveAllData;
		private final boolean timeTestOnly;
		private final String observerName;

		/**
		 * Constructs a test case from the given xml node
		 * 
		 * @param node
		 */
		public TestCase(Node node) {
			// Load attributes with default values
			this.length = XmlDocUtils.getLongValue(node, "length", 1000);
			this.seed = XmlDocUtils.getLongValue(node, "seed", 0);
			this.mode = XmlDocUtils.getValue(node, "mode", "serial");
			this.observerName = XmlDocUtils.getValue(node, "observerName", "Observer1");
			String saveData = XmlDocUtils.getValue(node, "save-all", "true");

			if (saveData.equals("time"))
				timeTestOnly = true;
			else
				timeTestOnly = false;
			
			if (saveData.equals("true"))
				saveAllData = true;
			else
				saveAllData = false;
		}

		
		/**
		 * Starts the test
		 */
		void start() {
			Coordinator c = Coordinator.getInstance();

			// Log the run
			TestEngine.getLog().println("RUN: Observer = " + observerName + "; length = " + length
					+ "; seed = " + seed + "; mode = " + mode
					+ "; all data = " + saveAllData);			
			
			// Send initialization commands
			c.setRandomSeed(seed, false);
			c.setObserver(observerName, mode);
			c.startLoadedModel(length, false);
		}
		
		
		/**
		 * Analyzes the test results (compares the existing data with obtained data)
		 */
		void analyzeResults() {
			if (timeTestOnly)
				return;
		}
		
		/**
		 * Runs the test case
		 * 
		 * @param engine
		 */
/*		public void run(RunEngine engine) throws Exception {
			engine.run(length, seed, observerName, mode, saveAllData);

			if (timeTestOnly)
				return;
			
			String fname = modelFile.getName();
			fname += "_" + length;
			fname += "_" + seed;
			fname += "_" + observerName;
			fname += "_" + mode;
			fname += saveAllData ? "_all" : "";

			fname = "data/" + fname;

			File file = new File(fname + ".dat");
			if (file.exists()) {
				// Compare new data with the previous one
				Dataset data = Dataset.readData(file);

				if (engine.compareData(data)) {
					TestEngine.getLog().println("DATA: OK");
				} else {
					System.err.println("DATA: FAIL");
					TestEngine.getLog().println("DATA: FAIL");
					engine.saveData(fname + "_failure");
				}
			} else {
				engine.saveData(fname);
			}
		}*/
		
		

	}

}

/**
 * Main class
 * 
 * @author Monad
 */
public class TestEngine {
	// Logger
	private static Logger logger = Logger.getLogger();
	
	// The stream for logging results
	private static PrintStream out = null;
	// List of all tests
	private static ArrayList<ModelTest> tests;

	
	/**
	 * Returns the log output stream
	 * @return
	 */
	public static PrintStream getLog() {
		return out;
	}
	
	/**
	 * Loads all tests from the given xml file
	 * 
	 * @param fname
	 */
	public static void loadTestFile(File file) throws Exception {
		// The path to the directory with tests (everything is relative to the test description file)
		File basePath = file.getParentFile();

		tests = new ArrayList<ModelTest>();

		// Load xml document
		Document xmlDoc = XmlDocUtils.loadXmlFile(file);
		Node root = xmlDoc.getFirstChild();

		// Get all test nodes
		ArrayList<Node> nodes = XmlDocUtils.getChildrenByTagName(root, "test");
		for (Node node : nodes) {
			try {
				ModelTest test = new ModelTest(node, basePath);
				tests.add(test);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Runs all loaded tests
	 */
	public static void runAllTests() {
		for (ModelTest test : tests) {
			try {
				test.run();
			}
			catch (Exception e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Displays all xml files in the current directory and asks a user to choose
	 * one of them
	 * 
	 * @return
	 */
	public static File selectXmlFile() throws Exception {
		ArrayList<File> files = FileUtils.findAllFiles(new File("."),
				new FilenameFilter() {
					public boolean accept(File dir, String fname) {
						if (fname.endsWith(".xml"))
							return true;
						else
							return false;
					}

				}, false);

		System.out.println("0: Exit");
		for (int i = 0; i < files.size() && i < 9; i++) {
			System.out.println((i + 1) + ": " + files.get(i).getName());
		}
		
		while (true) {
			System.out.print("\rYour choice:      ");
			int answer = System.in.read();
			
			if (Character.isDigit(answer)) {
				char ch = (char) answer;
				int n = Integer.parseInt(Character.toString(ch));
				
				if (n == 0)
					return null;
			
				if (n <= files.size())
					return files.get(n - 1);
			}
		}
	}

	
	/**
	 * Main method
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// The first thing to do is to set up the logger
		try {
			if (new File("TestEngine.properties").exists()) {
				PropertyConfigurator.configure("TestEngine.properties");
			} else {
				BasicConfigurator.configure();
				logger.error("File TestEngine.properties is not found: using default output streams for log information");
			}
		} catch (Exception e) {
			e.printStackTrace();
			BasicConfigurator.configure();
		}

		// Select an xml file with tests
		File file = selectXmlFile();
		if (file == null)
			return;
		
		// Create the logging stream
		File logFile = new File("tests.log");
		out = new PrintStream(new MyStream(logFile));
		
		// Load tests
		loadTestFile(file);

		
		// Initialize main objects
		ModelManager_Basic manager = new ModelManager_Basic();
		DataReceiver receiver = new DataReceiver();

		// Start the model manager
		new Thread(manager).start();

		// Create the coordinator
		Coordinator.init(manager, receiver, true);

		try {
			// Run all tests
			runAllTests();
		}
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		finally {
			if (out != null)
				out.close();
		}
		
		// Stop all processes and exit
		System.exit(0);
	}
}
