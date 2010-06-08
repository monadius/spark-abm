/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package org.spark.utils;

import java.io.Serializable;

/**
 * Basic implementation of a vector class
 */
public class Vector4d implements Serializable {
	/**
	 * Default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	public double	x, y, z, a;

	public Vector4d(Vector4d v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
		this.a = v.a;
	}
	
	
	public Vector4d(Vector v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}
	
	
	public Vector4d(double x, double y, double z, double a) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.a = a;
	}
	
	public Vector4d(double v) {
		this.x = this.y = this.z = this.a = v;
	}

	public Vector4d(java.awt.Color color) {
		set(color);
	}
	
	public Vector4d() {
		this.x = this.y = this.z = this.a = 0;
	}
	
	public Vector4d set(Vector4d v) {
		x = v.x;
		y = v.y;
		z = v.z;
		a = v.a;
		
		return this;
	}

	public Vector4d set(Vector v) {
		x = v.x;
		y = v.y;
		z = v.z;
		a = 0;
		
		return this;
	}

	
	public Vector4d set(java.awt.Color color) {
		this.x = color.getRed() / 255.0;
		this.y = color.getGreen() / 255.0;
		this.z = color.getBlue() / 255.0;
		
		return this;
	}
	
	
	public Vector4d set(double x, double y, double z, double a) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.a = a;
		
		return this;
	}
	

	public Vector4d set(double v) {
		x = y = z = a = v;
		
		return this;
	}
	
	
	public Vector4d add(Vector4d v) {
		x += v.x;
		y += v.y;
		z += v.z;
		a += v.a;
		
		return this;
	}


	public Vector4d add(Vector v) {
		x += v.x;
		y += v.y;
		z += v.z;
		
		return this;
	}

	
	public Vector4d sub(Vector4d v) {
		x -= v.x;
		y -= v.y;
		z -= v.z;
		a -= v.a;
		
		return this;
	}
	
	public Vector4d mul(double v) {
		x *= v;
		y *= v;
		z *= v;
		a *= v;
		
		return this;
	}
	
	public Vector4d div(double v) {
		x /= v;
		y /= v;
		z /= v;
		a /= v;
		
		return this;
	}
	
	public Vector4d negate() {
		x = -x;
		y = -y;
		z = -z;
		a = -a;
		
		return this;
	}
	
	public double dot(Vector4d v) {
		return x*v.x + y*v.y + z*v.z + a*v.z;
	}
	
	public double length() {
		return Math.sqrt(x*x + y*y + z*z + a*a);
	}
	
	public double norm2() {
		return x*x + y*y + z*z + a*a;
	}
	
	public Vector4d normalize() {
		double l = length();
		if (Math.abs(l) < 1e-10) {
			x = 0;
			y = 0;
			z = 0;
			a = 0;
		}
		else {
			l = 1 / l;
			x *= l;
			y *= l;
			z *= l;
			a *= l;
		}
		
		return this;
	}
	
	
	public Vector4d truncate(double min, double max) {
		if (x < min) x = min;
		else if (x > max) x = max;
		
		if (y < min) y = min;
		else if (y > max) y = max;
		
		if (z < min) z = min;
		else if (z > max) z = max;
		
		if (a < min) a = min;
		else if (a > max) a = max;
		
		return this;
	}
	
	
	public Vector4d truncateDistance(double max) {
		double d = x*x + y*y + z*z + a*a;
		
		if (d > max*max) {
			d = 1.0 / Math.sqrt(d);
			x *= d;
			y *= d;
			z *= d;
			a *= d;
		}
		
		return this;
	}
	
	
	public String toString() {
		return x + ";" + y + ";" + z + ";" + a;
	}
	
	public java.awt.Color toAWTColor() {
		float r = (float) x;
		float g = (float) y;
		float b = (float) z;

		if (r < 0) r = 0;
		else if (r > 1) r = 1;
		
		if (g < 0) g = 0;
		else if (g > 1) g = 1;
		
		if (b < 0) b = 0;
		else if (b > 1) b = 1;
		
		return new java.awt.Color(r, g, b);
	}
	
	
	public static Vector4d randomVector(double min, double max) {
		double x = RandomHelper.nextDoubleFromTo(min, max);
		double y = RandomHelper.nextDoubleFromTo(min, max);
		double z = RandomHelper.nextDoubleFromTo(min, max);
		double a = RandomHelper.nextDoubleFromTo(min, max);
		
		return new Vector4d(x, y, z, a);
	}
	
	
	public static Vector4d fromAWTColor(java.awt.Color color) {
		double x, y, z;
		
		x = color.getRed() / 255.0;
		y = color.getGreen() / 255.0;
		z = color.getBlue() / 255.0;
		
		return new Vector4d(x, y, z, 0);
	}
	
	
	public static Vector4d interpolate(Vector4d start, Vector4d end, double t) {
		Vector4d v = new Vector4d(start);

		v.x = start.x * (1 - t) + end.x * t;
		v.y = start.y * (1 - t) + end.y * t;
		v.z = start.z * (1 - t) + end.z * t;
		v.a = start.a * (1 - t) + end.a * t;
		
		return v;
	}
	
	
	@Override
	public boolean equals(Object v) {
		if (v == null)
			return false;
		
		if (v == this)
			return true;
		
		if (v instanceof Vector4d) {
			Vector4d vv = (Vector4d) v;
			return vv.x == x && vv.y == y && vv.z == z && vv.a == a;
		}
		
		if (v instanceof Vector) {
			Vector vv = (Vector) v;
			return vv.x == x && vv.y == y && vv.z == z && a == 0;
		}
		
		return false;
	}
	
}
