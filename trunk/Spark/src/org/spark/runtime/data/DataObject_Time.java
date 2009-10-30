package org.spark.runtime.data;

import org.spark.core.SimulationTime;
import org.spark.math.RationalNumber;

/**
 * Data object for the simulation time value
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class DataObject_Time extends DataObject {
	private SimulationTime time;

	DataObject_Time(SimulationTime time) {
		this.time = time;
	}
	
	
	public long getTick() {
		return time.getTick();
	}
	
	
	public RationalNumber getTime() {
		return time.getTime();
	}
	
	
	public SimulationTime getSimulationTime() {
		return time;
	}
	
	
	@Override
	public String toString() {
		return String.valueOf(time.getTick());
	}

}
