package org.spark.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;

import org.spark.runtime.ParameterFactory_Old;
import org.spark.runtime.Parameter_Old;
import org.spark.runtime.VariableSet;
import org.spark.runtime.VariableSetFactory;
import org.spark.utils.SpringUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ParameterPanel extends UpdatableFrame implements ActionListener {
	private static final long serialVersionUID = -1315411629669891403L;
	
	private final JPanel parameterPanel;
	private final JPanel setPanel;
	private final HashMap<String, Parameter_Old> parameters = new HashMap<String, Parameter_Old>();

	private VariableSet selectedSet = null;
	// TODO: depends on a selected set
	private boolean autoSaveFlag = false;
	
	private JComboBox setsBox;
	
	
	@SuppressWarnings("serial")
	private class SetsModel extends AbstractListModel {
		public final ArrayList<String> names = new ArrayList<String>();

		public void addElement(String name) {
			for (int i = 0; i < names.size(); i++) {
				if (names.get(i).equals(name))
					return;
			}
			
			int n = getSize();
			names.add(name);
			fireIntervalAdded(this, n, n);
		}
		
		public Object getElementAt(int index) {
			if (index < 0 || index >= names.size())
				return null;
			
			return names.get(index);
		}

		public int getSize() {
			return names.size();
		}
	}
	
	// TODO: use this model
	private final SetsModel setsModel = new SetsModel();
	

	public ParameterPanel(Node node, JFrame owner) {
		super(node, owner, "Model Parameters");
		
		// Create main panel
		JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        // Create panel for variable sets
        setPanel = new JPanel(new GridLayout(1, 4));
		setPanel.setMinimumSize(new Dimension(100, 50));
		setPanel.setBorder(BorderFactory.createTitledBorder("Parameter set"));
        
        // Create panel for parameters		
		parameterPanel = new JPanel(new SpringLayout());
		JScrollPane scroll = new JScrollPane(parameterPanel);
		
		// Add panels to the main panel
		panel.add(setPanel);
		panel.add(scroll);
		
		add(panel);
		
		// Heading
		parameterPanel.add(new JLabel("Name"));
		// TODO: find better solution
		parameterPanel.add(new JLabel("Current Value"));
		parameterPanel.add(new JLabel("Value"));
		
		loadVariableSets();
		loadParameters();
		
		SpringUtilities.makeCompactGrid(parameterPanel, 
				parameters.values().size() + 1, 3, 
				5, 5, 15, 5);
		pack();
		
		FrameLocationManager.setLocation(this, node);
	}
	
	
	private void loadVariableSets() {
		String[] tmp = VariableSetFactory.getNames();
		String[] names = new String[tmp.length + 1];
		
		names[0] = "none ";
		for (int i = 0; i < tmp.length; i++)
			names[i + 1] = tmp[i];
		
		JButton newButton = new JButton("New");
		newButton.setActionCommand("new-set");
		newButton.addActionListener(this);
		
		setsBox = new JComboBox(names);
		setsBox.setActionCommand("set");
		setsBox.setSelectedIndex(0);
		setsBox.addActionListener(this);
		
		JCheckBox autoBox = new JCheckBox("Auto save");
		autoBox.setSelected(autoSaveFlag);
		autoBox.setActionCommand("auto-set");
		autoBox.addActionListener(this);
		
		JButton saveButton = new JButton("Save");
		saveButton.setActionCommand("save-set");
		saveButton.addActionListener(this);
		
		setPanel.add(newButton);
		setPanel.add(setsBox);
		setPanel.add(autoBox);
		setPanel.add(saveButton);
		
		// Load selected set from xml
		selectedSet = null;
	}
	
	
	private void loadParameters() {
		Parameter_Old[] pars = ParameterFactory_Old.getParameters();
		
		for (int i = 0; i < pars.length; i++)
			addParameter(pars[i]);
	}
	
	
	public int getParametersNumber() {
		return parameters.size();
	}
	
	

	private void addParameter(Parameter_Old p) {
//		Parameter p = Parameter.createParameter(node);
		
		JLabel lname = new JLabel(p.getName());
		
		parameterPanel.add(lname);
		parameterPanel.add(p.getLabel());
		parameterPanel.add(p.getWidget());
		
		parameters.put(p.getName(), p);
	}	
	
	
	
	
	

	@Override
	public synchronized void reset() {
	}
	
	
	public synchronized void saveParameters(PrintStream out) {
		out.println("\"Parameters\"");
		
		int n = parameters.values().size();
		for (Parameter_Old p : parameters.values()) {
			out.print(p.getName());
			n -= 1;
			if (n > 0)
				out.print(',');
		}
		
		out.println();
		n = parameters.values().size();
		for (Parameter_Old p : parameters.values()) {
			out.print(p.getValue());
			n -= 1;
			if (n > 0)
				out.print(',');
		}
		
		out.println();
	}
	
	
	
	/**
	 * Returns a list of children with the specified name
	 * @param name
	 * @return
	 */
	private ArrayList<Node> getChildrenByTagName(Node node, String name) {
		ArrayList<Node> list = new ArrayList<Node>();

		for (Node child = node.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			if (child.getNodeName().equals(name))
				list.add(child);
		}

		return list;
	}

	/**
	 * Removes all children nodes with the specified name of the given node
	 * @param node
	 */
	private void removeChildren(Node node, String name) {
		for (Node item : getChildrenByTagName(node, name)) {
			node.removeChild(item);
		}
	}
	
	
	@Override
	public void writeXML(Document doc) {
		super.writeXML(doc);
		
		if (autoSaveFlag && selectedSet != null)
			selectedSet.saveVariablesIntoSet();
		
		Node root = null;
		NodeList list = doc.getElementsByTagName("variable-sets");
	
		if (list.getLength() > 0) {
			root = list.item(0);
		}
		else {
			root = doc.createElement("variable-sets");
			doc.getFirstChild().appendChild(root);
		}
		
		removeChildren(root, "variable-set");
		removeChildren(root, "#text");

		VariableSetFactory.saveXML(doc, root);
	}


	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand().intern();
		
		// 'new-set' command
		if (cmd == "new-set") {
			String setName = JOptionPane.showInputDialog("Input set name");
			if (setName == null || setName.trim().equals(""))
				return;
			
			setName = setName.trim();

			// Do not change the existent sets
			String[] names = VariableSetFactory.getNames();
			for (int i = 0; i < names.length; i++) {
				if (setName.equals(names[i]))
					return;
			}
			
			VariableSet newSet = VariableSetFactory.getVariableSet(setName);
			if (newSet == null)
				return;
			
			newSet.saveVariablesIntoSet();

			setsBox.addItem(setName);
			
			// Select the new set
			setsBox.setSelectedItem(setName);
			return;
		}
		
		// 'save-set' command
		if (cmd == "save-set") {
			if (selectedSet == null)
				return;
			
			selectedSet.saveVariablesIntoSet();
			return;
		}
		
		// 'auto-set' command
		if (cmd == "auto-set") {
			JCheckBox auto = (JCheckBox) e.getSource();
			autoSaveFlag = auto.isSelected();
			return;
		}
		
		// 'set' command
		if (cmd == "set") {
			JComboBox box = (JComboBox) e.getSource();
			
			String setName = (String) box.getSelectedItem();
			
			if (selectedSet != null) {
				if (autoSaveFlag)
					selectedSet.saveVariablesIntoSet();
			}
			
			// TODO: what will happen if a user creates 
			// a set with the name "Default"?
			if ("none ".equals(setName)) {
				// TODO: implement
				// ParameterFactory.loadDefaultValues();
				selectedSet = null;

				return;
			}
			
			selectedSet = VariableSetFactory.getVariableSet(setName);
			if (selectedSet == null)
				return;
			
			try {
				selectedSet.loadVariablesFromSet();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			
			GUIModelManager.getInstance().requestUpdate();
			return;
		}
		
	}

}
