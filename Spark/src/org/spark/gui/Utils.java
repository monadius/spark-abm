package org.spark.gui;

import java.awt.Window;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Utils {
	public static void addAttr(Document doc, Node node, String attrName, Object attrValue) {
		Node attr = doc.createAttribute(attrName);
		attr.setNodeValue(attrValue.toString());
		node.getAttributes().setNamedItem(attr);
	}

	
	
	public static String getValue(Node node, String attrName, String defaultValue) {
		Node tmp;
		
		String value = (tmp = node.getAttributes().getNamedItem(attrName)) != null ? 
				tmp.getNodeValue()
				: null;
				
		if (value == null)
			return defaultValue;
		
		return value;
	}

	
	
	public static boolean getBooleanValue(Node node, String attrName, boolean defaultValue) {
		String value = getValue(node, attrName, null);
				
		if (value == null)
			return defaultValue;
		
		return Boolean.valueOf(value);
	}

	
	public static int getIntegerValue(Node node, String attrName, int defaultValue) {
		String value = getValue(node, attrName, null);
				
		if (value == null)
			return defaultValue;
		
		return Integer.valueOf(value);
	}


	public static float getFloatValue(Node node, String attrName, float defaultValue) {
		String value = getValue(node, attrName, null);
				
		if (value == null)
			return defaultValue;
		
		return Float.valueOf(value);
	}
	

	public static double getDoubleValue(Node node, String attrName, double defaultValue) {
		String value = getValue(node, attrName, null);
				
		if (value == null)
			return defaultValue;
		
		return Double.valueOf(value);
	}


	public static File openFile(Window owner, final String extension) throws Exception {
		final JFileChooser fc = new JFileChooser(GUIModelManager.getInstance()
				.getCurrentDirectory());

		fc.setFileFilter(new FileFilter() {

			// Accept all directories and all xml files
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				
				if (extension == null)
					return true;

				String extension = getExtension(f);
				if (extension != null) {
					if (extension.equals(extension))
						return true;
					else
						return false;
				}

				return false;
			}

			// The description of this filter
			public String getDescription() {
				return "*." + extension;
			}
		});

		int returnVal = fc.showOpenDialog(owner);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			return file;
		}
		
		return null;
	}
	
	
	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}
}
