package org.spark.gui.dialogs;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * About dialog
 * @author Monad
 */
@SuppressWarnings("serial")
public class AboutDialog extends JDialog {
	private Image logo;
	private JPanel panel;
	
	private int w, h;
	
	public AboutDialog(JFrame owner) {
		super(owner, "About SPARK", true);
		
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		
		try {
			logo = ImageIO.read(new File("logo.jpg"));
			
			if (logo != null) {
				w = logo.getWidth(null);
				h = logo.getHeight(null);
			}
		}
		catch (Exception e) {
			logo = null;
		}

		if (w < 1) w = 300;
		if (h < 1) h = 200;
		
		panel = new JPanel();
		
		Dimension dim = new Dimension(w, h);
		panel.setMinimumSize(dim);
		panel.setMaximumSize(dim);
		panel.setPreferredSize(dim);
		
		add(panel);
		pack();
		
		setVisible(false);
	}
	
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		if (logo != null) {
			Graphics g2 = panel.getGraphics();
			g2.drawImage(logo, 0, 0, w, h, null);
			g2.dispose();
		}
	}
}
