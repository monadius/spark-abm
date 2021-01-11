package org.spark.space;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


/**
 * Basic implementation of an unbounded grid space
 * @author Alexey
 */
public class UnboundedGridSpace extends UnboundedSpace {
	/* Default serial version UID */
	private static final long serialVersionUID = 1L;
	
	/* Hash grid for fast querying operations */
	protected transient UnboundedSpace.UnboundedHashGrid	hashGrid;

	/**
	 * The default constructor
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 * @param wrapX
	 * @param wrapY
	 */
	public UnboundedGridSpace(double xMin, double xMax, 
			     double yMin, double yMax, 
			     boolean wrapX, boolean wrapY) {
		super(xMin, xMax, yMin, yMax, wrapX, wrapY);

		// FIXME: convert non-integer dimension into integer ones
		if (Math.floor(xMin) != xMin ||
			Math.floor(yMin) != yMin ||
			Math.floor(xMax) != xMax ||
			Math.floor(yMax) != yMax)
				throw new Error("UnboundedGridSpace: non-integer dimensions");
		
		// xMin, etc should be set before
		hashGrid = new UnboundedHashGrid( this, (int) getXSize(), (int) getYSize() );
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
		
		node.position.set(x, y);
		
		node.prev.next = node.next;
		node.next.prev = node.prev;
	
		SpaceNode node1 = hashGrid.getNode(x, y);
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
		hashGrid = new UnboundedHashGrid(this, (int) getXSize(), (int) getYSize());
	}

	/**
	 * Custom serialization is needed.
	 */
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}

}
