package org.spark.runtime.external.render;

import java.awt.Canvas;
import java.awt.event.KeyEvent;
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

import org.spark.runtime.data.DataObject_AgentData;
import org.spark.runtime.data.DataObject_SpaceAgents;
import org.spark.runtime.data.DataObject_SpaceLinks;
import org.spark.runtime.data.DataObject_Spaces;
import org.spark.runtime.data.DataRow;
import org.spark.runtime.external.render.images.TileManager;
import org.spark.math.Vector;
import org.spark.math.Vector4d;

import com.spinn3r.log5j.Logger;
import com.sun.opengl.util.Screenshot;
import com.sun.opengl.util.texture.Texture;

/**
 * JOGL renderer
 * 
 * @author Alexey
 * 
 */
public class JOGLRender extends Render implements GLEventListener {
	/* Logger */
	private static final Logger logger = Logger.getLogger();

	// private float mouse_x = 0.0f, mouse_y = 0.0f;
//	private boolean rightButtonPressed = false;

//	private int prevMouseX, prevMouseY;

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
	 * 
	 * @param interval
	 * @throws Exception
	 */
	public JOGLRender(int interval) throws Exception {
		super(interval);
		GLCapabilities cap = new GLCapabilities();
		cap.setStencilBits(8);
		GLCanvas glcanvas = new GLCanvas(cap);
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

		if (factory == null || !factory.canCreateGLPbuffer()) {
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
		} catch (Exception e) {
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
		gl.glClearStencil(0);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
//		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
//		gl.glPolygonMode(GL.GL_BACK, GL.GL_LINE);

		/* Set up light */

		float lightAmbient[] = { 0.2f, 0.2f, 0.2f }; // Ambient Light is 20%
		// white
		float lightDiffuse[] = { 0.8f, 0.8f, 0.8f }; // Diffuse Light is white

		// Position is somewhat in front of screen
		float lightPosition[] = { 10.0f, 0.0f, 20.0f };

		// Set The Ambient Lighting For Light0
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, lightAmbient, 0);

		// Set The Diffuse Lighting For Light0
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, lightDiffuse, 0);

		// Set The Position For Light0
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, lightPosition, 0);

