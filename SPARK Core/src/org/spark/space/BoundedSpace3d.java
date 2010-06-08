/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package org.spark.space;

import org.spark.data.Grid3d;
import org.spark.utils.RandomHelper;
import org.spark.utils.Vector;

public abstract class BoundedSpace3d extends Space {
	private static final long serialVersionUID = -1117772371480919688L;
	
	// Auxiliary class for fast query operations
	@SuppressWarnings("serial")
	static class HashGrid3d extends Grid3d {
		protected transient SpaceNode[][][]	nodes;
		
		public HashGrid3d(BoundedSpace3d space, int xSize, int ySize, int zSize) {
			super(space, xSize, ySize, zSize);
			
			nodes = new SpaceNode[xSize][ySize][zSize];
			for (int i = 0; i < xSize; i++)
				for (int j = 0; j < ySize; j++) {
					for (int k = 0; k < zSize; k++) {
						nodes[i][j][k] = new CircleNode(space, 0.0);
						nodes[i][j][k].next = nodes[i][j][k].prev = nodes[i][j][k];
					}
				}
		}
		
		public SpaceNode getNode(int x, int y, int z) {
			return nodes[x][y][z];
		}
		
		public SpaceNode getNode(double x, double y, double z) {
			return nodes[findX(x)][findY(y)][findZ(z)];
		}
	}

	// Space coordinates system
	protected final double	xMin, xMax, yMin, yMax, zMin, zMax;
	protected final double 	xSize, ySize, zSize;

	// Space topology
	protected boolean	wrapX, wrapY, wrapZ;

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
	

	/**
	 * @return Returns the size of the space along y axis
	 */
	public double getZSize() {
		return zSize;
	}

	
	public double getXMin() { return xMin; }
	public double getYMin() { return yMin; }
	public double getXMax() { return xMax; }
	public double getYMax() { return yMax; }
	public double getZMin() { return zMin; }
	public double getZMax() { return zMax; }
	public boolean getWrapX() { return wrapX; }
	public boolean getWrapY() { return wrapY; }
	public boolean getWrapZ() { return wrapZ; }
	
	/**
	 * The default constructor
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 * @param wrapX
	 * @param wrapY
	 */
	public BoundedSpace3d(double xMin, double xMax, 
			     double yMin, double yMax, 
			     double zMin, double zMax, 
			     boolean wrapX, boolean wrapY, boolean wrapZ) 
			{
	
		if (xMax < xMin) xMax = xMin + 1;
		if (yMax < yMin) yMax = yMin + 1;
		if (zMax < zMin) zMax = zMin + 1;
		
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		this.zMin = zMin;
		this.zMax = zMax;
		this.wrapX = wrapX;
		this.wrapY = wrapY;
		this.wrapZ = wrapZ;
		
		xSize = xMax - xMin;
		ySize = yMax - yMin;
		zSize = zMax - zMin;
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
		double dz = v2.z - v1.z;
		
		if (wrapX) {
			double dx2 = dx;
			if (dx > 0) dx2 -= xSize;
			else dx2 += xSize;

			if (Math.abs(dx2) < Math.abs(dx)) dx = dx2;
		}

		if (wrapY) {
			double dy2 = dy;
			if (dy > 0) dy2 -= ySize;
			else dy2 += ySize;

			if (Math.abs(dy2) < Math.abs(dy)) dy = dy2;
		}

		if (wrapZ) {
			double dz2 = dz;
			if (dz > 0) dz2 -= zSize;
			else dz2 += zSize;

			if (Math.abs(dz2) < Math.abs(dz)) dz = dz2;
		}

		
		return new Vector(dx, dy, dz);
	}
	
	
	public Vector getRandomPosition() {
		return new Vector(RandomHelper.random() * xSize + xMin,
						  RandomHelper.random() * ySize + yMin, 
						  RandomHelper.random() * zSize + zMin);
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
	
	
	
	public double restrictZ(double z) {
		if (wrapZ) {
			if (z < zMin) {
				z += zSize;
				if (z < zMin)
					z += (Math.floor((zMin - z) / zSize) + 1) * zSize;
			}
			else if (z >= zMax) {
				z -= zSize;
				if (z >= zMax)
					z -= (Math.floor((z - zMax) / zSize) + 1) * zSize;
			}
		}
		else {
			if (z < zMin)
				z = zMin;
			else if (z > zMax)
				z = zMax;
		}
		
		return z;
	}

}
