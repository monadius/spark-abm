package org.spark.utils;

import java.util.ArrayList;

import org.spark.math.Vector;

/**
 * Auxiliary operations on strings
 */
public class StringUtils {
	/**
	 * Splits a string into a list of substrings
	 */
	public static ArrayList<String> split(String str, String regexpr) {
		ArrayList<String> result = new ArrayList<String>();

		if (str == null)
			return result;
		
		String[] strs = str.split(regexpr);
		for (String s : strs) {
			result.add(s);
		}

		return result;
	}

	
	/**
	 * Returns the number represented by the string. 
	 * Returns 0 in a case of error.
	 */
	public static double StringToDouble(String str) {
		if (str == null)
			return 0.0;

		try	{
			return Double.parseDouble(str);
		}
		catch (NumberFormatException ex) {
			return 0.0;
		}
	}
	
	
	/**
	 * Returns the integer number represented by the string. 
	 * Returns 0 in a case of error.
	 */
	public static int StringToInteger(String str) {
		if (str == null)
			return 0;

		try	{
			return Integer.parseInt(str);
		}
		catch (NumberFormatException ex) {
			return 0;
		}
	}
	
	
	/**
	 * Converts the string to a vector
	 */
	public static Vector StringToVector(String str, String separator) {
		if (str == null)
			return null;
		
		String[] components = str.split(separator);
	
		try {
			switch (components.length) {
			case 0:
				return null;

			case 1:
				double v = Double.valueOf(components[0]);
				return new Vector(v);

			case 2:
				double v1 = Double.valueOf(components[0]);
				double v2 = Double.valueOf(components[1]);
				return new Vector(v1, v2, 0);
			
			default:
				double x = Double.valueOf(components[0]);
				double y = Double.valueOf(components[1]);
				double z = Double.valueOf(components[2]);
				return new Vector(x, y, z);
			}
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

}
