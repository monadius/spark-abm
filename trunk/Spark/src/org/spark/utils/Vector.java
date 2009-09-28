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
public class Vector implements Serializable {
	/**
	 * Default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	public double	x, y, z;

	public Vector(Vector v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}
	
	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector(double v) {
		this.x = this.y = this.z = v;
	}

	public Vector(java.awt.Color color) {
		set(color);
	}
	
	public Vector() {
		this.x = this.y = this.z = 0;
	}
	
	public Vector set(Vector v) {
		x = v.x;
		y = v.y;
		z = v.z;
		
		return this;
	}
	
	public Vector set(java.awt.Color color) {
		this.x = color.getRed() / 255.0;
		this.y = color.getGreen() / 255.0;
		this.z = color.getBlue() / 255.0;
		
		return this;
	}
	
	public Vector set(double x, double y) {
		this.x = x;
		this.y = y;
		
		return this;
	}
	
	public Vector set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		
		return this;
	}
	
	public Vector set(double v) {
		x = y = z = v;
		
		return this;
	}
	
	public Vector add(Vector v) {
		x += v.x;
		y += v.y;
		z += v.z;
		
		return this;
	}
	
	public Vector add(Vector v, double a) {
		x += v.x * a;
		y += v.y * a;
		z += v.z * a;
		
		return this;
	}
	
	public Vector sub(Vector v) {
		x -= v.x;
		y -= v.y;
		z -= v.z;
		
		return this;
	}
	
	public Vector mul(double v) {
		x *= v;
		y *= v;
		z *= v;
		
		return this;
	}
	
	public Vector div(double v) {
		x /= v;
		y /= v;
		z /= v;
		
		return this;
	}
	
	public Vector negate() {
		x = -x;
		y = -y;
		z = -z;
		
		return this;
	}
	
	public double dot(Vector v) {
		return x*v.x + y*v.y + z*v.z;
	}
	
	
	public Vector cross(Vector v) {
		double xx, yy, zz;
		
		xx = y * v.z - z * v.y;
		yy = z * v.x - x * v.z;
		zz = x * v.y - y * v.x;
		
		x = xx;
		y = yy;
		z = zz;
		
		return this;
	}
	
	
	/**
	 * Return component of vector parallel to a unit
	 * basis vector (it is assumed to have unit length)
	 * @param unitBasis
	 * @return
	 */
	public Vector parallelComponent(Vector unitBasis) {
		double projection = dot(unitBasis);
		return new Vector(unitBasis).mul(projection);
	}
	
	
	public Vector perpendicularComponent(Vector unitBasis) {
		return parallelComponent(unitBasis).negate().add(this);
	}
	
	
	public double length() {
		return Math.sqrt(x*x + y*y + z*z);
	}

	/**
	 * @deprecated
	 * @return
	 */
	public double norm2() {
		return x*x + y*y + z*z;
	}
	
	public double lengthSquared() {
		return x*x + y*y + z*z;
	}
	
	public Vector normalize() {
		double l = length();
		if (Math.abs(l) < 1e-10) {
			x = 0;
			y = 0;
			z = 0;
		}
		else {
			l = 1 / l;
			x *= l;
			y *= l;
			z *= l;
		}
		
		return this;
	}
	
	
	public Vector truncate(double min, double max) {
		if (x < min) x = min;
		else if (x > max) x = max;
		
		if (y < min) y = min;
		else if (y > max) y = max;
		
		if (z < min) z = min;
		else if (z > max) z = max;
		
		return this;
	}
	
	
	public Vector truncateDistance(double max) {
		double d = x*x + y*y + z*z;
		
		if (d > max*max) {
			d = max / Math.sqrt(d);
			x *= d;
			y *= d;
			z *= d;
		}
		
		return this;
	}
	

	
	public Vector truncateLength(double max) {
		double d = x*x + y*y + z*z;
		
		if (d > max*max) {
			d = max / Math.sqrt(d);
			x *= d;
			y *= d;
			z *= d;
		}
		
		return this;
	}

	
	public String toString() {
		return x + ";" + y + ";" + z;
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
	
	
	public static Vector getVector(double length, double dir) {
		dir *= Math.PI / 180;
		return new Vector(length * Math.cos(dir), length * Math.sin(dir), 0);
	}
	
	
	public static Vector randomVector(double min, double max) {
		double x = RandomHelper.nextDoubleFromTo(min, max);
		double y = RandomHelper.nextDoubleFromTo(min, max);
		double z = RandomHelper.nextDoubleFromTo(min, max);
		
		return new Vector(x, y, z);
	}

	
	public static Vector randomVector2d(double min, double max) {
		double x = RandomHelper.nextDoubleFromTo(min, max);
		double y = RandomHelper.nextDoubleFromTo(min, max);
		double z = 0;
		
		return new Vector(x, y, z);
	}

	
	public static Vector randomVector(double radius) {
		return randomUnitVector().mul(RandomHelper.random() * radius);
	}

	
	public static Vector randomVector2d(double radius) {
		return randomUnitVector2d().mul(RandomHelper.random() * radius);
	}

	
	
	public static Vector randomUnitVector() {
		return randomVector(-1, 1).normalize();
	}

	
	public static Vector randomUnitVector2d() {
		return randomVector2d(-1, 1).normalize();
	}

	
	
	public static Vector parseVector(String str) {
		double x, y, z;

		String[] vals = str.split(";", 3);
		if (vals.length < 3)
			return new Vector();
		
		x = Double.parseDouble(vals[0]);
		y = Double.parseDouble(vals[1]);
		z = Double.parseDouble(vals[2]);
		
		return new Vector(x, y, z);
	}
	
	
	public static Vector fromAWTColor(java.awt.Color color) {
		double x, y, z;
		
		x = color.getRed() / 255.0;
		y = color.getGreen() / 255.0;
		z = color.getBlue() / 255.0;
		
		return new Vector(x, y, z);
	}
	
	
	public static double distance(Vector v1, Vector v2) {
		double x = v1.x - v2.x;
		double y = v1.y - v2.y;
		double z = v1.z - v2.z;
		
		return Math.sqrt(x*x + y*y + z*z);
	}
	
	@Override
	public boolean equals(Object v) {
		if (v == this)
			return true;
		
		if (v instanceof Vector) {
			Vector vv = (Vector) v;
			return vv.x == x && vv.y == y && vv.z == z;
		}
		
		if (v instanceof Vector4d) {
			Vector4d vv = (Vector4d) v;
			return vv.a == 0 && vv.x == x && vv.y == y && vv.z == z;
		}
		
		return false;
	}
}
