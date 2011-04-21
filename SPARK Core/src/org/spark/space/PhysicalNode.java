package org.spark.space;

import org.jbox2d.collision.CircleDef;
import org.jbox2d.collision.FilterData;
import org.jbox2d.collision.PolygonDef;
import org.jbox2d.collision.Shape;
import org.jbox2d.collision.ShapeDef;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;
import org.spark.math.Vector;

@SuppressWarnings("serial")
public class PhysicalNode extends CircleNode {
	// Box2d body
	protected Body body;
	// Force applied to this node
	protected Vector force = new Vector();
	
	// Shape information
	protected ShapeInfo shape;
	
	// Static or dynamic
	protected boolean dynamicFlag;
	
	
	// Describes a shape
	public static class ShapeInfo {
		public static final int CIRCLE = 0;
		public static final int RECTANGLE = 1;
		
		// For rectangles: half sizes
		// For circles: hx = radius
		public final float hx, hy;
		public final int type;

		// Private constructor
		private ShapeInfo(int type, float hx, float hy) {
			this.type = type;
			this.hx = hx;
			this.hy = hy;
		}
		
		// Creates a circular shape
		public static ShapeInfo createCircle(double r) {
			return new ShapeInfo(CIRCLE, (float)r, 0);
		}
		
		// Creates a rectangular shape
		public static ShapeInfo createRectangle(double hx, double hy) {
			return new ShapeInfo(RECTANGLE, (float)hx, (float)hy);
		}
	}
	
	
	/**
	 * Creates a node with the given mass and radius
	 * @param space
	 * @param radius
	 * @param mass
	 */
	protected PhysicalNode(Space space, double radius, int type) {
		super(space, radius);
		this.body = null;
		this.dynamicFlag = (type == SpaceAgent.DYNAMIC_CIRCLE) ? true : false;
	}
	

