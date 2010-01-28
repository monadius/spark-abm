package org.spark.runtime.external.gui.dialogs;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;

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
public class RenderProperties extends JDialog implements ActionListener {
	private static final long serialVersionUID = -4770465039114801520L;

	/* Render for this dialog */
	private Render render;
	
	/* Main panels */
	private JPanel panel, titlePanel, agentPanel, spacePanel, dataPanel;
	private ArrayList<AgentStyle> agentStyles;
	
	private JCheckBox swapSpaceXY;
	
	
	/**
	 * Default constructor
	 * @param render
	 * @param editTitle
	 */
	public RenderProperties(Render render, boolean editTitle) {
		init(render, editTitle);
	}
	

	/**
	 * Initializes the dialog during its creation process
	 * @param render
	 * @param editTitle
	 */
	private void init(Render render, boolean editTitle) {
		this.render = render;
//		dataLayers = new ArrayList<Grid>(20);
		
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

//		agentPanel = new JPanel(new GridLayout(0, 7));
		agentPanel = new JPanel(new SpringLayout());
		agentPanel.setMinimumSize(new Dimension(100, 100));
//		agentPanel.setPreferredSize(new Dimension(200, 100));
		agentPanel.setBorder(BorderFactory.createTitledBorder("Agents"));
		
		dataPanel = new JPanel(new GridLayout(0, 1));
		dataPanel.setMinimumSize(new Dimension(100, 100));
//		dataPanel.setPreferredSize(new Dimension(200, 200));
		dataPanel.setBorder(BorderFactory.createTitledBorder("Data Layers"));

		spacePanel = new JPanel(new GridLayout(0, 2));
		spacePanel.setMinimumSize(new Dimension(100, 100));
//		dataPanel.setPreferredSize(new Dimension(200, 200));
		spacePanel.setBorder(BorderFactory.createTitledBorder("Spaces"));

		
		panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        if (editTitle) {
        	Window owner = getOwner();
        	// TODO: better solution is required
        	if (owner != null && owner instanceof JDialog) {
        	
        		titlePanel = new JPanel(new GridLayout(1, 1));
    			titlePanel.setMinimumSize(new Dimension(100, 100));
    			titlePanel.setBorder(BorderFactory.createTitledBorder("Name"));
    		
    			JTextField text = new JTextField( ((JDialog) owner).getTitle() );
    			text.setMinimumSize(new Dimension(100, 20));
    			text.setActionCommand("title");
    			text.addActionListener(this);
    			titlePanel.add(text);
    		
    			panel.add(titlePanel);
        	}
        }
			

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
		dataPanel.removeAll();
//		dataLayers.clear();

		HashMap<String, DataLayerStyle> dataLayerStyles = render.getDataLayerStyles();
		if (dataLayerStyles == null) return;
		
		DataLayerStyle activeLayer = render.getCurrentDataLayerStyle();

		ButtonGroup group = new ButtonGroup();
		
		JRadioButton button = new JRadioButton("none");
		if (activeLayer == null)
			button.setSelected(true);
		button.setActionCommand("none");
		button.addActionListener(this);
		group.add(button);
		dataPanel.add(button);
		
		for (DataLayerStyle dataLayer : dataLayerStyles.values()) {
			button = new JRadioButton(dataLayer.getName());
			if (dataLayer == activeLayer)
				button.setSelected(true);
			
			button.setActionCommand("data" + dataLayer.getName());
			button.addActionListener(this);
			group.add(button);
			dataPanel.add(button);
		}
		
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
		
		if (cmd.startsWith("space-list")) {
			JComboBox spaceList = (JComboBox) e.getSource();
			String spaceName = (String) spaceList.getSelectedItem();
			
			render.setSpace(new SpaceStyle(spaceName, swapSpaceXY.isSelected(), true));
			initDataLayers();
			pack();
			render.update();
			return;
		}
		
		
		if (cmd.startsWith("swapXY")) {
			render.setSwapXYFlag(swapSpaceXY.isSelected());
			render.update();
			return;
		}
		
		
		if (cmd.startsWith("agent_vis")) {
			JCheckBox visible = (JCheckBox) e.getSource();
			
			n = Integer.parseInt( cmd.substring( "agent_vis".length() ) );
			agentStyles.get(n).visible = visible.isSelected();
			render.updateDataFilter();
			return;
		}
		

		if (cmd.startsWith("agent_border")) {
			JCheckBox border = (JCheckBox) e.getSource();
			
			n = Integer.parseInt( cmd.substring( "agent_border".length() ) );
			agentStyles.get(n).border = border.isSelected();
			render.update();
			return;
		}

		if (cmd.startsWith("advanced")) {
			n = Integer.parseInt( cmd.substring( "advanced".length() ));
			new AgentStyleDialog(this, agentStyles.get(n)).setVisible(true);
			return;
		}
		
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

				
		if (cmd.equals("none")) {
			render.setDataLayer(null);
			render.updateDataFilter();
			return;
		}

		
		if (cmd.startsWith("data")) {
			String name = cmd.substring("data".length());
			
			render.setDataLayer(name);
			render.updateDataFilter();
			return;
		}
		
		
		if (cmd.startsWith("title")) {
			JTextField src = (JTextField) e.getSource();
			if (src == null)
				return;
			
			String title = src.getText();
			Window owner = getOwner();
			
			if (owner != null && owner instanceof JDialog) {
				((JDialog) owner).setTitle(title);
			}
			
			return;
		}
		
		
	}
}

