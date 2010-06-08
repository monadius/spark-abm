/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package org.spark.space;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.spark.core.ExecutionMode;
import org.spark.core.Observer;
import org.spark.data.DataLayer;
import org.spark.data.Grid;
import org.spark.data.ParallelGrid;
import org.spark.utils.Vector;



/**
 * The basic class for space.
 * It is an abstract class which defines all generic functionality
 * of arbitrary SPARK space 
 */
public abstract class Space implements Serializable {
	/* Serial version UID */
	private static final long serialVersionUID = 7300324697412064632L;

	/* Reference to the observer */
	private Observer observer;
	
	/* Space index is a substitution for space's name for fast operations */
	private int spaceIndex;
	
	private transient ArrayList<SpaceNode> nodeQueue;
	private final int executionMode;
	
	
	/* Determines the maximum size of all nodes in the space */
	protected double maximumNodeRadius;
	
	
	/* Collection of all data layers in this space */ 
	private transient HashMap<String, DataLayer> dataLayers;
	/* List of all data layers in this space */
	private transient ArrayList<DataLayer> dataLayersList; 

	
	
	/**
	 * Default constructor
	 */
	protected Space() {
		observer = Observer.getInstance();
		
		executionMode = observer.getExecutionMode();
		
		nodeQueue = new ArrayList<SpaceNode>(1000);
		dataLayers = new HashMap<String, DataLayer>();
		dataLayersList = new ArrayList<DataLayer>();
	}
	
	
	/**
	 * Sets space's index
	 * @param index
	 */
	// TODO: should not be public
	public void setIndex(int index) {
		this.spaceIndex = index;
	}
	
	
	/**
	 * Returns space's index
	 * @return
	 */
	public int getIndex() {
		return spaceIndex;
	}
	
	
	/**
	 * Adds a new data layer into the space
	 * @param name a name of a new data layer
	 * @param data a data layer itself
	 */
	public <T extends DataLayer> T addDataLayer(String name, T data) {
		if (data.getSpace() != this)
			throw new Error("Cannot add data layer " + name + " into space " + this);
		
		dataLayers.put(name, data);
		dataLayersList.add(data);

		return data;
	}
	
	
	/**
	 * Gets a specific data layer
	 * @param name a name of a data layer
	 * @return null if no such a data layer
	 */
	public DataLayer getDataLayer(String name) {
		return dataLayers.get(name);
	}
	
	
	/**
	 * Returns names of all data layers in the space
	 * @return names of all data layers
	 */
	public String[] getDataLayerNames() {
		String[] tmp = new String[dataLayers.size()];
		tmp = dataLayers.keySet().toArray(tmp);
		
		return tmp;
	}
	
	
	/**
	 * Processes all data layers
	 * @param tick
	 */
	public final void processAllDataLayers(long tick) {
		for (int i = 0; i < dataLayersList.size(); i++) {
			dataLayersList.get(i).process(tick);
		}
	}
	
	

	/**
	 * Processes all nodes (in concurrent execution mode)
	 */
	public final void processNodes() {
		for (SpaceNode node : nodeQueue) {
			if ((node.state & SpaceNode.NODE_IS_REMOVING) != 0) {
				removeNode0(node);
				continue;
			}
			
			if ((node.state & SpaceNode.NODE_IS_CREATING) != 0) {
				if ((node.state & SpaceNode.NODE_IS_MOVING) != 0) {
					addNodeAndMove0(node);
					node.newPosition.set(node.position);
				}
				else {
					addNode0(node);
				}
			}
			else {
				if ((node.state & SpaceNode.NODE_IS_MOVING) != 0) {
					changeNodePosition0(node);
					node.newPosition.set(node.position);
				}
			}
				
			node.state = 0;
		}
		
		nodeQueue.clear();
		postProcess();
	}
	
	
	/**
	 * Implement in a subclass for additional manual node processing
	 */
	protected void postProcess() {
		
	}
	
	
	/**
	 * Creates a circle node of the given radius r
	 * @param r
	 * @return
	 */
	protected final CircleNode createCircleNode(double r, SpaceAgent agent) {
		if (r > maximumNodeRadius)
			maximumNodeRadius = r;
		
		CircleNode node = createCircleNode0(r);
		node.agent = agent;
		node.next = node.prev = node;
		
		
		// observer.isSerial() is always true during the setup process
		if (observer.isSerial()) {
			addNode0(node);
			return node;
		}
		
		
		switch (executionMode) {
		case ExecutionMode.CONCURRENT_MODE:
			if (node.state == 0) {
				nodeQueue.add(node);
			}
			node.state |= SpaceNode.NODE_IS_CREATING;
			break;
			
		case ExecutionMode.PARALLEL_MODE:
			synchronized (nodeQueue) {
				if (node.state == 0) {
					nodeQueue.add(node);
				}
				node.state |= SpaceNode.NODE_IS_CREATING;
			}
			break;
			
		default:
			throw new Error("Unexpected execution mode");
		
		}

		return node;
	}
	
