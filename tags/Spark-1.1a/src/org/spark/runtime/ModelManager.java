package org.spark.runtime;

import java.io.File;
import java.util.ArrayList;

import org.spark.core.SparkModel;
import org.w3c.dom.Document;

/**
 * Main model manager in SPARK. Runs simulation and controls it.
 * 
 * @author Monad
 */
public abstract class ModelManager {
	/* Unique instance */
	private static ModelManager instance;

	/* Current model */
	private SparkModel model;

	/* Update listeners */
	protected ArrayList<IUpdateListener> listeners = new ArrayList<IUpdateListener>();

	/**
	 * Returns ModelManager instance
	 * 
	 * @return
	 */
	public static ModelManager getInstance() {
		return instance;
	}

	// TODO: use factory
	/**
	 * Initializes ModelManger. Should be called only one time
	 */
	/*
	 * public static void init() { if (instance != null) throw new
	 * Error("ModelManager is already initialized");
	 * 
	 * instance = new ModelManager(); }
	 */

	/**
	 * Protected constructor
	 */
	protected ModelManager() {
	}

	/**
	 * Returns current model
	 * 
	 * @return null if no model is loaded
	 */
	public SparkModel getModel() {
		return model;
	}

	public void removeUpdateListener(IUpdateListener listener) {
		listeners.remove(listener);
	}

//	public abstract File getCurrentDirectory();

//	public abstract void setCurrentDirectory(File dir);

	public abstract Document getModelXML();

	public abstract void loadModel(final File modelFile) throws Exception;

	public abstract void unloadModel() throws Exception;

	public abstract void saveChanges() throws Exception;
}
