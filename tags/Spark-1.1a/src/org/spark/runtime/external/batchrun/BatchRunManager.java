package org.spark.runtime.external.batchrun;

import java.io.File;
import java.util.ArrayList;

import org.spark.runtime.data.DataRow;
import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.data.DataFilter;
import org.spark.runtime.external.data.DataReceiver;
import org.spark.runtime.external.data.IDataConsumer;
import org.spark.runtime.external.render.Render;

/**
 * Manages a batch run process
 * @author Alexey
 *
 */
public class BatchRunManager implements IDataConsumer {
	/* Data receiver from the coordinator */
	private final DataReceiver receiver;
	
	/* Data filter */
	private final DataFilter dataFilter;
	
	/* Batch run controller */
	private final BatchRunController controller;
	
	/* Data set */
	private final DataSet dataSet;
	
	/* Indicates that a batch run process is working */
	private boolean runFlag;
	
	/* Current simulation length */
	private long currentSimulationLength;
	
	
	/**
	 * Event class which indicates that a batch run process is finished
	 * @author Alexey
	 *
	 */
	public static abstract class BatchRunEnded {
		public static final int TERMINATED = 1;
		public static final int FINISHED = 2;
		
		public abstract void finished(int flag);
	}
	
	/* List of events */
	private ArrayList<BatchRunEnded> events = new ArrayList<BatchRunEnded>();
	

	/**
	 * Creates a batch run manager
	 * @param controller
	 */
	public BatchRunManager(BatchRunController controller, String varName) {
		this.receiver = Coordinator.getInstance().getDataReceiver();
		this.controller = controller;
		this.dataFilter = new DataFilter(this, "state");
		
		if (varName != null) {
			dataSet = new DataSet();
			dataSet.addVariable(varName);
		}
		else {
			dataSet = null;
		}
	}
	
	
	/**
	 * Adds an event
	 * @param event
	 */
	public void addBatchRunEnded(BatchRunEnded event) {
		events.add(event);
	}
	
	
	/**
	 * Removes an event
	 * @param event
	 */
	public void removeBatchRunEnded(BatchRunEnded event) {
		events.remove(event);
	}
	
	

	/**
	 * Starts a batch run process
	 */
	public synchronized void start(boolean saveSnapshots, int snapshotInterval) {
		Coordinator c = Coordinator.getInstance();
		File outputFolder = controller.getDataFolder();
		if (outputFolder == null)
			outputFolder = c.getCurrentDir();
		
		// TODO: it does not work in a natural way:
		// if an output directory is removed in the stop function
		// then not all snapshots will be saved in that folder
		// because snapshots are saved on AWTEvent thread
		c.popOutputDir();
		c.pushOutputDir(outputFolder);
		
		runFlag = false;

		// Stop a simulation
		c.stopSimulation();
		
		// Set up initial values of parameters
		currentSimulationLength = controller.initialize();
		if (currentSimulationLength == 0) {
			stop(BatchRunEnded.FINISHED);
			return;
		}
		
		// Note: c.stopSimultaion() does not stop simulation immediately.
		// It is quite possible that the data filter will receive
		// some data from the stopping simulation
		
		// Register data consumers
		receiver.addDataConsumer(dataFilter);
		
		if (dataSet != null)
			receiver.addDataConsumer(dataSet.getFilter());
		
		// Start automatic snapshot saving
		if (saveSnapshots) {
			Render[] renders = c.getRenders();
			for (Render render : renders) {
				render.enableAutomaticSnapshots(snapshotInterval);
			}
		}
		
		// Start a new simulation
		c.startLoadedModel(currentSimulationLength, false);
	}
	

	/**
	 * Stops a batch run process
	 */
	private void stop(int flag) {
		controller.stop();
		
		receiver.removeDataConsumer(dataFilter);
		
		if (dataSet != null)
			receiver.removeDataConsumer(dataSet.getFilter());
		
		runFlag = false;
		
		for (BatchRunEnded event : events) {
			event.finished(flag);
		}
		
		// Stop automatic snapshot saving
		Render[] renders = Coordinator.getInstance().getRenders();
		for (Render render : renders) {
			render.disableAutomaticSnapshots();
		}
	}
	
	
	/**
	 * IDataConsumer implementation and a main method
	 */
	public synchronized void consume(DataRow row) {
		// We use 'runFlag' for preventing data receiving
		// of a simulation which is stopping
		if (row.getState().isInitialState())
			runFlag = true;
		
		if (!runFlag)
			return;
		
		// We are interested in end states only
		if (row.getState().isFinalState()) {
//			if (row.getState().getTick() == currentSimulationLength) {
			if (!row.getState().isTerminated()) {
				// Proceed to the next batch run step
				currentSimulationLength = controller.nextStep(dataSet);
				if (currentSimulationLength == 0) {
					// Stop the batch run process
					stop(BatchRunEnded.FINISHED);
					return;
				}
				
				// Begin a new simulation
				Coordinator.getInstance().startLoadedModel(currentSimulationLength, false);
			}
			else {
				// A simulation was stopped by some external forces

				// Stop the batch run process
				stop(BatchRunEnded.TERMINATED);
				return;
			}
		}
	}

}
