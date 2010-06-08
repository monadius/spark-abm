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


public class StandardSpace extends BoundedSpace {
	private static final long serialVersionUID = 628399366517778439L;
	private int hashXSize = 120;
	private int hashYSize = 120;
	
	
//public boolean debugflag;
	

	
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
	public StandardSpace(double xMin, double xMax, 
			     double yMin, double yMax, 
			     boolean wrapX, boolean wrapY) {
		super(xMin, xMax, yMin, yMax, wrapX, wrapY);

		// TODO: automatically adjust hash grid size
		hashXSize = (int)(xMax - xMin);
		hashYSize = (int)(yMax - yMin);
		
		if (hashXSize < 10)
			hashXSize = 10;
		else if (hashXSize > 120)
			hashXSize = 120;

		if (hashYSize < 10)
			hashYSize = 10;
		else if (hashYSize > 120)
			hashYSize = 120;

		// xMin, etc should be set before
		hashGrid = new HashGrid2d(this, hashXSize, hashYSize);
	}
	
	
	@Override
	protected void postProcess() {
//		hashGrid.verifyIntegrity();
	}
	

	
	@Override
	protected void addNode0(SpaceNode node) {
		SpaceNode node1 = hashGrid.getNode(node.position.x, node.position.y);
		node.next = node1.next;
		node1.next.prev = node;
		node.prev = node1;
		node1.next = node;
		
//		if (debugflag)
//			hashGrid.verifyIntegrity();
	}
	
	@Override
	protected void addNodeAndMove0(SpaceNode node) {
		double x = restrictX(node.newPosition.x);
		double y = restrictY(node.newPosition.y);

		node.position.set(x, y);
		
		SpaceNode node1 = hashGrid.getNode(x, y);
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
		
//		node.prev = node.next = node;
		
//		if (debugflag)
//			hashGrid.verifyIntegrity();
	}
	
	/**
	 * Changes the position of the node in the space
	 * @param node
	 */
	protected void changeNodePosition0(SpaceNode node) {
//		if (debugflag)
//			hashGrid.verifyIntegrity();

		double x = restrictX(node.newPosition.x);
		double y = restrictY(node.newPosition.y);
		
		node.position.set(x, y);
		
		node.prev.next = node.next;
		node.next.prev = node.prev;

//		if (debugflag)
//			hashGrid.verifyIntegrity();
		
		SpaceNode node1 = hashGrid.getNode(x, y);
		node.next = node1.next;
		node1.next.prev = node;
		node.prev = node1;
		node1.next = node;
		
//		if (debugflag)
//			hashGrid.verifyIntegrity();
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

		double radius = node.getRelativeSize() + maximumNodeRadius;
		int xOffset = (int)(radius / hashGrid.getXStep()) + 1;
		int yOffset = (int)(radius / hashGrid.getYStep()) + 1;
		
//		int i0 = -5, i1 = 5;
//		int j0 = -5, j1 = 5;
		int i0 = -xOffset, i1 = xOffset;
		int j0 = -yOffset, j1 = yOffset;
		
		if (!wrapX) {
			if (x0 + i0 < 0)
				i0 = -x0;
			
			if (x0 + i1 >= hashXSize)
				i1 = hashXSize - 1 - x0;
		}
		
		if (!wrapY) {
			if (y0 + j0 < 0)
				j0 = -y0;
			
			if (y0 + j1 >= hashYSize)
				j1 = hashYSize - 1 - y0;
		}
		
		for (int i = i0; i <= i1; i++) {
			// TODO: For non-torus topology here will be problems with multiple checks
			int x = hashGrid.restrictX(x0 + i);

			for (int j = j0; j <= j1; j++) {
				int y = hashGrid.restrictY(y0 + j);
				
				SpaceNode first = hashGrid.getNode(x, y);
				SpaceNode next = first.next;
				
				while (next != first) {
					if (node.intersects(next))
						result.add(next.agent);
					next = next.next;
				}

			}
		}
		
		return result;
	}

