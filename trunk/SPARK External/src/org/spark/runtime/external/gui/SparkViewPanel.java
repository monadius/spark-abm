package org.spark.runtime.external.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.gui.dialogs.RenderProperties;
import org.spark.runtime.external.render.Render;
import org.spark.utils.XmlDocUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.spinn3r.log5j.Logger;

import javax.swing.*;

/**
 * View panel
 * 
 * @author Monad
 */
@SuppressWarnings("serial")
public class SparkViewPanel extends JPanel implements ISparkPanel,
		ActionListener {
	private static final Logger logger = Logger.getLogger();
	
	/* Render for this view */
	private Render render;
	/* Node for the render */
	private Node xmlNode;
	
	/* Window for this panel */
	private final SparkWindow win;
	
	/* Tool bar */
	private JToolBar toolBar; 
	
	/* Pop-up menu */
	private JPopupMenu popup;

	// TODO: what happens when the render window is destroyed
	private RenderProperties renderDialog;

	/**
	 * Default constructor
	 * 
	 * @param node
	 * @param renderType
	 */
	public SparkViewPanel(WindowManager manager, Node node, int renderType) {
		init(node, renderType);
		
		// Set panel's location
		String location = XmlDocUtils.getValue(node, "location", null);
		win = manager.setLocation(this, location);
		
		if (render != null) {
			render.setName(win.getName());
			toolBar.setName(win.getName());
		}
	}
	
	
	/**
	 * Creates a view panel in the given window
	 * @param win
	 * @param renderType
	 */
	public SparkViewPanel(SparkWindow win, int renderType) {
		init(null, renderType);
		win.addPanel(this);
		
		this.win = win;
		if (render != null && win != null) {
			render.setName(win.getName());
			toolBar.setName(win.getName());
		}
	}
	
	
	/**
	 * Creates a tool bar
	 * @param node
	 */
	private void createToolBar(Render render) {
		// Create a tool bar
		toolBar = new JToolBar("View");
		toolBar.setPreferredSize(new Dimension(100, 40));

		// Create buttons
		
		// Control state buttons
		JRadioButton select = new JRadioButton("select");
		JRadioButton move = new JRadioButton("move");
		JRadioButton control = new JRadioButton("control");
		
		select.setToolTipText("Selection mode");
		move.setToolTipText("Camera control mode");
		control.setToolTipText("Model control mode");
		
		int controlState = render.getControlState();
		switch (controlState) {
		case Render.CONTROL_STATE_SELECT:
			select.setSelected(true);
			break;
		case Render.CONTROL_STATE_MOVE:
			move.setSelected(true);
			break;
		case Render.CONTROL_STATE_CONTROL:
			control.setSelected(true);
			break;
		}
		
		select.setActionCommand("control-state:" + Render.CONTROL_STATE_SELECT);
		move.setActionCommand("control-state:" + Render.CONTROL_STATE_MOVE);
		control.setActionCommand("control-state:" + Render.CONTROL_STATE_CONTROL);
		
		select.addActionListener(this);
		move.addActionListener(this);
		control.addActionListener(this);

		// Group control state buttons
		ButtonGroup group = new ButtonGroup();
		group.add(select);
		group.add(move);
		group.add(control);

		
		// Commands
		JButton properties = new JButton("props");
		properties.setActionCommand("properties");
		properties.addActionListener(this);
		
		JButton reset = new JButton("reset");
		reset.setActionCommand("reset");
		reset.addActionListener(this);
		
		JButton snapshot = new JButton("snapshot");
		snapshot.setActionCommand("snapshot");
		snapshot.addActionListener(this);
		
		JButton rename = new JButton("rename");
		rename.setActionCommand("rename");
		rename.addActionListener(this);
		
		JButton remove = new JButton("remove");
		remove.setActionCommand("remove");
		remove.addActionListener(this);
		

		
		// Add buttons
		toolBar.add(select);
		toolBar.add(move);
		toolBar.add(control);
		
		toolBar.addSeparator();
		
		toolBar.add(reset);
		toolBar.add(snapshot);
		toolBar.add(rename);
		toolBar.add(properties);
		
		toolBar.addSeparator();
		
		toolBar.add(remove);
		
		
		// Add the tool bar
		add(toolBar, BorderLayout.PAGE_START);
		
	}
	
	
	/**
	 * Initializes the panel
	 */
	private void init(Node node, int renderType) {
		setLayout(new BorderLayout());
		
		this.xmlNode = node;
		
		// Create render
		render = Coordinator.getInstance().createRender(node, renderType);
		if (render == null) {
			logger.error("Cannot create a renderer");
			return;
		}
		
		// Create a tool bar
		createToolBar(render);
		
		// Get the canvas and set up its event listeners
		Canvas canvas = render.getCanvas();
		canvas.addKeyListener(render);
		canvas.addMouseListener(render);
		canvas.addMouseMotionListener(render);
		canvas.addMouseWheelListener(render);
		
		add(canvas, BorderLayout.CENTER);

		// Create render properties dialog
		renderDialog = new RenderProperties(render);
		renderDialog.setVisible(false);

		// Create pop-up menu
		popup = new JPopupMenu();

		JMenuItem menuItem = new JMenuItem("Properties");
		menuItem.setActionCommand("properties");
		menuItem.addActionListener(this);
		popup.add(menuItem);

		menuItem = new JMenuItem("Snapshot");
		menuItem.setActionCommand("snapshot");
		menuItem.addActionListener(this);
		popup.add(menuItem);

		popup.addSeparator();
		
		menuItem = new JMenuItem("Rename");
		menuItem.setActionCommand("rename");
		menuItem.addActionListener(this);
		popup.add(menuItem);
		
		popup.addSeparator();

		menuItem = new JMenuItem("Remove View");
		menuItem.setActionCommand("remove");
		menuItem.addActionListener(this);
		popup.add(menuItem);

		// Add listener to components that can bring up popup menus.
		MouseListener popupListener = new PopupListener();
		canvas.addMouseListener(popupListener);
	}
	
	
	/**
	 * Removes the panel and its window
	 */
	public void remove() {
		String message = "Do you want to remove the window ";
		message += win.getName();
		message += "?";
		int result = JOptionPane.showConfirmDialog(this, message, "Kill", JOptionPane.YES_NO_OPTION);
		
		if (result == JOptionPane.YES_OPTION) {
			if (Coordinator.getInstance().getWindowManager().removeWindow(win)) {
				if (xmlNode != null)
					xmlNode.getParentNode().removeChild(xmlNode);

				// TODO: clear data filter, etc.
				
				render = null;
				renderDialog.dispose();
			}
		}
	}
	

	/**
	 * Shows the pop-up menu
	 * 
	 * @author Monad
	 * 
	 */
	class PopupListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (render.getControlState() == Render.CONTROL_STATE_SELECT)
				if (e.isPopupTrigger()) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
		}
	}

	/**
	 * Process actions of the pop-up menu
	 */
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand().intern();

		try {
			// Control states
			if (cmd.startsWith("control-state:")) {
				int state = Integer.parseInt(cmd.substring("control-state:".length()));
				if (render != null)
					render.setControlState(state);
				
				return;
			}
			
			// Reset
			if (cmd == "reset") {
				if (render != null)
					render.resetCamera(true);
				return;
			}
			
			// Properties
			if (cmd == "properties") {
				renderDialog.init();
				renderDialog.setVisible(true);
				return;
			} 
			
			// Snapshot
			if (cmd == "snapshot") {
				if (render != null)
					render.takeSnapshot("");
				
				return;
			}
			
			// Remove
			if (cmd == "remove") {
				remove();
				return;
			}
			
			// Rename
			if (cmd == "rename") {
				String newName = JOptionPane.showInputDialog("Input new name", win.getName());
				if (newName != null) {
					win.setName(newName);
					render.setName(win.getName());
				}
				return;
			}
			
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.toString());
		}

	}
	
	
	/**
	 * Updates XML node
	 */
	public void updateXML(SparkWindow location, Document xmlModelDoc, Node interfaceNode, File xmlModelFile) {
		if (xmlNode == null) {
			xmlNode = xmlModelDoc.createElement("renderframe");
			interfaceNode.appendChild(xmlNode);
		}

		XmlDocUtils.addAttr(xmlModelDoc, xmlNode, "location", location.getName());
		render.writeXML(xmlModelDoc, xmlNode, xmlModelFile.getParentFile());
	}



}
