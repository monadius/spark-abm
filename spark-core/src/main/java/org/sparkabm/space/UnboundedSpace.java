package org.spark.space;

import org.spark.data.Grid;

/**
 * Abstract representation of an unbounded space
 */
public abstract class UnboundedSpace extends BoundedSpace {
	/* Default serial version UID */
	private static final long serialVersionUID = 1L;
    
	/**
	 * Hash grid for operations inside an unbounded space
	 * @author Alexey
	 */
	static class UnboundedHashGrid extends Grid {
		/* Default serial version UID */
		private static final long serialVersionUID = 1L;
		protected transient SpaceNode[][]	nodes;

		/**
		 * Default constructor
		 * @param space
		 * @param xSize
		 * @param ySize
		 */
		public UnboundedHashGrid(UnboundedSpace space, int xSize, int ySize) {
			super(space, xSize, ySize);
			
			nodes = new SpaceNode[xSize + 2][ySize + 2];
			for (int i = 0; i < xSize + 2; i++)
				for (int j = 0; j < ySize + 2; j++) {
					nodes[i][j] = new CircleNode(space, 0.0);
					nodes[i][j].next = nodes[i][j].prev = nodes[i][j];
				}
		}
			
		public SpaceNode getNode(int x, int y) {
			return nodes[x + 1][y + 1];
		}
			
		public SpaceNode getNode(double x, double y) {
			int xx, yy;
				
			if (!wrapX) {
				if (x < xMin)
					xx = 0;
				else if (x >= xMax)
					xx = xSize + 1;
				else
					xx = findX(x) + 1;
			}
			else {
				xx = findX(x) + 1;
			}
			
			if (!wrapY) {
				if (y < yMin)
					yy = 0;
				else if (y >= yMax)
					yy = ySize + 1;
				else
					yy = findY(y) + 1;
			}
			else {
				yy = findY(y) + 1;
			}
				
			return nodes[xx][yy];
		}
	}
	
	
	/**
	 * Default constructor
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 * @param wrapX
	 * @param wrapY
	 */
	public UnboundedSpace(double xMin, double xMax, double yMin, double yMax,
			boolean wrapX, boolean wrapY) {
		super(xMin, xMax, yMin, yMax, wrapX, wrapY);
	}


	@Override
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
		
		return x;
	}


	@Override
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
		
		return y;
	}

}
