package org.spark.gui.render;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.spark.core.Agent;
import org.spark.core.Observer;
import org.spark.data.DataLayer;
import org.spark.space.BoundedSpace;
import org.spark.space.Space;
import org.spark.space.SpaceAgent;
import org.spark.space.SpaceLink;
import org.spark.space.SpaceNode;
import org.spark.utils.Vector;
import org.spark.utils.Vector4d;

import com.spinn3r.log5j.Logger;

public class JavaRender extends Render {
	private static final Logger logger = Logger.getLogger();

	private JavaRenderCanvas canvas;

	public JavaRender() {
		logger.info("Initializing JavaRender");

		canvas = new JavaRenderCanvas(this);
	}

	@Override
	public void display() {
		if (canvas != null)
			canvas.display();
	}

	@Override
	public Canvas getCanvas() {
		return canvas;
	}

	@Override
	public void takeSnapshot(String fname) {
		canvas.takeSnapshot(fname);
	}

	private AffineTransform transform = new AffineTransform();

	public void reshape(int x, int y, int width, int height) {
		Space space = Observer
				.getSpace(selectedSpace != null ? selectedSpace.name : null);
		float xMin = -60, xMax = 60, yMin = -60, yMax = 60;

		if (space != null) {
			if (space instanceof BoundedSpace) {
				BoundedSpace space2 = (BoundedSpace) space;
				xMin = (float) space2.getXMin();
				xMax = (float) space2.getXMax();
				yMin = (float) space2.getYMin();
				yMax = (float) space2.getYMax();
			}
		}

		if (xMin >= xMax - 1 || yMin >= yMax - 1) {
			xMin = yMin = -60;
			xMax = yMax = 60;
		}

		if (selectedSpace != null && selectedSpace.swapXY) {
			float t = xMin;
			xMin = yMin;
			yMin = t;

			t = xMax;
			xMax = yMax;
			yMax = xMax;
		}

		float a = width / (xMax - xMin);
		float c = -height / (yMax - yMin);
		float b = -a * xMin;
		float d = -c * yMax;

		AffineTransform t2 = new AffineTransform();
		t2.scale(a, c);

		transform.setToIdentity();
		transform.translate(b, d);
		transform.concatenate(t2);

		// System.out.println(transform.toString());
	}

