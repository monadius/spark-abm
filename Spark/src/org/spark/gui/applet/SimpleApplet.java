package org.spark.gui.applet;

import javax.swing.JApplet;
import javax.swing.JPanel;


public class SimpleApplet extends JApplet {

	private static final long serialVersionUID = 1L;
	
	
	public void init() {
		JPanel panel = new JPanel();
		new MainPanel(panel);
		this.add(panel);
	}
	
	
	public void start() {
		
	}
	
	
	public void stop() {
		
	}
	
	
	public void destroy() {
		
	}


}
