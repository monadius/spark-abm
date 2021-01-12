package org.sparkabm.runtime.data;

/**
 * Abstract data receiver class
 * @author Alexey
 *
 */
public abstract class AbstractDataReceiver {
	public abstract void receive(DataRow row);
}
