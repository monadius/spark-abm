package org.spark.core;

import java.io.Serializable;

import org.spark.math.RationalNumber;

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
	
	/**
	 * Internal constructor
	 * @param tick
	 * @param time
	 */
	SimulationTime(long tick, RationalNumber time) {
		this.tick = tick;
		this.time = new RationalNumber(time);
	}
	
	
	/**
	 * Copy constructor
	 * @param time
	 */
	SimulationTime(SimulationTime time) {
		this.tick = time.tick;
		this.time = new RationalNumber(time.time);
	}
	
	
	/**
	 * Internal default constructor
	 */
	SimulationTime() {
		this(0, new RationalNumber(0));
	}
	
	
	/**
	 * Resets the time
	 */
	void reset() {
		tick = 0;
		time = new RationalNumber(0);
	}
	
	
	/**
	 * Returns the number of ticks
	 * @return
	 */
	public long getTick() {
		return tick;
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
	void advanceTick() {
		tick++;
	}
	
	
	/**
	 * Sets the time
	 * @param t
	 */
	void setTime(RationalNumber t) {
		time = new RationalNumber(t);
	}


	public int compareTo(RationalNumber t) {
		return time.compareTo(t);
	}
}