//		drawable.addMouseListener(this);
//		drawable.addMouseMotionListener(this);
//		drawable.addMouseWheelListener(this);

		// Disable the depth test by default (for 2d-case mostly)
		gl.glDisable(GL.GL_DEPTH_TEST);
		
		int n = 16;
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
		glu.gluQuadricNormals(cube, GL.GL_SMOOTH);

	}
	
	
	/**
	 * Transforms screen coordinates into space coordinates
	 */
	protected Vector getCoordinates(int x, int y) {
		if (canvas == null)
			return new Vector();
		
		int w = canvas.getWidth();
		int h = canvas.getHeight();
		
		if (w < 1)
			w = 1;
		if (h < 1)
			h = 1;
		
		double xx = (xMax - xMin) / w * x + xMin;
		double yy = (yMin - yMax) / h * y + yMax;
		
		return new Vector(xx, yy, 0);
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
	 * 
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

		if (zMin >= zMax - 1) {
			zMin = -100;
			zMax = 100;
		}

		gl.glOrtho(x0, x1, y0, y1, zMin - 10, zMax + 10);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	
	private void glNormal(GL gl, Vector v0, double z0, Vector v1, double z1, Vector v2, double z2) {
		double x0 = v1.x - v0.x;
		double y0 = v1.y - v0.y;
		double x1 = v2.x - v0.x;
		double y1 = v2.y - v0.y;
		
		double zz0 = z1 - z0;
		double zz1 = z2 - z0;
		
		double nx = y0 * zz1 - zz0 * y1;
		double ny = zz0 * x1 - x0 * zz1;
		double nz = x0 * y1 - y0 * x1;
		
		gl.glNormal3d(nx, ny, nz);
	}

	/**
	 * Renders the given data layer
	 * 
	 * @param gl
	 * @param grid
	 * @param spaceIndex
	 */
	protected void renderDataLayer(GL gl, DataLayerGraphics info, DataRow data,
			int spaceIndex) {
		if (info == null)
			return;

		DataLayerGraphics.GridInfo gridInfo = info.getGridInfo(data);
		if (gridInfo == null)
			return;

		if (gridInfo.spaceIndex != spaceIndex)
			return;

		// Deal with a 3d-case
		/*
		 * if (grid.getZSize() > 0) { try { grid = new DataGridZSlice(grid,
		 * zPlane, zMin, zMax); } catch (Exception e) { logger.error(e); return;
		 * }
		 * 
		 * if (!slicedMode) { gl.glPushMatrix(); gl.glTranslatef(0, 0, zPlane);
		 * } }
		 */
		// if (selectedDataLayer.getGeometry() == null)
		// selectedDataLayer.setGeometry(GridGraphics.getGeometry(grid, xMin,
		// yMin));

		Vector[][] gridGeometry = info.getGeometry(data, xMin, yMin);
		if (gridGeometry == null)
			return;

		int n = gridGeometry.length - 1;
		int m = gridGeometry[0].length - 1;

		Vector[][] colors = info.getColors(data);
		// Vector[][] colors = GridGraphics.getColors(grid, selectedDataLayer);
		if (colors == null)
			return;

		double[][] heightMap = info.getHeightMap(data);

		if (heightMap != null) {
			// Render the height map (the central part only)
			for (int i = 0; i < n; i++) {
				gl.glBegin(GL.GL_TRIANGLE_STRIP);
				Vector v0 = gridGeometry[i][0];
				Vector v1 = gridGeometry[i + 1][0];
				Vector c0 = colors[i][0];
				Vector c1 = colors[i + 1][0];
				double z0 = heightMap[i][0];
				double z1 = heightMap[i + 1][0];

				gl.glColor3d(c0.x, c0.y, c0.z);
//				gl.glNormal3d(v0.x, v0.y, z0);
				gl.glVertex3d(v0.x, v0.y, z0);

				gl.glColor3d(c1.x, c1.y, c1.z);
//				gl.glNormal3d(v1.x, v1.y, z1);
				gl.glVertex3d(v1.x, v1.y, z1);

				for (int j = 1; j <= m; j++) {
					Vector v3 = gridGeometry[i][j];
					Vector v4 = gridGeometry[i + 1][j];
					Vector c3 = colors[i][j];
					Vector c4 = colors[i + 1][j];
					double z3 = heightMap[i][j];
					double z4 = heightMap[i + 1][j];
					
					gl.glColor3d(c3.x, c3.y, c3.z);
					glNormal(gl, v3, z3, v0, z0, v1, z1);
//					gl.glNormal3d(v3.x, v3.y, z3);
					gl.glVertex3d(v3.x, v3.y, z3);

					gl.glColor3d(c4.x, c4.y, c4.z);
					glNormal(gl, v4, z4, v3, z3, v1, z1);
//					gl.glNormal3d(v4.x, v4.y, z4);
					gl.glVertex3d(v4.x, v4.y, z4);

					v0 = v3;
					z0 = z3;
					v1 = v4;
					z1 = z4;
				}
				gl.glEnd();
			}
		} else {
			// Render the center part
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
			double xStep = gridInfo.xStep;
			double yStep = gridInfo.yStep;

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
		}

		/*
		 * if (grid.getZSize() > 0 && !slicedMode) { gl.glPopMatrix(); }
		 */
	}

	/**
	 * Renders all visible space links of the given type (style)
	 * 
	 * @param gl
	 * @param linkStyle
	 */
	protected void renderLinks(GL gl, DataObject_SpaceLinks links,
			int spaceIndex, AgentStyle linkStyle) {
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

			double x1 = end1.x, y1 = end1.y, z1 = end1.z;
			double x2 = end2.x, y2 = end2.y, z2 = end2.z;

			float w = (float) width[i];
			gl.glLineWidth(w);
			gl.glColor3d(color.x, color.y, color.z);

			gl.glVertex3d(x1, y1, z1);
			gl.glVertex3d(x2, y2, z2);
		}
		gl.glEnd();
	}
	
	
	/**
	 * Renders a tile
	 * @return true if successful
	 */
	protected boolean drawTile(GL gl, float scale, TileManager tiles, DataObject_AgentData agentData, 
				AgentStyle style, Vector4d color, int index) {
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
		
		Texture tex = tile.getTexture();
		if (tex == null)
			return false;
		
		/* Set up the blending mode */
		int blendSrc = style.getSrcBlend();
		int blendDst = style.getDstBlend();

		if (blendSrc >= 0 && blendDst >= 0) {
			gl.glEnable(GL.GL_BLEND);
			// gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
			gl.glBlendFunc(blendSrc, blendDst);
		}

		int alphaFunc = style.getAlphaFunc();

		if (alphaFunc >= 0) {
			gl.glEnable(GL.GL_ALPHA_TEST);
			gl.glAlphaFunc(alphaFunc, style.getAlphaFuncValue());
		}

		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, style.getTextureEnv());

		// enable texturing
		gl.glEnable(GL.GL_TEXTURE_2D);
		tex.bind();
		
		float xt0 = 0, xt1 = 1;
		float yt0 = 1, yt1 = 0;
		float x0 = -1, x1 = 1;
		float y0 = -1, y1 = 1;
		
		if (tile.xReflect) {
			xt0 = 1;
			xt1 = 0;
		}
		
		if (tile.yReflect) {
			yt0 = 0;
			yt1 = 1;
		}
		
		if (tile.image != null) {
			int w = tile.image.getWidth();
			int h = tile.image.getHeight();
			
			if (w < h && w > 0) {
				float f = (float) h / w;
				y0 = -f;
				y1 = f;
			}
			else if (w > h && h > 0) {
				float f = (float) w / h;
				x0 = -f;
				x1 = f;
			}
		}
		
