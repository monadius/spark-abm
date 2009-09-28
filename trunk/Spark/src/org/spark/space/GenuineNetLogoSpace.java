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
public class GenuineNetLogoSpace extends BoundedSpace {
	private static final long serialVersionUID = -5931005968744092818L;
	protected transient HashGrid2d		hashGrid;

	/**
	 * The default constructor
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 * @param wrapX
	 * @param wrapY
	 */
	public GenuineNetLogoSpace(double xMin, double xMax, 
			     double yMin, double yMax, 
			     boolean wrapX, boolean wrapY) {
		super(xMin - 0.5, xMax + 0.5, yMin - 0.5, yMax + 0.5, wrapX, wrapY);

		// FIXME: convert non-integer dimension into integer ones
		if (Math.floor(xMin) != xMin ||
			Math.floor(yMin) != yMin ||
			Math.floor(xMax) != xMax ||
			Math.floor(yMax) != yMax)
				throw new Error("GenuineNetLogoSpace: non-integer dimensions");
		
		// xMin, etc should be set before
		hashGrid = new HashGrid2d( this, (int) getXSize(), (int) getYSize() );
	}
	
	
	@Override
	protected void addNode0(SpaceNode node) {
		SpaceNode node1 = hashGrid.getNode(node.position.x, node.position.y);
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
		double x = node.newPosition.x;
		double y = node.newPosition.y;
		
		// TODO: make a test how much did the previous (incorrect)
		// solution work faster than the present (correct) one
		x = restrictX(x);
		y = restrictY(y);
/*		if (x < xMin) {
			if (wrapX)
				x += getXSize();
			else
				x = xMin;
		}
		else if (x >= xMax) {
			if (wrapX)
				x -= getXSize();
			else
				x = xMax;
		}
		
		if (y < yMin) {
			if (wrapY)
				y += getYSize();
			else
				y = yMin;
		}
		else if (y >= yMax) {
			if (wrapY)
				y -= getYSize();
			else
				y = yMax;
		}*/
		
		node.position.set(x, y);
		
		SpaceNode node1 = hashGrid.getNode(x, y);
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
		double x = node.newPosition.x;
		double y = node.newPosition.y;

		x = restrictX(x);
		y = restrictY(y);

/*		if (x < xMin) {
			if (wrapX)
				x += getXSize();
			else
				x = xMin;
		}
		else if (x >= xMax) {
			if (wrapX)
				x -= getXSize();
			else
				x = xMax;
		}
		
		if (y < yMin) {
			if (wrapY)
				y += getYSize();
			else
				y = yMin;
		}
		else if (y >= yMax) {
			if (wrapY)
				y -= getYSize();
			else
				y = yMax;
		}*/
		
		node.position.set(x, y);
		
		node.prev.next = node.next;
		node.next.prev = node.prev;
	
		SpaceNode node1 = hashGrid.getNode(x, y);
		node.next = node1.next;
		node1.next.prev = node;
		node.prev = node1;
		node1.next = node;
	}
	
	
	
//	private ArrayList<SpaceAgent> result = new ArrayList<SpaceAgent>(100);
	
	
	/**
	 * Gets all agents intersecting with the given space node
	 * @param node
	 * @return
	 */
	protected ArrayList<SpaceAgent> getAgents(SpaceNode node) {
		// TODO: if the mode is not parallel then can use a global set
		ArrayList<SpaceAgent> result = new ArrayList<SpaceAgent>(10);
//		result.clear();
		
		int x0 = hashGrid.findX(node.position.x);
		int y0 = hashGrid.findY(node.position.y);
		
		SpaceNode first = hashGrid.getNode(x0, y0);
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
		// TODO: if the mode is not parallel then can use a global set
		ArrayList<SpaceAgent> result = new ArrayList<SpaceAgent>(10);
//		result.clear();
		
		int x0 = hashGrid.findX(node.position.x);
		int y0 = hashGrid.findY(node.position.y);
		
		SpaceNode first = hashGrid.getNode(x0, y0);
		SpaceNode next = first.next;

		while (next != first) {
			if (next.agent.getClass() == type) {
				result.add(next.agent);
			}
			next = next.next;
		}

		return (ArrayList<T>) result;
	}
	
	
	/**
	 * Gets all agents derived from the given type intersecting with the space node
	 * @param node
	 * @param type
	 * @return
	 */
//	protected ArrayList<SpaceAgent> getAgents(SpaceNode node, Class<? extends SpaceAgent> type) {
	@SuppressWarnings("unchecked")
	protected <T extends SpaceAgent> ArrayList<T> getAgentsOfKind(SpaceNode node, Class<T> type) {
		// TODO: if the mode is not parallel then can use a global set
		ArrayList<SpaceAgent> result = new ArrayList<SpaceAgent>(10);
//		result.clear();
		
		int x0 = hashGrid.findX(node.position.x);
		int y0 = hashGrid.findY(node.position.y);
		
		SpaceNode first = hashGrid.getNode(x0, y0);
		SpaceNode next = first.next;

		while (next != first) {
			if (type.isInstance(next.agent)) {
				result.add(next.agent);
			}
			next = next.next;
		}

		return (ArrayList<T>) result;
	}
	
	
	/**
	 * Custom deserialization is needed.
	 */
	private void readObject(ObjectInputStream ois) throws IOException,
			ClassNotFoundException {

		ois.defaultReadObject();

		// Load hash grid
		hashGrid = new HashGrid2d( this, (int) getXSize(), (int) getYSize() );
	}

	/**
	 * Custom serialization is needed.
	 */
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}


}

