package org.sparkabm.gui.render;

import java.awt.Canvas;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sparkabm.gui.data.DataFilter;
import org.sparkabm.gui.data.DataReceiver;
import org.sparkabm.gui.data.IDataConsumer;
import org.sparkabm.gui.gui.SparkInspectionPanel;
import org.sparkabm.math.Vector;
import org.sparkabm.runtime.commands.Command_ControlEvent;
import org.sparkabm.runtime.data.DataCollectorDescription;
import org.sparkabm.runtime.data.DataObject_Inspection;
import org.sparkabm.runtime.data.DataObject_Spaces;
import org.sparkabm.runtime.data.DataRow;
import org.sparkabm.gui.Coordinator;
import org.sparkabm.gui.gui.SparkWindow;
import org.sparkabm.utils.XmlDocUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class Render implements KeyListener, IDataConsumer, MouseWheelListener, MouseListener, MouseMotionListener {
	// Logger
	private static final Logger logger = LogManager.getLogger();
	
	/* Types of renderers */
	public static final int JOGL_RENDER = 0;
	public static final int JAVA_2D_RENDER = 1;
	
	
	/* Data to be rendered */
	private DataRow data;
	
	private Object dataLock = new Object();
	
	/* Data filter */
	private final DataFilter dataFilter;
	
	/***** Snapshot parameters *****/
	
	/* Data filter for snapshots (have different collection interval) */
	private final DataFilter snapshotDataFilter;
	
	/* If false, then no snapshots are saved automatically */
	private boolean saveSnapshotsFlag;
	
	/* Prefix for file names of (automatic) snapshots */
	private String snapshotNamePrefix;
	
	
	/***** Style parameters *****/

	/* Active (selected) space to be rendered */
	protected SpaceStyle selectedSpace;
	/* Active data layers to be rendered */
	protected DataLayerGraphics selectedDataLayer;

	/* Styles of all agents of this renderer */
	protected final ArrayList<AgentStyle> agentStyles;
	
	/* Styles of all data layers in the selected space */
	protected final HashMap<String, DataLayerStyle> dataLayerStyles;

	/* A reference to all data layer styles in a model */
	protected HashMap<String, DataLayerStyle> globalDataLayerStyles;
	
	/******* Camera parameters ********/
	
	/* Offsets of the rendering area */
	protected float dx = 0, dy = 0;
	/* Rotation of the rendering area */
	protected float view_rotx = 20.0f, view_roty = 30.0f;// , view_rotz = 0.0f;
	
	
	/* Zoom coefficient */
	protected float zoom = 1;

	
	/***** Render control state ******/
	
	/* Control state */
	protected int controlState = 0;
	
	// Possible control states
	public static final int CONTROL_STATE_SELECT = 0;
	public static final int CONTROL_STATE_MOVE = 1;
	public static final int CONTROL_STATE_CONTROL = 2;
	
	/* Inspection panel */
	private SparkInspectionPanel inspectionPanel;
	
	/***** Render flags ******/
	
	/* Reshape request flag */
	protected volatile boolean reshapeRequested;
	
	/* Display request flag */
	private volatile boolean displayRequested;
	
	
	protected String renderName;
//	protected long updateTick;
	
	// Some constants
	private final static float DY_STEP = 1;
	private final static float DX_STEP = 1;
	private final static float ZOOM_FACTOR = 1.2f;
	private final static float MIN_ZOOM = 0.1f;
	private final static float MAX_ZOOM = 20;
	
	/**
	 * Default protected constructor
	 */
	protected Render(int interval) {
		dataLayerStyles = new HashMap<String, DataLayerStyle>();
		agentStyles = new ArrayList<AgentStyle>();
		dataFilter = new DataFilter(this, "render");
		dataFilter.setInterval(interval);

		controlState = CONTROL_STATE_SELECT;
		
		saveSnapshotsFlag = false;
		
		snapshotDataFilter = new DataFilter(new IDataConsumer() {
			public void consume(DataRow row) {
				if (!saveSnapshotsFlag)
					return;
				
				takeSnapshot(row, snapshotNamePrefix);
			}
		}, "render-snapshot");
		
		snapshotDataFilter.setInterval(0);
		snapshotDataFilter.setSynchronizedFlag(true);
	}
	
	
	/**
	 *  Method should be implemented by any specific render
	 * @return
	 */
	public abstract Canvas getCanvas();
	
	/**
	 *  The main render's method displays the given data.
	 *  Always called from the AWT event thread
	 */
	protected abstract void display(DataRow data);
	
	
	/**
	 * Transforms screen coordinates into space coordinates
	 */
	protected abstract Vector getCoordinates(int x, int y);
	
	
	/**
	 * Tells the renderer to redraw everything.
	 * This method is thread-safe because it puts the request
	 * into the AWT event queue
	 */
	public synchronized final void update() {
		if (displayRequested)
			return;
		
		displayRequested = true;
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				DataRow tmp;
				
				synchronized (dataLock) {
					tmp = data;
					displayRequested = false;
				}
				
				display(tmp);
			}
		});
	}
	
	
	/**
	 * Sets the control state
	 */
	public void setControlState(int state) {
		logger.debug("Set control state: " + state);
		this.controlState = state;
	}
	
	
	/**
	 * Returns the control state
	 */
	public int getControlState() {
		return controlState;
	}
	

	/**
	 * Forces reshaping of the canvas during the next display operation
	 */
	public void requestReshape() {
		reshapeRequested = true;
	}
	
	
	/**
	 * Saves a snapshot to the given file.
	 * This method is always called from the awt event queue
	 */
	protected abstract void saveSnapshot(File dir, String name, DataRow data);
	
	
	/**
	 * Saves a snapshot of the current data to an automatically generated file
	 * @param prefix
	 */
	public synchronized final void takeSnapshot(final String prefix) {
		takeSnapshot(data, prefix);
	}
	
	
	/**
	 * Saves a snapshot to an automatically generated file
	 */
	public synchronized final void takeSnapshot(final DataRow row, final String prefix) {
		final File dir = Coordinator.getInstance().getOutputDir();
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (row == null)
					return;
				
				String name = (prefix != null ? prefix : "");
				name += (renderName != null && renderName != "") ? renderName : "pic";
				name += "-";
				
				String time = String.valueOf(row.getTime().getTick());
				for (int i = time.length(); i < 4; i++)
					name += "0";
				
				name += time;

				saveSnapshot(dir, name, row);
			}
		});
	}
	
	
	/**
	 * Implementation of IDataConsumer interface
	 */
	public void consume(DataRow row) {
		synchronized (dataLock) {
			this.data = row;
			update();
		}
	}
	
	
	/**
	 * Updates data which is consumed by the renderer
	 */
	public void updateDataFilter() {
		dataFilter.removeAllData();
		
		dataFilter.addData(DataCollectorDescription.SPACES, null);
		if (selectedDataLayer != null)
			selectedDataLayer.updateDataFilter(dataFilter);
		
		for (AgentStyle agentStyle : agentStyles) {
			if (agentStyle.visible) {
				dataFilter.addData(DataCollectorDescription.SPACE_AGENTS, agentStyle.typeName);
				// TODO: this should be selected by a special checkbox
				dataFilter.addData(DataCollectorDescription.AGENT_DATA, agentStyle.typeName);
			}
		}
		
		snapshotDataFilter.copyDataParameters(dataFilter);
	}
	
	
	/**
	 * Returns the data filter associated with the renderer
	 * @return
	 */
	public void register(DataReceiver receiver) {
		receiver.addDataConsumer(dataFilter);
		receiver.addDataConsumer(snapshotDataFilter);
	}

	
	
	/**
	 * Enables automatic snapshots at the given time intervals
	 * @param flag
	 */
	public void enableAutomaticSnapshots(int interval) {
		saveSnapshotsFlag = true;
		snapshotDataFilter.setInterval(interval);
	}
	
	
	/**
	 * Disables automatic snapshots
	 */
	public void disableAutomaticSnapshots() {
		saveSnapshotsFlag = false;
		snapshotDataFilter.setInterval(0);
	}
	
	
	/**
	 * Returns saveSnapshotFlag
	 * @return
	 */
	public boolean getSaveSnapshotsFlag() {
		return saveSnapshotsFlag;
	}
	
	
	/**
	 * Sets the prefix for snapshot file names
	 * @param name
	 */
	public void setSnapshotNamePrefix(String name) {
		snapshotNamePrefix = name;
	}
	

	/**
	 * Returns the prefix for snapshot file names
	 * @return
	 */
	public String getSnapshotNamePrefix() {
		return snapshotNamePrefix;
	}
	
	
	/**
	 * Sets render's name
	 * @param name
	 */
	public void setName(String name) {
		renderName = name;
	}
	
	
	/**
	 * Returns render's name
	 * @return
	 */
	public String getName() {
		return renderName;
	}
	
	
	/**
	 * Returns all agent styles in this renderer
	 * @return
	 */
	public ArrayList<AgentStyle> getAgentStyles() {
		return agentStyles;
	}
	
	
	/**
	 * Returns the active space style
	 * @return
	 */
	public SpaceStyle getSelectedSpaceStyle() {
		return selectedSpace;
	}
	
	
	/**
	 * Returns all data layer styles in this renderer
	 * @return
	 */
	public HashMap<String, DataLayerStyle> getDataLayerStyles() {
		return dataLayerStyles;
	}
	
	
	/**
	 * Returns selected data layer
	 * @return
	 */
	public DataLayerGraphics getCurrentDataLayerGraphics() {
		return selectedDataLayer;
	}

	
	
	/**
	 * Sets the active space
	 * @param name
	 */
	public void setSpace(SpaceStyle style) {
		if (style == null) {
			selectedSpace = null;
			setDataLayerStyles(globalDataLayerStyles);
		}
		else {
			if (selectedSpace != null)
				reshapeRequested = true;
			selectedSpace = style;
			setDataLayerStyles(globalDataLayerStyles);
		}
	}
	
	
	/**
	 * Returns names of spaces available for the render
	 * @return
	 */
	public String[] getSpaceNames() {
		if (data == null)
			return null;
		
		DataObject_Spaces spaces = data.getSpaces();
		String[] names = spaces.getNames();
		
		if (spaces.getTotalNumber() == names.length)
			return names;
		
		names = new String[spaces.getTotalNumber()];
		for (int i = 0; i < spaces.getTotalNumber(); i++)
			names[i] = spaces.getNames()[i];
		
		return names;
	}
	
	
	/**
	 * Sets swapXY flag for the selected space
	 * @param flag
	 */
	public void setSwapXYFlag(boolean flag) {
		if (selectedSpace != null) {
			selectedSpace.swapXY = flag;
			requestReshape();
		}
	}
	
	
	/**
	 * Sets an active data layer
	 * @param name
	 */
	public void setDataLayer(DataLayerGraphics dataLayerGraphics) {
		this.selectedDataLayer = dataLayerGraphics;
	}

	
	
	/**
	 * Adds a new agent style
	 * @param style
	 */
	public void addAgentStyle(AgentStyle style) {
		agentStyles.add(style);
	}


	/**
	 * Sets a reference to all data layer styles in a model
	 * @param globalStyles
	 */
	public void setGlobalDataLayerStyles(HashMap<String, DataLayerStyle> globalStyles) {
		globalDataLayerStyles = globalStyles;
	}
	
	
	/**
	 * Sets data layer styles
	 * @param styles
	 */
	private void setDataLayerStyles(HashMap<String, DataLayerStyle> styles) {
		dataLayerStyles.clear();
		selectedDataLayer = null;
		
		for (String name : styles.keySet()) {
			DataLayerStyle style = styles.get(name);
			if (style == null)
				continue;
			
			dataLayerStyles.put(name, style);
		}
		
/*		if (selectedSpace == null)
			return;
		
		Space space = Observer.getSpace(selectedSpace.name);
		if (space == null)
			return;
		
		// Select only data layers defined in the selected space
		for (String name : styles.keySet()) {
			DataLayerStyle style = styles.get(name);
			if (style == null)
				continue;
			
			DataLayer data = space.getDataLayer(style.name);
			if (data == null)
				continue;
			
			dataLayerStyles.put(name, style);
		}*/
	}
	
	
	/**
	 * Changes the priority of agent styles
	 * @param style1
	 * @param style2
	 */
	public void swapAgentStyles(AgentStyle style1, AgentStyle style2) {
		if (style1 == style2)
			return;
		
		int i1 = agentStyles.indexOf(style1);
		int i2 = agentStyles.indexOf(style2);
		
		if (i1 == -1 || i2 == -1)
			return;
		
		agentStyles.set(i1, style2);
		agentStyles.set(i2, style1);
	}



	/**
	 * Clears the renderer
	 */
	public synchronized void clear() {
		agentStyles.clear();
		dataLayerStyles.clear();
		selectedDataLayer = null;
		globalDataLayerStyles = null;
	}

	
	
	/* Methods for saving/loading renderer properties into/from a model xml file */
	
	/**
	 * Creates a new render from the given xml document and
	 * of the specific type
	 */
	public static Render createRender(Node node, int renderType, int interval,
			HashMap<String, DataLayerStyle> dataLayerStyles,
			HashMap<String, String> agentTypesAndNames,
			File modelPath, boolean noGUI) {
		Render render = null;
		if (renderType == Render.JOGL_RENDER) {
			try {
				render = new JOGLRender(interval);
			} catch (Exception e) {
				e.printStackTrace();
				render = new JavaRender(interval, noGUI);
			}
		} else {
			render = new JavaRender(interval, noGUI);
		}
		
		// Load general properties
		int controlState = XmlDocUtils.getIntegerValue(node, "control-state", 0);
		render.setControlState(controlState);
		
		render.dx = XmlDocUtils.getFloatValue(node, "dx", 0);
		render.dy = XmlDocUtils.getFloatValue(node, "dy", 0);
		render.view_rotx = XmlDocUtils.getFloatValue(node, "rot-x", 20);
		render.view_roty = XmlDocUtils.getFloatValue(node, "rot-y", 30);
		render.zoom = XmlDocUtils.getFloatValue(node, "zoom", 1);
		

		// Create agent styles for this renderer
		ArrayList<AgentStyle> agentStyles = new ArrayList<AgentStyle>();
		HashMap<String, AgentStyle> agentMap = new HashMap<String, AgentStyle>();

		
		for (String agentType : agentTypesAndNames.keySet()) {
			AgentStyle agentStyle = new AgentStyle(agentType);
			agentStyles.add(agentStyle);

			String name = agentTypesAndNames.get(agentType);
			if (name != null)
				agentMap.put(name, agentStyle);

			agentStyle.name = name;
		}

		DataLayerGraphics selectedDataLayer = null;
		SpaceStyle selectedSpace = null;

		// Load attributes for each components (agents, data layers, spaces)
		if (node != null) {
			NodeList nodes = node.getChildNodes();

			// Iterate over all components
			for (int i = 0; i < nodes.getLength(); i++) {
				node = nodes.item(i);
				String nodeName = node.getNodeName().intern();
				
				String name = XmlDocUtils.getValue(node, "name", null);

				// space style
				if (nodeName == "spacestyle") {
					SpaceStyle spaceStyle = SpaceStyle.load(node);
							
					if (spaceStyle.selected)
						selectedSpace = spaceStyle;
					
				} 
				// data layer style
				else if (nodeName == "datalayerstyle") {
					selectedDataLayer = DataLayerGraphics.loadXML(node, dataLayerStyles);
				} 
				// agent style
				else if (node.getNodeName().equals("agentstyle")
						&& name != null) {
					AgentStyle agentStyle = agentMap.get(name);

					if (agentStyle == null)
						continue;

					agentStyle.load(node, modelPath);
				}
			}
		}

		Collections.sort(agentStyles);

		for (int j = 0; j < agentStyles.size(); j++) {
			render.addAgentStyle(agentStyles.get(j));
		}

		render.setGlobalDataLayerStyles(dataLayerStyles);

		if (selectedSpace == null) {
			// TODO: it is just a way around: find better solution
			selectedSpace = new SpaceStyle("space");
		}
		
		render.setSpace(selectedSpace);
		
		if (selectedDataLayer != null)
			render.setDataLayer(selectedDataLayer);

		return render;
	}
	
	/**
	 * Writes out style information into an xml file
	 * @param doc
	 * @param parent
	 */
	public synchronized void writeXML(Document doc, Node parent, File modelPath) {
		if (doc == null || parent == null)
			return;

		// Remove all old subnodes
		XmlDocUtils.removeChildren(parent, "spacestyle");
		XmlDocUtils.removeChildren(parent, "datalayerstyle");
		XmlDocUtils.removeChildren(parent, "agentstyle");
		XmlDocUtils.removeChildren(parent, "#text");
		
		// Save general properties
		XmlDocUtils.addAttr(doc, parent, "control-state", controlState);
		XmlDocUtils.addAttr(doc, parent, "dx", dx);
		XmlDocUtils.addAttr(doc, parent, "dy", dy);
		XmlDocUtils.addAttr(doc, parent, "rot-x", view_rotx);
		XmlDocUtils.addAttr(doc, parent, "rot-y", view_roty);
		XmlDocUtils.addAttr(doc, parent, "zoom", zoom);
		
		
		// Node for the selected space properties
		if (selectedSpace != null) {
			Node spaceNode = selectedSpace.createNode(doc, modelPath);
			parent.appendChild(spaceNode);
		}
		
		// Node for the selected data layer(s)
		if (selectedDataLayer != null) {
			Node dls = selectedDataLayer.createNode(doc, modelPath); 
			parent.appendChild(dls);
		}
		
		
		// Nodes for styles of agents
		for (int i = 0; i < agentStyles.size(); i++) {
			AgentStyle agentStyle = agentStyles.get(i);
			String name = agentStyle.name;
			
			// Ignore styles without name
			if (name == null)
				continue;
			
			Node agentNode = agentStyle.createNode(doc, i, modelPath);
			
			// Add a new node
			parent.appendChild(agentNode);
		}
	}

	
	/**
	 * Controls the camera position using the keyboard events
	 * @param code
	 * @param symbol
	 */
	private void controlCamera(int code, char symbol) {
		boolean flag = false;

		// Special reaction on + and -
		switch (symbol) {
		case '+':
			code = KeyEvent.VK_PLUS;
			break;
			
		case '-':
			code = KeyEvent.VK_MINUS;
			break;
		}
		
		switch (code) {
		case KeyEvent.VK_UP:
			dy -= DY_STEP;
			flag = true;
			break;
			
		case KeyEvent.VK_DOWN:
			dy += DY_STEP;
			flag = true;
			break;

		case KeyEvent.VK_LEFT:
			dx += DX_STEP;
			flag = true;
			break;
			
		case KeyEvent.VK_RIGHT:
			dx -= DX_STEP;
			flag = true;
			break;
			
		case KeyEvent.VK_PLUS:
			if (zoom < MAX_ZOOM) {
				zoom *= ZOOM_FACTOR;
				flag = true;
			}
			break;
			
		case KeyEvent.VK_MINUS:
			if (zoom > MIN_ZOOM) {
				zoom /= ZOOM_FACTOR;
				flag = true;
			}
			break;
		
		case KeyEvent.VK_ENTER:
			resetCamera(false);
			flag = true;
			break;
		}

		if (flag) {
			update();
		}
	}
	
	
	/**
	 * Resets the camera position
	 */
	public void resetCamera(boolean update) {
		dx = 0;
		dy = 0;
		zoom = 1;
		
		if (update) {
			update();
		}
	}
	
	
	/**
	 * Creates an inspector for the given coordinates
	 * @param x
	 * @param y
	 */
	private void createInspector(int x, int y) {
		if (inspectionPanel == null) {
			inspectionPanel = new SparkInspectionPanel(Coordinator.getInstance().getWindowManager(), renderName);
		}
		else {
			SparkWindow win = Coordinator.getInstance().getWindowManager().findWindow(inspectionPanel);
			if (win != null)
				win.setVisible(true);
		}

		Vector pos = getCoordinates(x, y);
		DataObject_Inspection.Parameters pars = new DataObject_Inspection.Parameters(selectedSpace.name, pos);
		
		inspectionPanel.init(pars);
	}
	

	/**
	 * Key listeners
	 */
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		char symbol = e.getKeyChar();

		switch (controlState) {
		case CONTROL_STATE_SELECT:
		case CONTROL_STATE_MOVE:
			controlCamera(code, symbol);
			break;
			
		case CONTROL_STATE_CONTROL:
			String spaceName = (selectedSpace != null) ? selectedSpace.name : "(no space)";
			Coordinator.getInstance().sendCommand(new Command_ControlEvent(spaceName, true, code, symbol));
			break;
		}
	}


	public void keyReleased(KeyEvent e) {
		int code = e.getKeyCode();
		char symbol = e.getKeyChar();
		
		switch (controlState) {
		case CONTROL_STATE_CONTROL:
			String spaceName = (selectedSpace != null) ? selectedSpace.name : "(no space)";
			Coordinator.getInstance().sendCommand(new Command_ControlEvent(spaceName, false, code, symbol));
			break;
		}
	}


	public void keyTyped(KeyEvent e) {
	}

	

	/**
	 * Sends a mouse control event
	 */
	private void sendMouseEvent(int eventType, MouseEvent mouseEvent, int mouseWheel) {
		int modifiers = mouseEvent.getModifiersEx();
		int mx = mouseEvent.getX();
		int my = mouseEvent.getY();

		// Translate screen coordinates into space coordinates
		Vector v = getCoordinates(mx, my);

		String spaceName = (selectedSpace != null) ? selectedSpace.name : "(no space)";
		Command_ControlEvent cmd = new Command_ControlEvent(spaceName, eventType,
				modifiers, v, mouseWheel);
		
		// Send the command
		Coordinator.getInstance().sendCommand(cmd);
	}

	
	
	/**
	 * Mouse listeners
	 * @param e
	 */
	private int prevMouseX, prevMouseY;
	private boolean rightButtonPressed, leftButtonPressed;
	
	/**
	 * Mouse pressed
	 */
	public void mousePressed(MouseEvent e) {
		prevMouseX = e.getX();
		prevMouseY = e.getY();
		
		int button = e.getButton();
		
		if (button == MouseEvent.BUTTON1) {
			leftButtonPressed = true;
		}
		
		if (button == MouseEvent.BUTTON3) {
			rightButtonPressed = true;
		}
		
		// Swith the control state
		switch (controlState) {
		case CONTROL_STATE_SELECT:
			// TODO: implement
			break;
		case CONTROL_STATE_MOVE:
			break;
		case CONTROL_STATE_CONTROL:
			if (button == MouseEvent.BUTTON1)
				sendMouseEvent(Command_ControlEvent.LBUTTON_DOWN, e, 0);
			if (button == MouseEvent.BUTTON3)
				sendMouseEvent(Command_ControlEvent.RBUTTON_DOWN, e, 0);
			break;
		}
	}

	/**
	 * Mouse released
	 */
	public void mouseReleased(MouseEvent e) {
		int button = e.getButton();

		if (button == MouseEvent.BUTTON1) {
			leftButtonPressed = false;
			if (controlState == CONTROL_STATE_CONTROL) {
				sendMouseEvent(Command_ControlEvent.LBUTTON_UP, e, 0);
			}
		}

		if (button == MouseEvent.BUTTON3) {
			rightButtonPressed = false;
			if (controlState == CONTROL_STATE_CONTROL) {
				sendMouseEvent(Command_ControlEvent.RBUTTON_UP, e, 0);
			}
		}
	}

	/**
	 * Mouse clicked
	 */
	public void mouseClicked(MouseEvent e) {
		int buttons = e.getButton();
		
		if (buttons == MouseEvent.BUTTON1) {
			if (controlState == CONTROL_STATE_SELECT) {
				createInspector(e.getX(), e.getY());
			}
		}
	}
	
	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Mouse dragged
	 */
	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		
		// Movement 
		if (controlState == CONTROL_STATE_MOVE) {
			if (leftButtonPressed) {
				dx -= 0.5f * (x - prevMouseX);
				dy += 0.5f * (y - prevMouseY);
				
				update();
			}
			else if (rightButtonPressed) {
				float thetaY = 0.01f * 360.0f * (x - prevMouseX);
				float thetaX = 0.01f * 360.0f * (prevMouseY - y);
				
				view_rotx += thetaX;
				view_roty += thetaY;
				update();
			}
		}
		
		if (controlState == CONTROL_STATE_CONTROL) {
			sendMouseEvent(Command_ControlEvent.MOUSE_MOVE, e, 0);
		}
		
		prevMouseX = x;
		prevMouseY = y;
