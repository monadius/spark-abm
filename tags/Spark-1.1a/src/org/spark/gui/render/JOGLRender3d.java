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
import org.spark.space.BoundedSpace;
import org.spark.space.Space;
import org.spark.space.SpaceAgent;
import org.spark.space.SpaceNode;
import org.spark.utils.Vector;
import org.spark.utils.Vector4d;

import com.spinn3r.log5j.Logger;



public class JOGLRender3d extends Render implements GLEventListener, MouseListener, MouseMotionListener, MouseWheelListener {
	private static final Logger logger = Logger.getLogger();
	
	// TODO: unused
	private float view_rotx = 20.0f, view_roty = 30.0f;//, view_rotz = 0.0f;
	private float wheel_scale = 1.0f;
	private float mouse_x = 0.0f, mouse_y = 0.0f;

	private int prevMouseX, prevMouseY;

	
	// used for refreshing the frame
	private GLCanvas canvas;
	private GLU glu = new GLU();
	private GLUquadric ball, cube;

	
//	private boolean active = false;
	private boolean takeScreenshot = false;
	
	
//	public void setActivity(boolean flag) {
//		active = flag;
//	}
	
	// TODO: rewrite this
//	public void setGLCanvas(GLCanvas canvas) {
//		this.canvas = canvas;
//	}

	
	public JOGLRender3d(GLCanvas canvas) {
		this.canvas = canvas;
		if (canvas != null)
			canvas.addGLEventListener(this);
	}
	
	
	public JOGLRender3d() {
		GLCanvas glcanvas = new GLCanvas();
		glcanvas.addGLEventListener(this);
		this.canvas = glcanvas;
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

	
/*	public void resize() {
		if (canvas == null) return;
		canvas.setSize(canvas.getSize());
		canvas.display();
	}*/
	
	


	public void init(GLAutoDrawable drawable) {
		logger.info("Initializing JOGLRender3d");
		
		GL gl = drawable.getGL();

		/* Set up OpenGL */
		
		gl.setSwapInterval(1);
		gl.glClearColor(1, 1, 1, 1);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
//		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);

		gl.glShadeModel(GL.GL_SMOOTH);  // Enable Smooth Shading
        gl.glClearDepth(1.0f);  // Depth Buffer Setup
        gl.glEnable(GL.GL_DEPTH_TEST);  // Enables Depth Testing
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glEnable(GL.GL_COLOR_MATERIAL);

        /* Set up light */
        
        float lightAmbient[] = {0.2f, 0.2f, 0.2f};  // Ambient Light is 20% white
        float lightDiffuse[] = {1.0f, 1.0f, 1.0f};  // Diffuse Light is white
        
        // Position is somewhat in front of screen
        float lightPosition[] = {0.0f, 0.0f, 0.0f};  
        
        // Set The Ambient Lighting For Light0
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, lightAmbient, 0);      
        
        // Set The Diffuse Lighting For Light0
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, lightDiffuse, 0);      
        
        // Set The Position For Light0
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, lightPosition, 0);    

        gl.glEnable(GL.GL_LIGHT0);  // Enable Light 0
        gl.glEnable(GL.GL_LIGHTING);  // Enable Lighting

        /* Create quadratics */
        ball = glu.gluNewQuadric();    // Create A New Quadratic
        glu.gluQuadricNormals(ball, GL.GL_SMOOTH);  // Generate Smooth Normals For The Quad

        cube = glu.gluNewQuadric();
		
		drawable.addMouseListener(this);
		drawable.addMouseMotionListener(this);
		drawable.addMouseWheelListener(this);

	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		GL gl = drawable.getGL();

		gl.glMatrixMode(GL.GL_PROJECTION);

//		System.err.println(Thread.currentThread().toString());
//		System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
//		System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
//		System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));
		gl.glLoadIdentity();

		if (selectedSpace == null)
			return;

		Space space = Observer.getSpace(selectedSpace.name);
		if (space == null)
			return;
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

		gl.glMatrixMode(GL.GL_PROJECTION);  // Select The Projection Matrix
        gl.glLoadIdentity();                // Reset The Projection Matrix
        
        // Calculate The Aspect Ratio Of The Window
//        glu.gluPerspective(45.0f, (float) width / (float) height, 0.1f, 100.0f);  
//        gl.glMatrixMode(GL.GL_MODELVIEW); // Select The Modelview Matrix
//        gl.glLoadIdentity();  
        
		gl.glOrtho(xMin, xMax, yMin, yMax, -100, 100);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	
	private String fname;
	
	public void readFrameBuffer(String fname) {
		this.fname = fname;
		takeScreenshot = true;
		canvas.display();
	}
	

	public void display(GLAutoDrawable drawable) {
		if (takeScreenshot) {

			try {
				com.sun.opengl.util.Screenshot.writeToFile(
						new File("out" + fname + ".png"),
//						new File("out.png"),
						canvas.getWidth(),
						canvas.getHeight() );
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
		if ((drawable instanceof GLJPanel)
				&& !((GLJPanel) drawable).isOpaque()
				&& ((GLJPanel) drawable)
						.shouldPreserveColorBufferIfTranslucent()) {
			gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		} else {
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		}

		gl.glPushMatrix();
		gl.glTranslatef(mouse_x, mouse_y, 0);
		gl.glScalef(wheel_scale, wheel_scale, wheel_scale);
		gl.glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(view_roty, 0.0f, 1.0f, 0.0f);

		for (int k = agentStyles.size() - 1; k >= 0; k--) {
			Agent[] agents;
			AgentStyle agentStyle = agentStyles.get(k);

			if (!agentStyle.visible) continue;
			
			agents = Observer.getInstance().getAgents(agentStyle.agentType);
			if (agents == null) continue;
			
			if (agentStyle.transparent) {
				gl.glEnable(GL.GL_BLEND);
			}
			
			int n = agents.length;
//			int donnut = agentStyle.border ? this.donnut : this.donnut2;
			
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
//						gl.glCallList(donnut);
						break;
					}
					gl.glPopMatrix();
				}
			}

			if (agentStyle.transparent) {
				gl.glDisable(GL.GL_BLEND);
			}

		}

		gl.glPopMatrix();
		
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
			//     mouseRButtonDown = true;
		}
	}

	public void mouseReleased(MouseEvent e) {
		if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
			//	      mouseRButtonDown = false;
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
//		mouse_y -= thetaX;
//		mouse_x -= thetaY;

//		if (!active) canvas.display();
		canvas.display();
	}

	public void mouseMoved(MouseEvent e) {
	}

//	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
		wheel_scale -= notches * 0.1f;
		
		if (wheel_scale < 0.1f)
			wheel_scale = 0.1f;
		else if (wheel_scale > 10.0f)
			wheel_scale = 10.0f;
		
//		if (!active) canvas.display();
		canvas.display();
	}
}