package org.spark.runtime.external.gui.dialogs;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.render.DataLayerStyle;
import org.spark.utils.SpringUtilities;
import org.spark.utils.Vector;


/**
 * Dialog for additional data layer colors
 * @author Monad
 *
 */
public class DataLayerColors extends JDialog implements ActionListener, ChangeListener {
	private static final long serialVersionUID = -215958960640303857L;
	private JPanel colorPanel;
	private DataLayerStyle currentStyle;
	
	
	/**
	 * Default constructor
	 * @param owner
	 */
	public DataLayerColors(JDialog owner) {
		super(owner, "", false);
		initialize();
	}

	
	/**
	 * Initializer
	 */
	private void initialize() {
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

		colorPanel = new JPanel();
		colorPanel.setBorder(BorderFactory.createTitledBorder("Additional Colors"));
        colorPanel.setLayout(new SpringLayout());

        JPanel commands = new JPanel();
        
        JButton add = new JButton("Add");
        add.setActionCommand("add");
        add.addActionListener(this);

        commands.add(add);
        
		JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        panel.add(colorPanel);
        panel.add(commands);
        
		this.add(panel);
		this.pack();
	}
	
	
	/**
	 * Initializer for the given data layer style
	 * @param dataLayers
	 */
	public void init(DataLayerStyle style) {
		this.currentStyle = style;

		colorPanel.removeAll();
		Integer[] keys = style.getKeys();
		
		for (int key : keys) {
			Vector color = style.getColorValue(key).color;
			double value = style.getColorValue(key).value;
			
			value *= 100;
			int val = (int) value;
			
			JSpinner spinnerValue = new JSpinner(new SpinnerNumberModel(val, 0, 100, 1));
			spinnerValue.setName("value" + key);
			spinnerValue.addChangeListener(this);
			
			JButton colorButton = new JButton();
			colorButton.setBackground(color.toAWTColor());
			colorButton.setActionCommand("color" + key);
			colorButton.addActionListener(this);
			
			JButton removeButton = new JButton("Remove");
			removeButton.setActionCommand("remove" + key);
			removeButton.addActionListener(this);
			
			colorPanel.add(spinnerValue);
			colorPanel.add(colorButton);
			colorPanel.add(removeButton);
		}

		SpringUtilities.makeCompactGrid(colorPanel, 
				keys.length, 3, 
				5, 5, 15, 5);
		
		this.pack();
	}
	
	
	/**
	 * Action listener
	 */
	public void actionPerformed(ActionEvent e) {
		if (currentStyle == null)
			return;
		
		String cmd = e.getActionCommand();
		
		if (cmd == null)
			return;
		
		// Add command
		if (cmd.startsWith("add")) {
			currentStyle.addColorAndValue(Vector.fromAWTColor(Color.white), 0.5);
			// Reinitialize
			init(currentStyle);
			Coordinator.getInstance().updateAllRenders();
			return;
		}
		
		
		// Remove command
		if (cmd.startsWith("remove")) {
			int key = Integer.parseInt(cmd.substring("remove".length()));
			currentStyle.removeColorAndValue(key);
			
			// Reinitialize
			init(currentStyle);
			
			Coordinator.getInstance().updateAllRenders();
			return;
		}
		
		
		// Color command
		if (cmd.startsWith("color")) {
			int key = Integer.parseInt(cmd.substring("color".length()));
			DataLayerStyle.ColorValue cv = currentStyle.getColorValue(key);
			if (cv == null)
				return;
			
			Vector color = cv.color;  
			Color c = JColorChooser.showDialog(this, "Choose Color", color.toAWTColor());
			
			if (c != null) {
				cv.color = Vector.fromAWTColor(c);
				((JButton) e.getSource()).setBackground(c);
				
				// Update
				Coordinator.getInstance().updateAllRenders();
			}
						
			return;
		}
		
	}


	/**
	 * Change listener
	 */
	public void stateChanged(ChangeEvent arg0) {
		Object src = arg0.getSource();
		
		if (src instanceof JSpinner) {
			JSpinner spinner = (JSpinner) src;
			String name = spinner.getName();
			
			int key = Integer.parseInt(name.substring("value".length()));
			DataLayerStyle.ColorValue cv = currentStyle.getColorValue(key);
			if (cv == null)
				return;
			
			int newValue = (Integer) spinner.getValue();
			if (newValue < 0)
				newValue = 0;
			else if (newValue > 100)
				newValue = 100;
			
			cv.value = newValue / 100.0;
			
			// Update
			Coordinator.getInstance().updateAllRenders();
		}
	}
	
}

