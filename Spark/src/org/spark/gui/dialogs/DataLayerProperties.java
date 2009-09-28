package org.spark.gui.dialogs;

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

import org.spark.core.Observer;
import org.spark.data.AdvancedDataLayer;
import org.spark.data.DataLayer;
import org.spark.gui.GUIModelManager;
import org.spark.gui.render.DataLayerStyle;
import org.spark.utils.Vector;


public class DataLayerProperties extends JDialog implements ActionListener {
	private static final long serialVersionUID = -4770465039114801520L;

	private JPanel panel;
	private HashMap<String, DataLayerStyle> dataLayers;
	
	
	public DataLayerProperties(JFrame owner) {
		super(owner, "", false);
		initialize();
	}

	public DataLayerProperties(JDialog owner) {
		super(owner, "", false);
		initialize();
	}

	
	private void initialize() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		panel = new JPanel();
        panel.setLayout(new GridLayout(0, 6));

		this.add(panel);
		this.pack();
	}
	
	
	public void init(HashMap<String, DataLayerStyle> dataLayers) {
		this.dataLayers = dataLayers;

		panel.removeAll();
		for (DataLayerStyle dataLayer : dataLayers.values()) {
			JLabel name = new JLabel(dataLayer.name);
			
			JTextField val1 = new JTextField(String.valueOf(dataLayer.val1));
			JButton color1 = new JButton();
			color1.setBackground(dataLayer.color1.toAWTColor());
			
			JTextField val2 = new JTextField(String.valueOf(dataLayer.val2));
			JButton color2 = new JButton();
			color2.setBackground(dataLayer.color2.toAWTColor());
			
			JButton normalize = new JButton("Normalize");
			normalize.addActionListener(this);
			normalize.setActionCommand("normalize" + dataLayer.name);
			
			val1.setName("val1" + dataLayer.name);
			val2.setName("val2" + dataLayer.name);
			
			panel.add(name);
			panel.add(val1);
			panel.add(color1);
			panel.add(val2);
			panel.add(color2);
			panel.add(normalize);
			
			
			
			color1.setActionCommand("color1" + dataLayer.name);
			color1.addActionListener(this);
			color2.setActionCommand("color2" + dataLayer.name);
			color2.addActionListener(this);
			
			val1.addActionListener(this);
			val1.setActionCommand("val1" + dataLayer.name);
			val2.addActionListener(this);
			val2.setActionCommand("val2" + dataLayer.name);
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
		
		if (cmd.startsWith("color1") || cmd.startsWith("color2")) {
			boolean first = cmd.startsWith("color1") ? true : false;
			
			String name= cmd.substring("colorx".length());
			DataLayerStyle style = dataLayers.get(name);
			
			if (style == null) return;
			
			Vector v = first ? style.color1 : style.color2;
			Color color = v.toAWTColor();
			
			color = JColorChooser.showDialog(this, "Choose Color", color);
			if (color != null) {
				v.set(color);
				((JButton) e.getSource()).setBackground(color);
				
				GUIModelManager.getInstance().requestUpdate();
			}
			
			return;
		}
		
		if (cmd.startsWith("normalize")) {
			String name= cmd.substring("normalize".length());
			DataLayer data = Observer.getInstance().findDataLayer(name);
			
			if (data != null && data instanceof AdvancedDataLayer) {
				AdvancedDataLayer data2 = (AdvancedDataLayer) data;
				
				double min = data2.getMin();
				double max = data2.getMax();
				
				DataLayerStyle style = dataLayers.get(name);
				if (style == null) return;
				
				style.val1 = min;
				style.val2 = max;
		
				JTextField val1 = (JTextField) getComponent("val1" + name);
				JTextField val2 = (JTextField) getComponent("val2" + name);
				
//				DecimalFormat format = new DecimalFormat("##0.#####E0");
//				val1.setText(format.format(min));
//				val2.setText(format.format(max));
				val1.setText(String.valueOf(min));
				val2.setText(String.valueOf(max));
				
				GUIModelManager.getInstance().requestUpdate();
				
			}
			
			return;
			
		}
		
		if (cmd.startsWith("val1") || cmd.startsWith("val2")) {
			boolean first = cmd.startsWith("val1") ? true : false;
			
			String name= cmd.substring("valx".length());
			DataLayerStyle style = dataLayers.get(name);
			
			if (style == null) return;
			
			double v = first ? style.val1 : style.val2;
			
			String snewVal = ((JTextField) e.getSource()).getText();
			
			try {
				v = Double.parseDouble(snewVal);
			
				if (first)
					style.val1 = v;
				else
					style.val2 = v;

				GUIModelManager.getInstance().requestUpdate();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			
			return;
		}
		
	}
	
}
