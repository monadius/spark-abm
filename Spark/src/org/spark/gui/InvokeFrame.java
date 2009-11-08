package org.spark.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.*;

import org.spark.core.SparkModel;
import org.spark.runtime.internal.ModelMethod;
import org.w3c.dom.Node;


public class InvokeFrame extends UpdatableFrame implements ActionListener {

	private static final long serialVersionUID = -8566625773910489478L;

	private JPanel panel;
	private final HashMap<String, ModelMethod> methods = new HashMap<String, ModelMethod>();
	
	
	public InvokeFrame(SparkModel model, Node node, JFrame owner) {
		super(node, owner, "Methods");
		
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
				
//		NodeList nodes = node.getChildNodes();
/*		for (int i = 0; i < nodes.getLength(); i++) {
			Node methodNode = nodes.item(i);
			
			if (methodNode.getNodeName().equals("method")) {
				addMethod(model, methodNode);
			}
		}*/
		ModelMethod[] methods = model.getMethods();
		for (int i = 0; i < methods.length; i++) {
			addMethod(methods[i]);
		}
		
		this.add(panel);
		pack();
		
		FrameLocationManager.setLocation(this, node);
	}


	
	private void addMethod(ModelMethod method) {
		if (method != null) {
			methods.put(method.getName(), method);
			JButton button = new JButton(method.getName());
			button.addActionListener(this);
			button.setActionCommand(method.getName());
			
			panel.add(button);
		}
	}



	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		ModelMethod method = methods.get(cmd);
		if (method != null) {
			method.invoke();
		}
	}
}