	/**
	 * Adds a new physical body into the world
	 * @param world
	 */
	protected void init(World world) {
		CircleDef cd = new CircleDef();
		double r = this.getRelativeSize();
		cd.radius = (float) r;
		cd.density = 1.0f;
//		cd.friction = 0.3f;
		
		shape = ShapeInfo.createCircle(r);
		
		BodyDef bd = new BodyDef();
//		bd.isBullet = true;
		bd.allowSleep = true;
//		bd.fixedRotation = true;
//		bd.linearDamping = 0.01f;
//		bd.angularDamping = 0.01f;
		
		body = world.createBody(bd);
		body.createShape(cd);
		body.setUserData(this);

		
		if (dynamicFlag)
			body.setMassFromShapes();
	}
	
	
	/**
	 * Changes the shape of the associated physical body
	 * @param newShape
	 */
	public void changeShape(ShapeInfo newShape) {
		if (body == null || newShape == null)
			return;
		
		// Create the definition for the new shape
		ShapeDef sd = null;
		switch (newShape.type) {
		// CIRCLE
		case ShapeInfo.CIRCLE:
			CircleDef cd = new CircleDef();
			cd.radius = newShape.hx;
			cd.density = 1.0f;
			sd = cd;
			break;
			
		// RECTANGLE
		case ShapeInfo.RECTANGLE:
			PolygonDef pd = new PolygonDef();
			pd.setAsBox(newShape.hx, newShape.hy);
			pd.density = 1.0f;
			sd = pd;
			break;
		}
		
		if (sd == null)
			return;

		// Remove the old shape
		Shape s = body.getShapeList();
		FilterData fd = new FilterData();
		if (s != null) {
			fd.set(s.getFilterData());
			body.destroyShape(s);
		}

		// Set the new shape
		sd.filter = fd;
		body.createShape(sd);
		this.shape = newShape;

		if (dynamicFlag)
			body.setMassFromShapes();
	}
	
	
	@Override
	public void setRadius(double r) {
		if (r == getRelativeSize())
			return;
		
		super.setRadius(r);
		changeShape(ShapeInfo.createCircle(getRelativeSize()));		
	}
	
	
	/**
	 * Updates proxies for each shape attached to the node body
	 */
	public void updateProxy() {
		if (body == null)
			return;
		
		World world = body.getWorld();
		if (world == null)
			return;
		
		for (Shape shape = body.getShapeList(); shape != null; shape = shape.getNext()) {
			world.refilter(shape);
		}
	}
	
	
	/**
	 * Fixes the rotation of the corresponding body
	 */
	public void setFixedRotationFlag(boolean flag) {
		if (body == null)
			return;
		
		if (flag)
		{
			body.m_flags |= Body.e_fixedRotationFlag;
			body.m_I = 0;
			body.m_invI = 0;
		}
		else
		{
			body.m_flags &= ~Body.e_fixedRotationFlag;
			
			// TODO: restore m_I and m_invI
		}
	}
	
	
	/**
	 * Adds the collision category
	 * @param category
	 */
	public void addCollisionCategory(int category) {
		if (category < 0 || category >= 16)
			return;
		
		if (body != null) {
			FilterData fd = new FilterData();
			
			for (Shape shape = body.getShapeList(); shape != null; shape = shape.getNext()) {
				fd.set(shape.getFilterData());
				fd.categoryBits |= 1 << category;
				shape.setFilterData(fd);
			}
		}
	}
	
	
	/**
	 * Sets the collision category
	 * @param category
	 */
	public void setCollisionCategory(int category) {
		if (category < 0 || category >= 16)
			return;
		
		if (body != null) {
			FilterData fd = new FilterData();
			
			for (Shape shape = body.getShapeList(); shape != null; shape = shape.getNext()) {
				fd.set(shape.getFilterData());
				fd.categoryBits = 1 << category;
				shape.setFilterData(fd);
			}			
		}
	}
	
	
	/**
	 * Adds the collision mask
	 * @param mask
	 */
	public void addCollisionMask(int mask) {
		if (mask < 0 || mask >= 16)
			return;
		
		if (body != null) {
			FilterData fd = new FilterData();
			
			for (Shape shape = body.getShapeList(); shape != null; shape = shape.getNext()) {
				fd.set(shape.getFilterData());
				fd.maskBits |= 1 << mask;
				shape.setFilterData(fd);
			}
		}
	}
	
	
	/**
	 * Removes the collision category
	 * @param category
	 */
	public void removeCollisionCategory(int category) {
		if (category < 0 || category >= 16)
			return;
		
		if (body != null) {
			int cat = 0xFFFF ^ (1 << category);
			FilterData fd = new FilterData();
			
			for (Shape shape = body.getShapeList(); shape != null; shape = shape.getNext()) {
				fd.set(shape.getFilterData());
				fd.categoryBits &= cat;
				shape.setFilterData(fd);
			}
		}
	}
	
	
	/**
	 * Removes the collision mask
	 * @param mask
	 */
	public void removeCollisionMask(int mask) {
		if (mask < 0 || mask >= 16)
			return;
		
		if (body != null) {
			mask = 0xFFFF ^ (1 << mask);
			FilterData fd = new FilterData();
			
			for (Shape shape = body.getShapeList(); shape != null; shape = shape.getNext()) {
				fd.set(shape.getFilterData());
				fd.maskBits &= mask;
				shape.setFilterData(fd);
			}
		}
	}
	
	
	/**
	 * Returns the information about agent's shape
	 * @return
	 */
	public ShapeInfo getShapeInfo() {
		return shape;
	}
	
	
	@Override
	public void setRotation(double theta) {
		super.setRotation(theta);
		
		if (body != null) {
			body.setXForm(body.getPosition(), (float)theta);
		}
	}
	
	
	/**
	 * Applies a force f to the node
	 * @param f
	 */
	public void applyForce(Vector f) {
		if (body != null) {
			Vec2 force = new Vec2((float) f.x, (float) f.y);
			Vec2 center = body.getWorldCenter();
			body.applyForce(force, center);
		}
		force.add(f);
	}
	
	
	/**
	 * Applies an impulse to the node
	 * @param f
	 */
	public void applyImpulse(Vector f) {
		if (body != null) {
			Vec2 impulse = new Vec2((float) f.x, (float) f.y);
			Vec2 center = body.getWorldCenter();
			body.applyImpulse(impulse, center);
		}
	}
	
	
	/**
	 * Returns the velocity of the node
	 * @return
	 */
	public Vector getVelocity() {
		if (body != null) {
			Vec2 v = body.getLinearVelocity();
			return new Vector(v.x, v.y, 0);
		}
		
		// Return the zero vector
		return new Vector();
	}
	
	
	/**
	 * Resets all forces applied to the node
	 */
	public void resetForces() {
		force.set(0);
	}
	
	
	/**
	 * Updates the position of the node using the position of the associated
	 * physical body
	 */
	public void updatePosition() {
		if (body == null)
			return;
		
		Vec2 pos = body.getPosition();
		this.newPosition = new Vector(pos.x, pos.y, 0);
	}
	

}
