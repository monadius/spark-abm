package org.spark.runtime.external.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.gui.menu.SparkMenu;
import org.spark.runtime.external.gui.menu.Swing_SparkMenu;

import com.spinn3r.log5j.Logger;

/**
 * Swing implementation of the SparkWindow class
 * @author Monad
 *
 */
public class Swing_SparkWindow extends SparkWindow {
	private static final Logger logger = Logger.getLogger();
	
	/* References to Swing objects */
	private JFrame frame;
	private JDialog dialog;
	private Window window;
	
	/* Component inside the window */
	private JComponent component;
	
	/**
	 * Protected constructor
	 * @param owner
	 */
	protected Swing_SparkWindow(SparkWindow owner) {
		super(owner);
		
		if (owner == null) {
			frame = new JFrame();
			window = frame;
			// TODO: stop the simulation and save GUI changes first
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					Coordinator.dispose();
					System.exit(0);
				}
			});
			
			frame.setPreferredSize(new Dimension(500, 600));
			frame.setMinimumSize(new Dimension(200, 200));
			frame.pack();
		}
		else {
			if (!(owner instanceof Swing_SparkWindow)) {
				logger.error("An attempt to create a Swing window owned by a non-Swing window");
				frame = new JFrame();
				window = frame;
				frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			}
			else {
				Swing_SparkWindow o = (Swing_SparkWindow) owner;
				if (o.frame != null)
					dialog = new JDialog(o.frame);
				else
					dialog = new JDialog(o.dialog);
				
				window = dialog;
				dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
				
				dialog.addComponentListener(new ComponentAdapter() {
					public void componentShown(ComponentEvent e) {
						onVisibilityChanged();
					}

					public void componentHidden(ComponentEvent e) {
						onVisibilityChanged();
					}
				});
			}
		}
	}
	

	@Override
	public Rectangle getLocation() {
		int x = window.getX();
		int y = window.getY();
		int w = window.getWidth();
		int h = window.getHeight();
		
		return new Rectangle(x, y, w, h);
	}
	

	@Override
	public boolean isVisible() {
		return window.isVisible();
	}
	

	@Override
	public void setLocation(int x, int y, int width, int height) {
		window.setBounds(x, y, width, height);
		window.setPreferredSize(new Dimension(width, height));
	}
	

	@Override
	public void setMenu(SparkMenu menu) {
		// Menu can be defined only for frames
		if (frame == null)
			return;
		
		if (!(menu instanceof Swing_SparkMenu))
			return;

		Swing_SparkMenu m = (Swing_SparkMenu) menu;
		JMenuBar menuBar = new JMenuBar();
		m.addToMenuBar(menuBar);
		
		frame.setJMenuBar(menuBar);
		frame.pack();
	}
	

	@Override
	public void setVisible(boolean visible) {
		window.setVisible(visible);
	}
	
	
	@Override
	public void dispose() {
		window.dispose();
	}
	
	
	@Override
	public void setName(String name) {
		super.setName(name);
		if (frame != null)
			frame.setTitle(getName());
		else
			dialog.setTitle(getName());
	}


	@Override
	public boolean addPanel0(ISparkPanel panel) {
		if (panel instanceof JComponent) {
			Container c;
			
			if (frame != null)
				c = frame.getContentPane();
			else
				c = dialog.getContentPane();

			if (component != null)
				c.remove(component);
			
			component = (JComponent) panel;
			c.add(component, BorderLayout.CENTER);
			window.pack();
			
			return true;
		}
		else {
			logger.error("Cannot add the panel: " + panel);
		}
		
		return false;
	}


	@Override
	public boolean addPanel0(ISparkPanel panel, String location) {
		if (panel instanceof JComponent) {
			Container c;
			
			if (frame != null)
				c = frame.getContentPane();
			else
				c = dialog.getContentPane();
			
			c.add((JComponent) panel, location);
			window.pack();
			
			return true;
		}
		else {
			logger.error("Cannot add the panel: " + panel);
		}
		
		return false;
	}
	
	
	@Override
	public Dimension getPreferredSize() {
		return window.getPreferredSize();
	}

}
