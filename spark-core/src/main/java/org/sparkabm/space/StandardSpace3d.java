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


public class StandardSpace3d extends BoundedSpace3d {
	private static final long serialVersionUID = 6067156857034610557L;
	private int hashXSize = 120;
	private int hashYSize = 120;
	private int hashZSize = 120;

	protected transient HashGrid3d hashGrid;

	/**
	 * The default constructor
	 * 
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 * @param wrapX
	 * @param wrapY
	 */
	public StandardSpace3d(double xMin, double xMax, double yMin, double yMax,
			double zMin, double zMax, boolean wrapX, boolean wrapY,
			boolean wrapZ) {
		super(xMin, xMax, yMin, yMax, zMin, zMax, wrapX, wrapY, wrapZ);

		// TODO: automatically adjust hash grid size
		hashXSize = (int) (xMax - xMin);
		hashYSize = (int) (yMax - yMin);
		hashZSize = (int) (zMax - zMin);

		if (hashXSize < 10)
			hashXSize = 10;
		else if (hashXSize > 120)
			hashXSize = 120;

		if (hashYSize < 10)
			hashYSize = 10;
		else if (hashYSize > 120)
			hashYSize = 120;

		if (hashZSize < 10)
			hashZSize = 10;
		else if (hashZSize > 120)
			hashZSize = 120;

		// xMin, etc should be set before
		hashGrid = new HashGrid3d(this, hashXSize, hashYSize,
				hashZSize);
	}

	@Override
	protected void addNode0(SpaceNode node) {
		SpaceNode node1 = hashGrid.getNode(node.position.x, node.position.y,
				node.position.z);
		node.next = node1.next;
		node1.next.prev = node;
		node.prev = node1;
		node1.next = node;
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
	 * Removes the node from the space
	 * 
	 * @param node
	 */
	@Override
	protected void removeNode0(SpaceNode node) {
		node.prev.next = node.next;
		node.next.prev = node.prev;
	}

	/**
	 * Changes the position of the node in the space
	 * 
	 * @param node
	 */
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
	 * 
	 * @param node
	 * @return
	 */
	protected ArrayList<SpaceAgent> getAgents(SpaceNode node) {
		// TODO: if the mode is not parallel then can use a global set
		ArrayList<SpaceAgent> result = new ArrayList<SpaceAgent>(10);
		// result.clear();

		int x0 = hashGrid.findX(node.position.x);
		int y0 = hashGrid.findY(node.position.y);
		int z0 = hashGrid.findZ(node.position.z);

		double radius = node.getRelativeSize() + maximumNodeRadius;
		int xOffset = (int) (radius / hashGrid.getXStep()) + 1;
		int yOffset = (int) (radius / hashGrid.getYStep()) + 1;
		int zOffset = (int) (radius / hashGrid.getZStep()) + 1;

		int i0 = -xOffset, i1 = xOffset;
		int j0 = -yOffset, j1 = yOffset;
		int k0 = -zOffset, k1 = zOffset;

		// TODO: if (wrapZ) then check that 2 * xOffset < hashXSize and contract
		// the search area if necessary

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

		if (!wrapZ) {
			if (z0 + k0 < 0)
				k0 = -z0;

			if (z0 + k1 >= hashZSize)
				k1 = hashZSize - 1 - z0;
		}

		for (int k = k0; k <= k1; k++) {
			int z = hashGrid.restrictZ(z0 + k);

			for (int i = i0; i <= i1; i++) {
				int x = hashGrid.restrictX(x0 + i);

				for (int j = j0; j <= j1; j++) {
					int y = hashGrid.restrictY(y0 + j);

					SpaceNode first = hashGrid.getNode(x, y, z);
					SpaceNode next = first.next;

					while (next != first) {
						if (node.intersects(next))
							result.add(next.agent);
						next = next.next;
					}

				}
			}
		}

		return result;
	}

	/**
	 * Gets all agents of the given type intersecting with the space node
	 * 
	 * @param node
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends SpaceAgent> ArrayList<T> getAgents(SpaceNode node,
			Class<T> type) {
		// TODO: if the mode is not parallel then can use a global set
		ArrayList<SpaceAgent> result = new ArrayList<SpaceAgent>(10);
		// result.clear();

		int x0 = hashGrid.findX(node.position.x);
		int y0 = hashGrid.findY(node.position.y);
		int z0 = hashGrid.findZ(node.position.z);

		double radius = node.getRelativeSize() + maximumNodeRadius;
		int xOffset = (int) (radius / hashGrid.getXStep()) + 1;
		int yOffset = (int) (radius / hashGrid.getYStep()) + 1;
		int zOffset = (int) (radius / hashGrid.getZStep()) + 1;

		int i0 = -xOffset, i1 = xOffset;
		int j0 = -yOffset, j1 = yOffset;
		int k0 = -zOffset, k1 = zOffset;

		// TODO: if (wrapZ) then check that 2 * xOffset < hashXSize and contract
		// the search area if necessary

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

		if (!wrapZ) {
			if (z0 + k0 < 0)
				k0 = -z0;

			if (z0 + k1 >= hashZSize)
				k1 = hashZSize - 1 - z0;
		}

		for (int k = k0; k <= k1; k++) {
			int z = hashGrid.restrictZ(z0 + k);

			for (int i = i0; i <= i1; i++) {
				int x = hashGrid.restrictX(x0 + i);

				for (int j = j0; j <= j1; j++) {
					int y = hashGrid.restrictY(y0 + j);

					SpaceNode first = hashGrid.getNode(x, y, z);
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
		}

		return (ArrayList<T>) result;
	}

	/**
	 * Gets all agents derived from the given type intersecting with the space
	 * node
	 * 
	 * @param node
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends SpaceAgent> ArrayList<T> getAgentsOfKind(
			SpaceNode node, Class<T> kind) {
		// TODO: if the mode is not parallel then can use a global set
		ArrayList<SpaceAgent> result = new ArrayList<SpaceAgent>(10);
		// result.clear();

		int x0 = hashGrid.findX(node.position.x);
		int y0 = hashGrid.findY(node.position.y);
		int z0 = hashGrid.findZ(node.position.z);

		double radius = node.getRelativeSize() + maximumNodeRadius;
		int xOffset = (int) (radius / hashGrid.getXStep()) + 1;
		int yOffset = (int) (radius / hashGrid.getYStep()) + 1;
		int zOffset = (int) (radius / hashGrid.getZStep()) + 1;

		int i0 = -xOffset, i1 = xOffset;
		int j0 = -yOffset, j1 = yOffset;
		int k0 = -zOffset, k1 = zOffset;

		// TODO: if (wrapZ) then check that 2 * xOffset < hashXSize and contract
		// the search area if necessary

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

		if (!wrapZ) {
			if (z0 + k0 < 0)
				k0 = -z0;

			if (z0 + k1 >= hashZSize)
				k1 = hashZSize - 1 - z0;
		}

		for (int k = k0; k <= k1; k++) {
			int z = hashGrid.restrictZ(z0 + k);

			for (int i = i0; i <= i1; i++) {
				int x = hashGrid.restrictX(x0 + i);

				for (int j = j0; j <= j1; j++) {
					int y = hashGrid.restrictY(y0 + j);

					SpaceNode first = hashGrid.getNode(x, y, z);
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
		hashGrid = new HashGrid3d(this, hashXSize, hashYSize, hashZSize);
	}

	/**
	 * Custom serialization is needed.
	 */
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}

}
