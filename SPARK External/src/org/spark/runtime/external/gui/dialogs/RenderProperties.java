package org.spark.runtime.external.gui.dialogs;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.spark.runtime.external.render.DataLayerGraphics;
import org.spark.runtime.external.render.SpaceStyle;
import org.spark.runtime.external.render.DataLayerStyle;
import org.spark.runtime.external.render.AgentStyle;
import org.spark.runtime.external.render.Render;
import org.spark.utils.SpringUtilities;


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
	private JPanel panel, agentPanel, spacePanel, dataPanel;
	private ArrayList<AgentStyle> agentStyles;
	
	private JCheckBox swapSpaceXY;
	
	
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
	
	// Controls for data layers
	private ArrayList<DataLayerControl> dataLayerControls;	
	
	
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
//		dataLayers = new ArrayList<Grid>(20);
		
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

//		agentPanel = new JPanel(new GridLayout(0, 7));
		agentPanel = new JPanel(new SpringLayout());
		agentPanel.setMinimumSize(new Dimension(100, 100));
//		agentPanel.setPreferredSize(new Dimension(200, 100));
		agentPanel.setBorder(BorderFactory.createTitledBorder("Agents"));
		
		dataPanel = new JPanel(new SpringLayout());
		dataPanel.setMinimumSize(new Dimension(100, 100));
//		dataPanel.setPreferredSize(new Dimension(200, 200));
		dataPanel.setBorder(BorderFactory.createTitledBorder("Data Layers"));

		spacePanel = new JPanel(new GridLayout(0, 2));
		spacePanel.setMinimumSize(new Dimension(100, 100));
//		dataPanel.setPreferredSize(new Dimension(200, 200));
		spacePanel.setBorder(BorderFactory.createTitledBorder("Spaces"));

		
		panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        panel.add(agentPanel);
        panel.add(spacePanel);
        panel.add(dataPanel);
        
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
		agentPanel.removeAll();
		agentStyles = render.getAgentStyles();
		
		for (int i = 0; i < agentStyles.size(); i++) {
			AgentStyle agentStyle = agentStyles.get(i);
			
			String agentName = agentStyle.name;
			if (agentName == null || agentName.equals(""))
				agentName = agentStyle.typeName;
			JLabel name = new JLabel(agentName);
			JCheckBox transparent = new JCheckBox("Transparent", agentStyle.transparent);
			JCheckBox visible = new JCheckBox("Visible", agentStyle.visible);
			JCheckBox border = new JCheckBox("Border", agentStyle.border);
			
			JButton advanced = new JButton("Advanced");
			JButton up = new JButton("Up");
			JButton down = new JButton("Down");

			transparent.setActionCommand("agent_trans" + i);
			visible.setActionCommand("agent_vis" + i);
			border.setActionCommand("agent_border" + i);
			advanced.setActionCommand("advanced" + i);
			up.setActionCommand("agent_up" + i);
			down.setActionCommand("agent_down" + i);
			
			transparent.addActionListener(this);
			visible.addActionListener(this);
			border.addActionListener(this);
			advanced.addActionListener(this);
			up.addActionListener(this);
			down.addActionListener(this);
			
			agentPanel.add(name);
			agentPanel.add(transparent);
			agentPanel.add(visible);
			agentPanel.add(border);
			agentPanel.add(advanced);
			agentPanel.add(up);
			agentPanel.add(down);
			
		}
		
		SpringUtilities.makeCompactGrid(agentPanel, 
				agentStyles.size(), 7, 
				5, 5, 15, 5);
	}
	
	
	/**
	 * Initializes controls for spaces
	 */
	private void initSpaces() {
		spacePanel.removeAll();
		SpaceStyle selectedSpace = render.getSelectedSpaceStyle();
		String[] spaceNames = render.getSpaceNames();
		
		if (spaceNames == null)
			spaceNames = new String[0];
		
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
	
		boolean swapXYflag = (selectedSpace != null) ? selectedSpace.swapXY : false;
		
		swapSpaceXY = new JCheckBox("Rotate", swapXYflag);
		swapSpaceXY.setActionCommand("swapXY");
		swapSpaceXY.addActionListener(this);
		
		spacePanel.add(spaceList);
		spacePanel.add(swapSpaceXY);
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
	 * Processes commands
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == null)
			return;
		
		int n;
		
		
		if (cmd.startsWith("agent_trans")) {
			JCheckBox transparent = (JCheckBox) e.getSource();
			
			n = Integer.parseInt( cmd.substring( "agent_trans".length() ) );
			agentStyles.get(n).transparent = transparent.isSelected();
			render.update();
			return;
		}
		
		// Space
		if (cmd.startsWith("space-list")) {
			JComboBox spaceList = (JComboBox) e.getSource();
			String spaceName = (String) spaceList.getSelectedItem();
			
			render.setSpace(new SpaceStyle(spaceName, swapSpaceXY.isSelected(), true));
			initDataLayers();
			pack();
			render.update();
			return;
		}
		
		// SwapXY
		if (cmd.startsWith("swapXY")) {
			render.setSwapXYFlag(swapSpaceXY.isSelected());
			render.update();
			return;
		}
		
		// Agent: visibility
		if (cmd.startsWith("agent_vis")) {
			JCheckBox visible = (JCheckBox) e.getSource();
			
			n = Integer.parseInt( cmd.substring( "agent_vis".length() ) );
			agentStyles.get(n).visible = visible.isSelected();
			render.updateDataFilter();
			return;
		}
		
		// Agent: border
		if (cmd.startsWith("agent_border")) {
			JCheckBox border = (JCheckBox) e.getSource();
			
			n = Integer.parseInt( cmd.substring( "agent_border".length() ) );
			agentStyles.get(n).border = border.isSelected();
			render.update();
			return;
		}

		// Agent: advanced
		if (cmd.startsWith("advanced")) {
			n = Integer.parseInt( cmd.substring( "advanced".length() ));
			new AgentStyleDialog(this, agentStyles.get(n)).setVisible(true);
			return;
		}
		
		// Agent: up
		if (cmd.startsWith("agent_up")) {
			n = Integer.parseInt( cmd.substring( "agent_up".length() ) );
			if (n == 0)
				return;
			
			render.swapAgentStyles(agentStyles.get(n), agentStyles.get(n - 1));
			initAgents();
			pack();
			validate();
			render.update();
			return;
		}
		
		// Agent: down
		if (cmd.startsWith("agent_down")) {
			n = Integer.parseInt( cmd.substring( "agent_down".length() ) );
			if (n >= agentStyles.size() - 1)
				return;
			
			render.swapAgentStyles(agentStyles.get(n), agentStyles.get(n + 1));
			initAgents();
			pack();
			validate();
			render.update();
			return;
		}
		
		
		/////////////////////////
		// Data layer commands //
		/////////////////////////


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


	@Override
	public void stateChanged(ChangeEvent e) {
		if (!(e.getSource() instanceof JSpinner))
			return;
		
		JSpinner spinner = (JSpinner) e.getSource();
		// Do not react on disabled controls
		if (!spinner.isEnabled())
			return;

		render.setDataLayer(createDataLayerGraphics());
		render.updateDataFilter();
	}
}

