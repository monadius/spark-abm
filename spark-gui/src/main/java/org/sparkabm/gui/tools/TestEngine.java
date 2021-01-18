package org.sparkabm.gui.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.PropertyConfigurator;

import org.sparkabm.gui.data.DataFilter;
import org.sparkabm.gui.data.DataReceiver;
import org.sparkabm.gui.data.IDataConsumer;
import org.sparkabm.runtime.data.DataRow;
import org.sparkabm.gui.Coordinator;
import org.sparkabm.gui.ProxyVariable;
import org.sparkabm.gui.ProxyVariableCollection;
import org.sparkabm.runtime.internal.manager.ModelManager_Basic;

import org.sparkabm.utils.FileUtils;
import org.sparkabm.utils.XmlDocUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Auxiliary output stream class
 *
 * @author Monad
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

    // Data filter for controlling the simulation process
    private final DataFilter dataFilter;

    // Synchronization lock
    private final Object lock = new Object();

    // Final data row for each test
    private DataRow finalRow;


    /**
     * Constructs a model test from the given xml node
     *
     * @param node
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
            TestCase testCase = new TestCase(n, basePath);
            testCases.add(testCase);
        }

        this.dataFilter = new DataFilter(this, "state");
    }

    /**
     * Runs all tests for the given model
     */
    public void run() throws Exception {
        Coordinator c = Coordinator.getInstance();
        c.loadModel(modelFile);

        if (testCases.size() == 0 || !c.isModelLoaded()) {
            return;
        }

        // Register the control filter
        c.getDataReceiver().addDataConsumer(dataFilter);

        TestEngine.getLog().println();
        TestEngine.getLog().println("MODEL: " + modelFile.getName());

        try {
            // Run all test cases
            for (TestCase test : testCases) {
                finalRow = null;

                synchronized (lock) {
                    test.start();
                    // Wait until the simulation ends
                    lock.wait();
                }

                if (finalRow == null)
                    throw new Exception("finalRow == null");

                test.analyzeResults(finalRow);
            }
        } finally {
            // Finalize
            c.getDataReceiver().removeDataConsumer(dataFilter);
            c.unloadModel();
        }
    }


    @Override
    /**
     * Method for controlling the simulation process
     */
    public void consume(DataRow row) {
        // We are interested in final states only
        if (row.getState().isFinalState()) {
            this.finalRow = row;

            // Proceed to the next test
            synchronized (lock) {
                lock.notifyAll();
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
        private final String mode;
        private final boolean saveAllData;
        private final boolean timeTestOnly;
        private final String observerName;

        // Data
        private TestEngineData data;

        // Base path to the directory with tests
        private File basePath;

        /**
         * Constructs a test case from the given xml node
         *
         * @param node
         */
        public TestCase(Node node, File basePath) {
            this.basePath = basePath;

            // Load attributes with default values
            this.length = XmlDocUtils.getLongValue(node, "length", 1000);
            this.seed = XmlDocUtils.getLongValue(node, "seed", 0);
            this.mode = XmlDocUtils.getValue(node, "mode", "serial");
            this.observerName = XmlDocUtils.getValue(node, "observer", "Observer1");
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

            if (!timeTestOnly) {
                // Prepare the data set
                String[] varNames = new String[0];

                ProxyVariableCollection varCollection = c.getVariables();
                if (varCollection != null) {
                    ProxyVariable[] vars = varCollection.getVariables();
                    varNames = new String[vars.length];

                    for (int i = 0; i < vars.length; i++) {
                        varNames[i] = vars[i].getName();
                    }
                }

                data = new TestEngineData((int) length, saveAllData, c.getAgentTypesAndNames(), varNames);

                c.getDataReceiver().addDataConsumer(data.getDataFilter());
            }

            // Send initialization commands
            c.setRandomSeed(seed, false);
            c.setObserver(observerName, mode);
            c.startLoadedModel(length, false);
        }


        /**
         * Saves the collected data in text and binary files
         *
         * @param fname
         */
        void saveData(String fname) {
            if (data == null)
                return;

            File text = new File(basePath, fname + ".csv");
            File bin = new File(basePath, fname + ".dat");

            try {
                data.saveAsText(text);
                data.saveAsBinary(bin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        /**
         * Analyzes the test results (compares the existing data with obtained data)
         */
        void analyzeResults(DataRow finalRow) {
            long elapsedTime = finalRow.getState().getElapsedTime();
            TestEngine.getLog().println("END: time = " + elapsedTime);

            if (timeTestOnly)
                return;

            // Remove data consumer
            Coordinator c = Coordinator.getInstance();
            c.getDataReceiver().removeDataConsumer(data.getDataFilter());

            // Analyze data
            String fname = modelFile.getName();
            fname += "_" + length;
            fname += "_" + seed;
            fname += "_" + observerName;
            fname += "_" + mode;
            fname += saveAllData ? "_all" : "";

            fname = "data/" + fname;

            try {
                File file = new File(basePath, fname + ".dat");
                if (file.exists()) {
                    // Compare new data with the previous one
                    TestEngineData data = TestEngineData.readData(file);
                    TestEngineData.CompareResult cmp = TestEngineData.compare(this.data, data);

                    StringBuilder str = new StringBuilder("DATA: ");
                    if (cmp.isConflict()) {
                        str.append("FAILURE; ");
                    } else {
                        str.append("OK; ");
                    }
                    str.append(cmp);
                    TestEngine.getLog().println(str);

                    if (cmp.isConflict()) {
                        System.err.println(str);
                        saveData(fname + "_failure.txt");
                    }
                } else {
                    saveData(fname);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
    private static Logger logger = Logger.getLogger(TestEngine.class.getName());

    // The stream for logging results
    private static PrintStream out = null;
    // List of all tests
    private static ArrayList<ModelTest> tests;


    /**
     * Returns the log output stream
     *
     * @return
     */
    public static PrintStream getLog() {
        return out;
    }

    /**
     * Loads all tests from the given xml file
     *
     * @param file
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
            } catch (Exception e) {
                logger.log(Level.SEVERE, "exception", e);
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
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // The first thing to do is to set up the logger
        // TODO: logger
//		try {
//			if (new File("TestEngine.properties").exists()) {
//				PropertyConfigurator.configure("TestEngine.properties");
//			} else {
//				BasicConfigurator.configure();
//				logger.error("File TestEngine.properties is not found: using default output streams for log information");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			BasicConfigurator.configure();
//		}

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
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception", e);
        } finally {
            if (out != null)
                out.close();
        }

        // Stop all processes and exit
        System.exit(0);
    }
}
