package org.sparkabm.space;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import org.sparkabm.core.Agent;
import org.sparkabm.math.Vector;

/**
 * A revolution joint for physical nodes
 * @author Alexey
 *
 */
@SuppressWarnings("serial")
public class RevoluteJointLink extends SpaceLink {
	// Parameters for creating a joint
	private Vec2 anchor;
	private float lowerLimit, upperLimit;
	private float motorSpeed, motorTorque;
	
	private PhysicalSpace2d space;
	private RevoluteJoint joint;
	
	/**
	 * Default constructor
	 */
	public RevoluteJointLink() {
	}
	
	
	@Override
	public void die() {
		if (joint != null) {
			space.destroyJoint(joint);
			joint = null;
		}
		
		super.die();
	}
	
	
	public void setAnchor(Vector v) {
		anchor = new Vec2((float) v.x, (float) v.y);
	}
	

	/**
	 * Sets the joint limits
	 */
	public void setLimits(double lowerLimit, double upperLimit) {
		this.lowerLimit = (float) lowerLimit;
		this.upperLimit = (float) upperLimit;
	}
	

	/**
	 * Returns the joint angle
	 */
	public double getAngle() {
		if (joint == null)
			return 0.0;
		
		return joint.getJointAngle();
	}
	
	
	/**
	 * Sets the joint motor speed
	 */
	public void setMotorSpeed(double speed) {
		if (joint != null) {
			joint.setMotorSpeed((float) speed);
		}
		else {
			motorSpeed = (float) speed;
		}
	}
	
	
	
	/**
	 * Sets the joint motor max torque
	 */
	public void setMotorTorque(double torque) {
		if (joint != null) {
			joint.setMaxMotorTorque((float) torque);
		}
		else {
			motorTorque = (float) torque;
		}
	}
	
	
	@Override
	public void connect(Agent end1, Agent end2) {
		// Cannot reconnect the link
		if (joint != null)
			return;
		
		if (!(end1 instanceof SpaceAgent) || !(end2 instanceof SpaceAgent))
			return;
		
		SpaceAgent e1 = (SpaceAgent) end1;
		SpaceAgent e2 = (SpaceAgent) end2;
		
		// Cannot connect agents in different spaces */
		if (e1.getSpace() != e2.getSpace())
			return;
		
		SpaceNode node1 = e1.getNode();
		SpaceNode node2 = e2.getNode();
		
		if (!(node1 instanceof PhysicalNode) || !(node2 instanceof PhysicalNode))
			return;
		
		PhysicalNode n1 = (PhysicalNode) node1;
		PhysicalNode n2 = (PhysicalNode) node2;
		
		if (n1.body == null || n2.body == null)
			return;
		
		
		RevoluteJointDef jd = new RevoluteJointDef();
		
		
		if (lowerLimit < upperLimit) {
			jd.lowerAngle = lowerLimit;
			jd.upperAngle = upperLimit;
			jd.enableLimit = true;
		}
		
		if (motorTorque != 0) {
			jd.enableMotor = true;
			jd.maxMotorTorque = motorTorque;
			jd.motorSpeed = motorSpeed;
		}
		
		if (anchor == null)
			anchor = n1.body.getWorldCenter();
		
		jd.initialize(n1.body, n2.body, anchor);
		
		space = (PhysicalSpace2d) n1.getSpace();
		joint = (RevoluteJoint) space.createJoint(jd);
		
		super.connect(end1, end2);
	}
	
	
	/**
	 * Returns the first point to which the link is connected
	 * @return
	 */
	public Vector getPoint1() {
		if (joint == null)
			return null;
		Vec2 v = joint.getAnchor1();
		return new Vector(v.x, v.y, 0);
	}
	
	
	/**
	 * Returns the second point to which the link is connected
	 * @return
	 */
	public Vector getPoint2() {
		if (joint == null)
			return null;
		Vec2 v = joint.getAnchor2();
		return new Vector(v.x, v.y, 0);
	}
	
	
	/**
	 * Returns a vector-distance (componentwise distance)
	 * between connected agents
	 * @return null if one or both ends are not initialized
	 */
	public Vector getVector() {
		if (joint == null)
			return null;
		
		Space space = ((SpaceAgent) end1).getSpace();
		return space.getVector(getPoint1(), getPoint2());
	}
	
}
