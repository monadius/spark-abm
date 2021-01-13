package org.spark.runtime.external;

import java.io.File;

//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.PropertyConfigurator;
import org.sparkabm.runtime.data.DataRow;
import org.spark.runtime.external.data.DataFilter;
import org.spark.runtime.external.data.DataReceiver;
import org.spark.runtime.external.data.DataSetTmp;
import org.spark.runtime.external.data.IDataConsumer;
import org.sparkabm.runtime.internal.manager.ModelManager_Basic;

/**
 * External SPARK interface
 * @author Alexey
 */
public class Interface {
	// Interface instance
	private static Interface instance;
	
	// Coordinator
	private static Coordinator c;
	
	
	/**
	 * Private constructor
	 */
	private Interface() {
	}
	
	/**
	 * Returns the interface instance
	 * @return
	 */
	public static Interface getInstance() {
		if (instance == null) {
			instance = new Interface();
			instance.init();
		}
		
		return instance;
	}
	
	
	/**
	 * Initializes SPARK
	 */
	private void init() {
		// The first thing to do is to set up the logger
		// TODO: update
//		try {
//			if (new File("BatchRunner.properties").exists()) {
//				PropertyConfigurator.configure("BatchRunner.properties");
//			} else {
//				BasicConfigurator.configure();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			BasicConfigurator.configure();
//		}

		// Initialize main objects
		ModelManager_Basic manager = new ModelManager_Basic();
		DataReceiver receiver = new DataReceiver();

		new Thread(manager).start();

		Coordinator.init(manager, receiver, true);
		c = Coordinator.getInstance();
	}
	
	
	
	/**
	 * Loads a model
	 * @param fname
	 */
	public void load(String fname) throws Exception {
		File model = new File(fname);
		
		if (!model.exists()) {
			throw new Exception("File " + fname + " does not exist");
		}
		
		c.loadModel(model);
	}
	
	
	/**
	 * Unloads the loaded model
	 */
	public void unload() {
		c.unloadModel();
	}
	
	
	/**
	 * Returns names of all parameters
	 * @return
	 */
	public String[] parameters() {
		ParameterCollection pc = c.getParameters();
		if (pc == null)
			return null;
		
		Parameter[] pars = pc.getParameters();
		String[] names = new String[pars.length];
		for (int i = 0; i < pars.length; i++) {
			names[i] = pars[i].getName();
		}
		
		return names;
	}
	
	
	/**
	 * Sets the value of the given parameter
	 * @param parName
	 * @param value
	 */
	public void setParameterValue(String parName, double value) {
		ParameterCollection pc = c.getParameters();
		if (pc == null)
			return;
		
		Parameter p = pc.getParameter(parName);
		if (p == null)
			return;
		
		p.setValue(value);
	}
	
	
	/**
	 * Returns the values of the given variable 
	 * @param varName
	 * @return
	 */
	public Double getVariableValue(String varName) {
		ProxyVariable v = c.getVariable(varName);
		if (v == null)
			return null;
		
		Object val = v.getValue();
		if (val instanceof Double)
			return (Double) val;
		
		return null;
	}
	
	
	
	/**
	 * Starts a new simulation.
	 * The method returns after the simulation is finished
	 * @param simulationLength
	 * @return the final data set
	 */
	public DataSetTmp start(int simulationLength, int randomSeed) {
		if (!c.isModelLoaded()) {
			System.err.println("No model is loaded");
			return null;
		}

		final Object lock = new Object();
		
		IDataConsumer listener = new IDataConsumer() {
			@Override
			public void consume(DataRow row) {
				// We are interested in final states only
				if (row.getState().isFinalState()) {
					// Notify the waiting processes about simulation end
					synchronized (lock) {
						lock.notifyAll();
					}
				}				
			}
		};
		

		// Initialize the data filter
		DataFilter filter = new DataFilter(listener, "state");
		c.getDataReceiver().addDataConsumer(filter);
		
		
		// Send initialization commands
		c.setRandomSeed(randomSeed, false);
		
		try {
			synchronized (lock) {
				// Start the model
				c.startLoadedModel(simulationLength, false);

				// Wait until the simulation ends
				lock.wait();
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Remove the data filter
		c.getDataReceiver().removeDataConsumer(filter);
		
		return c.getDataSet();
	}
	
	
	/**
	 * Test main function
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Interface spark = getInstance();
		
		System.out.println("Loading a model...");
		spark.load("C:\\Work\\My Projects\\SPARK\\SparkLogo\\Models\\RSV Multiple spaces\\output\\RSVModel.xml");
		System.out.println("Model is loaded");
		
		System.out.println("Running the simulation...");
		DataSetTmp data = spark.start(100, 0);
		System.out.println("The simulation is finished");
		
		String[] names = data.getNames();
		for (int i = 0; i < names.length; i++) {
			System.out.println(names[i]);
		}
		
		System.exit(0);
	}
}
