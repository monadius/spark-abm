package org.spark.runtime.external.render;

import static org.spark.utils.XmlDocUtils.getDoubleValue;
import static org.spark.utils.XmlDocUtils.getValue;
import static org.spark.utils.XmlDocUtils.getVectorValue;

import java.util.HashMap;

import org.spark.utils.Vector;
import org.spark.utils.XmlDocUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Describes a rendering style of a data layer 
 * @author Monad
 *
 */
public class DataLayerStyle {
	/* Data layer's name */
	private String name;
	
	/* Geometry of the data layer */
	private Vector[][] gridGeometry;
	
	/* Minimum and maximum values */
	private double val1, val2;
	
	/* Corresponding colors */
	private Vector color1, color2;
	
	/* Class for intermediate values (in fractions) and colors */
	public static class ColorValue {
		public double value;
		public Vector color;
		
		private ColorValue(Vector color, double value) {
			if (value < 0)
				value = 0;
			else if (value > 1)
				value = 1;
			
			this.value = value;
			this.color = color;
		}
	}

	/* Collection of all intermediate colors and values */
	private HashMap<Integer, ColorValue> colorTable = new HashMap<Integer, ColorValue>();
	
	/* Unique key is required for intermediate colors and values */
	private int keyCounter = 0;
	
	
	/* Temporary variables */
	private double[] tmpValues;
	private double[] tmpInvDifferences;
	private Vector[] tmpColors;
	
	
	/**
	 * Default constructor
	 */
	public DataLayerStyle() {
	}
	
	
	/**
	 * Copy constructor
	 * @param style
	 */
	public DataLayerStyle(DataLayerStyle style) {
		this(style.name, style.val1, style.val2, style.color1, style.color2);
	}
	
	
	/**
	 * Constructor
	 * @param name
	 * @param val1
	 * @param val2
	 * @param color1
	 * @param color2
	 */
	private DataLayerStyle(String name, double val1, double val2, Vector color1, Vector color2) {
		this.name = name;
		this.val1 = val1;
		this.val2 = val2;
		this.color1 = color1;
		this.color2 = color2;
	}
	
	
	/**
	 * Loads a data layer style from the given xml-node
	 * @param node
	 * @return
	 */
	public static DataLayerStyle LoadXml(Node node) {
		String name = getValue(node, "name", null);
		double val1 = getDoubleValue(node, "val1", 0);
		double val2 = getDoubleValue(node, "val2", 0);
		Vector color1 = getVectorValue(node, "color1", ";", new Vector(0));
		Vector color2 = getVectorValue(node, "color2", ";", new Vector(1, 0, 0));

		DataLayerStyle style = new DataLayerStyle(name, val1, val2, color1, color2);
		
		String str = getValue(node, "values-colors", null);
		if (str != null) {
			// Read intermediate colors and values
			String[] elements = str.split("/");
			
			for (int i = 0; i < elements.length; i += 2) {
				if (elements[i] == null || elements[i].length() == 0)
					break;
			
				double value = Double.parseDouble(elements[i]);
				Vector color = Vector.parseVector(elements[i + 1]);
				
				style.addColorAndValue(color, value);
			}
			
		}
		
		return style;
	}
	
	
	
	/**
	 * Saves data layer style in the given xml-node
	 * @param node
	 */
	public void SaveXml(Document doc, Node node) {
		XmlDocUtils.addAttr(doc, node, "name", name);
		XmlDocUtils.addAttr(doc, node, "val1", val1);
		XmlDocUtils.addAttr(doc, node, "color1", color1);
		XmlDocUtils.addAttr(doc, node, "val2", val2);
		XmlDocUtils.addAttr(doc, node, "color2", color2);
		
		XmlDocUtils.removeAttr(node, "values-colors");
		
		if (colorTable.size() == 0)
			return;
		
		// Save intermediate colors and values
		StringBuilder str = new StringBuilder();
		
		for (int key : colorTable.keySet()) {
			ColorValue cv = colorTable.get(key);
			
			double value = cv.value;
			Vector color = cv.color;
			
			str.append(value);
			str.append('/');
			str.append(color.toString());
			str.append('/');
		}
		
		XmlDocUtils.addAttr(doc, node, "values-colors", str);
	}
	
	
	