	/**
	 * Creates a circle node with a specific radius and for a given agent
	 * @param r
	 * @param agent
	 */
	protected CircleNode createCircleNode0(double r) {
		CircleNode node = new CircleNode(this, r);
		return node;
	}
	
	protected abstract void addNode0(SpaceNode node);
	
	protected abstract void addNodeAndMove0(SpaceNode node);
	
	/**
	 * Creates a square node of the given size r
	 * @param r
	 * @return
	 */
	protected final SquareNode createSquareNode(double r, SpaceAgent agent) {
		if (r > maximumNodeRadius)
			maximumNodeRadius = r;
		
		SquareNode node = createSquareNode0(r);
		node.agent = agent;
		node.next = node.prev = node;
		
		if (observer.isSerial()) {
			addNode0(node);
			return node;
		}
		
		switch (executionMode) {
		case ExecutionMode.CONCURRENT_MODE:
			if (node.state == 0) {
				nodeQueue.add(node);
			}
			node.state |= SpaceNode.NODE_IS_CREATING;
			break;
			
		case ExecutionMode.PARALLEL_MODE:
			synchronized (nodeQueue) {
				if (node.state == 0) {
					nodeQueue.add(node);
				}
				node.state |= SpaceNode.NODE_IS_CREATING;
			}
			break;
			
		default:
			throw new Error("Unexpected execution mode");
		}
		
		return node;
	}
	
	/**
	 * Creates a circle node with a specific radius and for a given agent
	 * @param r
	 * @param agent
	 */
	protected SquareNode createSquareNode0(double r) {
		SquareNode node = new SquareNode(this, r);
		return node;
	}
	
	/**
	 * Removes the node from the space
	 * @param node
	 */
	protected final void removeNode(SpaceNode node) {
		if (node.nodeIsRemoved)
			return;
		
		node.nodeIsRemoved = true;
		
		if (observer.isSerial()) {
			removeNode0(node);
			return;
		}

		switch (executionMode) {
		case ExecutionMode.CONCURRENT_MODE:
			if (node.state == 0) {
				nodeQueue.add(node);
			}
			node.state |= SpaceNode.NODE_IS_REMOVING;
			break;
			
		case ExecutionMode.PARALLEL_MODE:
			synchronized (nodeQueue) {
				if (node.state == 0) {
					nodeQueue.add(node);
				}
				node.state |= SpaceNode.NODE_IS_REMOVING;
			}
			break;
			
		default:
			throw new Error("Unexpected execution mode");
		}
	}
	
	protected abstract void removeNode0(SpaceNode node);
	
	/**
	 * Changes the position of the node in the space
	 * @param node
	 */
	protected final void changeNodePosition(SpaceNode node) {
		if (observer.isSerial()) {
			changeNodePosition0(node);
			node.newPosition.set(node.position);
			return;
		}
		
		switch (executionMode) {
		case ExecutionMode.CONCURRENT_MODE:
			if (node.state == 0) {
				nodeQueue.add(node);
			}
			node.state |= SpaceNode.NODE_IS_MOVING;
			break;
			
		case ExecutionMode.PARALLEL_MODE:
			synchronized (nodeQueue) {
				if (node.state == 0) {
					nodeQueue.add(node);
				}
				node.state |= SpaceNode.NODE_IS_MOVING;
			}
			break;
			
		default:
			throw new Error("Unexpected execution mode");
		
		}
		
	}
	
	protected abstract void changeNodePosition0(SpaceNode node);
	
	
	
	private CircleNode temp = new CircleNode(this, 0.0);
	
	/**
	 * Gets all agents in the specific circle neighborhood
	 * @param v - the center of the circle
	 * @param radius - the radius of the circle
	 * @return
	 */
	public ArrayList<SpaceAgent> getAgents(Vector v, double radius) {
		if (executionMode == ExecutionMode.SERIAL_MODE) {
			temp.setRadius0(radius);
			temp.position.set(v);
			return getAgents(temp);
		}
		else {
			CircleNode temp = new CircleNode(this, radius);
			temp.position.set(v);
			return getAgents(temp);
		}
	}
	
	/**
	 * Gets all agents in the place occupied by the agent
	 * @param agent
	 * @return
	 */
	public ArrayList<SpaceAgent> getAgents(SpaceAgent agent) {
		return getAgents(agent.node);
	}

	/**
	 * Gets all agents intersecting with the given space node
	 * @param node
	 * @return
	 */
	protected abstract ArrayList<SpaceAgent> getAgents(SpaceNode node);
	