	/**
	 * Gets all agents of the given type intersecting with the space node
	 * @param node
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends SpaceAgent> ArrayList<T> getAgents(SpaceNode node, Class<T> type) {
		// TODO: if the mode is not parallel then can use a global set
		ArrayList<SpaceAgent> result = new ArrayList<SpaceAgent>(10);
	
//		result.clear();
		
		int x0 = hashGrid.findX(node.position.x);
		int y0 = hashGrid.findY(node.position.y);
		
		double radius = node.getRelativeSize() + maximumNodeRadius;
		int xOffset = (int)(radius / hashGrid.getXStep()) + 1;
		int yOffset = (int)(radius / hashGrid.getYStep()) + 1;
		
//		int i0 = -5, i1 = 5;
//		int j0 = -5, j1 = 5;
		int i0 = -xOffset, i1 = xOffset;
		int j0 = -yOffset, j1 = yOffset;		
		if (!wrapX) {
			if (x0 + i0 < 0)
				i0 = -x0;
			
			if (x0 + i1 >= hashXSize)
				i1 = hashXSize - 1 - x0;
		}
		
		if (!wrapY) {
			if (y0 + j0 < 0)
				j0 = -y0;
			
			if (y0 + j1 >= hashYSize)
				j1 = hashYSize - 1 - y0;
		}
		
		for (int i = i0; i <= i1; i++) {
			// TODO: For non-torus topology here will be problems with multiple checks
			int x = hashGrid.restrictX(x0 + i);

			for (int j = j0; j <= j1; j++) {
				int y = hashGrid.restrictY(y0 + j);
				
				SpaceNode first = hashGrid.getNode(x, y);
				SpaceNode next = first.next;

				while (next != first) {
					if (next.agent.getClass() == type) {
						if (node.intersects(next))
							result.add(next.agent);
					}
					next = next.next;
				}

			}
		}
		
		return (ArrayList<T>)result;
	}
	
	
	/**
	 * Gets all agents derived from the given type intersecting with the space node
	 * @param node
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends SpaceAgent> ArrayList<T> getAgentsOfKind(SpaceNode node, Class<T> kind) {
		// TODO: if the mode is not parallel then can use a global set
		ArrayList<SpaceAgent> result = new ArrayList<SpaceAgent>(10);
	
//		result.clear();
		
		int x0 = hashGrid.findX(node.position.x);
		int y0 = hashGrid.findY(node.position.y);
		
		double radius = node.getRelativeSize() + maximumNodeRadius;
		int xOffset = (int)(radius / hashGrid.getXStep()) + 1;
		int yOffset = (int)(radius / hashGrid.getYStep()) + 1;
		
//		int i0 = -5, i1 = 5;
//		int j0 = -5, j1 = 5;
		int i0 = -xOffset, i1 = xOffset;
		int j0 = -yOffset, j1 = yOffset;		
		if (!wrapX) {
			if (x0 + i0 < 0)
				i0 = -x0;
			
			if (x0 + i1 >= hashXSize)
				i1 = hashXSize - 1 - x0;
		}
		
		if (!wrapY) {
			if (y0 + j0 < 0)
				j0 = -y0;
			
			if (y0 + j1 >= hashYSize)
				j1 = hashYSize - 1 - y0;
		}
		
		for (int i = i0; i <= i1; i++) {
			// TODO: For non-torus topology here will be problems with multiple checks
			int x = hashGrid.restrictX(x0 + i);

			for (int j = j0; j <= j1; j++) {
				int y = hashGrid.restrictY(y0 + j);
				
				SpaceNode first = hashGrid.getNode(x, y);
				SpaceNode next = first.next;

				while (next != first) {
					if (kind.isInstance(next.agent)) {
						if (node.intersects(next))
							result.add(next.agent);
					}
					next = next.next;
				}

			}
		}
		
		return (ArrayList<T>)result;
	}
	
	
	/**
	 * Custom deserialization is needed.
	 */
	private void readObject(ObjectInputStream ois) throws IOException,
			ClassNotFoundException {

		ois.defaultReadObject();

		// Load hash grid
		hashGrid = new HashGrid2d(this, hashXSize, hashYSize);
	}

	/**
	 * Custom serialization is needed.
	 */
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}

}
