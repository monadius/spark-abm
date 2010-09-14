package org.spark.math;

import java.io.Serializable;

/**
 * Represents a rational number
 * @author Monad
 *
 */
public class RationalNumber extends Number implements Comparable<RationalNumber>, Serializable {
	/* Serial version UID */
	private static final long serialVersionUID = -7438309292079517592L;
	
	public static final RationalNumber ZERO = new RationalNumber(0);
	public static final RationalNumber ONE = new RationalNumber(1);
	
	/* Parts of a rational number */
	private long numerator, denominator;

	
	/**
	 * Creates a rational number with given numerator
	 * and denominator
	 * @param numerator
	 * @param denominator if denominator == 0, then it is ignored
	 * (it is considered to be 1)
	 */
	public RationalNumber(long numerator, long denominator) {
		if (denominator == 0)
			denominator = 1;
		
		this.numerator = numerator;
		this.denominator = denominator;
		normalize();
	}
	
	
	/**
	 * Creates a copy of the given rational number
	 * @param r
	 */
	public RationalNumber(RationalNumber r) {
		this.numerator = r.numerator;
		this.denominator = r.denominator;
		
		// We don't need to normalize here
	}
	
	
	/**
	 * Creates a rational representation of an integer
	 * @param n
	 */
	public RationalNumber(long n) {
		this.numerator = n;
		this.denominator = 1;
	}
	
	
	public RationalNumber(double n) {
		// TODO: implement
		throw new Error("Unimplemented");
	}
	
	
	/**
	 * Adds the rational number 'b' to this rational number
	 * Result is saved in this rational number
	 * @param b
	 * @return
	 */
	public RationalNumber add(RationalNumber b) {
		// TODO: possible overflow problems
		numerator = numerator * b.denominator + b.numerator * denominator;
		denominator = b.denominator * denominator;
		normalize();

		return this;
	}
	
	
	/**
	 * Subtracts 'b' from the rational number
	 * @param b
	 * @return
	 */
	public RationalNumber sub(RationalNumber b) {
		// TODO: possible overflow issues
		numerator = numerator * b.denominator - b.numerator * denominator;
		denominator = b.denominator * denominator;
		normalize();
		
		return this;
	}

	
	/**
	 * Divides the number by 'b'
	 * @param b
	 * @return
	 */
	public RationalNumber div(RationalNumber b) {
		numerator *= b.denominator;
		denominator *= b.numerator;
		normalize();
		
		return this;
	}
	
	/**
	 * Multiplies the rational number by 'b'
	 * Result is saved in this rational number
	 * @param b
	 * @return
	 */
	public RationalNumber mul(RationalNumber b) {
		// TODO: possible overflow problems
		numerator *= b.numerator;
		denominator *= b.denominator;
		normalize();
		
		return this;
	}
	
	
	/**
	 * Normalizes the number
	 */
	private void normalize() {
		if (denominator < 0) {
			denominator = -denominator;
			numerator = -numerator;
		}
				
		long p = numerator;
		long q = denominator;
		long r;
		
		if (q == 1)
			return;
		
		if (p < 0) p = -p;

		// Euclid's algorithm for the GCD of (p, q)
		for (r = p % q; 
			 r != 0;
			 p = q, q = r, r = p % q);
		
		numerator /= q;
		denominator /= q;
	}
	
	/* Implementation of Number class */
	@Override
	public double doubleValue() {
		return (double) numerator / denominator;
	}

	@Override
	public float floatValue() {
		return (float) numerator / denominator;
	}

	@Override
	public int intValue() {
		return (int)(numerator / denominator);
	}

	@Override
	public long longValue() {
		return numerator / denominator;
	}

	/**
	 * Compares two rational numbers
	 */
	public int compareTo(RationalNumber o) {
		// TODO: overflow issues
		long result = numerator * o.denominator - o.numerator * denominator;
		
		if (result < 0)
			return -1;
		
		if (result > 0)
			return 1;
		
		return 0;
	}
	
	
	@Override
	public String toString() {
		if (denominator == 1)
			return Long.toString(numerator);
		
		return Long.toString(numerator) + "/" + Long.toString(denominator); 
	}
	
	@Override
	public int hashCode() {
		return (int)numerator ^ (int)denominator;
	}
	
	
	/**
	 * Parses the given string and returns the coded rational number
	 * @param str
	 * @return
	 */
	public static RationalNumber parse(String str) {
		String[] tmp = str.split("/");
		if (tmp == null || tmp.length == 0)
			return ZERO;
		
		if (tmp.length == 1)
			return new RationalNumber(Long.parseLong(tmp[0]));
		
		return new RationalNumber(Long.parseLong(tmp[0]),
				Long.parseLong(tmp[1]));
	}
	
}