//		gl.glColor4d(1, 1, 1, 1);
//		gl.glColor3d(1, 1, 1);
		double alpha = color.a;
		if (style.transparent)
			alpha *= style.getTransparencyCoefficient();
				
		if (style.getColorBlending())
			gl.glColor4d(color.x, color.y, color.z, alpha);
		else
			gl.glColor4d(1, 1, 1, alpha);
//			gl.glColor3d(1, 1, 1);

		// Scale
		gl.glScalef(scale, scale, scale);
		
		// Render a rectangle
		gl.glBegin(GL.GL_QUADS);
			gl.glTexCoord2f(xt0, yt0);
			gl.glVertex2f(x0, y0);
			gl.glTexCoord2f(xt1, yt0);
			gl.glVertex2f(x1, y0);
			gl.glTexCoord2f(xt1, yt1);
			gl.glVertex2f(x1, y1);
			gl.glTexCoord2f(xt0, yt1);
			gl.glVertex2f(x0, y1);
		gl.glEnd();
		
		// Disable special rendering features
		if (alphaFunc >= 0) {
			gl.glDisable(GL.GL_ALPHA_TEST);
		}

		if (blendSrc >= 0 && blendDst >= 0 && !style.transparent) {
			gl.glDisable(GL.GL_BLEND);
		}
		
		gl.glDisable(GL.GL_TEXTURE_2D);
		return true;
	}
	
	
	/**
	 * Renders a specific shape
	 */
	private void drawShape(GL gl, float scale, DataObject_SpaceAgents.ShapeInfo shape, boolean border) {
		switch (shape.type) {
		case 0:
			gl.glScalef(scale, scale, scale);
			gl.glCallList(circle);
			break;
			
		case 1:
			float hx = shape.hx;
			float hy = shape.hy;
			gl.glBegin(GL.GL_QUADS);
				gl.glVertex2f(-hx, -hy);
				gl.glVertex2f(-hx, hy);
				gl.glVertex2f(hx, hy);
				gl.glVertex2f(hx, -hy);
			gl.glEnd();
			break;
		}
		
	}	

	/**
	 * Renders agents
	 * 
	 * @param gl
	 * @param agents
	 * @param spaceIndex
	 * @param agentStyle
	 */
	protected void renderAgents(GL gl, DataObject_SpaceAgents agents,
			DataObject_AgentData agentData,
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
		double[] rotations = agents.getRotations();
		DataObject_SpaceAgents.ShapeInfo[] shapeInfo = agents.getShapeInfo();
		
		
		/* Transparent agents */
		if (agentStyle.transparent) {
			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		}
		
		// Stencil
		if (agentStyle.getStencilFunc() > 0) {
			gl.glEnable(GL.GL_STENCIL_TEST);
			gl.glStencilFunc(agentStyle.getStencilFunc(), 
					agentStyle.getStencilRef(), agentStyle.getStencilMask());
			gl.glStencilOp(agentStyle.getStencilFail(),
						   agentStyle.getStencilZFail(),
						   agentStyle.getStencilZPass());
		}

		/* Tile manager */
		TileManager tiles = agentStyle.getTileManager();
		if (agentData == null)
			tiles = null;

		// Render lists
		int circle = agentStyle.border ? this.circle : this.circle2;
		int square = agentStyle.border ? this.square : this.square2;
		int donnut = agentStyle.border ? this.torus : this.torus2;

		double rad2angles = 180.0 / Math.PI;
		
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
			double phi = rotations[i];
			if (phi != 0) {
				gl.glRotated(phi * rad2angles, 0, 0, 1);
			}
			
			float scale = (float) radii[i];
			
			boolean drawShape = true;
			
			// Render a picture first
			if (tiles != null) {
				if (drawTile(gl, scale, tiles, agentData, agentStyle, color, i)) {
					if (!agentStyle.getDrawShapeWithImageFlag())
						drawShape = false;
				}
			}			

			if (drawShape) {
				/* Usual rendering */
				if (agentStyle.transparent)
					gl.glColor4d(color.x, color.y, color.z, agentStyle.getTransparencyCoefficient());
				else
					gl.glColor3d(color.x, color.y, color.z);
				
				if (shapeInfo[i] != null) {
					drawShape(gl, scale, shapeInfo[i], agentStyle.border);
				}
				else {
					gl.glScalef(scale, scale, scale);

					switch (shapes[i]) {
					// case SpaceAgent.CIRCLE:
					case 1:
						gl.glCallList(circle);
						break;
					// case SpaceAgent.SQUARE:
					case 2:
						gl.glCallList(square);
						break;
					// case SpaceAgent.TORUS:
					case 3:
						gl.glCallList(donnut);
						break;
					}
				}
			}
			gl.glPopMatrix();
		}

		// Disable the stencil test
		if (agentStyle.getStencilFunc() > 0) {
			gl.glDisable(GL.GL_STENCIL_TEST);
		}
		
		/* Disable transparency */
		if (agentStyle.transparent) {
			gl.glDisable(GL.GL_BLEND);
		}
	}

	/**
	 * Displays agents in 3d
	 * 
	 * @param gl
	 * @param agents
	 * @param agentStyle
	 * @param spaceIndex
	 */
	protected void renderAgents3d(GL gl, DataObject_SpaceAgents agents,
			AgentStyle agentStyle, int spaceIndex) {
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
			// case SpaceAgent.CIRCLE:
			case 1:
				glu.gluSphere(ball, 1, 8, 8);
				break;
			// case SpaceAgent.SQUARE:
			case 2:
				glu.gluSphere(cube, 1, 4, 4);
				break;
			// case SpaceAgent.TORUS:
			case 3:
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
	 * 
	 * @param gl
	 * @param agents
	 * @param agentStyle
	 * @param spaceIndex
	 */
	protected void renderAgents3dSliced(GL gl, DataObject_SpaceAgents agents,
			AgentStyle agentStyle, int spaceIndex) {
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
/*		if (agentStyle.getTexture() != null) {
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

			gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, agentStyle
					.getTextureEnv());

			// enable texturing
			gl.glEnable(GL.GL_TEXTURE_2D);
			agentStyle.getTexture().bind();
		}
*/
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

	//		if (agentStyle.getTexture() != null) {
//				gl.glColor4d(color.x, color.y, color.z, color.a);
				/* Render a textured agent */
/*				gl.glBegin(GL.GL_QUADS);
				gl.glTexCoord2f(0.0f, 0.0f);
				gl.glVertex2f(-1, -1);
				gl.glTexCoord2f(1.0f, 0.0f);
				gl.glVertex2f(1, -1);
				gl.glTexCoord2f(1.0f, 1.0f);
				gl.glVertex2f(1, 1);
				gl.glTexCoord2f(0.0f, 1.0f);
				gl.glVertex2f(-1, 1);
				gl.glEnd();
			} else {*/
				/* Usual rendering */
				if (agentStyle.transparent)
					gl.glColor4d(color.x, color.y, color.z, 0.5);
				else
					gl.glColor3d(color.x, color.y, color.z);
				switch (shapes[i]) {
				// case SpaceAgent.CIRCLE:
				case 1:
					gl.glCallList(circle);
					break;
				// case SpaceAgent.SQUARE:
				case 2:
					gl.glCallList(square);
					break;
				// case SpaceAgent.TORUS:
				case 3:
					gl.glCallList(donnut);
					break;
				}
//			}
			gl.glPopMatrix();
		}

		/* Disable transparency */
		if (agentStyle.transparent) {
			gl.glDisable(GL.GL_BLEND);
		}

		/* Disable texture */
	/*	if (agentStyle.getTexture() != null) {
			gl.glDisable(GL.GL_TEXTURE_2D);
			// switch back to modulation of quad colours and texture

			gl.glDisable(GL.GL_ALPHA_TEST); // switch off transparency
			gl.glDisable(GL.GL_BLEND);
		}
	*/
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

		// if (reshapeRequested) {
		// reshape(gl);
		// }

		// TODO: call it when necessary only
		reshape(gl);

		boolean space3d = false;
		if (spaces.getMins()[spaceIndex].z < spaces.getMaxs()[spaceIndex].z)
			space3d = true;

		// Turn on the 3d-mode if necessary
		boolean mode3d = false;
		if (space3d)
			mode3d = true;

		if (selectedDataLayer != null && selectedDataLayer.is3d())
			mode3d = true;

		if (slicedMode)
			mode3d = false;

		// Clear drawing buffer
		if ((drawable instanceof GLJPanel)
				&& !((GLJPanel) drawable).isOpaque()
				&& ((GLJPanel) drawable)
						.shouldPreserveColorBufferIfTranslucent()) {
			gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		} else {
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | 
					GL.GL_DEPTH_BUFFER_BIT | 
					GL.GL_STENCIL_BUFFER_BIT);
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

		if (mode3d) {
			gl.glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
			gl.glRotatef(view_roty, 0.0f, 1.0f, 0.0f);
//			gl.glScalef(wheel_scale, wheel_scale, wheel_scale);
			gl.glEnable(GL.GL_DEPTH_TEST);
		}


		
		// Render selected data layer
		if (selectedDataLayer != null) {
			// DataObject_Grid gridData =
			// data.getGrid(selectedDataLayer.getName());
			renderDataLayer(gl, selectedDataLayer, data, spaceIndex);
		}

		if (mode3d) {
			gl.glEnable(GL.GL_COLOR_MATERIAL);
			gl.glEnable(GL.GL_LIGHT0); // Enable Light 0
			gl.glEnable(GL.GL_LIGHTING); // Enable Lighting
			gl.glEnable(GL.GL_AUTO_NORMAL);
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
				renderLinks(gl, (DataObject_SpaceLinks) agentsData, spaceIndex,
						agentStyle);
			else {
				if (!space3d) {
					DataObject_AgentData additionalData = data.getAgentData(agentStyle.typeName);					
					renderAgents(gl, agentsData, additionalData, spaceIndex, agentStyle);
				} else if (slicedMode) {
					renderAgents3dSliced(gl, agentsData, agentStyle, spaceIndex);
				} else {
					renderAgents3d(gl, agentsData, agentStyle, spaceIndex);
				}
			}
		}

		if (mode3d) {
//			gl.glDisable(GL.GL_DEPTH_TEST);
//			gl.glDisable(GL.GL_COLOR_MATERIAL);
//			gl.glDisable(GL.GL_LIGHT0); // Disable Light 0
//			gl.glDisable(GL.GL_LIGHTING); // Disable Lighting
//			gl.glDisable(GL.GL_AUTO_NORMAL);
		}

		// Restore world matrix
		gl.glPopMatrix();
	}

	/************** Event listeners *******************/

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
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