	protected void renderDataLayer(Graphics2D g, DataLayerWithColors grid,
			BoundedSpace space) {
		if (grid == null || space == null)
			return;

		if (selectedDataLayer.gridGeometry == null)
			selectedDataLayer.gridGeometry = grid.getGeometry2();

		Vector[][] gridGeometry = selectedDataLayer.gridGeometry;

		int xSize = gridGeometry.length;
		int ySize = gridGeometry[0].length;

		double xStep = grid.getXStep();
		double yStep = grid.getYStep();

		double xMin = space.getXMin();
		double yMin = space.getYMin();

		Vector[][] colors = grid.getColors(selectedDataLayer.val1,
				selectedDataLayer.val2, selectedDataLayer.color1,
				selectedDataLayer.color2);

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
	protected void renderLinks(Graphics2D g, Space space, AgentStyle linkStyle) {
		Agent[] links;

		if (!linkStyle.visible)
			return;

		if (!SpaceLink.class.isAssignableFrom(linkStyle.agentType))
			return;

		links = Observer.getInstance().getAgents(linkStyle.agentType);

		if (links == null)
			return;

		int n = links.length;

		for (int i = 0; i < n; i++) {
			SpaceLink link = (SpaceLink) links[i];
			SpaceAgent end1 = link.getEnd1();
			SpaceAgent end2 = link.getEnd2();

			if (end1 == null || end2 == null)
				continue;

			SpaceNode node1 = end1.getNode();
			SpaceNode node2 = end2.getNode();
			
			if (node1 == null || node2 == null)
				continue;

			if (node1.getSpace() != space || node2.getSpace() != space)
				continue;

			Vector pos1 = node1.getPosition();
			Vector pos2 = node2.getPosition();

			Vector4d color = link.getColor();

			if (pos1 == null || pos2 == null || color == null)
				continue;

			double x1 = pos1.x, y1 = pos1.y;
			double x2 = pos2.x, y2 = pos2.y;

			float width = (float) link.getWidth();
			g.setStroke(new BasicStroke(width));
			g.setColor(color.toAWTColor());

			Shape s = new Line2D.Double(x1, y1, x2, y2);
			g.draw(s);
		}

	}

	/**
	 * Renders all visible space agents of the given type (style)
	 * 
	 * @param g
	 * @param agentStyle
	 */
	protected void renderAgents(Graphics2D g, AgentStyle agentStyle) {
		Agent[] agents;

		if (!agentStyle.visible)
			return;
		
		if (SpaceLink.class.isAssignableFrom(agentStyle.agentType)) {
			renderLinks(g, Observer.getDefaultSpace(), agentStyle);
			return;
		}

		agents = Observer.getInstance().getAgents(agentStyle.agentType);

		if (agents == null)
			return;

		if (agentStyle.transparent) {
			// gl.glEnable(GL.GL_BLEND);
		}

		int n = agents.length;

		g.setStroke(new BasicStroke(0));

		for (int i = 0; i < n; i++) {
			if (agents[i] instanceof SpaceAgent) {
				SpaceAgent agent = (SpaceAgent) agents[i];
				SpaceNode node = agent.getNode();
				// TODO: when space agent is created
				// it should be added into observer's database only
				// after node for this agent is created
				if (node == null)
					continue;
				Vector pos = node.getPosition();
				Vector4d color = agent.getColor();

				if (pos == null || color == null)
					continue;

				double r = agent.getRelativeSize();
				double r2 = r * 2.0;
				double x = pos.x;
				double y = pos.y;

				g.setColor(color.toAWTColor());

				Shape s = null;

				switch (agent.getType()) {
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
	}

	protected void renderAgents(Graphics2D g, Space space, AgentStyle agentStyle) {
		Agent[] agents;

		if (!agentStyle.visible)
			return;

		if (SpaceLink.class.isAssignableFrom(agentStyle.agentType)) {
			renderLinks(g, space, agentStyle);
			return;
		}

		agents = Observer.getInstance().getAgents(agentStyle.agentType);

		if (agents == null)
			return;

		if (agentStyle.transparent) {
			// gl.glEnable(GL.GL_BLEND);
		}

		int n = agents.length;

		g.setStroke(new BasicStroke(0));

		for (int i = 0; i < n; i++) {
			if (agents[i] instanceof SpaceAgent) {
				SpaceAgent agent = (SpaceAgent) agents[i];
				SpaceNode node = agent.getNode();
				// TODO: when space agent is created
				// it should be added into observer's database only
				// after node for this agent is created
				if (node == null)
					continue;

				if (node.getSpace() != space)
					continue;

				Vector pos = node.getPosition();
				Vector4d color = agent.getColor();

				if (pos == null || color == null)
					continue;

				double r = agent.getRelativeSize();
				double r2 = r * 2.0;
				double x = pos.x;
				double y = pos.y;

				g.setColor(color.toAWTColor());

				Shape s = null;

				switch (agent.getType()) {
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
	}

	public void display(Graphics2D g) {
		g.transform(transform);

		g.translate(dx, dy);
		g.scale(zoom, zoom);

		if (selectedSpace == null)
			return;

		if (selectedSpace.swapXY) {
			g.rotate(-Math.PI / 2);
		}

		Space space = Observer.getSpace(selectedSpace.name);
		if (space == null)
			return;

		synchronized (this) {
			if (selectedDataLayer != null) {
				DataLayer gridData = space.getDataLayer(selectedDataLayer.name);

				// TODO: render other types of data layers
				if (gridData != null
						&& (gridData instanceof DataLayerWithColors)) {
					DataLayerWithColors grid = (DataLayerWithColors) gridData;

					if (space instanceof BoundedSpace)
						renderDataLayer(g, grid, (BoundedSpace) space);
				}
			}

		}

		int n = Observer.getInstance().getSpaceNames().length;

		for (int k = agentStyles.size() - 1; k >= 0; k--) {
			AgentStyle agentStyle = agentStyles.get(k);

			if (n > 1)
				renderAgents(g, space, agentStyle);
			else
				renderAgents(g, agentStyle);

		}

	}

}
