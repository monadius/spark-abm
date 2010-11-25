package org.spark.gui;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.awt.Window;
import java.util.ArrayList;



public final class FrameLocationManager {
	public static void saveLocationChanges(Document doc, Window win, Node node) {
		if (doc == null || node == null) 
			return;
		
		int x = win.getX();
		int y = win.getY();
		int w = win.getWidth();
		int h = win.getHeight();
		
		NamedNodeMap attributes = node.getAttributes();

		Node tmp = doc.createAttribute("x");
		tmp.setNodeValue(Integer.toString(x));
		attributes.setNamedItem(tmp);

		tmp = doc.createAttribute("y");
		tmp.setNodeValue(Integer.toString(y));
		attributes.setNamedItem(tmp);

		tmp = doc.createAttribute("width");
		tmp.setNodeValue(Integer.toString(w));
		attributes.setNamedItem(tmp);

		tmp = doc.createAttribute("height");
		tmp.setNodeValue(Integer.toString(h));
		attributes.setNamedItem(tmp);
	}
	
	
	public static void setLocation(Window win, Node node) {
		if (node == null) return;
		
		NamedNodeMap attributes = node.getAttributes();
		Node tmp;
		
		String sx = (tmp = attributes.getNamedItem("x")) != null ? tmp.getNodeValue() : "0";
		String sy = (tmp = attributes.getNamedItem("y")) != null ? tmp.getNodeValue() : "0";
		String sw = (tmp = attributes.getNamedItem("width")) != null ? tmp.getNodeValue() : "300";
		String sh = (tmp = attributes.getNamedItem("height")) != null ? tmp.getNodeValue() : "200";
		
		int x = Integer.parseInt(sx);
		int y = Integer.parseInt(sy);
		int w = Integer.parseInt(sw);
		int h = Integer.parseInt(sh);
		
		win.setLocation(x, y);
		win.setSize(w, h);
	}
	
	
	/**
	 * Computes the length of a tiling
	 * @param k number of windows
	 * @param w width of a rectangular region
	 * @param h height of a rectangular region
	 * @param preferredLength preferred length of a tiling
	 * @param lengthToHeight ratio length : height
	 * @return
	 */
	public static int computeTilingLength(int k, int w, int h, 
			int preferredLength, double lengthToHeight) {
		
		int length = preferredLength;

		// Number of windows along x axis
		int n = w / length;
		if (n == 0) {
			n = 1;
			length = w;
		}
		
		// Region is too small
		if (w * h < k)
			return 1;
		
		while (true) {
			int height = (int)(length / lengthToHeight);
			if (height <= 0)
				return 1;
			
			// Number of windows along y axis
			int m = h / height;
			
			if (m * n >= k)
				break;
			
			n += 1;
			length = w / n;
			
			if (length <= 0)
				return 1;
		}
		
		return length;
	}
	
	
	/**
	 * Tiles given frames in a specific rectangular region
	 * @param frames
	 * @param preferredLength
	 * @param lengthToHeight
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public static void tileFrames(ArrayList<Window> frames, 
			int preferredLength, double lengthToHeight, 
			int x, int y, int w, int h) {
		if (lengthToHeight < 1e-3 || w <= 1 || h <= 1)
			return;
		
		int k = frames.size();
		
		if (k == 0)
			return;
		
		int length = computeTilingLength(k, w, h, preferredLength, lengthToHeight);
		if (length <= 0)
			return;
		
		int height = (int)(length / lengthToHeight);
		if (height <= 0)
			return;
		
		int n = w / length;
		int m = h / height;
		
		int index = 0; 
		
		for (int j = 0; j < m; j++) {
			for (int i = 0; i < n; i++) {
				if (index >= k)
					return;
				
				Window frame = frames.get(index);
				frame.setSize(length, height);
				frame.setLocation(x + i * length, y + j * height);
				
				index++;
			}
		}
	}
}
