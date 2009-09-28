package org.spark.gui.render;

import java.awt.Canvas;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;

import org.spark.core.Agent;
import org.spark.gui.GUIModelManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class Render implements KeyListener{
	/* Types of renderers */
	public static final int JOGL_RENDER = 0;
	public static final int JAVA_2D_RENDER = 1;

	/* Active (selected) space to be rendered */
	protected SpaceStyle selectedSpace;
	/* Active data layers to be rendered */
	protected DataLayerStyle selectedDataLayer;

	/* Styles of all agents of this renderer */
	protected final ArrayList<AgentStyle> agentStyles;
	
	/* Styles of all data layers in a model defined in the selected space */
	protected final HashMap<String, DataLayerStyle> dataLayerStyles;

	/* A reference to all data layer styles in a model */
	protected HashMap<String, DataLayerStyle> globalDataLayerStyles;
	
	/* Offsets of the rendering area */
	protected float dx = 0, dy = 0;
	
	/* Zoom coefficient */
	protected float zoom = 1;

	/* Reshape request flag */
	protected boolean reshapeRequested;
	
	
	protected String renderName;
	protected long updateTick;
	
	
	private final static float DY_STEP = 1;
	private final static float DX_STEP = 1;
	private final static float ZOOM_FACTOR = 1.2f;
	private final static float MIN_ZOOM = 0.1f;
	private final static float MAX_ZOOM = 20;
	
	/**
	 * Default protected constructor
	 */
	protected Render() {
		dataLayerStyles = new HashMap<String, DataLayerStyle>();
		agentStyles = new ArrayList<AgentStyle>();
	}
	
	
	/**
	 *  Method should be implemented by any specific render
	 * @return
	 */
	public abstract Canvas getCanvas();
	
	/**
	 *  The main render's method
	 */
	public abstract void display();
	
	
	public void requestReshape() {
		reshapeRequested = true;
	}
	
	public abstract void takeSnapshot(String fname);
	
	public void takeSnapshot() {
		String name = (renderName != null) ? renderName : "pic";
		name += (updateTick+1);
		takeSnapshot(name);
	}
	
	
	public void setTick(long tick) {
		updateTick = tick;
	}
	
	public void setName(String name) {
		renderName = name;
	}
	
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
	public DataLayerStyle getCurrentDataLayerStyle() {
		return selectedDataLayer;
	}

	
	
	/**
	 * Sets the active space
	 * @param name
	 */
	public void setSpace(String name, boolean swapXY) {
		if (name == null) {
			selectedSpace = null;
			setDataLayerStyles(globalDataLayerStyles);
		}
		else {
//			if (Observer.getSpace(name) != null) {
				if (selectedSpace != null)
					reshapeRequested = true;
				selectedSpace = new SpaceStyle(name);
				selectedSpace.swapXY = swapXY;
				setDataLayerStyles(globalDataLayerStyles);
//			}
		}
	}
	
	
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
	public void setDataLayer(String name) {
		synchronized(this) {
			if (name == null) {
				this.selectedDataLayer = null;
				return;
			}
			
			DataLayerStyle dataLayer = dataLayerStyles.get(name);
			if (dataLayer == null) return;
			
			this.selectedDataLayer = dataLayer;
		}
	}

	
	/**
	 * Creates a new style for a specific agent type
	 * @param type
	 */
	public void addAgentType(Class<? extends Agent> type) {
		agentStyles.add(new AgentStyle(type));
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

	
	
	/* Methods for saving renderer properties in a model xml file */
	
	
	/**
	 * Returns a list of children with the specified name
	 * @param name
	 * @return
	 */
	private ArrayList<Node> getChildrenByTagName(Node node, String name) {
		ArrayList<Node> list = new ArrayList<Node>();

		for (Node child = node.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			if (child.getNodeName().equals(name))
				list.add(child);
		}

		return list;
	}

	/**
	 * Removes all children nodes with the specified name of the given node
	 * @param node
	 */
	private void removeChildren(Node node, String name) {
		for (Node item : getChildrenByTagName(node, name)) {
			node.removeChild(item);
		}
	}

	
	
	/**
	 * Writes out style information into an xml file
	 * @param doc
	 * @param parent
	 */
	public synchronized void writeXML(Document doc, Node parent) {
		if (doc == null || parent == null)
			return;

		removeChildren(parent, "spacestyle");
		removeChildren(parent, "datalayerstyle");
		removeChildren(parent, "agentstyle");
		removeChildren(parent, "#text");
		
		if (selectedSpace != null) {
			Node spaceNode = doc.createElement("spacestyle");
			
			Node attr = doc.createAttribute("name");
			attr.setNodeValue(selectedSpace.name);
			spaceNode.getAttributes().setNamedItem(attr);
		
			attr = doc.createAttribute("swapXY");
			attr.setNodeValue(String.valueOf(selectedSpace.swapXY));
			spaceNode.getAttributes().setNamedItem(attr);
			
			attr = doc.createAttribute("selected");
			attr.setNodeValue("true");
			spaceNode.getAttributes().setNamedItem(attr);
			
			parent.appendChild(spaceNode);
		}
		
		if (selectedDataLayer != null) {
			Node dls = doc.createElement("datalayerstyle");
			
			Node attr = doc.createAttribute("name");
			attr.setNodeValue(selectedDataLayer.name);
			dls.getAttributes().setNamedItem(attr);
			
			attr = doc.createAttribute("selected");
			attr.setNodeValue("true");
			dls.getAttributes().setNamedItem(attr);
			
			parent.appendChild(dls);
		}
		
		
		for (int i = 0; i < agentStyles.size(); i++) {
			AgentStyle agentStyle = agentStyles.get(i);
			String name = agentStyle.name;
			
			// Ignore styles without name
			if (name == null)
				continue;
			
			Node agentNode = agentStyle.createNode(doc, i);
			
			// Add a new node
			parent.appendChild(agentNode);
		}
	}


	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		char symbol = e.getKeyChar();
		boolean flag = false;

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
			dx = 0;
			dy = 0;
			zoom = 1;
			flag = true;
			break;
		}
		
		if (flag) {
			GUIModelManager.getInstance().requestUpdate();
		}
	}


	public void keyReleased(KeyEvent e) {
	}


	public void keyTyped(KeyEvent e) {
	}
}
