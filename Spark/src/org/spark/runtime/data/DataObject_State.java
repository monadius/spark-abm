package org.spark.runtime.data;

import org.spark.core.SimulationTime;
import org.spark.math.RationalNumber;

/**
 * Data object for the simulation state
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class DataObject_State extends DataObject {
	/* Current simulation time */
	private SimulationTime time;
	
	/* Random seed for the current simulation */
	private long seed;

	/* State flags */
	private int flags;
	
	
	// Indicates that a simulation is paused
	public static final int PAUSED_FLAG = 0x1;
	// Indicates that data contains information about initial simulation state
	public static final int INITIAL_STATE_FLAG = 0x2;
	// Indicates that data contains information about final simulation state
	public static final int FINAL_STATE_FLAG = 0x4;
	// Indicates that a simulation was terminated
	public static final int TERMINATED_FLAG = 0x8;
	

	/**
	 * Default constructor
	 * @param time
	 * @param seed
	 * @param paused
	 */
	public DataObject_State(SimulationTime time, long seed, int flags) {
		this.time = time;
		this.seed = seed;
		this.flags = flags;
	}
	
	
	public boolean isInitialState() {
		return (flags & INITIAL_STATE_FLAG) != 0;
	}
	
	
	public boolean isFinalState() {
		return (flags & FINAL_STATE_FLAG) != 0;
	}
	
	
	public boolean isTerminated() {
		return (flags & TERMINATED_FLAG) != 0;
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
	
	
	public boolean isPaused() {
		return (flags & PAUSED_FLAG) != 0;
	}
	
	
	public long getSeed() {
		return seed;
	}
	
	
	@Override
	public String toString() {
		return String.valueOf(time.getTick());
	}

}
