package org.spark.runtime;

import org.spark.core.SparkModel;


public abstract class AbstractModelManager {
	private static AbstractModelManager instance;
	
	
	public static AbstractModelManager getInstance() {
		return instance;
	}
	
	
	protected AbstractModelManager() {
		if (instance != null)
			throw new Error("Only one instance of AbstractModelManager is allowed");

		instance = this;
	}
	
	
	public abstract SparkModel getModel(); 
}
