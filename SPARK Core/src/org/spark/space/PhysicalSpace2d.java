package org.spark.space;

import java.util.ArrayList;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.PolygonDef;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;

/**
 * Experimental physical 2d space
 * @author Monad
 */
public class PhysicalSpace2d extends StandardSpace {
	// SerialVersionUID
	private static final long serialVersionUID = -4250159941684999472L;

	// JBox2D world
	private final World world;
	
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

		// Create JBox2D world
		AABB worldAABB = new AABB();
		worldAABB.lowerBound = new Vec2((float)min, (float)min2);
		worldAABB.upperBound = new Vec2((float)max, (float)max2);
		Vec2 gravity = new Vec2(0.0f, 0.0f);
		boolean doSleep = true;
		this.world = new World(worldAABB, gravity, doSleep);
		
		nodes = new ArrayList<PhysicalNode>(1000);
		
		// Create physical boundaries
		float xSize2 = (float)(xSize / 2);
		float ySize2 = (float)(ySize / 2);
		float xc = (float)(xMin + xSize / 2);
		float yc = (float)(yMin + ySize / 2);
		if (!wrapY) {
			PolygonDef sd = new PolygonDef();
			sd.setAsBox(xSize2, 0.1f, new Vec2(0, 0), 0.0f);

			BodyDef bd = new BodyDef();
			bd.position.set(xc, (float)yMin);
			Body b = world.createBody(bd);
			b.createShape(sd);

			bd.position.set(xc, (float)yMax);
			b = world.createBody(bd);
			b.createShape(sd);
		}
		
		if (!wrapX) {
			PolygonDef sd = new PolygonDef();
			sd.setAsBox(0.1f, ySize2, new Vec2(0, 0), 0.0f);

			BodyDef bd = new BodyDef();
			bd.position.set((float)xMin, yc);
			Body b = world.createBody(bd);
			b.createShape(sd);

			bd.position.set((float)xMax, yc);
			b = world.createBody(bd);
			b.createShape(sd);
		}

	}
	
	
	/**
	 * Creates a circle physical node
	 */
	@Override
	protected CircleNode createCircleNode0(double r, int type) {
		if (type == SpaceAgent.CIRCLE)
			return new CircleNode(this, r);
		
		return new PhysicalNode(this, r, type);
	}
	
	
	/**
	 * Adds a node into the context
	 */
	@Override
	protected void addNode0(SpaceNode node) {
		if (node instanceof PhysicalNode) {
			PhysicalNode pNode = (PhysicalNode) node;
			nodes.add(pNode);
			pNode.init(world);
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
			world.destroyBody(((PhysicalNode) node).body);
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
		if (node instanceof PhysicalNode) {
			PhysicalNode pNode = (PhysicalNode) node;
			Vec2 pos = new Vec2((float)pNode.position.x, (float)pNode.position.y);
			float angle = pNode.body.getAngle();
			pNode.body.setXForm(pos, angle);
		}
	}
	
	
	
	/**
	 * Does all physics
	 */
	@Override
	protected void postProcess() {
		float timeStep = 1.0f / 60.0f;

		world.setWarmStarting(true);
		world.setPositionCorrection(true);
		world.setContinuousPhysics(true);

		world.step(timeStep, 10);		
		
		// Update positions of nodes
		for (PhysicalNode node : nodes) {
			node.updatePosition();
			super.changeNodePosition0(node);
			float angle = node.body.getAngle();
			
			node.rotation = angle;

			if (!node.position.equals(node.newPosition)) {
				// If the position of the node was corrected
				node.newPosition.set(node.position);
				
				Vec2 pos = new Vec2((float)node.position.x, (float)node.position.y);
				node.body.setXForm(pos, angle);
			}
		}
	}
	
	/**
	 * Custom deserialization is needed.
	 */
/*	private void readObject(ObjectInputStream ois) throws IOException,
			ClassNotFoundException {
		ois.defaultReadObject();
		nodes = new ArrayList<PhysicalNode>(1000);
	}*/
}
