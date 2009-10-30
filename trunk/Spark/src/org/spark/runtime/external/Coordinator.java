package org.spark.runtime.external;

import java.io.File;

import org.spark.runtime.external.data.DataReceiver;
import org.spark.runtime.internal.manager.BasicModelManager;

import com.spinn3r.log5j.Logger;

/**
 * Central external class which coordinates interactions
 * between a model manager and a data receiver
 * @author Monad
 *
 */
public class Coordinator {
	/* Logger */
	private static final Logger logger = Logger.getLogger();
	
	/* A single instance of the class */
	private static Coordinator coordinator;
	
	
	private BasicModelManager modelManager;
	private DataReceiver receiver;
	private File currentDir;
	
	
	/**
	 * Private constructor
	 * @param manager
	 * @param receiver
	 */
	private Coordinator(BasicModelManager manager, DataReceiver receiver) {
		this.modelManager = manager;
		this.receiver = receiver;
		this.currentDir = null;
	}
	
	
	/**
	 * Creates a coordinator
	 * @param manager
	 * @param receiver
	 */
	public static void init(BasicModelManager manager, DataReceiver receiver) {
		if (coordinator != null) {
			logger.error("Coordinator is already created");
			throw new Error("Illegal operation");
		}
		
		coordinator = new Coordinator(manager, receiver);
	}
	

	/**
	 * Returns the instance of the class
	 * @return
	 */
	public static Coordinator getInstance() {
		return coordinator;
	}
	
	
	/**
	 * Returns the current directory (the base directory of a loaded model)
	 * @return
	 */
	public File getCurrentDir() {
		return currentDir;
	}
}
