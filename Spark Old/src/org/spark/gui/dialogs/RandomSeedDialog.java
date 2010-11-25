package org.spark.gui.dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.spark.utils.RandomHelper;

public class RandomSeedDialog extends JDialog implements ActionListener {
	/* Default UID */
	private static final long serialVersionUID = 1L;
	
	private JCheckBox checkTimeSeed;
	private JSpinner spinnerSeed;
	private JButton buttonOK;

	/**
	 * Default constructor
	 * @param owner
	 */
	public RandomSeedDialog(JFrame owner) {
		super(owner, true);
		setTitle("Random seed");
		
		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		
		
		// Create main panel
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setMinimumSize(new Dimension(300, 100));
		
		// Create widgets
		checkTimeSeed = new JCheckBox("Use current time as the seed");
		checkTimeSeed.addActionListener(this);
		
		spinnerSeed = new JSpinner(
				new SpinnerNumberModel(0, -20000000000l, 20000000000l, 1));
		
		buttonOK = new JButton("OK");
		buttonOK.addActionListener(this);

		// Add widgets
		panel.add(checkTimeSeed);
		panel.add(spinnerSeed);
		panel.add(buttonOK);
		
		// Add main panel
		this.add(panel);
		this.pack();
	}
	
	
	/**
	 * Initializes the dialog
	 */
	public void init() {
		spinnerSeed.setValue(RandomHelper.getSeed());
		
		boolean timeSeed = RandomHelper.isTimeSeed();
		spinnerSeed.setEnabled(!timeSeed);
		checkTimeSeed.setSelected(timeSeed);
	}


	/**
	 * Default action listener
	 */
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		
		
		if (src == checkTimeSeed) {
			boolean selected = checkTimeSeed.isSelected();
			
			spinnerSeed.setEnabled(!selected);
			return;
		}
		
		
		if (src == buttonOK) {
			boolean timeSeed = checkTimeSeed.isSelected();
			
			if (timeSeed) {
				RandomHelper.setTimeSeed();
			}
			else {
				int seed = ((Number) spinnerSeed.getValue()).intValue();
				RandomHelper.setSeed(seed);
			}
			
			this.setVisible(false);
			return;
		}
	}
}
