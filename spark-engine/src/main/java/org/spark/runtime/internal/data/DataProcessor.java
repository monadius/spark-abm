package org.spark.runtime.internal.data;

import org.spark.runtime.data.DataRow;

/**
 * Base class for all data processors
 * @author Monad
 *
 */
public abstract class DataProcessor {
	/**
	 * Invoked each time after data collection
	 * @param row
	 */
	public abstract void processDataRow(DataRow row) throws Exception;
	
	
	/**
	 * Invoked after the end of each simulation
	 * @throws Exception
	 */
	public abstract void finalizeProcessing() throws Exception;
}
