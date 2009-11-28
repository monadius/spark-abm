package org.spark.runtime.external.batchrun;

import org.spark.runtime.data.DataRow;
import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.data.DataFilter;
import org.spark.runtime.external.data.DataReceiver;
import org.spark.runtime.external.data.IDataConsumer;

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
	 * Starts a batch run process
	 */
	public synchronized void start() {
		Coordinator c = Coordinator.getInstance();
		runFlag = false;

		// Stop a simulation
		c.stopSimulation();
		
		// Set up initial values of parameters
		currentSimulationLength = controller.initialize();
		if (currentSimulationLength == 0) {
			stop();
			return;
		}
		
		// Note: c.stopSimultaion() does not stop simulation immediately.
		// It is quite possible that the data filter will receive
		// some data from the stopping simulation
		
		// Register data consumers
		receiver.addDataConsumer(dataFilter);
		
		if (dataSet != null)
			receiver.addDataConsumer(dataSet.getFilter());
		
		// Start a new simulation
		c.startLoadedModel(currentSimulationLength, false);
	}
	

	/**
	 * Stops a batch run process
	 */
	private void stop() {
		controller.stop();
		
		receiver.removeDataConsumer(dataFilter);
		
		if (dataSet != null)
			receiver.removeDataConsumer(dataSet.getFilter());
		
		runFlag = false;
	}
	
	
	/**
	 * IDataConsumer implementation and a main method
	 */
	public synchronized void consume(DataRow row) {
		// We use 'runFlag' for preventing receiving of data
		// of a simulation which is stopping
		if (row.getState().isInitial())
			runFlag = true;
		
		if (!runFlag)
			return;
		
		// We are interested in end states only
		if (row.getState().isEnd()) {
			if (row.getState().getTick() == currentSimulationLength) {
				// Proceed to the next batch run step
				currentSimulationLength = controller.nextStep(dataSet);
				if (currentSimulationLength == 0) {
					// Stop the batch run process
					stop();
					return;
				}
				
				// Begin a new simulation
				Coordinator.getInstance().startLoadedModel(currentSimulationLength, false);
			}
			else {
				// A simulation was stopped by some external forces

				// Stop the batch run process
				stop();
				return;
			}
		}
	}

}
