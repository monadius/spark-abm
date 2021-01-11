package org.spark.space;

import java.util.ArrayList;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.CircleDef;
import org.jbox2d.collision.CircleShape;
import org.jbox2d.collision.CollideCircle;
import org.jbox2d.collision.CollidePoly;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.PolygonDef;
import org.jbox2d.collision.PolygonShape;
import org.jbox2d.collision.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.XForm;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointDef;
import org.spark.math.Vector;

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
	 * Creates a joint
	 * @param jd
	 */
	public Joint createJoint(JointDef jd) {
		return world.createJoint(jd);
	}
	
	
	/**
	 * Destroy the joint
	 * @param j
	 */
	public void destroyJoint(Joint j) {
		world.destroyJoint(j);
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
	 * Gets all agents intersecting with the given space node
	 * @param node
	 * @return
	 */
	@Override
	protected ArrayList<SpaceAgent> getAgents(SpaceNode node) {
		ArrayList<SpaceAgent> result = new ArrayList<SpaceAgent>(10);
		
		Shape shape0 = null;
		XForm x0 = null;

		if (node instanceof PhysicalNode) {
			Body body = ((PhysicalNode) node).body;
			if (body != null) {
				shape0 = body.getShapeList();
				x0 = body.getXForm();
			}
		}

		if (shape0 == null) {
			// Create a special shape
			double r = node.getRelativeSize();
			Vector p = node.getPosition();

			CircleDef cd = new CircleDef();
			cd.radius = (float) r;
		
			shape0 = new CircleShape(cd);
			x0 = new XForm();
			x0.position = new Vec2((float) p.x, (float) p.y);
		}

		AABB aabb = new AABB(); 
		shape0.computeAABB(aabb, x0);

		// Get all shapes inside the AABB
		Shape[] shapes = world.query(aabb, 10000);
		
		// Iterate over all shapes
		for (Shape shape : shapes) {
			Body b = shape.getBody();
			if (b == null)
				continue;
			
			Object data = b.getUserData();
			if (data == null || !(data instanceof PhysicalNode))
				continue;
			
			PhysicalNode n = (PhysicalNode) data;
			SpaceAgent agent = n.agent;
			
			Manifold manifold = new Manifold();
			XForm x1 = b.getXForm();
			
			if (shape0 instanceof CircleShape) {
				// 0: Circle
				CircleShape s0 = (CircleShape) shape0;
				
				if (shape instanceof PolygonShape) {
					// 1: Polygon
					CollideCircle.collidePolygonAndCircle(manifold, (PolygonShape) shape, x1, s0, x0);
					if (manifold.pointCount > 0)
						result.add(agent);
				}
				else if (shape instanceof CircleShape) {
					// 1: Circle
					CollideCircle.collideCircles(manifold, (CircleShape) shape, x1, s0, x0);
					if (manifold.pointCount > 0)
						result.add(agent);
				}
			}
			else if (shape0 instanceof PolygonShape) {
				// 0: Polygon
				PolygonShape s0 = (PolygonShape) shape0;
				
				if (shape instanceof PolygonShape) {
					// 1: Polygon
					CollidePoly.collidePolygons(manifold, s0, x0, (PolygonShape) shape, x1);
					if (manifold.pointCount > 0)
						result.add(agent);
				}
				else if (shape instanceof CircleShape) {
					// 1: Circle
					CollideCircle.collidePolygonAndCircle(manifold, s0, x0, (CircleShape) shape, x1);
					if (manifold.pointCount > 0)
						result.add(agent);
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
	@Override
	@SuppressWarnings("unchecked")
	protected <T extends SpaceAgent> ArrayList<T> getAgents(SpaceNode node, Class<T> type) {
		ArrayList<SpaceAgent> result = new ArrayList<SpaceAgent>(10);
		
		Shape shape0 = null;
		XForm x0 = null;

		if (node instanceof PhysicalNode) {
			Body body = ((PhysicalNode) node).body;
			if (body != null) {
				shape0 = body.getShapeList();
				x0 = body.getXForm();
			}
		}

		if (shape0 == null) {
			// Create a special shape
			double r = node.getRelativeSize();
			Vector p = node.getPosition();

			CircleDef cd = new CircleDef();
			cd.radius = (float) r;
		
			shape0 = new CircleShape(cd);
			x0 = new XForm();
			x0.position = new Vec2((float) p.x, (float) p.y);
		}

		AABB aabb = new AABB(); 
		shape0.computeAABB(aabb, x0);

		// Get all shapes inside the AABB
		Shape[] shapes = world.query(aabb, 10000);
		
		// Iterate over all shapes
		for (Shape shape : shapes) {
			Body b = shape.getBody();
			if (b == null)
				continue;
			
			Object data = b.getUserData();
			if (data == null || !(data instanceof PhysicalNode))
				continue;
			
			PhysicalNode n = (PhysicalNode) data;
			SpaceAgent agent = n.agent;
			if (agent.getClass() != type)
				continue;
			
			Manifold manifold = new Manifold();
			XForm x1 = b.getXForm();
			
			if (shape0 instanceof CircleShape) {
				// 0: Circle
				CircleShape s0 = (CircleShape) shape0;
				
				if (shape instanceof PolygonShape) {
					// 1: Polygon
					CollideCircle.collidePolygonAndCircle(manifold, (PolygonShape) shape, x1, s0, x0);
					if (manifold.pointCount > 0)
						result.add(agent);
				}
				else if (shape instanceof CircleShape) {
					// 1: Circle
					CollideCircle.collideCircles(manifold, (CircleShape) shape, x1, s0, x0);
					if (manifold.pointCount > 0)
						result.add(agent);
				}
			}
			else if (shape0 instanceof PolygonShape) {
				// 0: Polygon
				PolygonShape s0 = (PolygonShape) shape0;
				
				if (shape instanceof PolygonShape) {
					// 1: Polygon
					CollidePoly.collidePolygons(manifold, s0, x0, (PolygonShape) shape, x1);
					if (manifold.pointCount > 0)
						result.add(agent);
				}
				else if (shape instanceof CircleShape) {
					// 1: Circle
					CollideCircle.collidePolygonAndCircle(manifold, s0, x0, (CircleShape) shape, x1);
					if (manifold.pointCount > 0)
						result.add(agent);
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
	@Override
	@SuppressWarnings("unchecked")
	protected <T extends SpaceAgent> ArrayList<T> getAgentsOfKind(SpaceNode node, Class<T> kind) {
		ArrayList<SpaceAgent> result = new ArrayList<SpaceAgent>(10);
		
		Shape shape0 = null;
		XForm x0 = null;

		if (node instanceof PhysicalNode) {
			Body body = ((PhysicalNode) node).body;
			if (body != null) {
				shape0 = body.getShapeList();
				x0 = body.getXForm();
			}
		}

		if (shape0 == null) {
			// Create a special shape
			double r = node.getRelativeSize();
			Vector p = node.getPosition();

			CircleDef cd = new CircleDef();
			cd.radius = (float) r;
		
			shape0 = new CircleShape(cd);
			x0 = new XForm();
			x0.position = new Vec2((float) p.x, (float) p.y);
		}

		AABB aabb = new AABB(); 
		shape0.computeAABB(aabb, x0);

		// Get all shapes inside the AABB
		Shape[] shapes = world.query(aabb, 10000);
		
		// Iterate over all shapes
		for (Shape shape : shapes) {
			Body b = shape.getBody();
			if (b == null)
				continue;
			
			Object data = b.getUserData();
			if (data == null || !(data instanceof PhysicalNode))
				continue;
			
			PhysicalNode n = (PhysicalNode) data;
			SpaceAgent agent = n.agent;
			if (!kind.isInstance(agent))
				continue;
			
			Manifold manifold = new Manifold();
			XForm x1 = b.getXForm();
			
			if (shape0 instanceof CircleShape) {
				// 0: Circle
				CircleShape s0 = (CircleShape) shape0;
				
				if (shape instanceof PolygonShape) {
					// 1: Polygon
					CollideCircle.collidePolygonAndCircle(manifold, (PolygonShape) shape, x1, s0, x0);
					if (manifold.pointCount > 0)
						result.add(agent);
				}
				else if (shape instanceof CircleShape) {
					// 1: Circle
					CollideCircle.collideCircles(manifold, (CircleShape) shape, x1, s0, x0);
					if (manifold.pointCount > 0)
						result.add(agent);
				}
			}
			else if (shape0 instanceof PolygonShape) {
				// 0: Polygon
				PolygonShape s0 = (PolygonShape) shape0;
				
				if (shape instanceof PolygonShape) {
					// 1: Polygon
					CollidePoly.collidePolygons(manifold, s0, x0, (PolygonShape) shape, x1);
					if (manifold.pointCount > 0)
						result.add(agent);
				}
				else if (shape instanceof CircleShape) {
					// 1: Circle
					CollideCircle.collidePolygonAndCircle(manifold, s0, x0, (CircleShape) shape, x1);
					if (manifold.pointCount > 0)
						result.add(agent);
				}
				
			}
		}
		
		return (ArrayList<T>)result;
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
				// If the position of the node was corrected (due to the topology)
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
