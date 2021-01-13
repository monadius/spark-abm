package org.spark.runtime.external.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import org.spark.runtime.external.render.DataLayerGraphics;
import org.spark.runtime.external.render.SpaceStyle;
import org.spark.runtime.external.render.DataLayerStyle;
import org.spark.runtime.external.render.AgentStyle;
import org.spark.runtime.external.render.Render;
import org.sparkabm.utils.SpringUtilities;


/**
 * A dialog for setting up visualization properties
 * @author Monad
 *
 */
public class RenderProperties extends JDialog implements ActionListener, ChangeListener {
	private static final long serialVersionUID = -4770465039114801520L;

	/* Render for this dialog */
	private Render render;
	
	/* Main panels */
	private JPanel panel, spacePanel, dataPanel;
	
	
	/* Space controls */
	private JCheckBox swapSpaceXY, spaceAutoSize;
	private JSpinner spaceCellXSize, spaceCellYSize;

	
	/* Agent controls */
	
	// Data for agent styles
	private AgentTableData agentData;
	// Table for agent styles
	private JTable agentTable;
	
	
	/* Data layer controls */
	
	// Controls for data layers
	private ArrayList<DataLayerControl> dataLayerControls;	

	
	// Data for agent styles
	@SuppressWarnings("serial")
	private class AgentTableData extends AbstractTableModel {
		// Styles of all agents
		private ArrayList<AgentStyle> styles;

		// Default constructor
		public AgentTableData() {
		}
		
		
		public AgentStyle get(int i) {
			if (styles == null || i < 0 || i >= styles.size())
				return null;
			return styles.get(i);
		}
		
		
		// Updates the agent styles
		public void update(ArrayList<AgentStyle> styles) {
			this.styles = styles;
			fireTableDataChanged();
		}
		

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public int getRowCount() {
			if (styles == null)
				return 0;
			return styles.size(); 
		}
		
		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "Name";
			case 1:
				return "Visible";
			case 2:
				return "Border";
			case 3:
				return "Transparent";
			case 4:
				return "Label";
			}
			
