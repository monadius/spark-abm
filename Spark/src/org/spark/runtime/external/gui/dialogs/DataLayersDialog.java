package org.spark.runtime.external.gui.dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.data.DataObject;
import org.spark.runtime.data.DataObject_Grid;
import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.render.DataLayerStyle;
import org.spark.utils.Vector;


/**
 * Dialog for data layers' parameters
 * @author Monad
 *
 */
public class DataLayersDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = -4770465039114801520L;

	private JPanel panel;
	private HashMap<String, DataLayerStyle> dataLayers;
	
	private DataLayerColors colorsDialog;
	
	
	/**
	 * Default constructor
	 * @param owner
	 */
	public DataLayersDialog(JFrame owner) {
		super(owner, "", false);
		initialize();
		
		colorsDialog = new DataLayerColors(this);
	}

	
	/**
	 * Initializer
	 */
	private void initialize() {
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

		panel = new JPanel();
        panel.setLayout(new GridLayout(0, 7));

		this.add(panel);
		this.pack();
	}
	
	
	/**
	 * Inits the dialog for the given data layers
	 * @param dataLayers
	 */
	public void init(HashMap<String, DataLayerStyle> dataLayers) {
		this.dataLayers = dataLayers;

		panel.removeAll();
		for (DataLayerStyle dataLayer : dataLayers.values()) {
			JLabel name = new JLabel(dataLayer.getName());
			
			JTextField val1 = new JTextField(String.valueOf(dataLayer.getVal1()));
			JButton color1 = new JButton();
			color1.setBackground(dataLayer.getColor1().toAWTColor());
			
			JTextField val2 = new JTextField(String.valueOf(dataLayer.getVal2()));
			JButton color2 = new JButton();
			color2.setBackground(dataLayer.getColor2().toAWTColor());
			
			JButton normalize = new JButton("Normalize");
			normalize.addActionListener(this);
			normalize.setActionCommand("normalize" + dataLayer.getName());
			
			JButton colors = new JButton("Colors");
			colors.setActionCommand("colors" + dataLayer.getName());
			colors.addActionListener(this);
			
			val1.setName("val1" + dataLayer.getName());
			val2.setName("val2" + dataLayer.getName());
			
			panel.add(name);
			panel.add(val1);
			panel.add(color1);
			panel.add(val2);
			panel.add(color2);
			panel.add(normalize);
			panel.add(colors);
			
			
			
			color1.setActionCommand("color1" + dataLayer.getName());
			color1.addActionListener(this);
			color2.setActionCommand("color2" + dataLayer.getName());
			color2.addActionListener(this);
			
			val1.addActionListener(this);
			val1.setActionCommand("val1" + dataLayer.getName());
			val2.addActionListener(this);
			val2.setActionCommand("val2" + dataLayer.getName());
		}
		
		this.pack();
	}
	
	
	
	private Component getComponent(String name) {
		if (name == null)
			return null;
		int n = panel.getComponentCount();
		
		for (int i = 0; i < n; i++) {
			Component comp = panel.getComponent(i);
			if (name.equals(comp.getName())) {
				return comp;
			}
		}
		
		return null;
	}


	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		
		if (cmd == null)
			return;
		
		// Color1 or Color2 commands
		if (cmd.startsWith("color1") || cmd.startsWith("color2")) {
			boolean first = cmd.startsWith("color1") ? true : false;
			
			String name= cmd.substring("colorx".length());
			DataLayerStyle style = dataLayers.get(name);
			
			if (style == null) return;
			
			Vector v = first ? style.getColor1() : style.getColor2();
			Color color = v.toAWTColor();
			
			color = JColorChooser.showDialog(this, "Choose Color", color);
			if (color != null) {
				v.set(color);
				((JButton) e.getSource()).setBackground(color);
				
				Coordinator.getInstance().updateAllRenders();
			}
			
			return;
		}
		

		// Colors command
		if (cmd.startsWith("colors")) {
			String name = cmd.substring("colors".length());
			DataLayerStyle style = dataLayers.get(name);
			
			if (style == null)
				return;
			
			colorsDialog.init(style);
			colorsDialog.setVisible(true);
			return;
		}
		
		
		// Normalize command
		if (cmd.startsWith("normalize")) {
			String name = cmd.substring("normalize".length());
			DataObject dataObject = Coordinator.getInstance()
				.getMostRecentData(DataCollectorDescription.DATA_LAYER, name);
			
			if (dataObject == null)
				return;
			
			DataObject_Grid data = (DataObject_Grid) dataObject;
			
			double min = data.getMin();
			double max = data.getMax();
				
			DataLayerStyle style = dataLayers.get(name);
			if (style == null) return;
				
			style.setVal1(min);
			style.setVal2(max);
		
			JTextField val1 = (JTextField) getComponent("val1" + name);
			JTextField val2 = (JTextField) getComponent("val2" + name);
				
//				DecimalFormat format = new DecimalFormat("##0.#####E0");
//				val1.setText(format.format(min));
//				val2.setText(format.format(max));
			val1.setText(String.valueOf(min));
			val2.setText(String.valueOf(max));
			
			Coordinator.getInstance().updateAllRenders();
//				GUIModelManager.getInstance().requestUpdate();
				
			return;
		}
		
		if (cmd.startsWith("val1") || cmd.startsWith("val2")) {
			boolean first = cmd.startsWith("val1") ? true : false;
			
			String name= cmd.substring("valx".length());
			DataLayerStyle style = dataLayers.get(name);
			
			if (style == null) return;
			
			double v = first ? style.getVal1() : style.getVal2();
			
			String snewVal = ((JTextField) e.getSource()).getText();
			
			try {
				v = Double.parseDouble(snewVal);
			
				if (first)
					style.setVal1(v);
				else
					style.setVal2(v);

				Coordinator.getInstance().updateAllRenders();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			
			return;
		}
		
	}
	
}

