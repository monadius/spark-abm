package org.spark.runtime.internal.data;

import org.spark.runtime.data.DataRow;
import org.spark.runtime.external.data.LocalDataReceiver;

/**
 * Sends data to a data receiver
 * @author Monad
 *
 */
public class LocalDataSender extends DataProcessor {
	private LocalDataReceiver receiver;

	/**
	 * Creates a local data sender for the given local data receiver
	 * @param receiver
	 */
	public LocalDataSender(LocalDataReceiver receiver) {
		this.receiver = receiver;
	}
	
	@Override
	public void finalizeProcessing() throws Exception {
	}

	@Override
	public void processDataRow(DataRow row) throws Exception {
		receiver.receive(row);
	}

}
