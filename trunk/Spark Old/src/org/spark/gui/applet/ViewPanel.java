package org.spark.gui.applet;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.spark.gui.render.Render;
import org.w3c.dom.Node;

import javax.swing.*;

public class ViewPanel extends JPanel implements IUpdatablePanel, ActionListener {

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
	
	public ViewPanel(Node node, int renderType) {

		// Create render
		render = AppletModelManager.getInstance().createRender(node, renderType);
		Canvas canvas = render.getCanvas();
		
		render.setName(name);
		
		canvas.setPreferredSize(new Dimension(300, 300));
		canvas.setMinimumSize(new Dimension(100, 100));
		this.add(canvas, BorderLayout.CENTER);

		// Create render properties dialog
		renderDialog = new RenderProperties(render);
		renderDialog.setVisible(false);
	    
		// Create pop-up menu
		popup = new JPopupMenu();

		JMenuItem menuItem = new JMenuItem("Properties");
	    menuItem.addActionListener(this);
	    popup.add(menuItem);


	    // Add listener to components that can bring up popup menus.
	    MouseListener popupListener = new PopupListener();
	    canvas.addMouseListener(popupListener);
	    
	    this.setMinimumSize(new Dimension(100, 100));
	    this.setPreferredSize(new Dimension(200, 200));
	    SparkApplet.getViewPanel().add(this);
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
	

	public void updateData(final long tick) {
		if (render != null && !invoked) {
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
	
	
	public void updateData() {
		updateData(0);
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
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.toString());
		}
		
	}

	public void reset() {
		// TODO Auto-generated method stub
		
	}

}
