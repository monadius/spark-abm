/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package org.spark.utils;

import cern.jet.random.engine.RandomEngine;

/**
 * Auxiliary class for working with random numbers
 */
// TODO: good random generator implementation
public class RandomHelper {
	private static long rngSeed;
	private static boolean timeSeed = true;
	
	private static RandomEngine generator; 
	
    static
    {
    	rngSeed = System.currentTimeMillis();
        generator = new cern.jet.random.engine.MersenneTwister((int) rngSeed);
    }
    
    
    /**
     * Sets the time seed
     */
    public static void setTimeSeed() {
    	timeSeed = true;
    }
    
    
    /**
     * Returns true if the time seed is used
     * @return
     */
    public static boolean isTimeSeed() {
    	return timeSeed;
    }
	
    
    /**
     * Sets the random generator's seed
     * @param seed
     */
    public static void setSeed(int seed) {
    	timeSeed = false;
    	rngSeed = seed;
    }
    
    
    /**
     * Resets the random generator
     */
    public static void reset(boolean synchronizedFlag) {
    	if (timeSeed) {
    		rngSeed = System.currentTimeMillis();
    	}

    	if (synchronizedFlag) {
    		generator = new SynchronizedMersenneTwister((int) rngSeed);
    	}
    	else {
    		generator = new cern.jet.random.engine.MersenneTwister((int) rngSeed);
    	}
    }
    
    
    /**
     * Returns the seed of the current random generator
     * @return
     */
    public static long getSeed() {
    	return rngSeed;
    }
    
	/**
	 * Returns a uniformly distributed random number from the interval (a,b)
	 * @param a a left end of an interval
	 * @param b a right end of an interval
	 * @return a random number
	 */
	public static double nextDoubleFromTo(double a, double b) {
//		return Math.random() * (b - a) + a;
		return generator.nextDouble() * (b - a) + a;
	}
	
	
	/**
	 * Returns a uniformly distributed random number in the interval (0, number)
	 * @param number
	 * @return
	 */
	public static double random(double number) {
//		return Math.random() * number;
		return generator.nextDouble() * number;
	}
	
	
	/**
	 * Returns a uniformly distributed number in the interval (0, 1)
	 * @return
	 */
	public static double random() {
		return generator.nextDouble();
	}
	
	
	/**
	 * Returns a uniformly distributed random number from the interval (a,b)
	 * @param a a left end of an interval
	 * @param b a right end of an interval
	 * @return a random number
	 */
	public static double random(double a, double b) {
//		return Math.random() * (b - a) + a; 
		return generator.nextDouble() * (b - a) + a;
	}
}
