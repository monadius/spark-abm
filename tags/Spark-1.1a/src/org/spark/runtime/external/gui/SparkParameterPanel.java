package org.spark.runtime.external.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;

import javax.swing.*;

import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.Parameter;
import org.spark.runtime.external.VariableSet;
import org.spark.runtime.external.VariableSetFactory;
import org.spark.utils.SpringUtilities;
import org.spark.utils.XmlDocUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * Parameter panel in SPARK
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class SparkParameterPanel extends JPanel implements ActionListener, ISparkPanel {
	private final JPanel parameterPanel;
	private final JPanel setPanel;
	private final HashMap<String, Parameter> parameters = new HashMap<String, Parameter>();

	private VariableSet selectedSet = null;
	// TODO: depends on a selected set
	private boolean autoSaveFlag = false;
	
	private JComboBox setsBox;
	
	
/*	private class SetsModel extends AbstractListModel {
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
*/	

	/**
	 * Default constructor
	 * @param manager
	 * @param node
	 */
	public SparkParameterPanel(WindowManager manager, Node node) {
		// Set layout
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        // Create panel for variable sets
        setPanel = new JPanel(new GridLayout(1, 4));
		setPanel.setMinimumSize(new Dimension(100, 50));
		setPanel.setBorder(BorderFactory.createTitledBorder("Parameter set"));
        
        // Create panel for parameters		
		parameterPanel = new JPanel(new SpringLayout());
		JScrollPane scroll = new JScrollPane(parameterPanel);
		
		// Add panels to the main panel
		add(setPanel);
		add(scroll);
		
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
		
		String location = XmlDocUtils.getValue(node, "location", null);
		manager.setLocation(this, location);
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
		Parameter[] pars = Coordinator.getInstance().getParameters().getParameters();
		
		for (int i = 0; i < pars.length; i++)
			addParameter(pars[i]);
	}
	
	
	public int getParametersNumber() {
		return parameters.size();
	}
	
	

	private void addParameter(Parameter p) {
//		Parameter p = Parameter.createParameter(node);
		
		JLabel lname = new JLabel(p.getName());
		
		parameterPanel.add(lname);
		parameterPanel.add(p.getLabel());
		parameterPanel.add(p.getWidget());
		
		parameters.put(p.getName(), p);
	}
	

	/**
	 * Updates the associated xml node
	 */
	public void updateXML(SparkWindow location, Document xmlModelDoc, Node interfaceNode, File xmlModelFile) {
		if (autoSaveFlag && selectedSet != null)
			selectedSet.saveVariablesIntoSet();
		
		Node root = null;
		root = XmlDocUtils.getChildByTagName(interfaceNode, "variable-sets");

		if (root == null) {
			root = xmlModelDoc.createElement("variable-sets");
			interfaceNode.appendChild(root);
		}

		XmlDocUtils.removeChildren(root, "variable-set");
		XmlDocUtils.removeChildren(root, "#text");

		VariableSetFactory.saveXML(xmlModelDoc, root);
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
			
//			GUIModelManager.getInstance().requestUpdate();
			return;
		}
		
	}

}
