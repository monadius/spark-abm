package org.spark.utils;

import com.spinn3r.log5j.Logger;

import cern.jet.random.engine.MersenneTwister;

/**
 * Makes the colt random generator thread safe
 * @author Monad
 *
 */
@SuppressWarnings("serial")
class SynchronizedMersenneTwister extends MersenneTwister {
	private static final Logger logger = Logger.getLogger();
	
	public SynchronizedMersenneTwister(int seed) {
		super(seed);
		logger.debug("Creating SynchronizedMersenneTwister");
	}
	
	
	@Override
	public synchronized int nextInt() {
		return super.nextInt();
	}
}
