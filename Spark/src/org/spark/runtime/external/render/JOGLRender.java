package org.spark.runtime.external.render;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLJPanel;
import javax.media.opengl.GLPbuffer;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import org.spark.runtime.data.DataObject_Grid;
import org.spark.runtime.data.DataObject_SpaceAgents;
import org.spark.runtime.data.DataObject_SpaceLinks;
import org.spark.runtime.data.DataObject_Spaces;
import org.spark.runtime.data.DataRow;
import org.spark.space.SpaceAgent;
import org.spark.utils.Vector;
import org.spark.utils.Vector4d;

import com.spinn3r.log5j.Logger;
import com.sun.opengl.util.Screenshot;


/**
 * JOGL renderer
 * @author Alexey
 *
 */
public class JOGLRender extends Render implements GLEventListener,
		MouseListener, MouseMotionListener, MouseWheelListener {
	/* Logger */
	private static final Logger logger = Logger.getLogger();

	private float view_rotx = 20.0f, view_roty = 30.0f;// , view_rotz = 0.0f;
	private float wheel_scale = 1.0f;
//	private float mouse_x = 0.0f, mouse_y = 0.0f;
	private boolean rightButtonPressed = false;

	private int prevMouseX, prevMouseY;

	// used for refreshing the frame
	private GLCanvas canvas;

	// shapes
	private int circle, circle2;
	private int square, square2;
	private int torus, torus2;

	private GLU glu = new GLU();
	private GLUquadric ball, cube;
	
	/* Current data */
	private DataRow data;
	
	/* If true then a 2d Z-slice is rendered */
	private boolean slicedMode = false;
	private float zPlane = 0;
	
	/* Information about current space bounds */
	private float xMin, yMin, xMax, yMax, zMin, zMax;
	
	
	/* Pbuffer for taking screenshots */
	private GLPbuffer pbuffer;
	private int pbufferWidth, pbufferHeight;


	/**
	 * Default constructor
	 * @param interval
	 * @throws Exception
	 */
	public JOGLRender(int interval) throws Exception {
		super(interval);
		GLCanvas glcanvas = new GLCanvas();
		if (glcanvas == null) {
			throw new Exception("Problems during OpenGL initialization");
		}
		
		glcanvas.addGLEventListener(this);
		this.canvas = glcanvas;
	}

	
	/**
	 * Returns renderer's canvas
	 */
	@Override
	public Canvas getCanvas() {
		return canvas;
	}

	
	/**
	 * Main display method
	 */
	@Override
	protected void display(DataRow row) {
		this.data = row;
		
		if (canvas != null)
			canvas.display();
	}


	/**
	 * Saves a screenshot into a file
	 */
	@Override
	protected void saveSnapshot(File dir, String fname, DataRow data) {
		GLDrawableFactory factory = GLDrawableFactory.getFactory();
		
		if (factory == null || !factory.canCreateGLPbuffer())
		{
			logger.error("Cannot create a pbuffer for taking a screenshot");
			return;
		}
		
		int w = 500;
		int h = 500;
		
		if (canvas != null) {
			w = canvas.getWidth();
			h = canvas.getHeight();
		}
		
		if (w <= 0 || h <= 0) {
			logger.error("Canvas width or height is invalid");
			return;
		}

		// Create a new pbuffer if something has been changed
		if (pbuffer == null || w != pbufferWidth || h != pbufferHeight) {
			if (pbuffer != null) {
				pbuffer.destroy();
			}

			// Create a pbuffer
			GLCapabilities glCap = new GLCapabilities();
			pbuffer = factory.createGLPbuffer(glCap, null, w, h, null);
			if (pbuffer == null) {
				logger.error("Cannot create a pbuffer");
				return;
			}

			pbuffer.addGLEventListener(this);
			pbufferWidth = w;
			pbufferHeight = h;
		}
		
		try {
			// Render the data
			DataRow currentData = this.data;
			this.data = data; 
			pbuffer.display();
			this.data = currentData;

			// Save the buffer content into a file
			GLContext context = pbuffer.createContext(null);
			
			context.makeCurrent();
			BufferedImage img = Screenshot.readToBufferedImage(w, h);
			context.release();
			
			context.destroy();

			File out = new File(dir, fname + ".png");
			ImageIO.write(img, "png", out);
		}
		catch (Exception e) {
			logger.error(e);
		}
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
		drawable.addMouseWheelListener(this);

		// Disable the depth test by default (for 2d-case mostly)
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

	
	/**
	 * Reshape event
	 */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		GL gl = drawable.getGL();
		reshape(gl);
	}
	

	/**
	 * Reshape method
	 * @param gl
	 */
	public void reshape(GL gl) {
		reshapeRequested = false;
		
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();

		boolean swapXY = (selectedSpace != null) ? selectedSpace.swapXY : false;
	
		if (data != null && selectedSpace != null) {
			DataObject_Spaces spaces = data.getSpaces();
			if (spaces != null) {
				int index = spaces.getIndex(selectedSpace.name);
				if (index != -1) {
					Vector min = spaces.getMins()[index];
					Vector max = spaces.getMaxs()[index];
					
					xMin = (float) min.x;
					yMin = (float) min.y;
					zMin = (float) min.z;
					
					xMax = (float) max.x;
					yMax = (float) max.y;
					zMax = (float) max.z;
				}
			}
		}
		
		if (xMin >= xMax - 1 || yMin >= yMax - 1) {
			xMin = yMin = -60;
			xMax = yMax = 60;
		}

		float x0 = xMin;
		float x1 = xMax;
		float y0 = yMin;
		float y1 = yMax;
		
		if (swapXY) {
			x0 = yMin;
			x1 = yMax;
			y0 = xMin;
			y1 = xMax;
		}

		if (zMin >= zMax - 1)
		{
			zMin = -100;
			zMax = 100;
		}
		
		gl.glOrtho(x0, x1, y0, y1, zMin - 10, zMax + 10);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
	}


	/**
	 * Renders the given data layer
	 * @param gl
	 * @param grid
	 * @param spaceIndex
	 */
	protected void renderDataLayer(GL gl, DataObject_Grid grid, int spaceIndex) {
		if (grid == null)
			return;
		
		// Deal with a 3d-case
		if (grid.getZSize() > 0) {
			try {
				grid = new DataGridZSlice(grid, zPlane, zMin, zMax);
			}
			catch (Exception e) {
				logger.error(e);
				return;
			}

			if (!slicedMode) {
				gl.glPushMatrix();
				gl.glTranslatef(0, 0, zPlane);
			}
		}

		if (selectedDataLayer.getGeometry() == null)
			selectedDataLayer.setGeometry(GridGraphics.getGeometry(grid, xMin, yMin));

		Vector[][] gridGeometry = selectedDataLayer.getGeometry();

		int n = gridGeometry.length - 1;
		int m = gridGeometry[0].length - 1;

		Vector[][] colors = GridGraphics.getColors(grid, selectedDataLayer);

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
		y = yMax;

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

		x = xMax;
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

		
		if (grid.getZSize() > 0 && !slicedMode) {
			gl.glPopMatrix();
		}
	}
	
	
	/**
	 * Renders all visible space links of the given type (style)
	 * @param gl
	 * @param linkStyle
	 */
	protected void renderLinks(GL gl, DataObject_SpaceLinks links, int spaceIndex, AgentStyle linkStyle) {
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

		gl.glBegin(GL.GL_LINES);
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
			gl.glLineWidth(w);
			gl.glColor3d(color.x, color.y, color.z);

			gl.glVertex2d(x1, y1);
			gl.glVertex2d(x2, y2);
		}
		gl.glEnd();
	}
	
	
	/**
	 * Renders agents
	 * @param gl
	 * @param agents
	 * @param spaceIndex
	 * @param agentStyle
	 */
	protected void renderAgents(GL gl, DataObject_SpaceAgents agents,
			int spaceIndex, AgentStyle agentStyle) {
		if (!agentStyle.visible)
			return;

		if (agents == null)
			return;

		int n = agents.getTotalNumber();
		Vector[] positions = agents.getPositions();
		double[] radii = agents.getRadii();
		Vector4d[] colors = agents.getColors();
		int[] shapes = agents.getShapes();
		int[] spaceIndices = agents.getSpaceIndices();

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

		// Render lists
		int circle = agentStyle.border ? this.circle : this.circle2;
		int square = agentStyle.border ? this.square : this.square2;
		int donnut = agentStyle.border ? this.torus : this.torus2;

		/* Iterate through all agents */
		for (int i = 0; i < n; i++) {
			if (spaceIndices[i] != spaceIndex)
				continue;

			Vector pos = positions[i];
			Vector4d color = colors[i];

			if (pos == null || color == null)
				continue;

			gl.glPushMatrix();
			gl.glTranslated(pos.x, pos.y, 0);
			float scale = (float) radii[i];
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
				switch (shapes[i]) {
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
			gl.glPopMatrix();
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

	
	/**
	 * Displays agents in 3d
	 * @param gl
	 * @param agents
	 * @param agentStyle
	 * @param spaceIndex
	 */
	protected void renderAgents3d(GL gl, DataObject_SpaceAgents agents, AgentStyle agentStyle,
			int spaceIndex) {
		if (!agentStyle.visible)
			return;
		
		if (agents == null)
			return;
		
		// Get data of agents
		int n = agents.getTotalNumber();
		Vector[] positions = agents.getPositions();
		double[] radii = agents.getRadii();
		Vector4d[] colors = agents.getColors();
		int[] shapes = agents.getShapes();
		int[] spaceIndices = agents.getSpaceIndices();

		// Render agents
		for (int i = 0; i < n; i++) {
			if (spaceIndices[i] != spaceIndex)
				continue;

			Vector pos = positions[i];
			Vector4d color = colors[i];

			if (pos == null || color == null)
				continue;

			// Save world matrix
			gl.glPushMatrix();
			gl.glTranslated(pos.x, pos.y, pos.z);
			
			float scale = (float) radii[i];
			gl.glScalef(scale, scale, scale);
			
			if (agentStyle.transparent)
				gl.glColor4d(color.x, color.y, color.z, 0.5);
			else
				gl.glColor3d(color.x, color.y, color.z);
			
			switch (shapes[i]) {
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

			// Restore world matrix
			gl.glPopMatrix();
		}
	}
	
	
	
	/**
	 * Displays 3d agents on a 2d slice
	 * @param gl
	 * @param agents
	 * @param agentStyle
	 * @param spaceIndex
	 */
	protected void renderAgents3dSliced(GL gl, DataObject_SpaceAgents agents, AgentStyle agentStyle,
			int spaceIndex) {
		if (!agentStyle.visible)
			return;

		if (agents == null)
			return;

		int n = agents.getTotalNumber();
		Vector[] positions = agents.getPositions();
		double[] radii = agents.getRadii();
		Vector4d[] colors = agents.getColors();
		int[] shapes = agents.getShapes();
		int[] spaceIndices = agents.getSpaceIndices();

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

		// Render lists
		int circle = agentStyle.border ? this.circle : this.circle2;
		int square = agentStyle.border ? this.square : this.square2;
		int donnut = agentStyle.border ? this.torus : this.torus2;

		/* Iterate through all agents */
		for (int i = 0; i < n; i++) {
			if (spaceIndices[i] != spaceIndex)
				continue;

			Vector pos = positions[i];
			Vector4d color = colors[i];

			if (pos == null || color == null)
				continue;
			
			// Compute the distance between the agent and the plane
			double dist = Math.abs(pos.z - zPlane);
			double r = radii[i];
			if (dist >= r - 1e-6)
				continue;

			gl.glPushMatrix();
			gl.glTranslated(pos.x, pos.y, 0);
			float scale = (float) Math.sqrt(r * r - dist * dist);
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
				switch (shapes[i]) {
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
			gl.glPopMatrix();
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
	

	/**
	 * Main display method
	 */
	public void display(GLAutoDrawable drawable) {
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

		GL gl = drawable.getGL();
		
//		if (reshapeRequested) {
//			reshape(gl);
//		}
		
		// TODO: call it when necessary only
		reshape(gl);
		
		boolean space3d = false;
		if (spaces.getMins()[spaceIndex].z < spaces.getMaxs()[spaceIndex].z)
			space3d = true;

		
		// Clear drawing buffer
		if ((drawable instanceof GLJPanel)
				&& !((GLJPanel) drawable).isOpaque()
				&& ((GLJPanel) drawable)
						.shouldPreserveColorBufferIfTranslucent()) {
			gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		} else {
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		}


		// Save world matrix 
		gl.glPushMatrix();

		// gl.glTranslatef(mouse_x, mouse_y, 0);
		// gl.glScalef(wheel_scale, wheel_scale, wheel_scale);

		// Make basic space transformations
		gl.glTranslatef(dx, dy, 0);
		gl.glScalef(zoom, zoom, zoom);
		
		if (selectedSpace.swapXY) {
			gl.glRotatef(-90, 0, 0, 1);
		}

		if (space3d && !slicedMode) {
			gl.glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
			gl.glRotatef(view_roty, 0.0f, 1.0f, 0.0f);
			gl.glScalef(wheel_scale, wheel_scale, wheel_scale);
			gl.glEnable(GL.GL_DEPTH_TEST);
		}

		// Render selected data layer
		if (selectedDataLayer != null) {
			DataObject_Grid gridData = data.getGrid(selectedDataLayer.getName());
			renderDataLayer(gl, gridData, spaceIndex);
		}


		if (space3d && !slicedMode) {
			gl.glEnable(GL.GL_COLOR_MATERIAL);
			gl.glEnable(GL.GL_LIGHT0); // Enable Light 0
			gl.glEnable(GL.GL_LIGHTING); // Enable Lighting
		}
		
		
		// Render visible agents
		for (int k = agentStyles.size() - 1; k >= 0; k--) {
			AgentStyle agentStyle = agentStyles.get(k);
			
			if (!agentStyle.visible)
				continue;
			
			DataObject_SpaceAgents agentsData = data
					.getSpaceAgents(agentStyle.typeName);
			
			if (agentsData == null)
				continue;
			
			if (agentsData instanceof DataObject_SpaceLinks)
				renderLinks(gl, (DataObject_SpaceLinks) agentsData, spaceIndex, agentStyle);
			else {
				if (!space3d) {
					renderAgents(gl, agentsData, spaceIndex, agentStyle);
				}
				else if (slicedMode) {
					renderAgents3dSliced(gl, agentsData, agentStyle, spaceIndex);
				}
				else {
					renderAgents3d(gl, agentsData, agentStyle, spaceIndex);
				}
			}
		}

		if (space3d && !slicedMode) {
			gl.glDisable(GL.GL_DEPTH_TEST);
			gl.glDisable(GL.GL_COLOR_MATERIAL);
			gl.glDisable(GL.GL_LIGHT0); // Disable Light 0
			gl.glDisable(GL.GL_LIGHTING); // Disable Lighting
		}

		// Restore world matrix
		gl.glPopMatrix();
	}
	



	/************** Event listeners *******************/
	

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
			rightButtonPressed = true;
			// mouseRButtonDown = true;
		}
	}
	

	public void mouseReleased(MouseEvent e) {
		if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
			rightButtonPressed = false;
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

		if (!rightButtonPressed) {
			view_rotx += thetaX;
			view_roty += thetaY;
		}
		else {
//			mouse_y -= thetaX;
//			mouse_x -= thetaY;
			zPlane += thetaY;
			
			if (zPlane < zMin)
				zPlane = zMin;
			else if (zPlane > zMax)
				zPlane = zMax;
		}

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
	
	
	/**
	 * Key event action
	 */
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		boolean flag = false;

		switch (code) {
		case KeyEvent.VK_SPACE:
			slicedMode = !slicedMode;
			flag = true;
			break;
			
		default:
			super.keyPressed(e);	
			return;
		}
		
		if (flag)
			canvas.display();
	}
}
