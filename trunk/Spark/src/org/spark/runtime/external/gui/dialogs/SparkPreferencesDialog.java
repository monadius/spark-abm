package org.spark.runtime.external.gui.dialogs;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.spark.runtime.external.Configuration;
import org.spark.runtime.external.render.Render;


/**
 * SPARK preferences dialog
 * @author Alexey
 *
 */
public class SparkPreferencesDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;

	/* Coordinator configuration */
	private final Configuration config; 
	
	/* Main panel */
	private final JPanel mainPanel;
	
	/* Main tabbed panel */
	private final JTabbedPane tabs;
	
	/* Specifies the maximum number of recent projects */
	private JComboBox recentProjectsBox;
	
	/* Graphics buttons */
	private JRadioButton buttonJava2d;
	private JRadioButton buttonJOGL;
	
	
	/**
	 * Default constructor
	 * @param owner
	 */
	public SparkPreferencesDialog(JFrame owner, Configuration config) {
		super(owner, "Preferences", true);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		
		this.config = config;
		this.mainPanel = new JPanel();
		this.tabs = new JTabbedPane();
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		// Create graphics panel
		JPanel graphics = createGraphicsPanel();
		
		// Create general panel
		JPanel general = new JPanel(new GridLayout(0, 2));
		recentProjectsBox = new JComboBox(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 30, 40, 50});
		recentProjectsBox.setActionCommand("RecentProjects");
		recentProjectsBox.addActionListener(this);
		general.add(new JLabel("Number of recent projects"));
		general.add(recentProjectsBox);
		
		// Add tabs to the main panel
		tabs.addTab("Graphics", graphics);
		tabs.addTab("General", general);
		
		mainPanel.add(tabs);
		
		// Create save button
		JButton save = new JButton("Save");
		save.setActionCommand("save");
		save.addActionListener(this);
		mainPanel.add(save);
		
		this.getContentPane().add(mainPanel);
		this.pack();
	}
	
	
	/**
	 * Initializes dialog's elements
	 */
	public void init() {
		int renderType = config.getRenderType();
		
		if (renderType == Render.JOGL_RENDER)
			buttonJOGL.setSelected(true);
		else
			buttonJava2d.setSelected(true);
		
		int maxRecentProjects = config.getMaxRecentProjects();
		recentProjectsBox.setSelectedItem(maxRecentProjects);
	}



	/**
	 * Initializes a panel with graphics options
	 */
	private JPanel createGraphicsPanel() {
		JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.setMinimumSize(new Dimension(300, 100));
		panel.setPreferredSize(new Dimension(400, 100));
		panel.setBorder(BorderFactory.createTitledBorder("Reload open model after selecting a new renderer"));
		
		ButtonGroup group = new ButtonGroup();
		
		/* Create Java2D button */
		buttonJava2d = new JRadioButton("Java2D");
		buttonJava2d.setActionCommand("Java2D");
		buttonJava2d.addActionListener(this);
		group.add(buttonJava2d);
		panel.add(buttonJava2d);

		/* Create JOGL button */
		buttonJOGL = new JRadioButton("JOGL (OpenGL)");
		buttonJOGL.setActionCommand("JOGL");
		buttonJOGL.addActionListener(this);
		group.add(buttonJOGL);
		panel.add(buttonJOGL);
		
		return panel;
	}
	
	
	/**
	 * Updates the configuration
	 */
	private void updateConfiguration() {
		if (buttonJOGL.isSelected())
			config.setRenderType(Render.JOGL_RENDER);
		else
			config.setRenderType(Render.JAVA_2D_RENDER);
		
		int max = (Integer) recentProjectsBox.getSelectedItem();
		config.setMaxRecentProjects(max);
	}
	
	

	/**
	 * Action listener
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == null)
			return;
		
		cmd = cmd.trim();
		
		if (cmd == "save") {
			updateConfiguration();
			setVisible(false);
			return;
		}
	}
	
}

