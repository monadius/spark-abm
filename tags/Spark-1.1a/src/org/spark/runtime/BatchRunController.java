package org.spark.runtime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.spark.core.Observer;
import org.spark.core.SparkModel;
import org.spark.gui.DatasetFrame;
import org.spark.gui.GUIModelManager;

public class BatchRunController {
	/* The number of repetitions of each simulation */
	private int numberOfRepetition;
	/* The repetition counter */
	private int repetition;
	
	/* Counter of different runs */
	private int counter;
	
	/* The number of ticks in each simulation */
	private long numberOfTicks;
	
	/* The prefix of the data file names */
	private String dataFileName = "data";
	
	/* Indicates whether to save the data after each run or not */
	private boolean saveDataFlag = true;
	
	/* Parameter sweep controller */
	private ParameterSweep parameterSweep;
	
	/* Analyzes collected data */
	private DataAnalyzer dataAnalyzer;
	
	/* Variable name for analysis */
	private String variableName;
	
	/* Variable set containing values of current parameters */
	private VariableSet currentParameters;
	
	/* Variable set containing value of good parameters */
	private VariableSet goodParameters;
	private int goodCounter, goodRepetition;
	
	/* Results log */
	private PrintStream log;
	
	/* Error estimate of the data */
	private double error;
	
	
	// TODO: dataSet should be independent of batch run controller
	private DataSet dataSet;
	
	
	/**
	 * Creates a default batch run controller
	 */
	public BatchRunController() {
		numberOfTicks = Long.MAX_VALUE;
		numberOfRepetition = 1;
	}
	
	
	/**
	 * Creates a batch run controller with the given
	 * number of repetitions and maximum number of ticks
	 * @param repetitionNumber
	 * @param maxTicks
	 */
	public BatchRunController(int repetitionNumber, long maxTicks) {
		if (repetitionNumber < 1)
			repetitionNumber = 1;
		
		if (maxTicks < 1)
			maxTicks = 1;
		
		this.numberOfTicks = maxTicks;
		this.numberOfRepetition = repetitionNumber;
	}
	
	
	/**
	 * Creates a batch run controller with the given
	 * number of repetitions, maximum number of ticks,
	 * and the data file names
	 * @param repetitionNumber
	 * @param maxTicks
	 * @param dataFileName
	 */
	public BatchRunController(int repetitionNumber, long maxTicks, String dataFileName) {
		this(repetitionNumber, maxTicks);
		this.dataFileName = dataFileName;
	}
	
	
	/**
	 * Sets the save data flag
	 * @param saveData
	 */
	public void setSaveDataFlag(boolean saveData) {
		this.saveDataFlag = saveData;
	}
	
	
	/**
	 * Sets the log file
	 * @param file
	 */
	public void setLogFile(File file) throws IOException {
		if (file == null)
			return;
		
		FileOutputStream fs = new FileOutputStream(file);
		log = new PrintStream(fs);
	}
	
	
	/**
	 * Sets the parameter sweep controller
	 * @param ps
	 */
	public void setParameterSweepController(ParameterSweep ps) {
		parameterSweep = ps;
	}
	
	
	// TODO: remove
	public void setDataSet(DataSet dataSet) {
		this.dataSet = dataSet;
	}
	
	
	/**
	 * Sets the data analyzer
	 * @param dataAnalyzer
	 */
	public void setDataAnalyzer(DataAnalyzer dataAnalyzer, String varName) {
		this.dataAnalyzer = dataAnalyzer;
		this.variableName = varName;
	}
	
	/**
	 * Updates the batch controller each tick
	 */
	// TODO: do we need this method?
	public void updateBatchController(long tick) {
		// TODO: remove
		if (dataSet != null)
			dataSet.collectData(tick);
	}
	
	/**
	 * Initializes the controller and returns the number of ticks
	 * for the first simulation run
	 * @return
	 */
	public long initialize() {
		error = 1e+10;
		repetition = 0;
		counter = 0;
		
		if (dataSet != null)
			dataSet.clear();
		
		// FIXME: inconsistent work with parameters:
		// here parameters are set after model.setup(),
		// in nextStep() parameters are set before model.setup().
		// Here it should be modified somehow
		// Fixed: setInitialValuesAndAdvance() is called
		// inside BatchRunDialog before setup
		
		// TODO: do not call this method twice
		if (parameterSweep != null) {
			parameterSweep.setInitialValuesAndAdvance();
		}
		
		currentParameters = new VariableSet("batch@run@current@set");
		currentParameters.synchronizeWithParameters();
		goodParameters = null;
		
		if (log != null) {
			log.print("Run,Repetition,Error,");
			log.println(currentParameters.getVariableNames());
		}
		
		return numberOfTicks;
	}
	
	
	/**
	 * Saves all collected data into a file
	 */
	public void saveData() {
		String fname = dataFileName + counter + "-" + repetition + ".csv";
		DatasetFrame dataset = GUIModelManager.getInstance().getDatasetFrame();
		
		if (dataset != null) {
			dataset.saveData(fname);
		}
	}
	
	
	/**
	 * This function is called when all runs are done
	 */
	private void stop() {
		if (goodParameters != null) {
			if (log != null) {
				log.println();
				log.print(goodCounter);
				log.print(',');
				log.print(goodRepetition);
				log.print(',');
				log.print(error);
				log.print(',');
				log.println(goodParameters.getVariableValues());
				log.flush();
			}
		}
		
		if (log != null) {
			log.close();
			log = null;
		}
	}
	
	/**
	 * Saves collected data, begins a new simulation step,
	 * and return the number of ticks for the new step.
	 * If ticks number == 0, then end the batch process.
	 * @return
	 */
	public long nextStep() {
		// Save data
		if (saveDataFlag) {
			saveData();
		}
		
		// Analyze data
		if (dataAnalyzer != null && dataSet != null) {
			double err = dataAnalyzer.analyze(dataSet, variableName);

			if (log != null) {
				log.print(counter);
				log.print(',');
				log.print(repetition);
				log.print(',');
				log.print(err);
				log.print(',');
				log.println(currentParameters.getVariableValues());
				log.flush();
			}
			
			if (err < error) {
				error = err;
				goodCounter = counter;
				goodRepetition = repetition;
				goodParameters = currentParameters;
			}
			
		}
		
		if (dataSet != null) {
			dataSet.clear();
		}
		
		repetition++;
		if (repetition >= numberOfRepetition) {
			if (parameterSweep != null) {
				if (!parameterSweep.setCurrentValuesAndAdvance()) {
					stop();
					return 0;
				}
				
				repetition = 0;
				counter++;
			}
			else {
				stop();
				return 0;
			}
		}

		currentParameters = new VariableSet("batch@run@current@set");
		currentParameters.synchronizeWithParameters();
		
		// Reset observer
		Observer.getInstance().reset();
		SparkModel model = GUIModelManager.getInstance().getModel(); 
		if (model == null) {
			stop();
			return 0;
		}

		// Reset variables
		model.synchronizeMethods();
		model.synchronizeVariables();
		
		// Setup is processed in serial mode always
		Observer.getInstance().beginSetup();
		model.setup();
		Observer.getInstance().finalizeSetup();		

		return numberOfTicks;
	}
}
