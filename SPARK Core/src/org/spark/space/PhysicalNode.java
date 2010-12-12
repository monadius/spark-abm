package org.spark.space;

import org.jbox2d.collision.CircleDef;
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
	
	// Static or dynamic
	protected boolean dynamicFlag;
	
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
		cd.radius = (float) this.getRelativeSize();
		cd.density = 1.0f;
//		cd.friction = 0.3f;
		
		BodyDef bd = new BodyDef();
//		bd.isBullet = true;
		bd.allowSleep = true;
		
		body = world.createBody(bd);
		body.createShape(cd);
		
		if (dynamicFlag)
			body.setMassFromShapes();
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
