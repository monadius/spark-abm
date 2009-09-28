package org.spark.gui.applet;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ParameterPanel implements IUpdatablePanel, ChangeListener, ActionListener {
	private static final long serialVersionUID = -1315411629669891403L;
	
	private JPanel panel;
	private HashMap<JComponent, Parameter> parameters = new HashMap<JComponent, Parameter>();

	private static class Parameter {
		public Method getValue, setValue;
		public String name;
		public JLabel nameLabel, valueLabel;
		public JComponent widget;

		public boolean defaultFlag;
		public Double defaultValue;
		public double min, max, step;
		public Class<?> type;
		
		public Object value;
		
		public int stepsNumber() {
			return (int)((max - min) / step);
		}
	}
	
	public ParameterPanel(Node node) {
		panel = SparkApplet.getParameterPanel();
		panel.setLayout(new GridLayout(0, 3, 10, 5));
		
		loadParameters(node);
	}
	
	
	private void loadParameters(Node node) {
		NodeList nodes = node.getChildNodes();
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Node paramNode = nodes.item(i);
			
			if (paramNode.getNodeName().equals("parameter")) {
				addParameter(paramNode);
			}
		}
	
		updateParameterValues();
	}
	
	
//	@SuppressWarnings("unchecked")
	private void addParameter(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		Node tmp;
		
		String name = (tmp = attributes.getNamedItem("name")) != null ? tmp.getNodeValue() : "???";
		String sget = (tmp = attributes.getNamedItem("get")) != null ? tmp.getNodeValue() : "";
		String sset = (tmp = attributes.getNamedItem("set")) != null ? tmp.getNodeValue() : "";
		String stype = (tmp = attributes.getNamedItem("type")) != null ? tmp.getNodeValue() : "Double";
		String smin = (tmp = attributes.getNamedItem("min")) != null ? tmp.getNodeValue() : "0";
		String smax = (tmp = attributes.getNamedItem("max")) != null ? tmp.getNodeValue() : "1";
		String sstep = (tmp = attributes.getNamedItem("step")) != null ? tmp.getNodeValue() : "1";
		String swidget = (tmp = attributes.getNamedItem("widget")) != null ? tmp.getNodeValue() : "TextBox";
		String svalue = (tmp = attributes.getNamedItem("default")) != null ? tmp.getNodeValue() : null;

		Parameter p = new Parameter();
		
		Method get = null, set = null;
		Class<?> type;
		try {
			type = Class.forName("java.lang." + stype);
			get = AppletModelManager.getModelClass().getClass().getMethod(sget);
			set = AppletModelManager.getModelClass().getClass().getMethod(sset, type);

			p.type = type;
		} catch (Exception e) {
			e.printStackTrace();
		}

		JLabel lname = new JLabel(name);
		JLabel lval = new JLabel();
		
		p.name = name;
		p.nameLabel = lname;
		p.valueLabel = lval;
		
		p.min = Double.parseDouble(smin);
		p.max = Double.parseDouble(smax);
		p.step = Double.parseDouble(sstep);
		if (svalue != null)
			p.defaultValue = Double.parseDouble(svalue);
		else
			p.defaultValue = null;
		
		if (p.step < 1e-3) p.step = 1;
		if (p.max < p.min) p.max = p.min;
		
		if (swidget.equals("TextBox")) {
			JTextField text = new JTextField();
			p.widget = text;

			// TODO: all for text field
		}
		else if (swidget.equals("Slider")) {
			JSlider slider = new JSlider();
			p.widget = slider;
			
			slider.setMinimum(0);
			slider.setMaximum(p.stepsNumber());
			slider.addChangeListener(this);
		}
		else if (swidget.equals("OnOff")) {
			JCheckBox checkbox = new JCheckBox();
			p.widget = checkbox;
			
			checkbox.addActionListener(this);
		}

		p.getValue = get;
		p.setValue = set;
		
		panel.add(lname);
		panel.add(lval);
		panel.add(p.widget);
		
		parameters.put(p.widget, p);
		
	}
	
	
	
	private void updateParameterValues() {
		try {
			for (Parameter p : parameters.values()) {
				if (!p.defaultFlag && p.defaultValue != null && p.type == Double.class) {
					if (p.setValue != null) {
						p.setValue.invoke(null, p.defaultValue);
						p.defaultFlag = true;
						
						p.value = p.defaultValue;
					}
				}
				
				if (p.getValue != null) {
					Object val = p.getValue.invoke(null);
					p.valueLabel.setText(val.toString());
					p.value = val;
					setWidgetValue(p, val);
				}
			}
		} catch (Exception e) { }
		
	}
	
	
	private void userParameterUpdate(Parameter p, Object value) {
		// FIXME: change everything only during update call for synchronization
		
		if (p.setValue == null || p.getValue == null) return;
		
		try {
			p.setValue.invoke(null, value);
			value = p.getValue.invoke(null);
			p.value = value;

			p.valueLabel.setText(value.toString());
			setWidgetValue(p, value);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	
	
	private void setWidgetValue(Parameter p, Object value) {
		if (p != null)
			p.value = value;
		
		if (p.widget instanceof JTextField) {
			JTextField text = (JTextField) p.widget;
			text.setText(value.toString());
		}
		else if (p.widget instanceof JSlider) {
			JSlider slider = (JSlider) p.widget;
			int n;
			
			if (value instanceof Boolean) {
				if ( ((Boolean) value).booleanValue() )
					n = 1;
				else
					n = 0;
			}
			else {
				Number num = (Number) value;
				
				n = (int)((num.doubleValue() - p.min) / p.step);
			}
			
			slider.setValue(n);
		}
		else if (p.widget instanceof JCheckBox) {
			JCheckBox checkbox = (JCheckBox) p.widget;
			
			boolean n;
			
			if (value instanceof Boolean) {
				n = ((Boolean) value).booleanValue();
			}
			else {
				Number num = (Number) value;
				n = num.doubleValue() != 0;
			}
			
			checkbox.setSelected(n);
		}
	}


	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
	    
		if (!source.getValueIsAdjusting()) {
			Parameter p = parameters.get(source);
			double n = source.getValue() * p.step + p.min;
	    	
			if (p.type.equals(Integer.class))
				userParameterUpdate(p, (int) n);
			else
				userParameterUpdate(p, n);
	    }		
	}


	public void actionPerformed(ActionEvent arg0) {
		JCheckBox source = (JCheckBox) arg0.getSource();
		
		Parameter p = parameters.get(source);
		
		if (p.type.equals(Boolean.class))
			userParameterUpdate(p, source.isSelected());
	}


	public void reset() {
		// TODO Auto-generated method stub
		
	}


	public void updateData(long tick) {
		// TODO Auto-generated method stub
		
	}


	public void updateData() {
		// TODO Auto-generated method stub
		
	}


}
