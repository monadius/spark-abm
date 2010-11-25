package org.spark.runtime.external.render;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.spark.runtime.data.DataObject_Grid;
import org.spark.runtime.data.DataObject_SpaceAgents;
import org.spark.runtime.data.DataObject_SpaceLinks;
import org.spark.runtime.data.DataObject_Spaces;
import org.spark.runtime.data.DataRow;
import org.spark.space.SpaceAgent;
import org.spark.utils.Vector;
import org.spark.utils.Vector4d;

import com.spinn3r.log5j.Logger;

/**
 * Render using Java2d
 * 
 * @author Monad
 * 
 */
public class JavaRender extends Render {
	private static final Logger logger = Logger.getLogger();

	/* Canvas */
	private JavaRenderCanvas canvas;

	/* Current data */
	private DataRow data;
	/* Information about current space bounds */
	private float xMin, yMin, xMax, yMax;
	

	/* Graphics transformation */
	private AffineTransform transform = new AffineTransform();

	/**
	 * Internal constructor
	 */
	JavaRender(int interval) {
		super(interval);
		logger.info("Initializing JavaRender");
		canvas = new JavaRenderCanvas(this);
	}

	@Override
	protected void display(DataRow data) {
		this.data = data;

		if (canvas != null)
			canvas.display();
	}

	@Override
	public Canvas getCanvas() {
		return canvas;
	}

	@Override
	public void saveSnapshot(File dir, String fname, DataRow data) {
		// Default values of width and height
		int w = 500;
		int h = 500;
		
		if (canvas != null) {
			w = canvas.getWidth();
			h = canvas.getHeight();
		}
		
		if (w <= 0 || h <= 0) {
			logger.warn("Width or height is negative");
			return;
		}
		
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = image.createGraphics();
		
		DataRow tmp = this.data;
		this.data = data;
		display(g);
		this.data = tmp;
		
		g.dispose();
		
		try {
			File out = new File(dir, fname + ".png");
			ImageIO.write(image, "png", out);
		}
		catch (Exception e) {
			logger.error(e);
		}
	}

