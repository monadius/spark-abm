/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package org.spark.space;

import org.spark.math.Vector;


public class SquareNode extends CircleNode {
	/**
	 * Default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	protected SquareNode(Space space, double radius) {
		super(space, radius);
	}
	
	@Override
	public int getShape() {
		return SpaceAgent.SQUARE;
	}
	
	
	@Override
	public boolean intersects(SpaceNode node) {
		if (node.space != space)
			return false;
		
		return node.intersectsWith(this);
	}
	
	
	@Override
	public boolean intersectsWith(SquareNode node) {
		double x1 = node.position.x;
		double y1 = node.position.y;
		double z1 = node.position.z;
		
		double x0 = position.x;
		double y0 = position.y;
		double z0 = position.z;
		
		double r1 = node.radius;
		double r0 = radius;
		
		Vector min0 = new Vector(x0 - r0, y0 - r0, z0 - r0);
		Vector max1 = new Vector(x1 + r1, y1 + r1, z1 + r1);
		Vector d = space.getVector(min0, max1);
		
		if (d.x < 0.0 || d.y < 0.0 || d.z < 0.0)
			return false;
		
		Vector min1 = new Vector(x1 - r1, y1 - r1, z1 - r1);
		Vector max0 = new Vector(x0 + r0, y0 + r0, z0 + r0);
		d = space.getVector(min1, max0);
		
		if (d.x < 0.0 || d.y < 0.0 || d.z < 0.0)
			return false;
		
		return true;
	}
	
	
	// TODO: intersection with a circle node


}
