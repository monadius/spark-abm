package org.spark.runtime;

import org.spark.core.ExecutionMode;
import org.spark.core.Observer;
import org.spark.gui.UpdatableFrame;
import org.spark.startup.ABMModel;


/**
 * Abstract simulation engine
 * @author Monad
 */
public abstract class AbstractSimulationEngine {
	/* Model execution routines */
	
//	private volatile boolean modelIsInitialized = false;
	protected volatile boolean updateRequested = false;
	protected volatile boolean paused = false;
	protected volatile int delayTime = 0;
	protected volatile Thread modelThread = null;
	
	protected volatile long maxTicks = 1000000000;
	protected volatile int runNumber = 1;
	protected volatile String dataFileName = "data";

	public static final Object lock = new Object();
	public volatile boolean synchFlag = true;

	
	protected ABMModel model;
	
	
	public AbstractSimulationEngine(ABMModel model) {
		this.model = model;
	}
	
	
	public void setupModel() {
		if (model == null) return;
		stopModel();

		// Setup is processed in serial mode always
		if (Observer.getInstance().isSerial()) {
			model.setup();
		}
		else {
			int mode = Observer.getInstance().getExecutionMode();
			Observer.getInstance().setExecutionMode(ExecutionMode.SERIAL_MODE);
			model.setup();
			Observer.getInstance().setExecutionMode(mode);
		}
		Observer.getInstance().finalizeSetup();

		paused = true;
		updateRequested = true;
//		modelIsInitialized = true;

//		mainFrame.reset();
//		for (UpdatableFrame frame : frames) {
//			frame.reset();
//		}

//		modelThread = new Thread(createModelRunClass(), "ModelThread");
		modelThread.start();
//		System.err.println(modelThread.toString());
	}
	
	
	protected void stopModel() {
		if (modelThread != null) {
			Thread t = modelThread;
//			modelIsInitialized = false;
			
			try {
				modelThread = null;

				synchronized(lock) {
					lock.notify();
				}
				
				t.join(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			finally {
				if (t.isAlive())
					throw new Error("Model thread has not been stopped");
				modelThread = null;
			}
		}
		
		Observer.getInstance().reset();
	}

	
	public boolean isModelPaused() {
		return paused;
	}
	
	
	public boolean pauseResumeModel() {
		if (modelThread == null)
			return true;
		return paused = !paused;
	}
	
	

	public void changeSimulationSpeed(int delayTime) {
		this.delayTime = delayTime;
	}
	
	
	public void setBatchParameters(long maxTicks, int runNumber, String dataFileName) {
		synchronized(lock) {
			if (maxTicks < 0)
				maxTicks = 1000000000;
			
			if (runNumber < 0)
				runNumber = 1;
			
			this.maxTicks = maxTicks;
			this.runNumber = runNumber;
			this.dataFileName = dataFileName;
		}
	}
	
	
	public long getMaxTicks() {
		return maxTicks;
	}
	
	
	public int getRunNumber() {
		return runNumber;
	}
	
	
	public String getDataFileName() {
		return dataFileName;
	}
	
	
	public void requestUpdate() {
		updateRequested = true;
	}

}
