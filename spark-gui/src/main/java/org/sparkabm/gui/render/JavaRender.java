package org.sparkabm.gui.render;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sparkabm.runtime.data.DataObject_AgentData;
import org.sparkabm.runtime.data.DataObject_SpaceAgents;
import org.sparkabm.runtime.data.DataObject_SpaceLinks;
import org.sparkabm.runtime.data.DataObject_Spaces;
import org.sparkabm.runtime.data.DataRow;
import org.sparkabm.gui.render.images.TileManager;
import org.sparkabm.math.Vector;
import org.sparkabm.math.Vector4d;

/**
 * Render using Java2d
 * 
 * @author Monad
 * 
 */
public class JavaRender extends Render {
	private static final Logger logger = LogManager.getLogger();

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
	JavaRender(int interval, boolean noGUI) {
		super(interval);
		logger.info("Initializing JavaRender");
		
		if (!noGUI)
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
		int w = 800;
		int h = 800;
		
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

		// Manual cell size
		if (selectedSpace != null && !selectedSpace.autoSize) {
			xMax = xMin + 1;
			yMin = yMax - 1;
			width = selectedSpace.cellXSize;
			height = selectedSpace.cellYSize;
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
		
		// Test
		int xsize = (int)(2 * (x1 - x0));
		int ysize = (int)(2 * (y1 - y0));
		
		if (xsize != width || ysize != width) {
//			canvas.setSize(xsize, ysize);
		}
	}
	
	
	/**
	 * Transforms screen coordinates into space coordinates
	 */
	protected Vector getCoordinates(int x, int y) {
		if (transform == null)
			return new Vector(x, y, 0);
		
		AffineTransform tr = new AffineTransform(transform);
		Point2D.Double pt = new Point2D.Double(x, y);
		
		tr.translate(dx, dy);
		tr.scale(zoom, zoom);
		
		try {
			tr = tr.createInverse();
		}
		catch (Exception e) {
			logger.error(e);
			return new Vector(x, y, 0);
		}
		
		Point2D ptOut = tr.transform(pt, null);
		
		Vector v = new Vector(ptOut.getX(), ptOut.getY(), 0);
		return v;
	}

	/**
	 * Renders a grid
	 * 
	 * @param g
	 * @param info
	 * @param data
	 * @param spaceIndex
	 */
	protected void renderDataLayer(Graphics2D g, DataLayerGraphics info, DataRow data,
			int spaceIndex) {
		if (info == null || data == null)
			return;
		
		DataLayerGraphics.GridInfo gridInfo = info.getGridInfo(data);
		if (gridInfo == null)
			return;
		
		if (gridInfo.spaceIndex != spaceIndex)
			return;

		int xSize = gridInfo.xSize;
		int ySize = gridInfo.ySize;

		double xStep = gridInfo.xStep;
		double yStep = gridInfo.yStep;

		Vector[][] colors = info.getColors(data);
		if (colors == null)
			return;

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
	 * Renders a tile
	 * @return true if successful
	 */
	protected boolean drawTile(Graphics2D g, TileManager tiles, DataObject_AgentData agentData, int index,
				double x, double y, double r, double theta) {
		if (agentData == null)
			return false;
		
		// Get parameters
		String tileSet = agentData.getStringVal(index, "tile-set");
		if (tileSet == null)
			return false;
		String tileName = agentData.getStringVal(index, "tile-name");
		if (tileName == null)
			return false;
		
		// Get an image
		TileManager.TileImage tile = tiles.getImage(tileSet, tileName);
		if (tile == null)
			return false;
		
		Image image = tile.image;
		
		// Draw the image
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		AffineTransform tr = new AffineTransform();
		tr.translate(x, y);
		
		double scale = Math.max(2 * r / w, 2 * r / h);
//		tr.scale(2 * r / w, 2 * r / h);
		tr.scale(scale, scale);
		
		if (theta != 0) {
			tr.rotate(theta);
		}

		tr.scale(1, -1);
		
		// Reflections
		if (tile.xReflect)
			tr.scale(-1, 1);
		if (tile.yReflect)
			tr.scale(1, -1);
		
		tr.translate(-w / 2.0, -h / 2.0);
		g.drawImage(image, tr, null);
		
		return true;
	}
	
	
	/**
	 * Draws a specific shape
	 */
	private void drawShape(Graphics2D g, DataObject_SpaceAgents.ShapeInfo shape, boolean border) {
		Shape s = null;
		switch (shape.type) {
		case 0:
			float r = shape.hx;
			s = new Ellipse2D.Float(-r, -r, 2 * r, 2 * r);
			break;
			
		case 1:
			float hx = shape.hx;
			float hy = shape.hy;
			s = new Rectangle2D.Float(-hx, -hy, 2 * hx, 2 * hy);
		}
		
		if (s == null)
			return;
		
		g.fill(s);
		if (border) {
			g.setColor(Color.black);
			g.draw(s);
		}	
	}
	

	/**
	 * Renders agents
	 */
	protected void renderAgents(Graphics2D g, DataObject_SpaceAgents agents,
			DataObject_AgentData agentData,
			int spaceIndex, AgentStyle agentStyle) {
		// If invisible, then return
		if (!agentStyle.visible)
			return;

		if (agents == null)
			return;

		// Get parameters
		int n = agents.getTotalNumber();
		Vector[] positions = agents.getPositions();
		double[] radii = agents.getRadii();
		Vector4d[] colors = agents.getColors();
		double[] rotations = agents.getRotations();
		DataObject_SpaceAgents.ShapeInfo[] shapeInfo = agents.getShapeInfo();
		int[] shapes = agents.getShapes();
		int[] spaceIndices = agents.getSpaceIndices();
		String[] labels = agents.getLabels();
		
		// Special composite
		Composite originalComposite = null;
		
		/* Transparent agents */
		if (agentStyle.transparent) {
			originalComposite = g.getComposite(); 
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		}
		
		/* Tile manager */
		TileManager tiles = agentStyle.getTileManager();
		if (agentData == null)
			tiles = null;
		
		/* Label options */
		Font font = agentStyle.getFont();
		Color labelColor = agentStyle.getLabelColor().toAWTColor();
		float dxLabel = agentStyle.getLabelDx();
		float dyLabel = agentStyle.getLabelDy();

		// Set basic options
		g.setFont(font);
		g.setStroke(new BasicStroke(0));
		FontMetrics metrics = g.getFontMetrics();

		// Render all agents
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
			
			boolean drawShape = true;
			
			// Render a picture first
			if (tiles != null) {
				if (drawTile(g, tiles, agentData, i, x, y, r, rotations[i])) {
					if (!agentStyle.getDrawShapeWithImageFlag())
						drawShape = false;
				}
			}

			// Render a geometric shape
			if (drawShape) {
				if (shapeInfo[i] != null) {
					AffineTransform oldTr = g.getTransform();
					g.translate(x, y);
					g.rotate(rotations[i]);
					drawShape(g, shapeInfo[i], agentStyle.border);
					g.setTransform(oldTr);
				}
				else if (r > 0) 
				{
					Shape s = null;

					switch (shapes[i]) {
				//			case SpaceAgent.CIRCLE:
					case 1:
						s = new Ellipse2D.Double(x - r, y - r, r2, r2);
						break;

					//			case SpaceAgent.SQUARE:
					case 2:
						s = new Rectangle2D.Double(x - r, y - r, r2, r2);
						break;

					//			case SpaceAgent.TORUS:
					case 3:
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
			
			// Draw labels
			if (agentStyle.label) {
				String label = labels[i];
				if (label != null) {
					g.setColor(labelColor);
					AffineTransform tr = g.getTransform();
					g.translate(x, y);
					g.scale(0.1f, -0.1f);
					
					float w = metrics.stringWidth(label);
//					float h = metrics.getHeight();
					float h = metrics.getDescent();
					
//					float dx = -(float) r * 10;
//					float dy = (float) r * 5;
					float dx = -w / 2 + dxLabel;
					float dy = h + dyLabel;
					g.drawString(label, dx, dy);
					
					g.setTransform(tr);
				}
			}
		}
		
		// Restore the original alpha composite
		if (originalComposite != null) {
			g.setComposite(originalComposite);
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
		if (canvas != null)
			reshape(0, 0, canvas.getWidth(), canvas.getHeight());
		else
			reshape(0, 0, 800, 800);

		// Apply transformations
		g.transform(transform);

		g.translate(dx, dy);
		g.scale(zoom, zoom);

		if (selectedSpace.swapXY) {
			g.rotate(-Math.PI / 2);
		}
		
//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
//		        RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);


		
		if (selectedDataLayer != null) {
//			DataObject_Grid gridData = data.getGrid(selectedDataLayer.getName());
			renderDataLayer(g, selectedDataLayer, data, spaceIndex);
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
			else {
				DataObject_AgentData additionalData = data.getAgentData(agentStyle.typeName);
				renderAgents(g, agentsData, additionalData, spaceIndex, agentStyle);
			}
		}

	}

}
