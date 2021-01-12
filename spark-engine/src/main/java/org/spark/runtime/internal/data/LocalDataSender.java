package org.spark.runtime.internal.data;

import org.spark.runtime.data.AbstractDataReceiver;
import org.spark.runtime.data.DataRow;

/**
 * Sends data to a data receiver
 * @author Monad
 *
 */
public class LocalDataSender extends DataProcessor {
	private AbstractDataReceiver receiver;

	/**
	 * Creates a local data sender for the given local data receiver
	 * @param receiver
	 */
	public LocalDataSender(AbstractDataReceiver receiver) {
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
