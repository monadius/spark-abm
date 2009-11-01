package org.spark.runtime.external.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.spark.gui.FrameLocationManager;
import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.gui.dialogs.RenderProperties;
import org.spark.runtime.external.render.Render;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.swing.*;

public class RenderFrame extends JDialog implements ActionListener {

	private static final long serialVersionUID = 9027694192580609463L;

	private String name;
	
	private Render render;
//	private Render render;
	private JPopupMenu popup;
	
	// TODO: what happens when the render window is destroyed
	private RenderProperties renderDialog;
	
	
	public Render getRender() {
		return render;
	}
	
	public RenderFrame(Node node, JFrame owner, int renderType) {
		super(owner, "");
		
		// Get name
		NamedNodeMap attrs = node.getAttributes();
		Node tmp;
		
		name = (tmp = attrs.getNamedItem("name")) != null ? tmp.getNodeValue() : "View";
		setTitle(name);

		// Create render
		render = Coordinator.getInstance().createRender(node, renderType);
		Canvas canvas = render.getCanvas();
		canvas.addKeyListener(render);
		
		render.setName(name);
		
		getContentPane().add(canvas, BorderLayout.CENTER);

		// Set location
		FrameLocationManager.setLocation(this, node);
		
		// Create render properties dialog
		renderDialog = new RenderProperties(this, render, true);
		renderDialog.setVisible(false);
	    
		// Create pop-up menu
		popup = new JPopupMenu();

		JMenuItem menuItem = new JMenuItem("Properties");
	    menuItem.addActionListener(this);
	    popup.add(menuItem);

		menuItem = new JMenuItem("Snapshot");
		menuItem.addActionListener(this);
		popup.add(menuItem);

		popup.addSeparator();
	    
	    menuItem = new JMenuItem("Remove View");
	    menuItem.addActionListener(this);
	    popup.add(menuItem);

	    // Add listener to components that can bring up popup menus.
	    MouseListener popupListener = new PopupListener();
	    canvas.addMouseListener(popupListener);
	    
		this.addWindowFocusListener(new WindowAdapter() {
		    public void windowGainedFocus(WindowEvent e) {
		    	if (render.getCanvas() != null) {
		    		render.getCanvas().requestFocusInWindow();
		    	}
		    }
		});
	}
	
	
	class PopupListener extends MouseAdapter {
	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	            popup.show(e.getComponent(),
	                       e.getX(), e.getY());
	        }
	    }
	}
	
	
	

	public void actionPerformed(ActionEvent arg0) {
		Object src = arg0.getSource();
		
		try {
			if (src instanceof JMenuItem) {
				JMenuItem item = (JMenuItem) src;
				String name = item.getText();

				if (name.equals("Properties")) {
					renderDialog.init();
					renderDialog.setVisible(true);
				}
				else if (name.equals("Snapshot")) {
					if (render != null)
						render.takeSnapshot();
				}
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.toString());
		}
		
	}

	
	
}
