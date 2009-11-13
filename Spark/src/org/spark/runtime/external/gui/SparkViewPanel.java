package org.spark.runtime.external.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.gui.dialogs.RenderProperties;
import org.spark.runtime.external.render.Render;
import org.spark.utils.XmlDocUtils;
import org.w3c.dom.Node;

import javax.swing.*;

/**
 * View panel
 * 
 * @author Monad
 */
@SuppressWarnings("serial")
public class SparkViewPanel extends JPanel implements ISparkPanel,
		ActionListener {
	/* Render for this view */
	private Render render;

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
		manager.setLocation(this, location);
	}
	
	
	/**
	 * Creates a view panel in the given window
	 * @param win
	 * @param renderType
	 */
	public SparkViewPanel(SparkWindow win, int renderType) {
		init(null, renderType);
		win.addPanel(this);
	}
	
	
	/**
	 * Initializes the panel
	 */
	private void init(Node node, int renderType) {
		setLayout(new BorderLayout());
		
		// Create render
		render = Coordinator.getInstance().createRender(node, renderType);
		Canvas canvas = render.getCanvas();
		canvas.addKeyListener(render);
		

		add(canvas, BorderLayout.CENTER);

		// Create render properties dialog
		renderDialog = new RenderProperties(render, true);
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

		menuItem = new JMenuItem("Remove View");
		menuItem.setActionCommand("remove");
		menuItem.addActionListener(this);
		popup.add(menuItem);

		// Add listener to components that can bring up popup menus.
		MouseListener popupListener = new PopupListener();
		canvas.addMouseListener(popupListener);
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
			if (cmd == "properties") {
				renderDialog.init();
				renderDialog.setVisible(true);
			} else if (cmd == "snapshot") {
				if (render != null)
					render.takeSnapshot();
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.toString());
		}

	}

}
