package org.spark.space;

import java.util.ArrayList;

import org.spark.cluster.ClusterManager;


public class ClusterNetLogoSpace extends GridSpace {
	private static final long serialVersionUID = 1L;
	// define border area for which the information about agents is available
	protected int xBorder, yBorder;
	
	
	public int getXBorder() {
		return xBorder;
	}
	
	
	public int getYBorder() {
		return yBorder;
	}
	
	
	public ClusterNetLogoSpace(double xMin, double xMax, double yMin, double yMax,
			boolean wrapX, boolean wrapY) {
		this(xMin, xMax, yMin, yMax, wrapX, wrapY, 0, 0);
	}
	
	
	public ClusterNetLogoSpace(double xMin, double xMax, double yMin,
			double yMax, boolean wrapX, boolean wrapY, int xBorder, int yBorder) {
		// TODO: think about asymmetric borders: left, right, bottom, top
		super(xMin - xBorder, xMax + xBorder, yMin - yBorder, yMax + yBorder, wrapX, wrapY);
		
		this.xBorder = xBorder;
		this.yBorder = yBorder;
	}
	
	@Override
	protected void changeNodePosition0(SpaceNode node) {
		if (ClusterManager.getInstance().getGlobalSpace().moveNode(node))
			return;
		
		super.changeNodePosition0(node);
	}
	
	/**
	 * Removes all agents at the border
	 */
	void removeAgentsAtTheBorder() {
		// FIXME: hack
		SpaceNode[][] nodes = hashGrid.nodes;
		
		int xSize = nodes.length;
		int ySize = nodes[0].length;
		
		for (int i = 0; i < xBorder; i++) {
			for (int j = 0; j < ySize; j++) {
				SpaceNode node = nodes[i][j];
				node.next = node.prev = node;

				node = nodes[xSize - i - 1][j];
				node.next = node.prev = node;
			}
		}
		
		for (int j = 0; j < yBorder; j++) {
			for (int i = xBorder; i < xSize - xBorder; i++) {
				SpaceNode node = nodes[i][j];
				node.next = node.prev = node;

				node = nodes[i][ySize - j - 1];
				node.next = node.prev = node;
			}
		}
	}
	

	/**
	 * Returns all agents from the hash grid in the specified border region.
	 * Negative values of x and y indicate right (bottom) borders.
	 * Zero values of x and y indicate the whole border
	 * @param x
	 * @param y 
	 * @return
	 */
	ArrayList<SpaceAgent> getBorderAgents(int x, int y) {
		ArrayList<SpaceAgent> agents = new ArrayList<SpaceAgent>(100);
		
		SpaceNode[][] nodes = hashGrid.nodes;

		int xSize = nodes.length;
		int ySize = nodes[0].length;
		
		int x1, x2, y1, y2;
		
		if (x < 0) {
			x1 = xSize - xBorder + x;
			x2 = xSize - xBorder;
		}
		else if (x == 0) {
			x1 = xBorder;
			x2 = xSize - xBorder;
		}
		else {
			x1 = xBorder;
			x2 = x + xBorder;
		}
		
		if (y < 0) {
			y1 = ySize - yBorder + y;
			y2 = ySize - yBorder;
		}
		else if (y == 0) {
			y1 = yBorder;
			y2 = ySize - yBorder;
		}
		else {
			y1 = yBorder;
			y2 = y + yBorder;
		}
		
		for (int i = x1; i < x2; i++) {
			for (int j = y1; j < y2; j++) {
				SpaceNode first = nodes[i][j];
				SpaceNode node = first.next;

				while (node != first) {
					// FIXME: hack
					node.agent.setDeepSerialization(false);
					agents.add(node.agent);
					node = node.next;
				}
			}
		}
		
		return agents;
	}
}
