package org.spark.runtime;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.spark.runtime.internal.ModelVariable;

/**
 * Model parameter
 * @author Monad
 */
@SuppressWarnings("serial")
public class Parameter_Old extends SpinnerNumberModel implements ChangeListener, ActionListener {
	/* Associated variable */
	protected ModelVariable variable;
	
	/* A list of specific values: if it is not null, then
	 * only values from this list can be assigned to the parameter
	 */
	// TODO: other value types
	protected Double[] values;
	
	/* Parameter's name */
	protected String name;
	
	/* Parameter's parameters */
	protected double min, max, step;

	/* Widget name associated with the parameter */
	protected String widgetName;
	
	/* Widget associated with the parameter */
	private JComponent widget;
	
	/* Label which shows the parameter's value */
	private JLabel valueLabel;
	
	/* Two formats for decimal numbers */
	private DecimalFormat format1 = new DecimalFormat("#.###");
	private DecimalFormat format2 = new DecimalFormat("0.###E0");
	private double threshold = 1e-3;
	
	
	/**
	 * Auxiliary class for updating the widget
	 */
	private abstract class WidgetUpdater {
		public void update() {
			if (valueLabel != null)
				valueLabel.setText(getStringValue());
		}
	}
	
	private WidgetUpdater widgetUpdater;
	

