/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package org.spark.space;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


/**
 * Implementation of the space which behaves in the way similar to NetLogo, 
 * that is, space is divided into cells but the agents can move
 * inside each cell in the continuous manner.
 */
public class GridSpace3d extends BoundedSpace3d {
	private static final long serialVersionUID = 7981503746019652525L;
	protected transient HashGrid3d	hashGrid;

	/**
	 * The default constructor
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 * @param wrapX
	 * @param wrapY
	 */
	public GridSpace3d(double xMin, double xMax, 
			     double yMin, double yMax,
			     double zMin, double zMax,
			     boolean wrapX, boolean wrapY, boolean wrapZ) {
		super(xMin, xMax, yMin, yMax, zMin, zMax, wrapX, wrapY, wrapZ);

		// FIXME: convert non-integer dimension into integer ones
		if (Math.floor(xMin) != xMin ||
			Math.floor(yMin) != yMin ||
			Math.floor(xMax) != xMax ||
			Math.floor(yMax) != yMax ||
			Math.floor(zMin) != zMin ||
			Math.floor(zMax) != zMax)
				throw new Error("NetLogoSpace3d: non-integer dimensions");
		
		// xMin, etc should be set before
		hashGrid = new HashGrid3d( this, (int) getXSize(), (int) getYSize(), (int) getZSize() );
	}
	
	
	@Override
	protected void addNode0(SpaceNode node) {
		SpaceNode node1 = hashGrid.getNode(node.position.x, node.position.y, node.position.z);
		node.next = node1.next;
		node1.next.prev = node;
		node.prev = node1;
		node1.next = node;
	}
	
	
	/**
	 * Removes the node from the space
	 * @param node
	 */
	@Override
	protected void removeNode0(SpaceNode node) {
		node.prev.next = node.next;
		node.next.prev = node.prev;		
	}
	
	
	@Override
	protected void addNodeAndMove0(SpaceNode node) {
		double x = restrictX(node.newPosition.x);
		double y = restrictY(node.newPosition.y);
		double z = restrictZ(node.newPosition.z);
		
		node.position.set(x, y, z);
		
		SpaceNode node1 = hashGrid.getNode(x, y, z);
		node.next = node1.next;
		node1.next.prev = node;
		node.prev = node1;
		node1.next = node;
	}
	
	/**
	 * Changes the position of the node in the space
	 * @param node
	 */
	@Override
	protected void changeNodePosition0(SpaceNode node) {
		double x = restrictX(node.newPosition.x);
		double y = restrictY(node.newPosition.y);
		double z = restrictZ(node.newPosition.z);
		
		node.position.set(x, y, z);
		
		node.prev.next = node.next;
		node.next.prev = node.prev;
	
		SpaceNode node1 = hashGrid.getNode(x, y, z);
		node.next = node1.next;
		node1.next.prev = node;
		node.prev = node1;
		node1.next = node;
	}
	
	
	
	
	/**
	 * Gets all agents intersecting with the given space node
	 * @param node
	 * @return
	 */
	protected ArrayList<SpaceAgent> getAgents(SpaceNode node) {
		ArrayList<SpaceAgent> result = new ArrayList<SpaceAgent>(10);
		
		int x0 = hashGrid.findX(node.position.x);
		int y0 = hashGrid.findY(node.position.y);
		int z0 = hashGrid.findZ(node.position.z);
		
		SpaceNode first = hashGrid.getNode(x0, y0, z0);
		SpaceNode next = first.next;

		while (next != first) {
			result.add(next.agent);
			next = next.next;
		}
		
		return result;
	}

	/**
	 * Gets all agents of the given type intersecting with the space node
	 * @param node
	 * @param type
	 * @return
	 */
//	protected ArrayList<SpaceAgent> getAgents(SpaceNode node, Class<? extends SpaceAgent> type) {
	@SuppressWarnings("unchecked")
	protected <T extends SpaceAgent> ArrayList<T> getAgents(SpaceNode node, Class<T> type) {
		ArrayList<T> result = new ArrayList<T>(10);		
		
		int x0 = hashGrid.findX(node.position.x);
		int y0 = hashGrid.findY(node.position.y);
		int z0 = hashGrid.findZ(node.position.z);
		
		SpaceNode first = hashGrid.getNode(x0, y0, z0);
		SpaceNode next = first.next;

		while (next != first) {
			if (next.agent.getClass() == type) {
				result.add((T)next.agent);
			}
			next = next.next;
		}

		return result;
	}


	/**
	 * Gets all agents derived from the given type intersecting with the space node
	 * @param node
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends SpaceAgent> ArrayList<T> getAgentsOfKind(SpaceNode node, Class<T> type) {
		ArrayList<T> result = new ArrayList<T>(10);		
		
		int x0 = hashGrid.findX(node.position.x);
		int y0 = hashGrid.findY(node.position.y);
		int z0 = hashGrid.findZ(node.position.z);
		
		SpaceNode first = hashGrid.getNode(x0, y0, z0);
		SpaceNode next = first.next;

		while (next != first) {
			if (type.isInstance(next.agent)) {
				result.add((T)next.agent);
			}
			next = next.next;
		}

		return result;
	}
	
	/**
	 * Custom deserialization is needed.
	 */
	private void readObject(ObjectInputStream ois) throws IOException,
			ClassNotFoundException {

		ois.defaultReadObject();

		// Load hash grid
		hashGrid = new HashGrid3d(this, (int) getXSize(), (int) getYSize(), (int) getZSize());
	}

	/**
	 * Custom serialization is needed.
	 */
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}
}
