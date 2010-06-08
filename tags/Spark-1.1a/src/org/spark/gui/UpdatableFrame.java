package org.spark.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;


public abstract class UpdatableFrame extends JDialog implements IUpdatableFrame {
	protected Node node;
	
	private static final long serialVersionUID = 1L;
	
	protected int id;

	public UpdatableFrame(Node node) {
		this(node, null, "");
	}
	
	public UpdatableFrame(Node node, JFrame owner, String title) {
		super(owner, title);
		this.node = node;

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				GUIModelManager.getInstance().hideFrame(id);
			}
		});
	}
	
	
	public void writeXML(Document doc) {
		FrameLocationManager.saveLocationChanges(doc, this, node);
	}
	

	public void updateData(long tick) {
	}
	
	
	public synchronized void reset() {
	}
	
	public void updateData() {
	}
	

	
	public void setId(int id) {
		this.id = id;
	}
	
	
	public static void setFrameVisibility(ArrayList<UpdatableFrame> frames, int id, boolean visible) {
		for (UpdatableFrame frame : frames) {
			if (frame.id == id) {
				frame.setVisible(visible);
				break;
			}
		}
	}
}
