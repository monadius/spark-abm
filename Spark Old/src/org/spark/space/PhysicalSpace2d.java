package org.spark.space;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import org.spark.utils.Vector;

/**
 * Experimental physical 2d space
 * @author Monad
 */
public class PhysicalSpace2d extends StandardSpace {
	// SerialVersionUID
	private static final long serialVersionUID = -4250159941684999472L;

	// All physical space nodes
	protected transient ArrayList<PhysicalNode> nodes;
	
	/**
	 * Creates a physical 2d space
	 * @param min
	 * @param max
	 * @param min2
	 * @param max2
	 * @param wrapX
	 * @param wrapY
	 */
	public PhysicalSpace2d(double min, double max, double min2, double max2,
			boolean wrapX, boolean wrapY) {
		super(min, max, min2, max2, wrapX, wrapY);
		
		nodes = new ArrayList<PhysicalNode>(1000);
	}
	
	
	/**
	 * Creates a circle physical node
	 */
	@Override
	protected CircleNode createCircleNode0(double r) {
		return new PhysicalNode(this, r);
	}
	
	
	/**
	 * Adds a node into the context
	 */
	@Override
	protected void addNode0(SpaceNode node) {
		if (node instanceof PhysicalNode) {
			nodes.add((PhysicalNode) node);
		}
		
		super.addNode0(node);
	}

	
	/**
	 * Removes a node from the context
	 */
	@Override
	protected void removeNode0(SpaceNode node) {
		if (node instanceof PhysicalNode) {
			nodes.remove(node);
		}
		
		super.removeNode0(node);
	}
	
	
	/**
	 * Adds a new node and sets its position
	 */
	@Override
	protected void addNodeAndMove0(SpaceNode node) {
		addNode0(node);
		changeNodePosition0(node);
	}
	
	
	/**
	 * Moves a node
	 */
	@Override
	protected void changeNodePosition0(SpaceNode node) {
		super.changeNodePosition0(node);
	}
	
	
	
	/**
	 * Does all physics
	 */
	@Override
	protected void postProcess() {
		Vector d = new Vector();
		
		// Compute Hooke forces first
		for (PhysicalNode node : nodes) {
			// Apply shaping force
			ArrayList<SpaceAgent> agentsHere = getAgents(node.getPosition(), node.getRelativeSize());
						
			// Iterate through all intersecting nodes
			for (SpaceAgent agent : agentsHere) {
				SpaceNode node2 = agent.getNode();
				
				if (node2 instanceof PhysicalNode) {
					// We are assuming that the position is already updated
					d.set(node.position);
					d.sub(node2.position);
					
					double dist = d.length();
					if (dist <= 1e-6)
						dist = 1e-6;
					double r = node.getRelativeSize() + node2.getRelativeSize() - dist;
					
					// No intersection; theoretically, impossible here
					if (r < 0)
						continue;
					
					d.mul(r / dist * 50);
					node.applyForce(d);
				}
			}
		}
		
		
		// Change nodes positions
		for (PhysicalNode node : nodes) {
			node.integrate(0.1);
			node.velocity.mul(0.6);
			node.resetForces();
			
			// Manually change node position in the space
			changeNodePosition0(node);
			node.newPosition.set(node.position);
			node.velocity.truncateDistance(0.5);
//			node.velocity.set(0);

		}
	}
	
	/**
	 * Custom deserialization is needed.
	 */
	private void readObject(ObjectInputStream ois) throws IOException,
			ClassNotFoundException {
		ois.defaultReadObject();
		nodes = new ArrayList<PhysicalNode>(1000);
	}
}
