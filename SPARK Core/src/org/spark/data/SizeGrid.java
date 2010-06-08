package org.spark.data;

import org.spark.space.Space;
import org.spark.space.SpaceAgent;
import org.spark.space.SpaceNode;
import org.spark.utils.Vector;

/**
 * Grid which respects the size of agents
 * @author Alexey
 *
 */
public class SizeGrid extends Grid {
	/**
	 * Default SVUID
	 */
	private static final long serialVersionUID = 1L;

	public SizeGrid(Space space0, int xSize, int ySize) {
		super(space0, xSize, ySize);
	}

	
	@Override
	public double getValue(SpaceAgent agent) {
		SpaceNode node = agent.getNode();
		Vector p = node.getPosition();
		double r = node.getRelativeSize();
		
		int x0, y0;
		int x1, y1;
		
		double xOffset0, yOffset0;
		double xOffset1, yOffset1;
		
		double area = xStep * yStep;
		double invArea = 1.0 / area;
		
		double value = 0;

		
		// Initialization
		x0 = findX(p.x - r);
		y0 = findY(p.y - r);
		
		// TODO: optimize
		// addX = (int) 2 * r / xStep
		// addY = (int) 2 * r / yStep
		// etc. + torus
		x1 = findX(p.x + r);
		y1 = findY(p.y + r);
		
		xOffset0 = xMin + (x0 + 1) * xStep - (p.x - r);
		yOffset0 = yMin + (y0 + 1) * yStep - (p.y - r);
		
		xOffset1 = (p.x + r) - (xMin + x1 * xStep);
		yOffset1 = (p.y + r) - (yMin + y1 * yStep);
		
		
		double[][] data = this.readData;
		
		// TODO: torus
		// x0 == x1
		if (x0 == x1) {
			// x0 == x1 && y0 == y1
			if (y0 == y1) {
				value = data[x0][y0] * 4 * r * r * invArea;
				return value;
			}
			
			value += data[x0][y0] * yOffset0 * 2 * r;
			
			for (int j = y0 + 1; j < y1; j++) {
				value += data[x0][j] * 2 * r * yStep;
			}
			
			value += data[x0][y1] * 2 * r * yOffset1;
			
			value *= invArea;
			return value;
		}
		
		// y0 == y1
		if (y0 == y1) {
			value += data[x0][y0] * xOffset0 * 2 * r;
			
			for (int i = x0 + 1; i < x1; i++) {
				value += data[i][y0] * xStep * 2 * r;
			}
			
			value += data[x1][y0] * xOffset1 * 2 * r;
			
			value *= invArea;
			return value;
		}
		
		// TODO: torus topology
		
		// 00
		value += data[x0][y0] * invArea * xOffset0 * yOffset0;
		
		// x0
		for (int i = x0 + 1; i < x1; i++) {
			value += data[i][y0] * invArea * yOffset0 * xStep;
		}
		
		// 10
		value += data[x1][y0] * invArea * xOffset1 * yOffset0;
		
		// 0x
		for (int j = y0 + 1; j < y1; j++) {
			value += data[x0][j] * invArea * xOffset0 * yStep;
		}
		
		// xx
		for (int i = x0 + 1; i < x1; i++) {
			for (int j = y0 + 1; j < y1; j++) {
				value += data[i][j];
			}
		}
		
		// 1x
		for (int j = y0 + 1; j < y1; j++) {
			value += data[x1][j] * invArea * xOffset1 * yStep;
		}
		
		// 01
		value += data[x0][y1] * invArea * xOffset0 * yOffset1;
		
		// x1
		for (int i = x0 + 1; i < x1; i++) {
			value += data[i][y1] * invArea * xStep * yOffset1;
		}
		
		// 11
		value += data[x1][y1] * invArea * xOffset1 * yOffset1;
		
		
		return value;
	}



	@Override
	public void setValue(SpaceAgent agent, double value) {
		SpaceNode node = agent.getNode();
		Vector p = node.getPosition();
		double r = node.getRelativeSize();
		
		int x0, y0;
		int x1, y1;
		
		double xOffset0, yOffset0;
		double xOffset1, yOffset1;
		
		double area = 4 * r * r;
		double invArea = 1.0 / area;
		
		
		// Initialization
		x0 = findX(p.x - r);
		y0 = findY(p.y - r);
		
		// TODO: optimize
		// addX = (int) 2 * r / xStep
		// addY = (int) 2 * r / yStep
		// etc. + torus
		x1 = findX(p.x + r);
		y1 = findY(p.y + r);
		
		xOffset0 = xMin + (x0 + 1) * xStep - (p.x - r);
		yOffset0 = yMin + (y0 + 1) * yStep - (p.y - r);
		
		xOffset1 = (p.x + r) - (xMin + x1 * xStep);
		yOffset1 = (p.y + r) - (yMin + y1 * yStep);
		
		// TODO: torus
		// x0 == x1
		if (x0 == x1) {
			// x0 == x1 && y0 == y1
			if (y0 == y1) {
				data[x0][y0] = value;
				return;
			}
			
			value *= invArea;
			
			data[x0][y0] = value * yOffset0 * 2 * r;
			
			for (int j = y0 + 1; j < y1; j++) {
				data[x0][j] = value * 2 * r * yStep;
			}

			data[x0][y1] = value * 2 * r * yOffset1;
			
			return;
		}
		
		// y0 == y1
		if (y0 == y1) {
			value *= invArea;
			
			data[x0][y0] = value * xOffset0 * 2 * r;
			
			for (int i = x0 + 1; i < x1; i++) {
				data[i][y0] = value * xStep * 2 * r;
			}
			
			data[x1][y0] = value * xOffset1 * 2 * r;
			
			return;
		}
		
		// TODO: torus topology
		
		value *= invArea;
		
		// 00
		data[x0][y0] = value * xOffset0 * yOffset0;
		
		// x0
		for (int i = x0 + 1; i < x1; i++) {
			data[i][y0] = value * yOffset0 * xStep;
		}
		
		// 10
		data[x1][y0] = value * xOffset1 * yOffset0;
		
		// 0x
		for (int j = y0 + 1; j < y1; j++) {
			data[x0][j] = value * xOffset0 * yStep;
		}
		
		// xx
		for (int i = x0 + 1; i < x1; i++) {
			for (int j = y0 + 1; j < y1; j++) {
				data[i][j] = value * xStep * yStep;
			}
		}
		
		// 1x
		for (int j = y0 + 1; j < y1; j++) {
			data[x1][j] = value * xOffset1 * yStep;
		}
		
		// 01
		data[x0][y1] = value * xOffset0 * yOffset1;
		
		// x1
		for (int i = x0 + 1; i < x1; i++) {
			data[i][y1] = value * xStep * yOffset1;
		}
		
		// 11
		data[x1][y1] = value * xOffset1 * yOffset1;
	}
}