	public void setGeometry(Vector[][] geometry) {
		this.gridGeometry = geometry;
	}
	
	
	public Vector[][] getGeometry() {
		return gridGeometry;
	}
	
	
	public String getName() {
		return name;
	}
	
	
	public double getVal1() {
		return val1;
	}
	
	
	public double getVal2() {
		return val2;
	}
	
	
	public Vector getColor1() {
		return color1;
	}
	
	
	public Vector getColor2() {
		return color2;
	}
	
	
	public void setVal1(double val1) {
		this.val1 = val1;
	}
	
	
	public void setVal2(double val2) {
		this.val2 = val2;
	}
	
	
	public void setColor1(Vector color1) {
		this.color1 = color1;
	}
	
	
	public void setColor2(Vector color2) {
		this.color2 = color2;
	}
	
	
	public int addColorAndValue(Vector color, double value) {
		colorTable.put(keyCounter, new ColorValue(color, value));
		return keyCounter++;
	}
	
	
	public void removeColorAndValue(int key) {
		colorTable.remove(key);
	}
	
	
	public void changeColorAndValue(int key, Vector newColor, double newValue) {
		ColorValue cv = colorTable.get(key);
		
		if (cv != null) {
			cv.color = newColor;
			cv.value = newValue;
		}
	}
	
	
	public Integer[] getKeys() {
		Integer[] keys = new Integer[colorTable.size()];
		return colorTable.keySet().toArray(keys);
	}
	
	
	public ColorValue getColorValue(int key) {
		return colorTable.get(key);
	}
	
	
	/**
	 * Returns true if intermediate colors are present
	 * @return
	 */
	public boolean sortValues() {
		int n = colorTable.size();
		
		if (n == 0)
			return false;
		
		tmpValues = new double[n + 2];
		tmpColors = new Vector[n + 2];
		tmpInvDifferences = new double[n + 1];
		
		tmpValues[0] = 0;
		tmpColors[0] = color1;
		
		tmpValues[n + 1] = 1;
		tmpColors[n + 1] = color2;
		
		int i = 1;
		for (int key : colorTable.keySet()) {
			ColorValue cv = colorTable.get(key);
			tmpValues[i] = cv.value;
			tmpColors[i] = cv.color;
			i++;
		}
		
		// Sort values and colors
		for (i = 1; i < n + 1; i++) {
			double min = tmpValues[i];
			int minIndex = i;
			
			for (int j = i + 1; j < n + 1; j++) {
				double val = tmpValues[j];
				if (val < min) {
					min = val;
					minIndex = j;
				}
			}
			
			if (minIndex != i) {
				tmpValues[minIndex] = tmpValues[i];
				tmpValues[i] = min;
				
				Vector t = tmpColors[i];
				tmpColors[i] = tmpColors[minIndex];
				tmpColors[minIndex] = t;
			}
		}
		
		for (i = 0; i < n + 1; i++) {
			double dv = tmpValues[i + 1] - tmpValues[i];
			if (dv < 1e-6)
				dv = 1;
			
			tmpInvDifferences[i] = 1.0 / dv;
		}
		
		return true;
	}
	
	
	// TODO: optimize
	public Vector getColor(double value) {
		double dv = val2 - val1;
		if (dv < 1e-6)
			dv = 1;
		
		// Normalize value
		value -= val1;
		value /= dv;
		
		if (value <= 0)
			return color1;
		else if (value >= 1)
			return color2;
		
		Vector c1 = null, c2 = null;
		double t = 0;
		int n = tmpColors.length;
		
		if (n == 2) {
			// Two values
			t = value;
			c1 = color1;
			c2 = color2;
		}
		else if (n == 3) {
			// Three values
			if (value <= tmpValues[1]) {
				t = value * tmpInvDifferences[0];
				c1 = color1;
				c2 = tmpColors[1];
			}
			else {
				t = (value - tmpValues[1]) * tmpInvDifferences[1];
				c1 = tmpColors[1];
				c2 = color2;
			}
		}
		else {
			// Many values
			for (int i = 1; i < n; i++) {
				if (value <= tmpValues[i]) {
					t = (value - tmpValues[i - 1]) * tmpInvDifferences[i - 1];
					c1 = tmpColors[i - 1];
					c2 = tmpColors[i];
					break;
				}
			}
		}
		
		double tt = 1 - t;
		double r = c1.x * tt + c2.x * t;
		double g = c1.y * tt + c2.y * t;
		double b = c1.z * tt + c2.z * t;
		
		return new Vector(r, g, b);
	}
}
