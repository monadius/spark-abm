package org.spark.space;

import org.spark.utils.Vector;

@SuppressWarnings("serial")
public class PhysicalNode extends CircleNode {
	// Velocity
	protected Vector velocity = new Vector();
	// Force applied to this node
	protected Vector force = new Vector();
	// Mass of the node
	protected double mass;
	
	
	/**
	 * Creates a node with the given mass and radius
	 * @param space
	 * @param radius
	 * @param mass
	 */
	protected PhysicalNode(Space space, double radius, double mass) {
		super(space, radius);
		if (mass <= 1e-6)
			mass = 1;
		
		this.mass = mass;
		
	}
	
	
	/**
	 * Creates a node with the mass proportional to r^2
	 * @param space
	 * @param radius
	 */
	protected PhysicalNode(Space space, double radius) {
		this(space, radius, radius * radius);
	}
	
	
	/**
	 * Applies a force f to the node
	 * @param f
	 */
	public void applyForce(Vector f) {
		force.add(f);
	}
	
	
	/**
	 * Resets all forces applied to the node
	 */
	public void resetForces() {
		force.set(0);
	}
	
	
	/**
	 * Computes a new velocity and position of the node based
	 * on Newton's equation. Euler integration scheme is used.
	 * Changes newPosition only
	 * @param dt
	 */
	protected void integrate(double dt) {
		// da = f/m dt
		Vector a = new Vector(force);
		a.mul(mass * dt);
		// v += da
		velocity.add(a);
		
		// dv = v dt
		Vector dv = new Vector(velocity);
		dv.mul(dt);
		// p += dv
		newPosition.add(dv);

//		velocity.mul(0.3);
	}

}
