/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package org.spark.space;

import java.util.ArrayList;

import org.spark.data.Grid;
import org.spark.utils.RandomHelper;
import org.spark.utils.Vector;

public abstract class BoundedSpace extends Space {
	private static final long serialVersionUID = 9016717038171133599L;
	
	// Auxiliary class for fast query operations
	@SuppressWarnings("serial")
	static class HashGrid2d extends Grid {
		protected transient SpaceNode[][]	nodes;
		
		public HashGrid2d(BoundedSpace space, int xSize, int ySize) {
			super(space, xSize, ySize);
			
			nodes = new SpaceNode[xSize][ySize];
			for (int i = 0; i < xSize; i++)
				for (int j = 0; j < ySize; j++) {
					nodes[i][j] = new CircleNode(space, 0.0);
					nodes[i][j].next = nodes[i][j].prev = nodes[i][j];
				}
		}
		
		public SpaceNode getNode(int x, int y) {
			return nodes[x][y];
		}
		
		public SpaceNode getNode(double x, double y) {
			int xx = findX(x);
			int yy = findY(y);
			
			return nodes[xx][yy];
		}
		
		
		/**
		 * Debug method
		 */
		public void verifyIntegrity() {
			ArrayList<SpaceNode> list = new ArrayList<SpaceNode>(100);
			
			for (int i = 0; i < xSize; i++)
				for (int j = 0; j < ySize; j++) {
					SpaceNode first = nodes[i][j];
					if (first == null || first.agent != null)
						throw new Error("Integrity error 1");
					
					list.clear();
					
					for (SpaceNode next = first.next; next != first; next = next.next) {
						if (next == null)
							throw new Error("Integrity error 2");
						
						if (next.agent == null)
							throw new Error("Integrity error 3");
						
						list.add(next);
					}
					
					int index = list.size() - 1;
					
					for (SpaceNode prev = first.prev; prev != first; prev = prev.prev) {
						if (prev == null)
							throw new Error("Integrity error 4");
						
						if (prev.agent == null)
							throw new Error("Integrity error 5");
						
						if (prev != list.get(index))
							throw new Error("Integrity error 6");
						
						index--;
					}
				}
		}
		
	}

	
	// Space coordinates system
	protected final double	xMin, xMax, yMin, yMax, xSize, ySize;

	// Space topology
	protected boolean	wrapX, wrapY;

	/**
	 * @return Returns the size of the space along x axis
	 */
	public double getXSize() {
		return xSize;
	}
	
	/**
	 * @return Returns the size of the space along y axis
	 */
	public double getYSize() {
		return ySize;
	}
	
	
	public double getXMin() { return xMin; }
	public double getYMin() { return yMin; }
	public double getXMax() { return xMax; }
	public double getYMax() { return yMax; }
	public boolean getWrapX() { return wrapX; }
	public boolean getWrapY() { return wrapY; }
	
	/**
	 * The default constructor
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 * @param wrapX
	 * @param wrapY
	 */
	public BoundedSpace(double xMin, double xMax, 
			     double yMin, double yMax, 
			     boolean wrapX, boolean wrapY) {
		//nodes = new ArrayList<SpaceNode>(100);
	
		if (xMax < xMin) xMax = xMin + 1;
		if (yMax < yMin) yMax = yMin + 1;
		
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		this.wrapX = wrapX;
		this.wrapY = wrapY;

		this.xSize = xMax - xMin;
		this.ySize = yMax - yMin;
	}
	
	/**
	 * Returns the vector representing the distance between v1 and v2
	 * @param v1
	 * @param v2
	 * @return
	 */
	public Vector getVector(Vector v1, Vector v2) {
		double dx = v2.x - v1.x;
		double dy = v2.y - v1.y;
		
		if (wrapX) {
			double dx2 = dx;
			if (dx > 0) dx2 -= getXSize();
			else dx2 += getXSize();

			if (Math.abs(dx2) < Math.abs(dx)) dx = dx2;
		}

		if (wrapY) {
			double dy2 = dy;
			if (dy > 0) dy2 -= getYSize();
			else dy2 += getYSize();

			if (Math.abs(dy2) < Math.abs(dy)) dy = dy2;
		}

		return new Vector(dx, dy, 0);
	}
	
	
	public Vector getRandomPosition() {
		return new Vector(RandomHelper.random() * getXSize() + xMin,
						  RandomHelper.random() * getYSize() + yMin, 0);
	}
	
	
	public double restrictX(double x) {
		if (wrapX) {
			if (x < xMin) {
				x += xSize;
				if (x < xMin)
					x += (Math.floor((xMin - x) / xSize) + 1) * xSize;
			}
			else if (x >= xMax) {
				x -= xSize;
				if (x >= xMax)
					x -= (Math.floor((x - xMax) / xSize) + 1) * xSize;
			}
		}
		else {
			if (x < xMin)
				x = xMin;
			else if (x > xMax)
				x = xMax;
		}
		
		return x;
	}


	public double restrictY(double y) {
		if (wrapY) {
			if (y < yMin) {
				y += ySize;
				if (y < yMin)
					y += (Math.floor((yMin - y) / ySize) + 1) * ySize;
			}
			else if (y >= yMax) {
				y -= ySize;
				if (y >= yMax)
					y -= (Math.floor((y - yMax) / ySize) + 1) * ySize;
			}
		}
		else {
			if (y < yMin)
				y = yMin;
			else if (y > yMax)
				y = yMax;
		}
		
		return y;
	}


}
