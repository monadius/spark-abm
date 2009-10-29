/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package org.spark.space;

import org.spark.utils.Vector;


/**
 * The simplest space node
 */
public class CircleNode extends SpaceNode {
	/**
	 * Default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	// The radius of the circle
	private double radius;
	
	/**
	 * Creates the circle node of the given radius
	 * @param space
	 * @param radius
	 */
	protected CircleNode(Space space, double radius) {
		super(space);
		
//		if (radius <= 0.0)
//			radius = 1.0;
		
		this.radius = radius;
	}
	
	/**
	 * Changes the radius
	 * @param r
	 */
	public void setRadius(double r) {
		if (r > space.maximumNodeRadius)
			space.maximumNodeRadius = r;
		
		radius = r;
	}
	
	
	/**
	 * For internal use only. Preserves maximumNodeRadius.
	 * @param r
	 */
	void setRadius0(double r) {
		radius = r;
	}

	
	@Override
	public boolean intersects(SpaceNode node) {
		if (node.space != space)
			return false;
		
		return node.intersectsWith(this);
	}
	
	
	@Override
	public double getRelativeSize() {
		return radius;
	}
	
	
	@Override
	public int getShape() {
		return SpaceAgent.CIRCLE;
	}
	

	@Override
	protected boolean intersectsWith(CircleNode circle) {
		Vector v = space.getVector(position, circle.position);

		double d = radius + circle.radius;
		if (d*d > v.lengthSquared())
			return true;
		
		return false;
	}

	@Override
	protected boolean intersectsWith(SquareNode node) {
		// TODO: write the real intersection code here
		Vector v = space.getVector(position, node.position);

		double d = radius + node.getRelativeSize();
		if (d*d > v.lengthSquared())
			return true;		
		
		return false;
	}

}
