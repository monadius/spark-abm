package org.spark.core;

/**
 * Defines constants for different execution modes
 * @author Monad
 *
 */
public final class ExecutionMode {
	public static final int SERIAL_MODE = 0;
	public static final int CONCURRENT_MODE = 1;
	public static final int PARALLEL_MODE = 3;
	
	/**
	 * Returns true if the given number corresponds
	 * to an execution mode
	 * @param mode
	 * @return
	 */
	public static boolean isMode(int mode) {
		switch (mode) {
		case SERIAL_MODE:
		case CONCURRENT_MODE:
		case PARALLEL_MODE:
			return true;
			
		default:
			return false;
		}
	}
	

	/**
	 * Converts the given number into the corresponding execution
	 * mode name
	 * @param mode
	 * @return
	 */
	public static String toString(int mode) {
		switch (mode) {
		case SERIAL_MODE:
			return "serial";
		case CONCURRENT_MODE:
			return "concurrent";
		case PARALLEL_MODE:
			return "parallel";
		
		default:
			return "undefined";
		}
	}
	
	
	/**
	 * Returns the mode number corresponding to the given mode name
	 * @param str
	 * @return
	 */
	public static int parse(String str) throws Exception {
		str = str.toLowerCase().intern();
		
		if (str == "serial")
			return SERIAL_MODE;
		
		if (str == "concurrent")
			return CONCURRENT_MODE;
		
		if (str == "parallel")
			return PARALLEL_MODE;
		
		throw new Exception("Illegal execution mode name: " + str);
	}
}