	/**
	 * Reshape method
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void reshape(int x, int y, int width, int height) {
//		if (xMin == 0 && xMax == 0 && yMin == 0 && yMax == 0) {
			if (data != null && selectedSpace != null) {
				DataObject_Spaces spaces = data.getSpaces();
				if (spaces != null) {
					int index = spaces.getIndex(selectedSpace.name);
					if (index != -1) {
						Vector min = spaces.getMins()[index];
						Vector max = spaces.getMaxs()[index];
						
						xMin = (float) min.x;
						yMin = (float) min.y;
						xMax = (float) max.x;
						yMax = (float) max.y;
					}
				}
			}
//		}

		if (xMin >= xMax - 1 || yMin >= yMax - 1) {
			xMin = yMin = -60;
			xMax = yMax = 60;
		}

		float x0 = xMin;
		float x1 = xMax;
		float y0 = yMin;
		float y1 = yMax;
		
		if (selectedSpace != null && selectedSpace.swapXY) {
			x0 = yMin;
			x1 = yMax;
			y0 = xMin;
			y1 = xMax;
		}

		float a = width / (x1 - x0);
		float c = -height / (y1 - y0);
		float b = -a * x0;
		float d = -c * y1;

		AffineTransform t2 = new AffineTransform();
		t2.scale(a, c);

		transform.setToIdentity();
		transform.translate(b, d);
		transform.concatenate(t2);
	}

	/**
	 * Renders a grid
	 * 
	 * @param g
	 * @param grid
	 * @param space
	 */
	protected void renderDataLayer(Graphics2D g, DataObject_Grid grid,
			int spaceIndex) {
		if (grid == null)
			return;
		
		if (grid.getSpaceIndex() != spaceIndex)
			return;

		int xSize = grid.getXSize();
		int ySize = grid.getYSize();

		double xStep = grid.getXStep();
		double yStep = grid.getYStep();

		Vector[][] colors = GridGraphics.getColors(grid, selectedDataLayer);

		Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, xStep, yStep);
		double x = xMin;
		double y = yMin;
		for (int i = 0; i < xSize; i++, x += xStep) {
			y = yMin;
			for (int j = 0; j < ySize; j++, y += yStep) {
				rect.x = x;
				rect.y = y;
				g.setColor(colors[i][j].toAWTColor());
				g.fill(rect);
			}
		}

	}

	/**
	 * Renders all visible space links of the given type (style)
	 * 
	 * @param g
	 * @param linkStyle
	 */
	protected void renderLinks(Graphics2D g, DataObject_SpaceLinks links, int spaceIndex, AgentStyle linkStyle) {
		if (!linkStyle.visible)
			return;

		if (links == null)
			return;

		int n = links.getTotalNumber();
		Vector[] ends1 = links.getEnd1();
		Vector[] ends2 = links.getEnd2();
		Vector4d[] colors = links.getColors();
		int[] spaceIndices = links.getSpaceIndices();
		double[] width = links.getWidth();

		for (int i = 0; i < n; i++) {
			Vector end1 = ends1[i]; 
			Vector end2 = ends2[i];

			if (end1 == null || end2 == null)
				continue;

			if (spaceIndices[i] != spaceIndex)
				continue;

			Vector4d color = colors[i];

			double x1 = end1.x, y1 = end1.y;
			double x2 = end2.x, y2 = end2.y;

			float w = (float) width[i];
			g.setStroke(new BasicStroke(w));
			g.setColor(color.toAWTColor());

			Shape s = new Line2D.Double(x1, y1, x2, y2);
			g.draw(s);
		}

	}


	/**
	 * Renders agents
	 * 
	 * @param g
	 * @param agents
	 * @param spaceIndex
	 * @param agentStyle
	 */
	protected void renderAgents(Graphics2D g, DataObject_SpaceAgents agents,
			int spaceIndex, AgentStyle agentStyle) {
		if (!agentStyle.visible)
			return;

		if (agents == null)
			return;

		g.setStroke(new BasicStroke(0));
		int n = agents.getTotalNumber();
		Vector[] positions = agents.getPositions();
		double[] radii = agents.getRadii();
		Vector4d[] colors = agents.getColors();
		int[] shapes = agents.getShapes();
		int[] spaceIndices = agents.getSpaceIndices();

		for (int i = 0; i < n; i++) {
			if (spaceIndices[i] != spaceIndex)
				continue;

			Vector pos = positions[i];
			Vector4d color = colors[i];

			if (pos == null || color == null)
				continue;

			double r = radii[i];
			double r2 = r * 2.0;
			double x = pos.x;
			double y = pos.y;

			g.setColor(color.toAWTColor());

			Shape s = null;

			switch (shapes[i]) {
			case SpaceAgent.CIRCLE:
				s = new Ellipse2D.Double(x - r, y - r, r2, r2);
				break;

			case SpaceAgent.SQUARE:
				s = new Rectangle2D.Double(x - r, y - r, r2, r2);
				break;

			case SpaceAgent.TORUS:
				s = new Ellipse2D.Double(x - r, y - r, r2, r2);
				break;
			}

			g.fill(s);
			if (agentStyle.border) {
				g.setColor(Color.black);
				g.draw(s);
			}
		}
	}

	/**
	 * Displays the data
	 * 
	 * @param g
	 */
	protected void display(Graphics2D g) {
		if (data == null)
			return;

		if (selectedSpace == null)
			return;

		DataObject_Spaces spaces = data.getSpaces();
		if (spaces == null)
			return;

		// Get the index of the selected space
		int index = spaces.getIndex(selectedSpace.name);
		if (index == -1)
			return;
		
		int spaceIndex = spaces.getIndices()[index];
		
		// TODO: call it when necessary only
		reshape(0, 0, canvas.getWidth(), canvas.getHeight());

		// Apply transformations
		g.transform(transform);

		g.translate(dx, dy);
		g.scale(zoom, zoom);

		if (selectedSpace.swapXY) {
			g.rotate(-Math.PI / 2);
		}


		
		if (selectedDataLayer != null) {
			DataObject_Grid gridData = data.getGrid(selectedDataLayer.getName());
			renderDataLayer(g, gridData, spaceIndex);
		}

		for (int k = agentStyles.size() - 1; k >= 0; k--) {
			AgentStyle agentStyle = agentStyles.get(k);
			
			if (!agentStyle.visible)
				continue;
			
			DataObject_SpaceAgents agentsData = data
					.getSpaceAgents(agentStyle.typeName);
			
			if (agentsData == null)
				continue;
			
			if (agentsData instanceof DataObject_SpaceLinks)
				renderLinks(g, (DataObject_SpaceLinks) agentsData, spaceIndex, agentStyle);
			else
				renderAgents(g, agentsData, spaceIndex, agentStyle);
		}

	}

}
