package org.spark.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;

import org.spark.core.Observer;
import org.spark.core.ObserverFactory;
import org.spark.core.SparkModel;
import org.spark.gui.render.DataLayerStyle;
import org.spark.gui.render.Render;
import org.spark.runtime.AbstractModelManager;
import org.spark.runtime.BatchRunController;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public abstract class GUIModelManager extends AbstractModelManager {
	private static GUIModelManager instance;
	
//	protected ClassLoader classLoader = null;

	protected IUpdatableFrame mainFrame;
	protected ArrayList<UpdatableFrame> frames = new ArrayList<UpdatableFrame>();
//	protected ABMModel model;
	protected SparkModel model;
	
	protected File aboutFile;

	
	public static GUIModelManager getInstance() {
		return instance;
	}
	
	
	public File getAboutFile() {
		return aboutFile;
	}
	
	
/*	public ParameterFrame getParameterFrame() {
		for (UpdatableFrame frame : frames) {
			if (frame instanceof ParameterFrame)
				return (ParameterFrame) frame;
		}
		
		return null;
	}
*/	
	
	/**
	 * Creates the observer for the currently loaded model
	 * @param name
	 * @param mode
	 */
	public void CreateObserver(String name, int mode) throws Exception {
		if (model == null)
			return;
		
		if (!name.startsWith("org.spark.core."))
			name = "org.spark.core." + name;
		
		ObserverFactory.create(model, name, mode);
	}
	
	
	public ArrayList<Render> getAllRenders() {
		Render render = null;
		ArrayList<Render> renders = new ArrayList<Render>();
		
		if (mainFrame != null && mainFrame instanceof MainFrame)
			render = ((MainFrame) mainFrame).getRender();
		
		if (render != null)
			renders.add(render);
			
		
		for (UpdatableFrame frame : frames) {
			if (frame instanceof RenderFrame) {
				render = ((RenderFrame) frame).getRender();
				
				if (render != null)
					renders.add(render);
			}
		}
		
		return renders;
	}
	
	
	public ParameterPanel getParameterPanel() {
		for (UpdatableFrame frame : frames) {
			if (frame instanceof ParameterPanel)
				return (ParameterPanel) frame;
		}
		
		return null;
	}
	
	
	public DatasetFrame getDatasetFrame() {
		for (UpdatableFrame frame : frames) {
			if (frame instanceof DatasetFrame)
				return (DatasetFrame) frame;
		}
		
		return null;
	}
	
	
	/**
	 * Returns the path of the file relative to the open model
	 * @param file
	 * @return
	 */
	public String getRelativePath(File file) {
		if (file != null)
			return file.getAbsolutePath();
		else
			return null;
	}
	

	

	public GUIModelManager() {
		if (instance != null)
			throw new Error("GUIModelManager is already created");
		
		instance = this;
	}
	
	public static Class<? extends SparkModel> getModelClass() {
		if (instance != null && instance.model != null)
			return instance.model.getClass();
	
		return null;
	}
	
	
	@Override
	public SparkModel getModel() {
		return model;
	}
	
	
		
	/**
	 * Creates a render window
	 */
	public RenderFrame createRenderFrame(Node node) {
		int renderType;
		
		if (mainFrame instanceof MainFrame)
			renderType = ((MainFrame) mainFrame).renderType;
		else
			renderType = Render.JAVA_2D_RENDER;
		
		RenderFrame frame = new RenderFrame(node, 
				(JFrame) mainFrame,	renderType);
		frames.add(frame);
		
		return frame;
	}
	
	
	/**
	 * Removes a frame from the list
	 * @param frame
	 */
	public void removeUpdatableFrame(UpdatableFrame frame) {
		frames.remove(frame);
	}
	

	/**
	 * Updates the information about an updatable frame if necessary
	 * @param frame
	 */
	public void updateUpdatableFrame(UpdatableFrame frame) {
	}
	
	
	public void setRenderType(int renderType) {
		if (mainFrame instanceof MainFrame)
			((MainFrame) mainFrame).renderType = renderType; 
	}
	
	
	public File getXmlDocumentFile() {
		return null;
	}
	

	public abstract File getCurrentDirectory();
	
	public abstract void setCurrentDirectory(File dir);

	public abstract Document getXmlDocument();
	
	public abstract HashMap<String, DataLayerStyle> getDataLayerStyles();
	
	
	public abstract Render createRender(Node node, int renderType);
	
	public abstract void showAllFrames();
	
	
	public abstract void setFrameVisibility(int id, boolean visible);
	
	
	public abstract void hideFrame(int id);
	
	
	public abstract void loadModel(final java.io.File modelFile) throws Exception;
	
	
	public abstract void unloadModel() throws Exception;	
	
	
	public abstract void saveGUIChanges() throws Exception;	
	
	public abstract void saveDataLayerStyles();	
	
	
	protected void convertOldModelFile(Document doc) {
		// Derive information about variables from parameters
		Node root = doc.getFirstChild();
		NodeList list = doc.getElementsByTagName("variables");
		
		// There are already variables
		if (list.getLength() != 0)
			return;
		
		Node variables = doc.createElement("variables");
		root.appendChild(variables);
		
		list = doc.getElementsByTagName("parameter");
		for (int i = 0; i < list.getLength(); i++) {
			Node par = list.item(i);
			
			// Create a new variable based on parameter's 'get' attribute
			Node item = par.getAttributes().getNamedItem("get");
			if (item != null) {
				String name = item.getNodeValue();
				Node var = doc.createElement("variable");
				
				Node attr = doc.createAttribute("name");
				attr.setNodeValue(name);
				var.getAttributes().setNamedItem(attr);
				
				attr = doc.createAttribute("get");
				attr.setNodeValue(name);
				var.getAttributes().setNamedItem(attr);
				
				item = par.getAttributes().getNamedItem("set");
				attr = doc.createAttribute("set");
				attr.setNodeValue(item.getNodeValue());
				var.getAttributes().setNamedItem(attr);
				
				item = par.getAttributes().getNamedItem("type");
				attr = doc.createAttribute("type");
				attr.setNodeValue(item.getNodeValue());
				var.getAttributes().setNamedItem(attr);
				
				variables.appendChild(var);
				
				// Associate new variable with parameter
				attr = doc.createAttribute("variable");
				attr.setNodeValue(name);
				par.getAttributes().setNamedItem(attr);
			}
		}
	}
	
	
	
	/* Model execution routines */
	
//	private volatile boolean modelIsInitialized = false;
	protected volatile boolean updateRequested = false;
	protected volatile boolean paused = false;
	protected volatile boolean saveStateFlag = false;	
	protected volatile File stateFile = null;
	protected volatile int delayTime = 0;
	protected volatile Thread modelThread = null;
	
	protected volatile long initTick = 0;
//	protected volatile long maxTicks = 1000000000;
//	protected volatile int runNumber = 1;
//	protected volatile String dataFileName = "data";

	public static final Object lock = new Object();
	public volatile boolean synchFlag = true;

	/* Current batch run controller */
	protected volatile BatchRunController batchRunController;
	
	/* Batch run controller which will be used for the next run */
	private BatchRunController newBatchRunController;

	
	public void setupModel() {
		if (model == null) return;
		stopModel();

		model.synchronizeMethods();
		
		// Call twice in order to validate changes that
		// could occur after the first call (some variables
		// should be adjusted to min/max values)
		model.synchronizeVariables();
		model.synchronizeVariables();
		
		// Setup is processed in serial mode always
		Observer.getInstance().beginSetup();
		model.setup();
		Observer.getInstance().finalizeSetup();
		
		// Synchronize variables right after setup method
		model.synchronizeVariables();

		paused = true;
		updateRequested = true;
//		modelIsInitialized = true;

		mainFrame.reset();
		for (UpdatableFrame frame : frames) {
			frame.reset();
		}
		
		if (newBatchRunController != null) {
			batchRunController = newBatchRunController;
			newBatchRunController = null;
		}
		else {
			batchRunController = new BatchRunController(
					1, Long.MAX_VALUE, "data");
		}

		modelThread = new Thread(createModelRunClass(), "ModelThread");
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
	
	
/*	public void setBatchParameters(long maxTicks, int runNumber, String dataFileName) {
		synchronized(lock) {
			if (maxTicks < 0)
				maxTicks = 1000000000;
			
			if (runNumber < 0)
				runNumber = 1;
			
			this.maxTicks = maxTicks;
			this.runNumber = runNumber;
			this.dataFileName = dataFileName;
		}
	}*/
	public void setBatchRunController(BatchRunController ctrl) {
		newBatchRunController = ctrl;
	}
	
	
/*	public long getMaxTicks() {
		return maxTicks;
	}
	
	
	public int getRunNumber() {
		return runNumber;
	}
	
	
	public String getDataFileName() {
		return dataFileName;
	}
*/	
	
	public void requestUpdate() {
		updateRequested = true;
	}

	

	protected abstract Runnable createModelRunClass();
	
	
	/**
	 * Serializes model state
	 */
	public void saveModelState(String fname) {
		if (fname == null)
			fname = "state.dat";
		
		stateFile = new File(getXmlDocumentFile().getParentFile(), fname);
		saveStateFlag = true;
	}
	
	
	/**
	 * Loads model state
	 */
	protected void loadModelState(File file) throws Exception {
		if (file == null)
			return;
		
		File stateFile = file;
		if (!stateFile.exists())
			throw new Exception("No model state file found");
		
		stopModel();
		
		FileInputStream in = null;
		try {
//-			Thread.currentThread().setContextClassLoader(classLoader);
			in = new FileInputStream(stateFile);
			ObjectInputStream ois = new ObjectInputStream(in);
			initTick = ois.readLong();
			throw new Error("Method is not implemented");
//			Observer.loadState(model, in, classLoader);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (in != null)
				in.close();
		}

		// Start model
		model.synchronizeVariables();
		model.synchronizeVariables();
		
//		paused = true;
		updateRequested = true;

		mainFrame.reset();
		for (UpdatableFrame frame : frames) {
			frame.reset();
		}

		modelThread = new Thread(createModelRunClass(), "ModelThread");
		modelThread.start();
	}
}
