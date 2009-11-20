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
	
	/* Indicates whether the simulation is paused or not */
	private boolean paused;
	
	/* Random seed for the current simulation */
	private long seed;
	
	/* If true then the state is the initial state */
	private boolean initialState;

	
	/**
	 * Default constructor
	 * @param time
	 * @param seed
	 * @param paused
	 */
	public DataObject_State(SimulationTime time, long seed, boolean paused, boolean initial) {
		this.time = time;
		this.seed = seed;
		this.paused = paused;
		// TODO: initial state should be defined by a special value of time
		this.initialState = initial;
	}
	
	
	public boolean isInitial() {
		return initialState;
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
		return paused;
	}
	
	
	public long getSeed() {
		return seed;
	}
	
	
	@Override
	public String toString() {
		return String.valueOf(time.getTick());
	}

}
