package org.spark.gui.render;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import org.spark.core.Agent;
import org.spark.core.Observer;
import org.spark.data.DataLayer;
import org.spark.gui.GUIModelManager;
import org.spark.space.BoundedSpace;
import org.spark.space.BoundedSpace3d;
import org.spark.space.Space;
import org.spark.space.SpaceAgent;
import org.spark.space.SpaceLink;
import org.spark.space.SpaceNode;
import org.spark.utils.Vector;
import org.spark.utils.Vector4d;

import com.spinn3r.log5j.Logger;

public class JOGLRender extends Render implements GLEventListener,
		MouseListener, MouseMotionListener, MouseWheelListener {
	private static final Logger logger = Logger.getLogger();

	// TODO: unused
	private float view_rotx = 20.0f, view_roty = 30.0f;// , view_rotz = 0.0f;
	private float wheel_scale = 1.0f;
	private float mouse_x = 0.0f, mouse_y = 0.0f;

	private int prevMouseX, prevMouseY;

	// used for refreshing the frame
	private GLCanvas canvas;

	// shapes
	private int circle, circle2;
	private int square, square2;
	private int torus, torus2;

	private GLU glu = new GLU();
	private GLUquadric ball, cube;
	
	private boolean takeScreenshot = false;

	// public void setActivity(boolean flag) {
	// active = flag;
	// }

	public JOGLRender(GLCanvas canvas) throws Exception {
		if (canvas == null)
			throw new Exception("GLCanvas cannot be null");

		this.canvas = canvas;
		if (canvas != null)
			canvas.addGLEventListener(this);
	}

	public JOGLRender() throws Exception {
		GLCanvas glcanvas = new GLCanvas();
		if (glcanvas == null) {
			throw new Exception("Problems during OpenGL initialization");
		}
		glcanvas.addGLEventListener(this);
		this.canvas = glcanvas;

		// this.canvas = new JOGLCanvas(glcanvas);
	}

	@Override
	public Canvas getCanvas() {
		return canvas;
	}

	@Override
	public void display() {
		if (canvas != null)
			canvas.display();
	}

	/*
	 * public void resize() { if (canvas == null) return;
	 * canvas.setSize(canvas.getSize()); canvas.display(); }
	 */

	public void init(GLAutoDrawable drawable) {
		logger.info("Initializing JOGLRender");

		// Use debug pipeline
		// drawable.setGL(new DebugGL(drawable.getGL()));

		GL gl = drawable.getGL();

		gl.setSwapInterval(1);
		// gl.glEnable(GL.GL_POINT_SMOOTH);
		gl.glClearColor(1, 1, 1, 1);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
		// gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);

		/* Set up light */

		float lightAmbient[] = { 0.2f, 0.2f, 0.2f }; // Ambient Light is 20%
		// white
		float lightDiffuse[] = { 1.0f, 1.0f, 1.0f }; // Diffuse Light is white

		// Position is somewhat in front of screen
		float lightPosition[] = { 0.0f, 0.0f, 0.0f };

		// Set The Ambient Lighting For Light0
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, lightAmbient, 0);

		// Set The Diffuse Lighting For Light0
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, lightDiffuse, 0);

		// Set The Position For Light0
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, lightPosition, 0);

		drawable.addMouseListener(this);
		drawable.addMouseMotionListener(this);
		// drawable.addMouseWheelListener(this);

		gl.glDisable(GL.GL_DEPTH_TEST);

		int n = 10;
		circle = gl.glGenLists(1);
		gl.glNewList(circle, GL.GL_COMPILE);
		gl.glBegin(GL.GL_TRIANGLE_FAN);
		gl.glVertex2d(0, 0);
		for (int i = 0; i <= n; i++) {
			double angle = 2 * Math.PI / n * i;
			gl.glVertex2d(Math.cos(angle), Math.sin(angle));
		}
		gl.glEnd();
		gl.glColor3f(0, 0, 0);
		gl.glBegin(GL.GL_LINE_LOOP);
		for (int i = 0; i <= n; i++) {
			double angle = 2 * Math.PI / n * i;
			gl.glVertex2d(Math.cos(angle), Math.sin(angle));
		}
		gl.glEnd();
		gl.glEndList();

		torus = gl.glGenLists(1);
		gl.glNewList(torus, GL.GL_COMPILE);
		gl.glBegin(GL.GL_QUADS);
		for (int i = 0; i < n; i++) {
			double r1 = 0.5, r2 = 1.0;
			double angle1 = 2 * Math.PI / n * i;
			double angle2 = 2 * Math.PI / n * (i + 1);
			double c1 = Math.cos(angle1), s1 = Math.sin(angle1);
			double c2 = Math.cos(angle2), s2 = Math.sin(angle2);

			gl.glVertex2d(c1 * r1, s1 * r1);
			gl.glVertex2d(c1 * r2, s1 * r2);
			gl.glVertex2d(c2 * r2, s2 * r2);
			gl.glVertex2d(c2 * r1, s2 * r1);
		}
		gl.glEnd();

		gl.glColor3f(0, 0, 0);
		gl.glBegin(GL.GL_LINE_LOOP);
		for (int i = 0; i <= n; i++) {
			double angle = 2 * Math.PI / n * i;
			gl.glVertex2d(Math.cos(angle), Math.sin(angle));
		}
		gl.glEnd();
		gl.glBegin(GL.GL_LINE_LOOP);
		for (int i = 0; i <= n; i++) {
			double angle = 2 * Math.PI / n * i;
			gl.glVertex2d(0.5 * Math.cos(angle), 0.5 * Math.sin(angle));
		}
		gl.glEnd();
		gl.glEndList();

		square = gl.glGenLists(1);
		gl.glNewList(square, GL.GL_COMPILE);
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex2f(-1f, -1f);
		gl.glVertex2f(-1f, 1f);
		gl.glVertex2f(1f, 1f);
		gl.glVertex2f(1f, -1f);
		gl.glEnd();
		gl.glColor3f(0, 0, 0);
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex2f(-1f, -1f);
		gl.glVertex2f(-1f, 1f);
		gl.glVertex2f(1f, 1f);
		gl.glVertex2f(1f, -1f);

		gl.glEnd();
		gl.glEndList();

		torus2 = gl.glGenLists(1);
		gl.glNewList(torus2, GL.GL_COMPILE);
		gl.glBegin(GL.GL_QUADS);
		for (int i = 0; i < n; i++) {
			double r1 = 0.5, r2 = 1.0;
			double angle1 = 2 * Math.PI / n * i;
			double angle2 = 2 * Math.PI / n * (i + 1);
			double c1 = Math.cos(angle1), s1 = Math.sin(angle1);
			double c2 = Math.cos(angle2), s2 = Math.sin(angle2);

			gl.glVertex2d(c1 * r1, s1 * r1);
			gl.glVertex2d(c1 * r2, s1 * r2);
			gl.glVertex2d(c2 * r2, s2 * r2);
			gl.glVertex2d(c2 * r1, s2 * r1);
		}
		gl.glEnd();
		gl.glEndList();

		circle2 = gl.glGenLists(1);
		gl.glNewList(circle2, GL.GL_COMPILE);
		gl.glBegin(GL.GL_TRIANGLE_FAN);
		gl.glVertex2d(0, 0);
		for (int i = 0; i <= n; i++) {
			double angle = 2 * Math.PI / n * i;
			gl.glVertex2d(Math.cos(angle), Math.sin(angle));
		}
		gl.glEnd();
		gl.glEndList();

		square2 = gl.glGenLists(1);
		gl.glNewList(square2, GL.GL_COMPILE);
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex2f(-1f, -1f);
		gl.glVertex2f(-1f, 1f);
		gl.glVertex2f(1f, 1f);
		gl.glVertex2f(1f, -1f);
		gl.glEnd();
		gl.glEndList();

		/* Create quadratics */
		ball = glu.gluNewQuadric(); // Create A New Quadratic
		glu.gluQuadricNormals(ball, GL.GL_SMOOTH); // Generate Smooth Normals
		// For The Quad

		cube = glu.gluNewQuadric();

	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		GL gl = drawable.getGL();
		// System.err.println(Thread.currentThread().toString());
		// System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
		// System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
		// System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));

		reshape(gl);
	}
	

	public void reshape(GL gl) {
		reshapeRequested = false;
		
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();

		boolean swapXY = (selectedSpace != null) ? selectedSpace.swapXY : false;
		
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

		if (swapXY) {
			float t = xMin;
			xMin = yMin;
			yMin = t;
			
			t = xMax;
			xMax = yMax;
			yMax = t;
		}
		
		gl.glOrtho(xMin, xMax, yMin, yMax, -100, 100);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	private String fname;

	@Override
	public void takeSnapshot(String fname) {
		this.fname = fname;
		takeScreenshot = true;
		canvas.display();
	}

	protected void renderDataLayerOld(GL gl, DataLayerWithColors grid,
			BoundedSpace space) {
		if (grid == null || space == null)
			return;

		if (selectedDataLayer.gridGeometry == null)
			selectedDataLayer.gridGeometry = grid.getGeometry();

		Vector[][] gridGeometry = selectedDataLayer.gridGeometry;

		int n = gridGeometry.length - 1;
		int m = gridGeometry[0].length - 1;

		Vector[][] colors = grid.getColors(selectedDataLayer.val1,
				selectedDataLayer.val2, selectedDataLayer.color1,
				selectedDataLayer.color2);

		for (int i = 0; i < n; i++) {
			gl.glBegin(GL.GL_QUAD_STRIP);
			Vector v0 = gridGeometry[i][0];
			Vector v1 = gridGeometry[i + 1][0];
			Vector c0 = colors[i][0];
			Vector c1 = colors[i + 1][0];
			gl.glColor3d(c0.x, c0.y, c0.z);
			gl.glVertex2d(v0.x, v0.y);
			gl.glColor3d(c1.x, c1.y, c1.z);
			gl.glVertex2d(v1.x, v1.y);

			for (int j = 1; j <= m; j++) {
				Vector v3 = gridGeometry[i][j];
				Vector v4 = gridGeometry[i + 1][j];
				Vector c3 = colors[i][j];
				Vector c4 = colors[i + 1][j];

				gl.glColor3d(c3.x, c3.y, c3.z);
				gl.glVertex2d(v3.x, v3.y);
				gl.glColor3d(c4.x, c4.y, c4.z);
				gl.glVertex2d(v4.x, v4.y);

			}
			gl.glEnd();
		}
	}

	protected void renderDataLayer(GL gl, DataLayerWithColors grid,
			BoundedSpace space) {
		if (grid == null || space == null)
			return;

		if (selectedDataLayer.gridGeometry == null)
			selectedDataLayer.gridGeometry = grid.getGeometry2();

		Vector[][] gridGeometry = selectedDataLayer.gridGeometry;

		int n = gridGeometry.length - 1;
		int m = gridGeometry[0].length - 1;

		Vector[][] colors = grid.getColors(selectedDataLayer.val1,
				selectedDataLayer.val2, selectedDataLayer.color1,
				selectedDataLayer.color2);

		// if (grid instanceof QueueGrid) {
		// gl.glRotatef(-90, 0, 0, 1);
		// }

		// Render center
		for (int i = 0; i < n; i++) {
			gl.glBegin(GL.GL_QUAD_STRIP);
			Vector v0 = gridGeometry[i][0];
			Vector v1 = gridGeometry[i + 1][0];
			Vector c0 = colors[i][0];
			Vector c1 = colors[i + 1][0];
			gl.glColor3d(c0.x, c0.y, c0.z);
			gl.glVertex2d(v0.x, v0.y);
			gl.glColor3d(c1.x, c1.y, c1.z);
			gl.glVertex2d(v1.x, v1.y);

			for (int j = 1; j <= m; j++) {
				Vector v3 = gridGeometry[i][j];
				Vector v4 = gridGeometry[i + 1][j];
				Vector c3 = colors[i][j];
				Vector c4 = colors[i + 1][j];

				gl.glColor3d(c3.x, c3.y, c3.z);
				gl.glVertex2d(v3.x, v3.y);
				gl.glColor3d(c4.x, c4.y, c4.z);
				gl.glVertex2d(v4.x, v4.y);

			}
			gl.glEnd();
		}

		// Render borders
		double xStep = grid.getXStep();
		double yStep = grid.getYStep();

		double xMin = space.getXMin();
		double yMin = space.getYMin();

		double x, y;

		// Render the bottom border
		gl.glBegin(GL.GL_TRIANGLE_STRIP);

		x = xMin;
		y = yMin;

		Vector c0;
		Vector c1 = colors[0][0];

		for (int i = 0; i <= n; i++, x += xStep) {
			c0 = c1;
			c1 = colors[i][0];
			Vector v0 = gridGeometry[i][0];

			gl.glColor3d((c0.x + c1.x) / 2, (c0.y + c1.y) / 2,
					(c0.z + c1.z) / 2);
			gl.glVertex2d(x, y);

			gl.glColor3d(c1.x, c1.y, c1.z);
			gl.glVertex2d(v0.x, v0.y);
		}

		gl.glVertex2d(x, y);
		gl.glEnd();

		// Render the top border
		gl.glBegin(GL.GL_TRIANGLE_STRIP);

		x = xMin;
		y = space.getYMax();

		c1 = colors[0][m];

		for (int i = 0; i <= n; i++, x += xStep) {
			c0 = c1;
			c1 = colors[i][m];
			Vector v0 = gridGeometry[i][m];

			gl.glColor3d((c0.x + c1.x) / 2, (c0.y + c1.y) / 2,
					(c0.z + c1.z) / 2);
			gl.glVertex2d(x, y);

			gl.glColor3d(c1.x, c1.y, c1.z);
			gl.glVertex2d(v0.x, v0.y);
		}

		gl.glVertex2d(x, y);
		gl.glEnd();

		// Render the left border
		gl.glBegin(GL.GL_TRIANGLE_STRIP);

		x = xMin;
		y = yMin;

		c1 = colors[0][0];

		for (int j = 0; j <= m; j++, y += yStep) {
			c0 = c1;
			c1 = colors[0][j];
			Vector v0 = gridGeometry[0][j];

			gl.glColor3d((c0.x + c1.x) / 2, (c0.y + c1.y) / 2,
					(c0.z + c1.z) / 2);
			gl.glVertex2d(x, y);

			gl.glColor3d(c1.x, c1.y, c1.z);
			gl.glVertex2d(v0.x, v0.y);
		}

		gl.glVertex2d(x, y);
		gl.glEnd();

		// Render the right border
		gl.glBegin(GL.GL_TRIANGLE_STRIP);

		x = space.getXMax();
		y = yMin;

		c1 = colors[n][0];

		for (int j = 0; j <= m; j++, y += yStep) {
			c0 = c1;
			c1 = colors[n][j];
			Vector v0 = gridGeometry[n][j];

			gl.glColor3d((c0.x + c1.x) / 2, (c0.y + c1.y) / 2,
					(c0.z + c1.z) / 2);
			gl.glVertex2d(x, y);

			gl.glColor3d(c1.x, c1.y, c1.z);
			gl.glVertex2d(v0.x, v0.y);
		}

		gl.glVertex2d(x, y);
		gl.glEnd();
	}
	
	
	/**
	 * Renders all visible space links of the given type (style)
	 * 
	 * @param g
	 * @param linkStyle
	 */
	protected void renderLinks(GL gl, Space space, AgentStyle linkStyle) {
		Agent[] links;

		if (!linkStyle.visible)
			return;

		if (!SpaceLink.class.isAssignableFrom(linkStyle.agentType))
			return;

		links = Observer.getInstance().getAgents(linkStyle.agentType);

		if (links == null)
			return;

		int n = links.length;

		gl.glBegin(GL.GL_LINES);
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
			gl.glLineWidth(width);
			gl.glColor3d(color.x, color.y, color.z);

			gl.glVertex2d(x1, y1);
			gl.glVertex2d(x2, y2);
		}
		gl.glEnd();

	}
	

	public void display(GLAutoDrawable drawable) {
		if (takeScreenshot) {

			try {
				com.sun.opengl.util.Screenshot.writeToFile(
						new File(GUIModelManager.getInstance().getXmlDocumentFile().getParentFile(),
								fname + ".png"),
				// new File("out.png"),
						canvas.getWidth(), canvas.getHeight());
			} catch (GLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			takeScreenshot = false;
			return;
		}

		GL gl = drawable.getGL();
		if (reshapeRequested) {
			reshape(gl);
		}
		
		if ((drawable instanceof GLJPanel)
				&& !((GLJPanel) drawable).isOpaque()
				&& ((GLJPanel) drawable)
						.shouldPreserveColorBufferIfTranslucent()) {
			gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		} else {
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		}

		if (selectedSpace == null)
			return;

		Space space = Observer.getSpace(selectedSpace.name);
		if (space == null)
			return;

		gl.glPushMatrix();
		// gl.glTranslatef(mouse_x, mouse_y, 0);
		// gl.glScalef(wheel_scale, wheel_scale, wheel_scale);

		gl.glTranslatef(dx, dy, 0);
		gl.glScalef(zoom, zoom, zoom);
		
		if (selectedSpace.swapXY) {
			gl.glRotatef(-90, 0, 0, 1);
		}

		if (space instanceof BoundedSpace3d) {
			gl.glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
			gl.glRotatef(view_roty, 0.0f, 1.0f, 0.0f);

			gl.glEnable(GL.GL_COLOR_MATERIAL);

			gl.glEnable(GL.GL_LIGHT0); // Enable Light 0
			gl.glEnable(GL.GL_LIGHTING); // Enable Lighting
		}

		synchronized (this) {

			// TODO: render other types of data layers

			if (selectedDataLayer != null) {
				DataLayer gridData = space.getDataLayer(selectedDataLayer.name);

				if (gridData != null
						&& (gridData instanceof DataLayerWithColors)) {
					DataLayerWithColors grid = (DataLayerWithColors) gridData;

					if (space instanceof BoundedSpace)
						renderDataLayer(gl, grid, (BoundedSpace) space);
				}

			}

		}

		for (int k = agentStyles.size() - 1; k >= 0; k--) {
			Agent[] agents;
			AgentStyle agentStyle = agentStyles.get(k);

			if (!agentStyle.visible)
				continue;
			
			if (SpaceLink.class.isAssignableFrom(agentStyle.agentType)) {
				renderLinks(gl, Observer.getDefaultSpace(), agentStyle);
				continue;
			}

			agents = Observer.getInstance().getAgents(agentStyle.agentType);
			if (agents == null)
				continue;

			if (space instanceof BoundedSpace3d) {
				displayAgents3d(gl, agents, agentStyle, space);
				continue;
			}

			/* Transparent agents */
			if (agentStyle.transparent) {
				gl.glEnable(GL.GL_BLEND);
				gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			}

			/* Textured agents */
			if (agentStyle.getTexture() != null) {
				int blendSrc = agentStyle.getSrcBlend();
				int blendDst = agentStyle.getDstBlend();

				if (blendSrc >= 0 && blendDst >= 0) {
					gl.glEnable(GL.GL_BLEND);
					// gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
					gl.glBlendFunc(blendSrc, blendDst);
				}

				int alphaFunc = agentStyle.getAlphaFunc();

				if (alphaFunc >= 0) {
					gl.glEnable(GL.GL_ALPHA_TEST);
					gl.glAlphaFunc(alphaFunc, agentStyle.alphaFuncValue);
				}

				gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE,
						agentStyle.getTextureEnv());

				// enable texturing
				gl.glEnable(GL.GL_TEXTURE_2D);
				agentStyle.getTexture().bind();
			}

			int n = agents.length;
			int circle = agentStyle.border ? this.circle : this.circle2;
			int square = agentStyle.border ? this.square : this.square2;
			int donnut = agentStyle.border ? this.torus : this.torus2;

			/* Iterate through all agents */
			for (int i = 0; i < n; i++) {
				/* Only space agents can be rendered */
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

					gl.glPushMatrix();
					gl.glTranslated(pos.x, pos.y, 0);
					float scale = (float) agent.getRelativeSize();
					gl.glScalef(scale, scale, scale);

					if (agentStyle.getTexture() != null) {
						gl.glColor4d(color.x, color.y, color.z, color.a);
						/* Render a textured agent */
						gl.glBegin(GL.GL_QUADS);
						gl.glTexCoord2f(0.0f, 0.0f);
						gl.glVertex2f(-1, -1);
						gl.glTexCoord2f(1.0f, 0.0f);
						gl.glVertex2f(1, -1);
						gl.glTexCoord2f(1.0f, 1.0f);
						gl.glVertex2f(1, 1);
						gl.glTexCoord2f(0.0f, 1.0f);
						gl.glVertex2f(-1, 1);
						gl.glEnd();
					} else {
						/* Usual rendering */
						if (agentStyle.transparent)
							gl.glColor4d(color.x, color.y, color.z, 0.5);
						else
							gl.glColor3d(color.x, color.y, color.z);
						switch (agent.getType()) {
						case SpaceAgent.CIRCLE:
							gl.glCallList(circle);
							break;
						case SpaceAgent.SQUARE:
							gl.glCallList(square);
							break;
						case SpaceAgent.TORUS:
							gl.glCallList(donnut);
							break;
						}
					}
					/*
					 * if (agent.getType() == SpaceAgent.SQUARE)
					 * gl.glCallList(square); else gl.glCallList(circle);
					 */
					gl.glPopMatrix();
				}
			}

			/* Disable transparency */
			if (agentStyle.transparent) {
				gl.glDisable(GL.GL_BLEND);
			}

			/* Disable texture */
			if (agentStyle.getTexture() != null) {
				gl.glDisable(GL.GL_TEXTURE_2D);
				// switch back to modulation of quad colours and texture

				gl.glDisable(GL.GL_ALPHA_TEST); // switch off transparency
				gl.glDisable(GL.GL_BLEND);
			}

		}

		if (space instanceof BoundedSpace3d) {
			gl.glDisable(GL.GL_COLOR_MATERIAL);
			gl.glDisable(GL.GL_LIGHT0); // Enable Light 0
			gl.glDisable(GL.GL_LIGHTING); // Enable Lighting
		}

		gl.glPopMatrix();

	}

	private void displayAgents3d(GL gl, Agent[] agents, AgentStyle agentStyle,
			Space space) {
		int n = agents.length;
		// int donnut = agentStyle.border ? this.donnut : this.donnut2;

		for (int i = 0; i < n; i++) {
			if (agents[i] instanceof SpaceAgent) {
				SpaceAgent agent = (SpaceAgent) agents[i];
				SpaceNode node = agent.getNode();
				// TODO: when space agent is created
				// it should be added into observer's database only
				// after node for this agent is created
				if (node == null || node.getSpace() != space)
					continue;
				Vector pos = node.getPosition();
				Vector4d color = agent.getColor();

				if (pos == null || color == null)
					continue;

				gl.glPushMatrix();
				gl.glTranslated(pos.x, pos.y, pos.z);
				float scale = (float) agent.getRelativeSize();
				gl.glScalef(scale, scale, scale);
				if (agentStyle.transparent)
					gl.glColor4d(color.x, color.y, color.z, 0.5);
				else
					gl.glColor3d(color.x, color.y, color.z);
				switch (agent.getType()) {
				case SpaceAgent.CIRCLE:
					glu.gluSphere(ball, 1, 8, 8);
					break;
				case SpaceAgent.SQUARE:
					glu.gluSphere(cube, 1, 4, 4);
					break;
				case SpaceAgent.TORUS:
					// TODO: not supported
					// gl.glCallList(donnut);
					break;
				}
				gl.glPopMatrix();
			}
		}
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
	}

	// Methods required for the implementation of MouseListener
	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		prevMouseX = e.getX();
		prevMouseY = e.getY();
		if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
			// mouseRButtonDown = true;
		}
	}

	public void mouseReleased(MouseEvent e) {
		if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
			// mouseRButtonDown = false;
		}
	}

	public void mouseClicked(MouseEvent e) {
	}

	// Methods required for the implementation of MouseMotionListener
	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		Dimension size = e.getComponent().getSize();

		float thetaY = 360.0f * ((float) (x - prevMouseX) / (float) size.width);
		float thetaX = 360.0f * ((float) (prevMouseY - y) / (float) size.height);

		prevMouseX = x;
		prevMouseY = y;

		view_rotx += thetaX;
		view_roty += thetaY;
		mouse_y -= thetaX;
		mouse_x -= thetaY;

		// if (!active) canvas.display();
		canvas.display();
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
		wheel_scale -= notches * 0.1f;

		if (wheel_scale < 0.1f)
			wheel_scale = 0.1f;
		else if (wheel_scale > 10.0f)
			wheel_scale = 10.0f;

		// if (!active) canvas.display();
		canvas.display();
	}
}