package org.spark.runtime.external.gui.dialogs;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.spark.runtime.external.Coordinator;


/**
 * Model properties dialog
 * @author Alexey
 */
public class ModelPropertiesDialog extends JDialog implements ActionListener {
	/* Default UID */
	private static final long serialVersionUID = 1L;
	
	/* Main panel */
	private final JPanel mainPanel;
	
	/* Random seed controls */
	private JCheckBox checkTimeSeed;
	private JSpinner spinnerSeed;
	
	/* Observer selection controls */
	private JComboBox boxObserverName;
	private JComboBox boxExecutionMode;
	
	/* OK button */
	private JButton buttonOK;

	/**
	 * Default constructor
	 * @param owner
	 */
	public ModelPropertiesDialog(JFrame owner) {
		super(owner, true);
		setTitle("Model Properties");
		
		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		
		// Create main panel
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setMinimumSize(new Dimension(300, 100));
		
		// Create controls
		JPanel randomSeed = createRandomSeedPanel();
		JPanel observerSelection = createObserverPanel();
		
		buttonOK = new JButton("OK");
		buttonOK.addActionListener(this);

		// Add controls
		mainPanel.add(randomSeed);
		mainPanel.add(observerSelection);
		mainPanel.add(buttonOK);
		
		// Add main panel
		this.add(mainPanel);
		this.pack();
	}
	
	
	/**
	 * Creates random seed panel
	 * @return
	 */
	private JPanel createRandomSeedPanel() {
		JPanel panel = new JPanel(new GridLayout(2, 1));
		
		checkTimeSeed = new JCheckBox("Use current time as the seed");
		checkTimeSeed.addActionListener(this);
		
		spinnerSeed = new JSpinner(
				new SpinnerNumberModel(0, -20000000000l, 20000000000l, 1));
		
		panel.add(checkTimeSeed);
		panel.add(spinnerSeed);
		
		return panel;
	}
	
	
	/**
	 * Creates observer selection panel
	 * @return
	 */
	private JPanel createObserverPanel() {
		JPanel panel = new JPanel(new GridLayout(0, 2));
		
		boxObserverName = new JComboBox(new String[] {
				"default",
				"Observer1",
				"Observer2",
				"ObserverParallel"
		});
		
		boxObserverName.setSelectedIndex(0);
		
		boxExecutionMode = new JComboBox(new String[] {
				"serial",
				"concurrent",
				"parallel"
		});
		
		boxExecutionMode.setSelectedIndex(0);
		
		
		// Add controls to the panel
		boxObserverName.addActionListener(this);
		
		panel.add(new JLabel("Observer name"));
		panel.add(boxObserverName);
		panel.add(new JLabel("Execution mode"));
		panel.add(boxExecutionMode);
		
		return panel;
	}
	
	
	/**
	 * Initializes the dialog
	 */
	public void init() {
		Coordinator c = Coordinator.getInstance();
		spinnerSeed.setValue(c.getRandomSeed());
		
		boolean timeSeed = c.getTimeSeedFlag();
		spinnerSeed.setEnabled(!timeSeed);
		checkTimeSeed.setSelected(timeSeed);
		
		String observerName = c.getObserverName();
		String executionMode = c.getExecutionMode();
		
		if (observerName == null)
			observerName = "default";
		
		boxObserverName.setSelectedItem(observerName);
		boxExecutionMode.setSelectedItem(executionMode);
	}
	
	
	/**
	 * Updates the properties of a loaded model
	 */
	private void updateModelProperties() {
		// Update random seed
		boolean timeSeed = checkTimeSeed.isSelected();
		int seed = ((Number) spinnerSeed.getValue()).intValue();

		Coordinator.getInstance().setRandomSeed(seed, timeSeed);
		
		// Update observer
		String observerName = (String) boxObserverName.getSelectedItem();
		if (observerName.equals("default"))
			observerName = null;
		
		String executionMode = (String) boxExecutionMode.getSelectedItem();
		Coordinator.getInstance().setObserver(observerName, executionMode);
	}


	/**
	 * Default action listener
	 */
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		
		/* CheckTimeSeed */
		if (src == checkTimeSeed) {
			boolean selected = checkTimeSeed.isSelected();
			
			spinnerSeed.setEnabled(!selected);
			return;
		}
		
		/* BoxObserverName */
		if (src == boxObserverName) {
			if (boxObserverName.getSelectedItem().equals("default"))
				boxExecutionMode.setEnabled(false);
			else
				boxExecutionMode.setEnabled(true);
		}
		
		/* ButtonOK */
		if (src == buttonOK) {
			updateModelProperties();
			
			this.setVisible(false);
			return;
		}
	}

}
