package org.spark.runtime.external.data;

import org.spark.runtime.data.DataRow;

public interface IDataConsumer {
	public void consume(DataRow row);
}