	/**
	 * Internal constructor
	 */
	Parameter_Old() {
	}
	
	
	/**
	 * Returns min value of the parameter
	 * @return
	 */
	public double getMin() {
		return min;
	}
	
	
	/**
	 * Returns max value of the parameter
	 * @return
	 */
	public double getMax() {
		return max;
	}
	
	
	/**
	 * Returns step size for the parameter
	 * @return
	 */
	public double getStep() {
		return step;
	}
	
	
	/**
	 * Sets a specific list of parameter's values
	 * @param values
	 */
	protected void setValues(String[] vals) {
		values = new Double[vals.length];
		
		for (int i = 0; i < vals.length; i++) {
			Double v = 0.0;
			
			try {
				v = Double.valueOf(vals[i]);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				values[i] = v;
			}
		}
	}
	
	
	/**
	 * Adjusts the default output format
	 */
	public void adjustFormat() {
		// Special formats for a list of values
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				double v = Math.abs(Math.floor(values[i]));
				if (v > 1e-18 && v < step)
					step = v;
			}
		}
		
		// Formatting depends on the step size
		double threshold = 1e-3;
		String format = "###";
		for (int i = 3; i < 18; i++) {
			if (step >= threshold)
				break;
			
			format += "#";
			threshold *= 1e-1;
		}
		
		format1 = new DecimalFormat("#." + format);
		format2 = new DecimalFormat("0." + format + "E0");
	}
	
	
	
	/**
	 * Returns parameter's name
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Returns a widget associated with this parameter
	 * @return
	 */
	public JComponent getWidget() {
		// Do not create the same widget twice
		if (widget != null)
			return widget;
		
		if (values != null) {
			final JComboBox box = new JComboBox(values);
			box.addActionListener(this);
			// TODO: make true eventually to allow new values to be
			// entered by a user (then we need to save these new values)
			box.setEditable(false);
			widget = box;
			
			widgetUpdater = new WidgetUpdater() {
				@Override
				public void update() {
					super.update();
					
					Object value = getValue();
					// TODO: other types
					if (values != null && value instanceof Double) {
						Double v = (Double) value;
						
						// TODO: simply box.setSelectedItem(v);
						for (int i = 0; i < values.length; i++) {
							if (values[i].equals(v)) {
								box.setSelectedItem(v);
								break;
							}
						}
						
					}
				}
			};
		}
		else
		// TODO: default SpinnerNumberModel does not work
		// with boolean values. Implement spinner model which
		// deals with Boolean, Integer, Double.
		if (widgetName.equals("Spinner")) {
			JSpinner spinner = new JSpinner(this);
			widget = spinner;
			widgetUpdater = new WidgetUpdater() {};
		}
		else if (widgetName.equals("Slider")) {
			final JSlider slider = new JSlider();
			widget = slider;
			
			slider.setMinimum(0);
			slider.setMaximum((int)Math.round((max - min) / step));
			slider.setValue(0);
			slider.addChangeListener(this);
			
			widgetUpdater = new WidgetUpdater() {
				@Override
				public void update() {
					super.update();
					
					// TODO: do we need to use slider.setAdj..(true)
					// in order to prevent stateChanged() events?
					Object value = getValue();
					if (value instanceof Boolean) {
						slider.setValue(((Boolean) value).booleanValue() ? slider.getMaximum() : slider.getMinimum());
					}
					else {
						double v = ((Number) value).doubleValue();
						if (v < min)
							v = min;
						else if (v > max)
							v = max;
						slider.setValue((int)Math.round((v - min) / step));
					}
				}
			};
		}
		else if (widgetName.equals("OnOff")) {
			final JCheckBox checkbox = new JCheckBox();
			widget = checkbox;
			checkbox.addActionListener(this);
			
			widgetUpdater = new WidgetUpdater() {
				@Override
				public void update() {
					super.update();
					
					Object value = getValue();
					if (value instanceof Boolean)
						checkbox.setSelected((Boolean) value);
					else
						checkbox.setSelected(((Number) value).doubleValue() > 0);
				}
			};
		}
		
		widgetUpdater.update();
		
		return widget;
	}
	
	
	/**
	 * Returns the string representation of the parameter's value
	 * @return
	 */
	public String getStringValue() {
		Object value = getValue();
		String str;
		
		if (variable.getType() == Double.class) {
			double val = (Double) value;
			if (Math.abs(val) < threshold && Math.abs(val) > 0)
				str = format2.format(val);
			else
				str = format1.format(val);
		}
		else {
			str = value.toString(); 
		}
		
		return str;
	}
	
	
	/**
	 * Returns the label which show the parameter's value
	 * @return
	 */
	public JLabel getLabel() {
		if (valueLabel == null)
			valueLabel = new JLabel(getStringValue());
		
		return valueLabel;
	}


	/**
	 * Called whenever the value of the associated variable has been changed
	 * or by the slider widget
	 */
	public void stateChanged(ChangeEvent e) {
		// Slider widget
		if (e.getSource() instanceof JSlider) {
			JSlider source = (JSlider) e.getSource();
		    
			if (!source.getValueIsAdjusting()) {
				double n = source.getValue() * step + min;
		    	
				if (variable.getType() == Integer.class)
					setValue((int) n);
				else if (variable.getType() == Boolean.class)
					setValue(n > 0);
				else if (variable.getType() == Double.class)
					setValue(n);
				else
					throw new Error("Unsupported variable type: " + variable.getType());
		    }
			
			return;
		}
		
		// TODO: modify underlying value (SpinnerNumberModel)
		if (variable.getType() != Boolean.class) {
			Number value = (Number) variable.getValue();

			try {
				// TODO: deal with integers
				if (value.doubleValue() < min)
					variable.setValue(min);
				else if (value.doubleValue() > max)
					variable.setValue(max);
				
				super.setValue(value);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		// Notify parameter's own state listeners
		fireStateChanged();
		
		// TODO: when slider is updated then again stateChanged will be called
		// which will update variable's value (slider is not precise and depends
		// on 'min', 'max', 'step'. Is it possible to enter into an infinite loop
		// in that way?
		if (widgetUpdater != null)
			widgetUpdater.update();
	}
	
	

	/**
	 * Called by the CheckBox widget
	 */
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		
		if (src instanceof JCheckBox) {
			JCheckBox source = (JCheckBox) src;
		
			if (variable.getType() == Boolean.class)
				setValue(source.isSelected());
		}
		else if (src instanceof JComboBox) {
			JComboBox source = (JComboBox) src;
			
			setValue(source.getSelectedItem());
		}
	}
	
	
	
	/**
	 * Sets parameter's value from the string value
	 * @param svalue
	 */
	public void setValue(String svalue) {
		try {
			variable.setValue(svalue);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets a new parameter's value
	 */
	@Override
	public void setValue(Object value) {
		// Boolean type is processed by this class
		if (variable.getType() != Boolean.class) {
			// TODO: fireStateChanged() will be called twice for the widget:
			// one time here and another time in stateChanged() in this class
			super.setValue(value);
		}
		
		try {
			variable.setValue(value);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Returns parameter's value.
	 * This value is always not null even if the underlying
	 * variable is null. Null value of the variable corresponds
	 * to zero value of the parameter.
	 */
	@Override
	public Object getValue() {
		Object value = variable.getValue();

		if (value == null) {
			if (variable.getType() == Boolean.class)
				return new Boolean(false);
			else if (variable.getType() == Double.class)
				return new Double(0);
			else if (variable.getType() == Integer.class)
				return new Integer(0);

			// TODO: do we need to throw an exception?
			return new Double(0);
		}
		
		return value;
	}
	
	
	/**
	 * Returns next parameter's value
	 */
	@Override
	public Object getNextValue() {
		if (variable.getType() == Boolean.class) {
			if ( ((Boolean) variable.getValue()).booleanValue() )
				return false;
			else
				return true;
		}
		
		return super.getNextValue();
	}
	
	
	/**
	 * Returns previous parameter's value
	 */
	@Override
	public Object getPreviousValue() {
		if (variable.getType() == Boolean.class) {
			return getNextValue();
		}
		
		return super.getPreviousValue();
	}


}