			return null;
		}
		
		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0)
				return String.class;
			else
				return Boolean.class;
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return false;
			else
				return true;
		}
		

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (styles == null || rowIndex >= styles.size())
				return null;
			
			AgentStyle style = styles.get(rowIndex);
			
			switch (columnIndex) {
			// Name
			case 0:
				return style.name;
			// Visible
			case 1:
				return style.visible;
			// Border
			case 2:
				return style.border;
			// Transparent
			case 3:
				return style.transparent;
			// Label
			case 4:
				return style.label;
			}
			
			return null;
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			AgentStyle style = get(row);
			if (style == null)
				return;
			
			switch (col) {
			case 0:
				// Name
				return;
			case 1:
				// Visible
				style.visible = (Boolean) value;
				render.updateDataFilter();
				return;
			case 2:
				// Border
				style.border = (Boolean) value;
				break;
			case 3:
				// Transparent
				style.transparent = (Boolean) value;
				break;
			case 4:
				style.label = (Boolean) value;
				break;
				
			default:
				return;
			}
			
			render.update();
		}

	}
	
	
	// Control elements for a data layer
	private static class DataLayerControl {
		public final DataLayerStyle style;		
		public final JRadioButton selector;
		public final JSpinner colorWeight;
		public final JSpinner heightWeight;
		
		public DataLayerControl(DataLayerStyle style, 
					JRadioButton selector, JSpinner spinnerColor, JSpinner spinnerHeight) {
			this.style = style;
			this.selector = selector;
			this.colorWeight = spinnerColor;
			this.heightWeight = spinnerHeight;
		}
		
		public void setEnabled(boolean flag) {
			colorWeight.setEnabled(flag);
			heightWeight.setEnabled(flag);
		}
	}
	
	
	/**
	 * Default constructor
	 * @param render
	 * @param editTitle
	 */
	public RenderProperties(Render render) {
		init(render);
	}
	

	/**
	 * Initializes the dialog during its creation process
	 * @param render
	 * @param editTitle
	 */
	private void init(Render render) {
		this.render = render;
		
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

		///////////////////////
		// Initialize agents
		JPanel agentPanel = new JPanel(new BorderLayout());
		agentPanel.setPreferredSize(new Dimension(500, 300));
		agentPanel.setBorder(BorderFactory.createTitledBorder("Agents"));
		
		// Create a table for agent styles
		agentData = new AgentTableData();
		agentTable = new JTable(agentData);
		agentTable.setColumnSelectionAllowed(false);
		JScrollPane tablePane = new JScrollPane(agentTable);
		agentPanel.add(tablePane);
		
		// Create a tool bar for agents
		JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
		
		JButton up = new JButton("Up");
		JButton down = new JButton("Down");
		JButton advanced = new JButton("Advanced");

		up.setActionCommand("agent-up");
		down.setActionCommand("agent-down");
		advanced.setActionCommand("agent-advanced");
		
		up.addActionListener(this);
		down.addActionListener(this);
		advanced.addActionListener(this);
		
		toolBar.add(up);
		toolBar.add(down);
		toolBar.add(advanced);
		
		agentPanel.add(toolBar, BorderLayout.EAST);
		
		
//		agentPanel.add(scrollPane);
		
		////////////////////////////
		// Initialize data layers
		dataPanel = new JPanel(new SpringLayout());
		dataPanel.setMinimumSize(new Dimension(100, 100));
		dataPanel.setBorder(BorderFactory.createTitledBorder("Data Layers"));
		JScrollPane dataPane = new JScrollPane(dataPanel);
		

		/////////////////////////
		// Initialize spaces
		spacePanel = new JPanel(new GridLayout(0, 3));
		spacePanel.setMinimumSize(new Dimension(100, 100));
		spacePanel.setBorder(BorderFactory.createTitledBorder("Spaces"));

		
		// Initialize the main panel
		panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        panel.add(agentPanel);
        panel.add(spacePanel);
        panel.add(dataPane);
        
		this.add(panel);
		this.pack();
	}
	
	
	/**
	 * Initializes controls for data layers
	 */
	private void initDataLayers() {
		dataLayerControls = new ArrayList<DataLayerControl>();
		dataPanel.removeAll();

		// Get all available data layer styles
		HashMap<String, DataLayerStyle> dataLayerStyles = render.getDataLayerStyles();
		if (dataLayerStyles == null) 
			return;

		///////////////////////////////////
		// Create controls
		ButtonGroup group = new ButtonGroup();
		
		int i = 0;
		for (DataLayerStyle style : dataLayerStyles.values()) {
			// Create a selector
			JRadioButton selector = new JRadioButton(style.getName());
			group.add(selector);
			selector.setActionCommand("data-selector" + i);

			// Create a color spinner
			JSpinner spinnerColor = new JSpinner(new SpinnerNumberModel(0, 0, 10, 0.1));
			spinnerColor.setName("data-color" + i);
			
			// Create a height spinner
			JSpinner spinnerHeight = new JSpinner(new SpinnerNumberModel(0, 0, 10, 0.1));
			spinnerHeight.setName("data-height" + i);
			
			// Create a control
			DataLayerControl ctrl = new DataLayerControl(style, selector, spinnerColor, spinnerHeight);
			dataLayerControls.add(ctrl);
			
			i += 1;
		}
		
		// Create special controls
		JRadioButton buttonNone = new JRadioButton("(none)");
		group.add(buttonNone);
		buttonNone.setActionCommand("data-none");
		
		JRadioButton buttonSpecial = new JRadioButton("(special)");
		group.add(buttonSpecial);
		buttonSpecial.setActionCommand("data-special");
		
		
		////////////////////////////////////////////
		// Initialize values of controls
		DataLayerGraphics dataGraphics = render.getCurrentDataLayerGraphics();
		
		if (dataGraphics == null) {
			buttonNone.setSelected(true);
		}
		else {
			ArrayList<DataLayerGraphics.DataLayerInfo> descriptors = dataGraphics.getDescriptors();
			
			for (i = 0; i < descriptors.size(); i++) {
				DataLayerStyle style = descriptors.get(i).dataLayerStyle;
				double colorWeight = descriptors.get(i).colorWeight;
				double heightWeight = descriptors.get(i).heightWeight;
				
				// Find the corresponding control
				for (DataLayerControl ctrl : dataLayerControls) {
					if (ctrl.style == style) {
						ctrl.colorWeight.setValue(colorWeight);
						ctrl.heightWeight.setValue(heightWeight);
						break;
					}
				}
			}

			// Set the selection
			boolean selected = false;
			if (!dataGraphics.isSpecial() && descriptors.size() == 1) {
				DataLayerStyle style = descriptors.get(0).dataLayerStyle;
				for (DataLayerControl ctrl : dataLayerControls) {
					if (ctrl.style == style) {
						selected = true;
						ctrl.selector.setSelected(true);
						break;
					}
				}
			}
			
			if (dataGraphics.isSpecial()) {
				selected = true;
				buttonSpecial.setSelected(true);
			}
			
			if (!selected)
				buttonNone.setSelected(true);
		}
		
		
		///////////////////////////////////////////
		// Initialize actions
		for (DataLayerControl ctrl : dataLayerControls) {
			boolean activeFlag = buttonSpecial.isSelected();
			
			ctrl.selector.addActionListener(this);
			ctrl.colorWeight.addChangeListener(this);
			ctrl.heightWeight.addChangeListener(this);
			
			ctrl.colorWeight.setEnabled(activeFlag);
			ctrl.heightWeight.setEnabled(activeFlag);
		}
		
		buttonNone.addActionListener(this);
		buttonSpecial.addActionListener(this);
		

		///////////////////////////////////////////
		// Put the controls on the data panel
		// Headers
		dataPanel.add(new JLabel("Name"));
		dataPanel.add(new JLabel("Color Weight"));
		dataPanel.add(new JLabel("Height Weight"));
		
		// The first row
		dataPanel.add(buttonNone);
		dataPanel.add(new JLabel(""));
		dataPanel.add(new JLabel(""));
		
		// Main rows
		for (DataLayerControl ctrl : dataLayerControls) {
			dataPanel.add(ctrl.selector);
			dataPanel.add(ctrl.colorWeight);
			dataPanel.add(ctrl.heightWeight);
		}
		
		// The last row
		dataPanel.add(buttonSpecial);
		dataPanel.add(new JLabel(""));
		dataPanel.add(new JLabel(""));
		
		SpringUtilities.makeCompactGrid(dataPanel, 3 + dataLayerControls.size(), 3, 
				0, 0, 10, 5);
	}
	
	
	/**
	 * Initializes controls for agents
	 */
	private void initAgents() {
		ArrayList<AgentStyle> styles = render.getAgentStyles();
		agentData.update(styles);
	}
	
	
	/**
	 * Initializes controls for spaces
	 */
	private void initSpaces() {
		// Remove all components
		spacePanel.removeAll();
		
		// Get the selected space and names of all spaces
		SpaceStyle selectedSpace = render.getSelectedSpaceStyle();
		String[] spaceNames = render.getSpaceNames();
		
		if (spaceNames == null)
			spaceNames = new String[0];
		
		// Create the space selection control
		JComboBox spaceList = new JComboBox(spaceNames);

		if (selectedSpace != null && selectedSpace.name != null) {
			for (int i = 0; i < spaceNames.length; i++) {
				if (selectedSpace.name.equals(spaceNames[i])) {
					spaceList.setSelectedIndex(i);
					break;
				}
			}
		}
		
		spaceList.setActionCommand("space-list");
		spaceList.addActionListener(this);
	
		// Create the swap-xy control
		boolean swapXYflag = (selectedSpace != null) ? selectedSpace.swapXY : false;
		
		swapSpaceXY = new JCheckBox("Rotate", swapXYflag);
		swapSpaceXY.setActionCommand("space-swapXY");
		swapSpaceXY.addActionListener(this);
		
		
		// Create the controls for changing space cell sizes
		
		// Auto size
		boolean autoSizeFlag = (selectedSpace != null) ? selectedSpace.autoSize : true;
		spaceAutoSize = new JCheckBox("Auto size", autoSizeFlag);
		spaceAutoSize.setActionCommand("space-auto-size");
		spaceAutoSize.addActionListener(this);
		
		// Cell sizes
		int xSize = (selectedSpace != null) ? selectedSpace.cellXSize : 10;
		int ySize = (selectedSpace != null) ? selectedSpace.cellYSize : 10;
		
		spaceCellXSize = new JSpinner(new SpinnerNumberModel(xSize, 1, 512, 1));
		spaceCellYSize = new JSpinner(new SpinnerNumberModel(ySize, 1, 512, 1));
		
		spaceCellXSize.setName("space-cell-xsize");
		spaceCellYSize.setName("space-cell-ysize");
		
		spaceCellXSize.addChangeListener(this);
		spaceCellYSize.addChangeListener(this);
		
		spaceCellXSize.setEnabled(!autoSizeFlag);
		spaceCellYSize.setEnabled(!autoSizeFlag);
		
		
		// Add all components to the panel
		spacePanel.add(spaceList);
		spacePanel.add(swapSpaceXY);
		spacePanel.add(new JLabel());
		
		spacePanel.add(spaceAutoSize);
		spacePanel.add(spaceCellXSize);
		spacePanel.add(spaceCellYSize);
	}
	
	
	/**
	 * Initializes all controls
	 */
	public void init() {
		initSpaces();
		initDataLayers();
		initAgents();

		this.pack();
	}
	
	
	/**
	 * Creates a data layer graphics description
	 * @return
	 */
	private DataLayerGraphics createDataLayerGraphics() {
		DataLayerGraphics dlg = new DataLayerGraphics();

		for (DataLayerControl ctrl : dataLayerControls) {
			double color = ((Number) ctrl.colorWeight.getValue()).doubleValue();
			double height = ((Number) ctrl.heightWeight.getValue()).doubleValue();
			
			if (color > 0 || height > 0)
				dlg.addDataLayer(ctrl.style, color, height);
		}
		
		return dlg;
	}

	
	
	/**
	 * Processes agent commands
	 * @param cmd
	 */
	private void processAgentCommands(ActionEvent e, String cmd) {
		cmd = cmd.intern();

		// Get the first selected row
		int row = agentTable.getSelectedRow();
		// If nothing is selected, then return
		if (row == -1)
			return;
		
		AgentStyle selectedStyle = agentData.get(row);
		if (selectedStyle == null)
			return;
		
		// Agent: up
		if (cmd == "agent-up") {
			AgentStyle prevStyle = agentData.get(row - 1);
			if (prevStyle == null)
				return;
			
			render.swapAgentStyles(selectedStyle, prevStyle);
			agentData.update(render.getAgentStyles());
			agentTable.setRowSelectionInterval(row - 1, row - 1);
			
			render.update();
			return;
		}
		
		// Agent: down
		if (cmd == "agent-down") {
			AgentStyle nextStyle = agentData.get(row + 1);
			if (nextStyle == null)
				return;
			
			render.swapAgentStyles(selectedStyle, nextStyle);
			agentData.update(render.getAgentStyles());
			agentTable.setRowSelectionInterval(row + 1, row + 1);

			render.update();
			return;
		}
		
		// Agent: advanced
		if (cmd == "agent-advanced") {
			new AgentStyleDialog(this, render, selectedStyle).setVisible(true);
			return;
		}
	}
	
	
	/**
	 * Processes data layer commands
	 * @param cmd
	 */
	private void ProcessDataLayerCommands(ActionEvent e, String cmd) {
		// Data layer: none
		if (cmd.equals("data-none")) {
			for (DataLayerControl c : dataLayerControls) {
				c.setEnabled(false);
			}
			
			render.setDataLayer(null);
			render.updateDataFilter();
			return;
		}
		
		// Data layer: special
		if (cmd.equals("data-special")) {
			for (DataLayerControl c : dataLayerControls) {
				c.setEnabled(true);
			}
			
			render.setDataLayer(createDataLayerGraphics());
			render.updateDataFilter();
			return;
		}

		// Data layer: selector
		if (cmd.startsWith("data-selector")) {
			int index = Integer.parseInt(cmd.substring("data-selector".length()));
			if (index < 0 || index >= dataLayerControls.size())
				return;
			
			DataLayerControl ctrl = dataLayerControls.get(index);
			for (DataLayerControl c : dataLayerControls) {
				c.setEnabled(false);

				if (c == ctrl)
					c.colorWeight.setValue(1);
				else
					c.colorWeight.setValue(0);
				
				c.heightWeight.setValue(0);
			}
			
			render.setDataLayer(createDataLayerGraphics());
			render.updateDataFilter();
			return;
		}
	}
	
	
	/**
	 * Processes space commands
	 * @param cmd
	 */
	private void processSpaceCommands(ActionEvent e, String cmd) {
		cmd = cmd.intern();
		
		// Space: selection
		if (cmd == "space-list") {
			JComboBox spaceList = (JComboBox) e.getSource();
			String spaceName = (String) spaceList.getSelectedItem();
			
			SpaceStyle spaceStyle = new SpaceStyle(spaceName);
			spaceStyle.swapXY = swapSpaceXY.isSelected();
			spaceStyle.selected = true;
			
			render.setSpace(spaceStyle);
			initDataLayers();
			pack();
			render.update();
			return;
		}
		
		// Space: swap xy
		if (cmd == "space-swapXY") {
			render.setSwapXYFlag(swapSpaceXY.isSelected());
			render.update();
			return;
		}
		
		// Space: auto-size
		if (cmd == "space-auto-size") {
			boolean autoSize = spaceAutoSize.isSelected();
			
			spaceCellXSize.setEnabled(!autoSize);
			spaceCellYSize.setEnabled(!autoSize);
			
			SpaceStyle spaceStyle = render.getSelectedSpaceStyle();
			if (spaceStyle == null)
				return;
			
			spaceStyle.autoSize = autoSize;
			
			if (!autoSize) {
				spaceStyle.cellXSize = ((Number) spaceCellXSize.getValue()).intValue();
				spaceStyle.cellYSize = ((Number) spaceCellYSize.getValue()).intValue();
			}
			
			render.requestReshape();
			render.update();
		}
		
	}
	

	/**
	 * Processes commands
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == null)
			return;
		
		// Space commands
		if (cmd.startsWith("space")) {
			processSpaceCommands(e, cmd);
			return;
		}
		
		// Agent commands
		if (cmd.startsWith("agent")) {
			processAgentCommands(e, cmd);
			return;
		}
		
		// Data layer commands
		if (cmd.startsWith("data")) {
			ProcessDataLayerCommands(e, cmd);
			return;
		}
	}


	@Override
	public void stateChanged(ChangeEvent e) {
		if (!(e.getSource() instanceof JSpinner))
			return;
		
		JSpinner spinner = (JSpinner) e.getSource();
		// Do not react on disabled controls
		if (!spinner.isEnabled())
			return;
		
		String name = spinner.getName();
		if (name == null)
			return;
		
		
		// Data layer
		if (name.startsWith("data")) {
			render.setDataLayer(createDataLayerGraphics());
			render.updateDataFilter();
			return;
		}
		
		// Space
		if (name.startsWith("space")) {
			SpaceStyle spaceStyle = render.getSelectedSpaceStyle();
			if (spaceStyle == null)
				return;
			
			spaceStyle.cellXSize = ((Number) spaceCellXSize.getValue()).intValue();
			spaceStyle.cellYSize = ((Number) spaceCellYSize.getValue()).intValue();
			
			render.requestReshape();
			render.update();
		}
	}
}

