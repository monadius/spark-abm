package org.spark.gui.cluster;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.spark.cluster.ClusterManager;
import org.spark.gui.IUpdatableFrame;
import org.spark.gui.render.Render;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class SlaveMainFrame extends JFrame implements IUpdatableFrame {

	private static final long serialVersionUID = 2800524835363109821L;

	private Canvas canvas;
	private Render render;
	
	private JLabel	tickLabel;

	
	public SlaveMainFrame() {
		super("Main Frame");
		
//		this.canvas = new GLCanvas();
		this.render = null;

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				ClusterManager.getInstance().getComm().finalizeMPI();
				System.exit(0);
			}
		});
		
		JPanel panel = new JPanel();
		panel.setMinimumSize(new Dimension(100, 100));
		
		tickLabel = new JLabel("");
		tickLabel.setMinimumSize(new Dimension(100, 80));
		
		panel.add(tickLabel);
		
		this.getContentPane().add(panel, BorderLayout.NORTH);
		this.pack();
	}
	
	
	void setupRender(Node node) {
		if (canvas != null) {
			this.getContentPane().remove(canvas);
		}
		
		render = SlaveModelManager.getInstance().createRender(node);
		canvas = render.getCanvas();

	    this.getContentPane().add(canvas, BorderLayout.CENTER);
	}
	
	
	
	
	
	
	public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
	}


	public void updateTick(long tick) {
		tickLabel.setText(Long.toString(tick));
	}


	public void reset() {
	}


	public void writeXML(Document doc) {
	}

	
	private boolean invoked = false;

	public void updateData(final long tick) {
		if (!invoked && canvas != null) {
			
			invoked = true;
			
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					if (canvas != null)
						render.display();

					tickLabel.setText(String.valueOf(tick));
					
					synchronized(SlaveModelManager.lock) {
						invoked = false;
						SlaveModelManager.lock.notify();
					}
				}
			});
		}
	}

	
	public void updateData() {
		if (!invoked && canvas != null) {
			
			invoked = true;
			
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					if (canvas != null)
						render.display();

					invoked = false;
				}
			});
		}
	}
	
}