	/**
	 * Gets all agents in the specific circle neighborhood and of the given type
	 * @param v - the center of the circle
	 * @param radius - the radius of the circle
	 * @param type
	 * @return
	 */
//	public ArrayList<SpaceAgent> getAgents(Vector v, double radius, 
//			Class<? extends SpaceAgent> type) {
	public <T extends SpaceAgent> ArrayList<T> getAgents(Vector v, double radius, 
			Class<T> type) {
		if (executionMode == ExecutionMode.SERIAL_MODE) {
			temp.setRadius0(radius);
			temp.position.set(v);
			return getAgents(temp, type);
		}
		else {
			CircleNode temp = new CircleNode(this, radius);
			temp.position.set(v);
			return getAgents(temp, type);
		}
	}
	
	
	/**
	 * Gets all agents in the specific circle neighborhood 
	 * which are derived from the given type
	 * @param v - the center of the circle
	 * @param radius - the radius of the circle
	 * @param type
	 * @return
	 */
	public <T extends SpaceAgent> ArrayList<T> getAgentsOfKind(Vector v, double radius, 
			Class<T> type) {
		if (executionMode == ExecutionMode.SERIAL_MODE) {
			temp.setRadius0(radius);
			temp.position.set(v);
			return getAgentsOfKind(temp, type);
		}
		else {
			CircleNode temp = new CircleNode(this, radius);
			temp.position.set(v);
			return getAgentsOfKind(temp, type);
		}
	}
	
	/**
	 * Gets all agents of the given type at the place occupied by the agent 
	 * @param agent
	 * @param type
	 * @return
	 */
	public <T extends SpaceAgent> ArrayList<T> getAgents(SpaceAgent agent, Class<T> type) {
		return getAgents(agent.node, type);
	}
	
	/**
	 * Gets all agents derived from the given type at the place occupied by the agent 
	 * @param agent
	 * @param type
	 * @return
	 */
	public <T extends SpaceAgent> ArrayList<T> getAgentsOfKind(SpaceAgent agent, Class<T> type) {
		return getAgentsOfKind(agent.node, type);
	}
	

	/**
	 * Gets all agents of the given type intersecting with the space node
	 * @param node
	 * @param type
	 * @return
	 */
	protected abstract <T extends SpaceAgent> ArrayList<T> getAgents(SpaceNode node, Class<T> type);	

	/**
	 * Gets all agents derived from the given type intersecting with the space node
	 * @param node
	 * @param type
	 * @return
	 */
	protected abstract <T extends SpaceAgent> ArrayList<T> getAgentsOfKind(SpaceNode node, Class<T> type);	

	
	/**
	 * Returns the vector representing the distance between v1 and v2
	 * @param v1
	 * @param v2
	 * @return
	 */
	public abstract Vector getVector(Vector v1, Vector v2);
	
	/**
	 * Returns the vector representing the distance between agent1 and agent2
	 * @param agent1
	 * @param agent2
	 * @return
	 */
	public Vector getVector(SpaceAgent agent1, SpaceAgent agent2) {
		return getVector(agent1.node.position, agent2.node.position);
	}
	
	/**
	 * Returns the random point in the space
	 * @return
	 */
	public abstract Vector getRandomPosition();

	
	
	/**
	 * Calls the beginStep() method for all data layers
	 */
	public void dataLayersBeginStep() {
		for (DataLayer data : dataLayersList) {
			data.beginStep();
		}
	}

	
	/**
	 * Calls the endStep() method for all data layers
	 */
	public void dataLayersEndStep() {
		for (DataLayer data : dataLayersList) {
			data.endStep();
		}
	}

	
	/**
	 * Serializes the space with data layers
	 * @param oos
	 */
	public void serializeSpace(ObjectOutputStream oos) throws Exception {
		oos.writeObject(this);
	}
	
	
	/**
	 * Loads the space with data layers
	 * @param ois
	 */
	public static Space loadSpace(ObjectInputStream ois) throws Exception {
		return (Space) ois.readObject();
	}
	
	
	/**
	 * Custom deserialization is needed.
	 */
	private void readObject(ObjectInputStream ois) throws IOException,
			ClassNotFoundException {

		ois.defaultReadObject();

		nodeQueue = new ArrayList<SpaceNode>(1000);
		
		// Load data layers
		dataLayers = new HashMap<String, DataLayer>();
		dataLayersList = new ArrayList<DataLayer>();
		
		int n = ois.readInt();
		for (int i = 0; i < n; i++) {
			String name = (String) ois.readObject();
			DataLayer data = (DataLayer) ois.readObject();
			
			if (data instanceof Grid) {
				((Grid) data).setSpace(this);
			}
			else if (data instanceof ParallelGrid) {
				((ParallelGrid) data).setSpace(this);
			}
			
			addDataLayer(name, data);
		}
	}

	/**
	 * Custom serialization is needed.
	 */
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();

		oos.writeInt(dataLayersList.size());
		for (String name : dataLayers.keySet()) {
			DataLayer data = dataLayers.get(name);
			
			oos.writeObject(name);
			oos.writeObject(data);
		}
	}
}
