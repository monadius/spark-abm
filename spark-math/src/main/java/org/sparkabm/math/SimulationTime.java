package org.sparkabm.math;

import java.io.Serializable;


/**
 * Represents the time in a simulation
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class SimulationTime implements Comparable<RationalNumber>, Serializable {
	/* Counts the number of ticks */
	private long tick;
	
	/* Approximates 'continuous' time */
	private RationalNumber time;
	
	/* System time of the simulation start */
	private long startTime;
	
	/* Time of the tick represented by the system clock */
	private long tickTime;
	
	/* Time elapsed from the previous tick (in seconds) */
	private double elapsedTime;
	
	/**
	 * Internal constructor
	 * @param tick
	 * @param time
	 */
	protected SimulationTime(long tick, RationalNumber time) {
		init(tick, time, System.currentTimeMillis());
	}
	

	/**
	 * Initialized the class
	 */
	private void init(long tick, RationalNumber time, long startTime, long tickTime, double elapsedTime) {
		this.tick = tick;
		this.time = new RationalNumber(time);

		this.startTime = startTime;
		this.tickTime = tickTime;
		this.elapsedTime = elapsedTime;
	}
	
	private void init(long tick, RationalNumber time, long startTime) {
		init(tick, time, startTime, startTime, 0);
	}
	
	
	/**
	 * Copy constructor
	 * @param time
	 */
	protected SimulationTime(SimulationTime time) {
		init(time.tick, time.time, time.startTime, time.tickTime, time.elapsedTime);
	}
	
	
	/**
	 * Internal default constructor
	 */
	protected SimulationTime() {
		this(0, new RationalNumber(0));
	}
	
	
	/**
	 * Resets the time
	 */
	protected void reset() {
		init(0, new RationalNumber(0), System.currentTimeMillis());
	}
	
	
	/**
	 * Returns the number of ticks
	 * @return
	 */
	public long getTick() {
		return tick;
	}
	
	
	/**
	 * Returns the time elapsed from the previous tick
	 * @return
	 */
	public double getElapsedTime() {
		return elapsedTime;
	}
	
	
	/**
	 * Returns the time elapsed from the beginning of the current simulation
	 * @return
	 */
	public double getTotalTime() {
		return (tickTime - startTime) * 0.001;
	}

	
	/**
	 * Returns the simulation time
	 * @return
	 */
	public double getDoubleTime() {
		return time.doubleValue();
	}
	
	
	/**
	 * Returns the exact time
	 * @return
	 */
	public RationalNumber getTime() {
		return new RationalNumber(time);
	}
	
	
	/**
	 * Advances ticks by one
	 * @param dt
	 */
	protected void advanceTick() {
		tick++;
		
		// TODO: implement this correctly
		// Pausing/resuming should be taken into account (maybe)
		// Measure the speed of System.currentTimeMillis()
/*		long curTime = System.currentTimeMillis();
		if (curTime > tickTime) {
			elapsedTime = (curTime - tickTime) * 0.001;
		}
		else {
			elapsedTime = 0.0;
		}
		
		tickTime = curTime;*/
	}
	
	
	/**
	 * Sets the time
	 * @param t
	 */
	protected void setTime(RationalNumber t) {
		time = new RationalNumber(t);
	}


	public int compareTo(RationalNumber t) {
		return time.compareTo(t);
	}
}
