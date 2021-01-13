package org.sparkabm.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sparkabm.utils.XmlDocUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Model parameter
 * 
 * @author Monad
 */
@SuppressWarnings("serial")
public class Parameter extends SpinnerNumberModel implements ChangeListener,
		ActionListener {
	/* Associated variable */
	private ProxyVariable variable;

	/*
	 * A list of specific values: if it is not null, then only values from this
	 * list can be assigned to the parameter
	 */
	// TODO: other value types
	private Double[] values;

	/* Parameter's name */
	private String name;

	/**
	 * Parameter's options
	 */
	public static class Options {
		public final double min, max, step;

		/**
		 * Constructor
		 */
		public Options(double min, double max, double step) {
			if (max < min)
				max = min;

			if (step <= 0.0)
				step = 1.0;

			this.min = min;
			this.max = max;
			this.step = step;
		}

		/**
		 * Copy constructor
		 */
		public Options(Options opt) {
			this(opt.min, opt.max, opt.step);
		}
		
		/**
		 * Saves options in the given node attributes
		 */
		public void saveXml(Document doc, Node node) {
			XmlDocUtils.addAttr(doc, node, "min", min);
			XmlDocUtils.addAttr(doc, node, "max", max);
			XmlDocUtils.addAttr(doc, node, "step", step);
		}
		
		/**
		 * Creates options based on the given xml-node
		 */
		public static Options loadXml(Node node) {
			double min = XmlDocUtils.getDoubleValue(node, "min", 0.0);
			double max = XmlDocUtils.getDoubleValue(node, "max", 0.0);
			double step = XmlDocUtils.getDoubleValue(node, "step", 0.0);
			
			return new Options(min, max, step);
		}
	}

	/* Parameter's default options */
	private Options defaultOptions;
	/* Parameter's options defined by a user */
	private Options userOptions;

	/* Indicates if user options must be used */
	private boolean useUserOptions;

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
	Parameter() {
	}

	/**
	 * Initializes the parameter
	 */
	void init(String name, ProxyVariable var, double min, double max,
			double step) {
		this.name = name;
		this.variable = var;
		this.defaultOptions = new Options(min, max, step);
		this.userOptions = new Options(min, max, step);
		this.useUserOptions = false;

		var.addChangeListener(this);
		updateModel();
	}

	/**
	 * Returns the corresponding variable
	 */
	public ProxyVariable getVariable() {
		return variable;
	}

	/**
	 * Returns min value of the parameter
	 * 
	 * @return
	 */
	public double getMin() {
		if (useUserOptions)
			return userOptions.min;
		else
			return defaultOptions.min;
	}

	/**
	 * Returns max value of the parameter
	 * 
	 * @return
	 */
	public double getMax() {
		if (useUserOptions)
			return userOptions.max;
		else
			return defaultOptions.max;
	}

	/**
	 * Returns step size for the parameter
	 * 
	 * @return
	 */
	public double getStep() {
		if (useUserOptions)
			return userOptions.step;
		else
			return defaultOptions.step;
	}

	/**
	 * Sets a specific list of parameter's values
	 * 
	 * @param vals
	 */
	void setValues(String[] vals) {
		values = new Double[vals.length];
		double min = 0.0, max = 0.0;

		for (int i = 0; i < vals.length; i++) {
			Double v = 0.0;

			try {
				v = Double.valueOf(vals[i]);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				values[i] = v;
				if (v < min)
					min = v;

				if (v > max)
					max = v;
			}
		}

		this.defaultOptions = new Options(min, max, defaultOptions.step);
		updateModel();
	}

	/**
	 * Updates the related data models
	 */
	private void updateModel() {
		double min = getMin();
		double max = getMax();
		double step = getStep();

		setMinimum(min);
		setMaximum(max);
		setStepSize(step);
		adjustFormat(step);

		if (widget instanceof JSlider) {
			double v = ((Number) getValue()).doubleValue();
			JSlider slider = (JSlider) widget;
			slider.setMaximum((int) Math.round((max - min) / step));

			if (v < min)
				v = min;
			else if (v > max)
				v = max;
			slider.setValue((int) Math.round((v - min) / step));
		}
	}
	
	/**
	 * Returns true if user-defined options are used
	 */
	public boolean isUsingUserOptions() {
		return useUserOptions;
	}
	
	/**
	 * Sets the useUserOptions flag
	 */
	public void activateUserOptions(boolean flag) {
		if (this.useUserOptions != flag) {
			this.useUserOptions = flag;
			updateModel();
		}
	}
	
	
	/**
	 * Returns user options
	 */
	public Options getUserOptions() {
		return userOptions;
	}
	
	
	/**
	 * Sets user options
	 */
	public void setUserOptions(Options opt) {
		this.userOptions = new Options(opt);
		if (useUserOptions) {
			updateModel();
		}
	}
	

	/**
	 * Adjusts the default output format
	 */
	private void adjustFormat(double step) {
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
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns a widget associated with this parameter
	 * 
	 * @return
	 */
	public JComponent getWidget() {
		// Do not create the same widget twice
		if (widget != null)
			return widget;

		int type = variable.getType();

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
		/*
		 * else if (widgetName.equals("Spinner")) { JSpinner spinner = new
		 * JSpinner(this); widget = spinner; widgetUpdater = new WidgetUpdater()
		 * {}; }
		 */
		else if (type == ProxyVariable.DOUBLE_TYPE) {
			final JSlider slider = new JSlider();
			widget = slider;

			double min = getMin();
			double max = getMax();
			double step = getStep();

			slider.setMinimum(0);
			slider.setMaximum((int) Math.round((max - min) / step));
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
						slider
								.setValue(((Boolean) value).booleanValue() ? slider
										.getMaximum()
										: slider.getMinimum());
					} else {
						double min = getMin();
						double max = getMax();
						double step = getStep();

						double v = ((Number) value).doubleValue();
						if (v < min)
							v = min;
						else if (v > max)
							v = max;
						slider.setValue((int) Math.round((v - min) / step));
					}
				}
			};
		} else if (type == ProxyVariable.BOOL_TYPE) {
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
						checkbox
								.setSelected(((Number) value).doubleValue() > 0);
				}
			};
		}

		widgetUpdater.update();

		return widget;
	}

	/**
	 * Returns the string representation of the parameter's value
	 * 
	 * @return
	 */
	public String getStringValue() {
		Object value = getValue();
		String str;

		if (variable.getType() == ProxyVariable.DOUBLE_TYPE) {
			double val = (Double) value;
			if (Math.abs(val) < threshold && Math.abs(val) > 0)
				str = format2.format(val);
			else
				str = format1.format(val);
		} else {
			str = value.toString();
		}

		return str;
	}

	/**
	 * Returns the label which show the parameter's value
	 * 
	 * @return
	 */
	public JLabel getLabel() {
		if (valueLabel == null)
			valueLabel = new JLabel(getStringValue());

		return valueLabel;
	}

	/**
	 * Called whenever the value of the associated variable has been changed or
	 * by the slider widget
	 */
	public void stateChanged(ChangeEvent e) {
		// Slider widget
		if (e.getSource() instanceof JSlider) {
			JSlider source = (JSlider) e.getSource();

			if (!source.getValueIsAdjusting()) {
				double n = source.getValue() * getStep() + getMin();

				switch (variable.getType()) {
				case ProxyVariable.BOOL_TYPE:
					setValue(n > 0);
					break;

				case ProxyVariable.DOUBLE_TYPE:
					setValue(n);
					break;

				default:
					throw new Error("Unsupported variable type: "
							+ variable.getTypeName());
				}
			}

			return;
		}

		// TODO: modify underlying value (SpinnerNumberModel)
		if (variable.getType() != ProxyVariable.BOOL_TYPE) {
			Number value = (Number) variable.getValue();

			try {
				double min = getMin();
				double max = getMax();

				// TODO: deal with integers
				if (value.doubleValue() < min)
					variable.setValue(min);
				else if (value.doubleValue() > max)
					variable.setValue(max);

				super.setValue(value);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		// Notify parameter's own state listeners
		fireStateChanged();

		// TODO: when slider is updated then again stateChanged will be called
		// which will update variable's value (slider is not precise and depends
		// on 'min', 'max', 'step'. Is it possible to enter into an infinite
		// loop
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

			if (variable.getType() == ProxyVariable.BOOL_TYPE)
				setValue(source.isSelected());
		} else if (src instanceof JComboBox) {
			JComboBox source = (JComboBox) src;

			setValue(source.getSelectedItem());
		}
	}

	/**
	 * Sets parameter's value from the string value
	 * 
	 * @param svalue
	 */
	public void setValue(String svalue) {
		try {
			variable.setValue(svalue);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets a new parameter's value
	 */
	@Override
	public void setValue(Object value) {
		// Boolean type is processed by this class
		if (variable.getType() != ProxyVariable.BOOL_TYPE) {
			// TODO: fireStateChanged() will be called twice for the widget:
			// one time here and another time in stateChanged() in this class
			super.setValue(value);
		}

		try {
			variable.setValue(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns parameter's value. This value is always not null even if the
	 * underlying variable is null. Null value of the variable corresponds to
	 * zero value of the parameter.
	 */
	@Override
	public Object getValue() {
		Object value = variable.getValue();

		if (value == null) {
			switch (variable.getType()) {
			case ProxyVariable.BOOL_TYPE:
				return new Boolean(false);

			case ProxyVariable.DOUBLE_TYPE:
				return new Double(0.0);
			}

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
		if (variable.getType() == ProxyVariable.BOOL_TYPE) {
			if (((Boolean) variable.getValue()).booleanValue())
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
		if (variable.getType() == ProxyVariable.BOOL_TYPE) {
			return getNextValue();
		}

		return super.getPreviousValue();
	}

}
