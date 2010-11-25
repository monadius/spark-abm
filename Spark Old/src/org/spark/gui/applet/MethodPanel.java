package org.spark.gui.applet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.swing.*;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class MethodPanel implements IUpdatablePanel, ActionListener {

	private JPanel panel;
	private HashMap<String, Method> methods = new HashMap<String, Method>();
	
	
	
	public MethodPanel(Node node) {
		panel = SparkApplet.getMethodPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
				
		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node methodNode = nodes.item(i);
			
			if (methodNode.getNodeName().equals("method")) {
				addMethod(methodNode);
			}
		}
		
	}


	
	private void addMethod(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		Node tmp;
		
		String name = (tmp = attributes.getNamedItem("name")) != null ? tmp.getNodeValue() : "???";
		String smethod = (tmp = attributes.getNamedItem("method")) != null ? tmp.getNodeValue() : "";
		
		Method method = null;
		
		try {
			method = AppletModelManager.getModelClass().getClass().getMethod(smethod);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		if (method != null) {
			methods.put(name + method.getName(), method);
			JButton button = new JButton(name);
			button.addActionListener(this);
			button.setActionCommand(name + method.getName());
			
			panel.add(button);
		}
	}



	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		Method method = methods.get(cmd);
		if (method != null) {
			try {
				method.invoke(AppletModelManager.getModel());
			}
			catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e);
			}
		}
	}



	public void reset() {
		// TODO Auto-generated method stub
		
	}



	public void updateData(long tick) {
		// TODO Auto-generated method stub
		
	}



	public void updateData() {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
