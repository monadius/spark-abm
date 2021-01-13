package org.spark.runtime.external.batchrun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.VariableSet;
import org.spark.runtime.external.VariableSetFactory;
import org.spark.runtime.external.data.DataSetTmp;
import org.spark.runtime.external.render.Render;

/**
 * Batch run controller
 * @author Alexey
 */
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
	/* Folder for saving results */
	private File dataFolder;
	
	/* Indicates whether to save the data after each run or not */
	private boolean saveDataFlag = true;
	
	/* Specifies points for which the data is saved */
	private int saveDataInterval = 1;
	
	/* Indicates if the final snapshots should be saved */
	private boolean saveFinalSnapshots = false;
	
	/* Data analyzer */
	private DataAnalyzer dataAnalyzer;
	
	/* Variable name for analysis */
	private String variableName;
	
	/* Parameter sweep controller */
	private ParameterSweep parameterSweep;
	
	/* Variable set containing values of current parameters */
	private VariableSet currentParameters;
	
	/* Variable set containing value of good parameters */
	private VariableSet goodParameters;
	private int goodCounter, goodRepetition;
	
	/* Results log */
	private PrintStream log;
	
	/* Error estimate of the data */
	private double error;
	
	/* Data layer saver */
	private final DataLayerSaver dataLayerSaver;
	
	
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
		
		this.dataLayerSaver = new DataLayerSaver();
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
	 * Sets the data analyzer
	 * @param dataAnalyzer
	 */
	public void setDataAnalyzer(DataAnalyzer dataAnalyzer, String varName) {
		this.dataAnalyzer = dataAnalyzer;
		this.variableName = varName;
	}
	
	
	/**
	 * Sets the save data flag
	 * @param saveData
	 */
	public void setSaveDataFlag(boolean saveData) {
		this.saveDataFlag = saveData;
	}
	
	
	/**
	 * Sets the interval for data points
	 * @param interval
	 */
	public void setSaveDataInterval(int interval) {
		if (interval < 1)
			interval = 1;
		
		this.saveDataInterval = interval;
	}
	
	
	/**
	 * Sets the flag for saving final snapshots
	 * @param saveSnapshots
	 */
	public void setSaveFinalSnapshotsFlag(boolean saveSnapshots) {
		this.saveFinalSnapshots = saveSnapshots;
	}
	
	/**
	 * Sets options for saving data layers
	 */
	public void setDataLayers(String[] names, int interval, int precision, boolean oneFileFlag) {
		dataLayerSaver.removeAllDataLayers();
		
		dataLayerSaver.setOneFileFlag(oneFileFlag);
		dataLayerSaver.setPrecision(precision);
		dataLayerSaver.getFilter().setInterval(interval);
		
		for (String name : names) {
			dataLayerSaver.addDataLayer(name);
		}
	}
	
	
	/**
	 * Sets a log file and initializes an output folder
	 * @param file
	 */
	public void initOutputFolder(File root) {
		if (root == null)
			return;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		String name = sdf.format(Calendar.getInstance().getTime());
		dataFolder = new File(root, name);
		
		for (int i = 1; i < 1000; i++) {
			if (!dataFolder.exists())
				break;
			
			dataFolder = new File(root, name + " " + i);
		}

		if (!dataFolder.exists())
			if (!dataFolder.mkdirs()) {
				System.out.println("Cannot create the folder: " + dataFolder);
			}
		
		try {
			File logFile = new File(dataFolder, dataFileName + "_log.csv");
			FileOutputStream fs = new FileOutputStream(logFile);
			log = new PrintStream(fs);
		}
		catch (Exception e) {
			e.printStackTrace();
			log = null;
		}
	}
	
	
	/**
	 * Returns the folder for saving batch run data
	 * @return
	 */
	public File getDataFolder() {
		return dataFolder;
	}
	
	
	/**
	 * Sets the parameter sweep controller
	 * @param ps
	 */
	public void setParameterSweepController(ParameterSweep ps) {
		parameterSweep = ps;
	}
	
	
	/**
	 * Initializes the controller and returns the number of ticks
	 * for the first simulation run
	 * @return
	 */
	public synchronized long initialize() {
		error = 1e+10;
		repetition = 0;
		counter = 0;
		
		Coordinator c = Coordinator.getInstance();
		
		if (parameterSweep != null) {
			parameterSweep.setInitialValuesAndAdvance();
		}
		
		currentParameters = VariableSetFactory.createVariableSet("batch@run@current@set");
		currentParameters.synchronizeWithParameters(c.getParameters());
		goodParameters = null;
		
		if (log != null) {
			log.print("Run,Repetition,Error,");
			log.println(currentParameters.getVariableNames());
		}

		for (Render render : Coordinator.getInstance().getRenders()) {
			render.setSnapshotNamePrefix("" + counter + "-" + repetition + "-");
		}

		// Register the data layer saver
		dataLayerSaver.setFileNamePrefix("" + counter + "-" + repetition + "-");
		c.getDataReceiver().addDataConsumer(dataLayerSaver.getFilter());

		
		return numberOfTicks;
	}
	
	
	/**
	 * Saves all collected data into a file
	 */
	public synchronized void saveData() {
		String fname = dataFileName + counter + "-" + repetition + ".csv";
		// FIXME: rewrite when data sets are implemented
		DataSetTmp dataset = Coordinator.getInstance().getDataSet();
		
		if (dataset != null) {
			dataset.saveData(new File(dataFolder, fname), saveDataInterval);
		}
	}
	
	
	/**
	 * This function is called when all runs are done
	 */
	void stop() {
		dataLayerSaver.clear();
		Coordinator.getInstance().getDataReceiver().removeDataConsumer(dataLayerSaver.getFilter());
		
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
	public synchronized long nextStep(DataSet dataSet) {
		// Save data
		if (saveDataFlag) {
			saveData();
		}
		
		// Save snapshots
		if (saveFinalSnapshots) {
			for (Render render : Coordinator.getInstance().getRenders()) {
				render.takeSnapshot("" + counter + "-" + repetition + "-");
			}
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
		
		dataLayerSaver.clear();
		
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

		currentParameters = VariableSetFactory.createVariableSet("batch@run@current@set");
		currentParameters.synchronizeWithParameters(Coordinator.getInstance().getParameters());

		for (Render render : Coordinator.getInstance().getRenders()) {
			render.setSnapshotNamePrefix("" + counter + "-" + repetition + "-");
		}

		dataLayerSaver.setFileNamePrefix("" + counter + "-" + repetition + "-");
		
		return numberOfTicks;
	}
}

