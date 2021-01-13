package org.sparkabm.gui.data;

import org.sparkabm.runtime.data.DataRow;

public interface IDataConsumer {
	public void consume(DataRow row);
}
