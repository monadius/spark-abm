package org.spark.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.spark.gui.dialogs.RenderProperties;
import org.spark.gui.render.Render;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.swing.*;

public class RenderFrame extends UpdatableFrame implements ActionListener {

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
		super(node, owner, "");
		
		// Set id to -1 in order to prevent any window menu updates
		// before this frame will be added to that menu
		// (-1 is not a valid id for window menu)
		id = -1;
		
		// Get name
		NamedNodeMap attrs = node.getAttributes();
		Node tmp;
		
		name = (tmp = attrs.getNamedItem("name")) != null ? tmp.getNodeValue() : "View";
		setTitle(name);

		// Create render
		render = GUIModelManager.getInstance().createRender(node, renderType);
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
	

	private boolean invoked = false;
	
	@Override
	public void updateData(final long tick) {
		if (this.isVisible() && !invoked) {
			invoked = true;
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					if (render != null) {
						render.setTick(tick);
						render.display();
					}
					
					invoked = false;
				}
			});
		}
	}
	
	
	@Override
	public void updateData() {
		updateData(0);
	}
	
	
	@Override
	public void setTitle(String title) {
		super.setTitle(title);
		this.name = title;
		if (render != null)
			render.setName(title);
		GUIModelManager.getInstance().updateUpdatableFrame(this);
	}

	
	
	/**
	 * Destroys the frame
	 */
	private void removeView() {
		render = null;
		if (renderDialog != null) {
			renderDialog.dispose();
			renderDialog = null;
		}
		
		if (node != null) {
			Document doc = ModelManager.getInstance().getXmlDocument();
			if (doc != null) {
				doc.getFirstChild().removeChild(node);
			}
			
			node = null;
		}
		
		ModelManager.getInstance().removeUpdatableFrame(this);
		this.dispose();
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
				else if (name.equals("Remove View")) {
					removeView();
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

	
	
	@Override
	public void writeXML(Document doc) {
		super.writeXML(doc);
		
		if (name != null) {
			Node attr = doc.createAttribute("name");
			attr.setNodeValue(name);
			node.getAttributes().setNamedItem(attr);
		}
		
		if (render != null)
			render.writeXML(doc, this.node);
	}
}
