package org.sparkabm.space;

import org.sparkabm.math.Vector;

/**
 * A square node for which collision are computed correctly
 */
public class Square2Node extends CircleNode {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	protected Square2Node(Space space, double radius) {
		super(space, radius);
	}
		
	
	@Override
	public int getShape() {
		return SpaceAgent.SQUARE2;
	}
		
		
	@Override
	public boolean intersects(SpaceNode node) {
		if (node.space != space)
			return false;
			
		return node.intersectsWith(this);
	}
		
		
	@Override
	public boolean intersectsWith(Square2Node node) {
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
	
	
	@Override
	public boolean intersectsWith(SquareNode node) {
		return intersectsWith((CircleNode) node);
	}

	

	@Override
	public boolean intersectsWith(CircleNode node) {
		// TODO: does not work in 3d spaces
		double r = radius;
		double cr = node.radius;
			
		Vector d = space.getVector(position, node.position);
		double x = Math.abs(d.x);
		double y = Math.abs(d.y);
//		double z = Math.abs(d.z);

		if (x > r + cr || y > r + cr) 
			return false;

		if (x <= r || y <= r)
			return true;

		return (x - r) * (x - r) + (y - r) * (y - r) <= cr * cr;
	}

}
