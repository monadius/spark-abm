package org.spark.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.spark.core.ExecutionMode;
import org.spark.utils.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
class ModelTest {
	private final File modelFile;
	private final ArrayList<TestCase> testCases;

	/**
	 * Constructs a model test from the given xml node
	 * 
	 * @param xmlNode
	 */
	public ModelTest(Node node) throws Exception {
		NamedNodeMap attributes = node.getAttributes();
		Node tmp;

		// Load attributes with default values
		String model = (tmp = attributes.getNamedItem("model")) != null ? tmp
				.getNodeValue() : null;
		if (model == null)
			throw new Exception(
					"'model' attribute should be specified for all tests");

		modelFile = new File(model);
		if (!modelFile.exists())
			throw new Exception("Model file '" + model + "' does not exist");

		// Load test cases (runs)
		testCases = new ArrayList<TestCase>();
		NodeList list = node.getChildNodes();

		for (int i = 0; i < list.getLength(); i++) {
			tmp = list.item(i);

			if (tmp.getNodeName().equals("run")) {
				// Create a test case
				TestCase testCase = new TestCase(tmp);
				testCases.add(testCase);
			}
		}
	}

	/**
	 * Runs all tests for the given model
	 * 
	 * @param engine
	 */
	public void run(RunEngine engine) {
		try {
			engine.loadModel(modelFile);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		for (TestCase testCase : testCases) {
			try {
				testCase.run(engine);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void run2(RunEngine2 engine) {
		try {
			engine.loadModel(modelFile);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		for (TestCase testCase : testCases) {
			try {
				testCase.run2(engine);
			} catch (Exception e) {
				e.printStackTrace();
			}
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
		private final int mode;
		private final boolean saveAllData;
		private final boolean timeTestOnly;
		private final String observerName;

		/**
		 * Constructs a test case from the given xml node
		 * 
		 * @param node
		 */
		public TestCase(Node node) {
			NamedNodeMap attributes = node.getAttributes();
			Node tmp;

			// Load attributes with default values
			String sLength = (tmp = attributes.getNamedItem("length")) != null ? tmp
					.getNodeValue()
					: "1000";
			String sSeed = (tmp = attributes.getNamedItem("seed")) != null ? tmp
					.getNodeValue()
					: "0";
			String sMode = (tmp = attributes.getNamedItem("mode")) != null ? tmp
					.getNodeValue() : "serial";
			observerName = (tmp = attributes.getNamedItem("observer")) != null ? tmp
					.getNodeValue()
					: "Observer1";
			String saveData = (tmp = attributes.getNamedItem("save-all")) != null ? tmp
					.getNodeValue()
					: "true";

			// Assign loaded values to the variables
			seed = Long.parseLong(sSeed);
			length = Long.parseLong(sLength);

			int mode;
			
			try {
				mode = ExecutionMode.parse(sMode);
			}
			catch (Exception e) {
				e.printStackTrace();
				mode = ExecutionMode.SERIAL_MODE;
			}
			
			this.mode = mode;

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
		 * Runs the test case
		 * 
		 * @param engine
		 */
		public void run(RunEngine engine) throws Exception {
			engine.run(length, seed, observerName, mode, saveAllData);

			if (timeTestOnly)
				return;
			
			String fname = modelFile.getName();
			fname += "_" + length;
			fname += "_" + seed;
			fname += "_" + observerName;
//			fname += "_" + ExecutionMode.toString(mode);
			fname += "_" + (mode == ExecutionMode.SERIAL_MODE ? "true" : ExecutionMode.toString(mode));
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
		}
		
		
		public void run2(RunEngine2 engine) throws Exception {
			engine.run(length, seed, observerName, mode, saveAllData);

			if (timeTestOnly)
				return;
		}
	}
}

/**
 * Main class
 * 
 * @author Monad
 */
public class TestEngine {
	private static PrintStream out = null;
//	private static RunEngine runEngine;
	private static RunEngine2 runEngine2;
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
		tests = new ArrayList<ModelTest>();

		// Load xml document
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document xmlDoc = db.parse(file);

		// Get all test nodes
		NodeList list = xmlDoc.getElementsByTagName("test");
		for (int i = 0; i < list.getLength(); i++) {
			Node tmp = list.item(i);
			try {
				ModelTest test = new ModelTest(tmp);
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
/*		if (tests == null || runEngine == null)
			return;

		for (ModelTest test : tests) {
			test.run(runEngine);
		}*/
		
		for (ModelTest test : tests) {
			test.run2(runEngine2);
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
	 * Regular Java main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// The first thing to do is to set up the logger
		try {
			if (new File("spark.log4j.properties").exists()) {
				PropertyConfigurator.configure("spark.log4j.properties");
			} else {
				BasicConfigurator.configure();
			}
		} catch (Exception e) {
			e.printStackTrace();
			BasicConfigurator.configure();
		}
		
		try {
			// Create the logging stream
			File logFile = new File("tests.log");
			out = new PrintStream(new MyStream(logFile));

			// Create the run engine
//			runEngine = new RunEngine(out);
			runEngine2 = new RunEngine2(System.out);

			// Select an xml file with tests
			File file = selectXmlFile();

			if (file == null)
				return;

			// Load tests
			loadTestFile(file);

			// Run all tests
			runAllTests();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} finally {
			if (out != null)
				out.close();
		}
	}
}
