/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package org.spark.space;

import java.io.Serializable;

import org.spark.utils.Vector;
import org.spark.utils.Vector4d;


/**
 * The basic space node class 
 */
public abstract class SpaceNode implements Serializable {
	/**
	 * Default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	/* A reference to space where this node exists */
	protected transient Space space;
	
	// Auxiliary references for fast query operation
	protected transient SpaceNode		next;
	protected transient SpaceNode		prev;
	
	// The agent associated with this space node
	protected SpaceAgent	agent;
	
	// Current position
	protected Vector		position;
	// Vector for changing the position
	protected Vector		newPosition;
	
	// Which action to perform on this node
	protected int			state;

	// Indicates that the node was removed from the space
	boolean nodeIsRemoved = false;

	
	protected final static int NODE_IS_CREATING = 0x1;
	protected final static int NODE_IS_REMOVING = 0x2;
	protected final static int NODE_IS_MOVING	= 0x4;
	// Old position
//	protected Vector		oldPosition;
	
	/**
	 * The default constructor
	 * @param space
	 */
	protected SpaceNode(Space space) {
//		System.out.println("Creating space node");

		this.space = space;
		
		position = new Vector();
//		oldPosition = new Vector();
		newPosition = new Vector();
	}
	

	/**
	 * Returns node's shape code
	 * @return
	 */
	public abstract int getShape();
	
	/**
	 * Gets the current position
	 * @return
	 */
	public Vector getPosition() {
		return new Vector(position);
	}
	
	
	/**
	 * Returns node's space
	 * @return
	 */
	public Space getSpace() {
		return space;
	}
	
	
	/**
	 * Gets the new position
	 * @return
	 */
	public Vector getNewPosition() {
		// FIXME: should it be new Vector(newPosition)?
		return newPosition;
	}
	
	/**
	 * Associates the agent with the node
	 * @param agent
	 */
	protected void AssociateAgent(SpaceAgent agent) {
		this.agent = agent;
	}
	
	/**
	 * Sets the random position
	 */
	public void setRandomPosition() {
//		oldPosition.set(position);
//		position.set(space.getRandomPosition());
		
//		space.changeNodePosition(this, oldPosition, position);
		newPosition.set(space.getRandomPosition());
		space.changeNodePosition(this);
	}
	
	
	/**
	 * Moves the node to the position v
	 * @param v
	 */
	public void jump(Vector v) {
//		oldPosition.set(position);
//		position.set(v);
//		Observer.getSpace().changeNodePosition(this, oldPosition, position);
		newPosition.set(v);
		space.changeNodePosition(this);
	}
	
	/**
	 * Changes the position by the vector dv
	 * @param dv
	 */
	public void move(Vector dv) {
//		oldPosition.set(position);
//		position.add(dv);
//		Observer.getSpace().changeNodePosition(this, oldPosition, position);
		newPosition.add(dv);
		space.changeNodePosition(this);
	}
	
	/**
	 * Tests whether two nodes intersect
	 * @param node
	 * @return
	 */
	public abstract boolean intersects(SpaceNode node);
	
	
	/**
	 * A method for testing an intersection with a circle
	 * @param node
	 * @return
	 */
	protected abstract boolean intersectsWith(CircleNode node);
	
	
	/**
	 * A method for testing an intersection with a square
	 * @param node
	 * @return
	 */
	protected abstract boolean intersectsWith(SquareNode node);
	
	/**
	 * Returns the relative size of the node
	 * @return
	 */
	public double getRelativeSize() {
		return 1.0;
	}
	
	
	//*********************
	// Rendering interface
	//*********************
	
	protected Vector4d color = new Vector4d();
	
	public void setColor(Vector color) {
		this.color.set(color);
	}
	
	
	public void setColor(Vector4d color) {
		this.color.set(color);
	}
	
	
	public Vector4d getColor() {
		// TODO: new Vector(color) but then it is impossible to
		// modify individual color components
		return color;
	}
	
	
	public Vector getColor3() {
		return new Vector(color.x, color.y, color.z);
	}
	
	
	public double getAlpha() {
		return color.a;
	}
	
	
	public void setAlpha(double a) {
		color.a = a;
	}
	
	
	public Vector getRGBColor() {
		return new Vector(color.x, color.y, color.z);
	}
	
	
	@Override
	public String toString() {
		String str = "";

		str += "Space: " + space + "; ";
		str += "State: " + Integer.toString(state) + "; ";
		str += "Position: " + position.toString() + "; ";
		str += "NewPosition: " + newPosition.toString() + "; ";
		
		return str;
	}
}
