package org.spark.runtime.external.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;

import org.spark.runtime.external.Coordinator;
import org.spark.utils.XmlDocUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


public class SparkMethodPanel extends JPanel implements ActionListener, ISparkPanel {
	private static final long serialVersionUID = -8566625773910489478L;

	/**
	 * Default constructor
	 * @param node
	 */
	public SparkMethodPanel(WindowManager manager, Node node, ArrayList<String> methods) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		for (String name : methods) {
			addMethod(name);
		}
		
		String location = XmlDocUtils.getValue(node, "location", null);
		manager.setLocation(this, location);
	}
	

	/**
	 * Updates XML node
	 */
	public void updateXML(SparkWindow location, Document xmlModelDoc, Node interfaceNode, File xmlModelFile) {
		// Nothing to do here
	}




	
	private void addMethod(String name) {
		JButton button = new JButton(name);
		button.addActionListener(this);
		button.setActionCommand(name);

		this.add(button);
	}



	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		if (cmd != null) {
			Coordinator.getInstance().invokeMethod(cmd);
		}
	}
}
