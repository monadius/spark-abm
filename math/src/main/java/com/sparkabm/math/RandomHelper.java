/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package com.sparkabm.math;

import com.spinn3r.log5j.Logger;

import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

/**
 * Auxiliary class for working with random numbers
 */
public class RandomHelper {
	// Logger
	private static final Logger logger = Logger.getLogger();
	
	// Random seed
	private static long rngSeed;
	// If true then the current time is used for seeding a generator
	private static boolean timeSeed = true;
	
	// If true then the operations are synchronized
	private static boolean synchronizedFlag = false;
	
	// The random number generators
	private static RandomEngine generator1;
	private static RandomEngine generator2;
	
	private static RandomEngine generator0;
	
	
	// The generators for normally distributed numbers
	private static Normal normalGenerator1;
	private static Normal normalGenerator2;
	
	private static Normal normalGenerator0;
	
	
    static
    {
    	logger.debug("Initializing RandomHelper class");
    	rngSeed = System.currentTimeMillis();
        generator1 = new MersenneTwister((int) rngSeed);
        generator2 = new MersenneTwister((int) rngSeed);
        normalGenerator1 = new Normal(0.0, 1.0, generator1);
        normalGenerator2 = new Normal(0.0, 1.0, generator2);
        
        generator0 = generator1;
        normalGenerator0 = normalGenerator1;
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
     * Resets the random generators
     */
    public static void reset(boolean synchronizedFlag) {
    	if (timeSeed) {
    		rngSeed = System.currentTimeMillis();
    	}

    	logger.debug("Generators are resetted: " + (int)rngSeed);
    	
    	RandomHelper.synchronizedFlag = synchronizedFlag;
   		generator1 = new MersenneTwister((int) rngSeed);
   		generator2 = new MersenneTwister((int) rngSeed);
        normalGenerator1 = new Normal(0.0, 1.0, generator1);
        normalGenerator2 = new Normal(0.0, 1.0, generator2);
   		
   		generator0 = generator1;
        normalGenerator0 = normalGenerator1;
    }
    
    
    /**
     * Activates the first generator 
     */
    public static void usePrimaryGenerator() {
    	generator0 = generator1;
    	normalGenerator0 = normalGenerator1;
    }

    /**
     * Activates the second generator 
     */
    public static void useSecondaryGenerator() {
    	generator0 = generator2;
    	normalGenerator0 = normalGenerator2;
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
		if (synchronizedFlag) {
			synchronized (RandomHelper.class) {
				return generator0.nextDouble() * (b - a) + a;
			}
		}
		
		return generator0.nextDouble() * (b - a) + a;
	}
	
	
	/**
	 * Returns a uniformly distributed random number in the interval (0, number)
	 * @param number
	 * @return
	 */
	public static double random(double number) {
		if (synchronizedFlag) {
			synchronized (RandomHelper.class) {
				return generator0.nextDouble() * number;
			}
		}
		
		return generator0.nextDouble() * number;
	}
	
	
	/**
	 * Returns a uniformly distributed number in the interval (0, 1)
	 * @return
	 */
	public static double random() {
		if (synchronizedFlag) {
			synchronized (RandomHelper.class) {
				return generator0.nextDouble();
			}
		}
		
		return generator0.nextDouble();
	}
	
	
	/**
	 * Returns a uniformly distributed random number from the interval (a,b)
	 * @param a a left end of an interval
	 * @param b a right end of an interval
	 * @return a random number
	 */
	public static double random(double a, double b) {
		if (synchronizedFlag) {
			synchronized (RandomHelper.class) {
				return generator0.nextDouble() * (b - a) + a;
			}
		}

		return generator0.nextDouble() * (b - a) + a;
	}

	
	/**
	 * Returns a normally distributed random number with the parameters (0,1)
	 * @return
	 */
	public static double normal() {
		if (synchronizedFlag) {
			synchronized (RandomHelper.class) {
				return normalGenerator0.nextDouble();
			}
		}
		
		return normalGenerator0.nextDouble();
	}
	
	
	/**
	 * Returns a normally distributed random number with the given
	 * mean and standard deviation
	 */
	public static double normal(double mean, double std) {
		if (synchronizedFlag) {
			synchronized (RandomHelper.class) {
				return normalGenerator0.nextDouble(mean, std);
			}
		}

		return normalGenerator0.nextDouble(mean, std);
	}
}



