/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package org.spark.space;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.spark.core.Agent;
import org.spark.core.Observer;
import org.spark.utils.Vector;
import org.spark.utils.Vector4d;



/**
 * The basic space agent class 
 */
public class SpaceAgent extends Agent {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;

	// The space node which determines the position of the agent
	protected SpaceNode	node;

	public static final int CIRCLE = 1;
	public static final int SQUARE = 2;
	public static final int TORUS = 3;
	
	public static final Vector BLACK = new Vector(0, 0, 0);
	public static final Vector WHITE = new Vector(1.0, 1.0, 1.0);
	public static final Vector BLUE = new Vector(0, 0, 1.0);
	public static final Vector GREEN = new Vector(0, 1.0, 0);
	public static final Vector RED = new Vector(1.0, 0, 0);
	public static final Vector CYAN = new Vector(0.0, 1.0, 1.0);
	public static final Vector YELLOW = new Vector(1.0, 1.0, 0.0);
	public static final Vector MAGENTA = new Vector(1.0, 0.0, 1.0);
	public static final Vector GREY = new Vector(0.6, 0.6, 0.6);
	
	private int type = CIRCLE;
	
	public int getType() {
		return type;
	}
	
	/**
	 * Creates a space agent in the specific space
	 * with the given size and shape.
	 * @space
	 * @param r
	 * @param type
	 */
	public SpaceAgent(Space space, double r, int type) {
		switch (type) {
		case SQUARE:
			node = space.createSquareNode(r, this);
			this.type = SQUARE;
			break;
		case TORUS:
			node = space.createCircleNode(r, this);
			this.type = TORUS;
			break;
		default: 
			node = space.createCircleNode(r, this);
			this.type = CIRCLE;
			break;
		}
	}
	
	
	/**
	 * Creates a space agent in a default space
	 * with the given size and shape
	 * @param r
	 * @param type
	 */
	public SpaceAgent(double r, int type) {
		this(Observer.getDefaultSpace(), r, type);
	}

	
	/**
	 * Creates the space agent with the circle space node of the radius r
	 * @param r
	 */
	public SpaceAgent(double r) {
		this(r, CIRCLE);
	}
	
	/**
	 * Creates the space agent with the circle space node of the radius 1.0
	 */
	public SpaceAgent() {
		this(1.0);
	}
	
	
	/**
	 * Moves the agent to the position v
	 * @param v new position
	 */
	public void jump(Vector v) {
		node.jump(v);
	}
	
	/**
	 * @deprecated
	 * Moves the agent to the position v
	 * @param v
	 * Use jump instead this method
	 */
	public void moveTo(Vector v) {
		node.jump(v);
	}

	/**
	 * Changes the position of the agent by the vector v
	 * @param v
	 */
	public void move(Vector v) {
		node.move(v);
	}

	
	/**
	 * Moves the agent to the random position
	 */
	public void setRandomPosition() {
		node.setRandomPosition();
	}
	
	/**
	 * Removes the agent from the space and the context
	 */
	public void die() {
		node.space.removeNode(node);
		// TODO: set it to null: look at all models and find incorrect behavior
		// because of the next line
		// Also be careful with parallel execution mode
//		node = null;
		super.die();
	}
	
	
	/**
	 * Moves the agent into a new space and 
	 * to a new position inside this space.
	 * @param space
	 * @param pos
	 * Note: does not work in parallel execution mode correctly
	 */
	public void moveToSpace(Space space, Vector pos) {
		// If the agent's node is already removed,
		// then cannot move it to another space
		// TODO: correct it for parallel execution mode
		if (node.nodeIsRemoved)
			return;
		
		// TODO: move this code into Space class
		// in order to implement parallel execution mode
		if (space != null && space != node.space) {
			Vector4d color = node.color;
			double r = node.getRelativeSize();
			node.space.removeNode(node);
			
			switch (type) {
			case SQUARE:
				node = space.createSquareNode(r, this);
				break;
			case TORUS:
				node = space.createCircleNode(r, this);
				break;
			default: 
				node = space.createCircleNode(r, this);
				break;
			}
			
			node.color = color;
		}

		node.jump(pos);
	}
	
	
	/**
	 * Returns the relative size (radius for circle node) of the agent
	 * @return
	 */
	public double getRelativeSize() {
		return node.getRelativeSize();
	}
	
	/**
	 * Gets the position of the agent
	 * @return
	 */
	public Vector getPosition() {
		return node.getPosition();
	}
	
	/**
	 * Gets the space node of this agent
	 * @return space node
	 */
	public SpaceNode getNode() {
		return node;
	}
	
	
	/**
	 * Returns the space in which the agent exists
	 * @return
	 */
	public Space getSpace() {
		return node.getSpace();
	}
	

	/**
	 * Sets the color of the agent
	 * @param c new color
	 */
	public void setColor(Vector c) {
		node.setColor(c);
	}

	/**
	 * Sets the color of the agent
	 * @param c new color
	 */
	public void setColor(Vector4d c) {
		node.setColor(c);
	}

	
	/**
	 * Gets the color of the agent 
	 * @return color
	 */
	public Vector4d getColor() {
		return node.getColor();
	}
	
	
	public Vector getRGBColor() {
		return node.getRGBColor();
	}
	
	
	/**
	 * Custom deserialization is needed.
	 */
	private void readObject(ObjectInputStream ois) throws IOException,
			ClassNotFoundException {
//		System.out.println("Reading space agent");

		ois.defaultReadObject();

		// Restore the agent's node in the space.
		// Even when the serialization is not deep, the node for
		// the agent is associated with the space.
		// Because of this the external routine should take care
		// about removing such nodes
		// FIXME: in parallel SPARK we have only one space
		node.space = Observer.getDefaultSpace();
		node.space.addNode0(node);
		
		// FIXME: why do the state = 2 without this line?
		node.state = 0;
	}

	/**
	 * Custom serialization is needed.
	 */
	private void writeObject(ObjectOutputStream oos) throws IOException {
//		System.out.println("Writing space agent");

		oos.defaultWriteObject();

		// remove the agent's node from the space instantly
		// FIXME: synchronization issues
		// do serialization only in a serial way
		if (deepSerialization)
			node.space.removeNode0(node);
	}

}