/*		Dimension size = e.getComponent().getSize();

		float thetaY = 360.0f * ((float) (x - prevMouseX) / (float) size.width);
		float thetaX = 360.0f * ((float) (prevMouseY - y) / (float) size.height);

		prevMouseX = x;
		prevMouseY = y;

		if (!rightButtonPressed) {
			view_rotx += thetaX;
			view_roty += thetaY;
		} else {
			// mouse_y -= thetaX;
			// mouse_x -= thetaY;
			zPlane += thetaY;

			if (zPlane < zMin)
				zPlane = zMin;
			else if (zPlane > zMax)
				zPlane = zMax;
		}

		canvas.display();*/
	}

	
	/**
	 * Mouse moved
	 */
	public void mouseMoved(MouseEvent e) {
		switch (controlState) {
		case CONTROL_STATE_SELECT:
		case CONTROL_STATE_MOVE:
			break;
			
		case CONTROL_STATE_CONTROL:
			sendMouseEvent(Command_ControlEvent.MOUSE_MOVE, e, 0);
			return;
		}
	}
	
	
	/**
	 * Listens for mouse wheel events
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
		
		switch (controlState) {
		case CONTROL_STATE_SELECT:
		case CONTROL_STATE_MOVE:
			break;
			
		case CONTROL_STATE_CONTROL:
			sendMouseEvent(Command_ControlEvent.MOUSE_WHEEL, e, notches);
			return;
		}
		
		// Zoom in/out
		zoom -= notches * 0.1f;
		if (zoom < MIN_ZOOM)
			zoom = MIN_ZOOM;
		else if (zoom > MAX_ZOOM)
			zoom = MAX_ZOOM;
		
		// Update the picture
		update();
	}
}
