package org.spark.runtime.external.gui;

import static org.spark.utils.XmlDocUtils.*;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * Loads windows from an xml document
 * @author Monad
 *
 */
public class XML_WindowsLoader {
	/**
	 * Loads windows from the given node containing SPARK interface information 
	 * @param windowManager
	 * @param node
	 */
	public static void loadWindows(WindowManager windowManager, Node node) {
		ArrayList<Node> windows = getChildrenByTagName(node, "windows");
		
		// No windows
		if (windows.size() == 0)
			return;
		
		windows = getChildrenByTagName(windows.get(0), "window");
		WindowManager.SparkWindowFactory factory = windowManager.getWindowFactory();
		
		for (Node winNode : windows) {
			String name = getValue(winNode, "name", "?");
			int x = getIntegerValue(winNode, "x", 0);
			int y = getIntegerValue(winNode, "y", 0);
			int width = getIntegerValue(winNode, "width", 300);
			int height = getIntegerValue(winNode, "height", 300);
			boolean visible = getBooleanValue(winNode, "visible", true);
			boolean main = getBooleanValue(winNode, "main", false);
			
			if (width <= 0)
				width = 300;
			
			if (height <= 0)
				height = 300;
			
			if (!main) {
				SparkWindow window = factory.createWindow(name, x, y, width, height);
				window.setVisible(visible);
			}
			else {
				windowManager.getMainWindow().setName(name);
				windowManager.getMainWindow().setLocation(x, y, width, height);
			}
		}
	}
	
	
	/**
	 * Saves information about windows in the given xml document
	 * @param windowManager
	 * @param doc
	 * @param node
	 */
	public static void saveWindows(WindowManager windowManager, Document xmlModelDoc, Node interfaceNode, File xmlModelFile) {
		ArrayList<Node> winNodes = getChildrenByTagName(interfaceNode, "windows");
		Node windowsNode = null;
		
		// No windows
		if (winNodes.size() == 0) {
			windowsNode = xmlModelDoc.createElement("windows");
			interfaceNode.appendChild(windowsNode);
		}
		else {
			windowsNode = winNodes.get(0);
		}
		
		// Remove old definitions of windows
		removeChildren(windowsNode, "window");
		
		// Remove junk as well
		removeChildren(windowsNode, "#text");
		
		SparkWindow[] windows = windowManager.getWindows();
		for (SparkWindow win : windows) {
			saveWindow(xmlModelDoc, windowsNode, win, false);
			
			// TODO: location of the panel is determined by the window
			// If the name of the window has been changed then the location
			// also need to be updated. Now, it is done for ViewPanel only.
			ISparkPanel panel = win.getPanel();
			if (panel != null)
				panel.updateXML(win, xmlModelDoc, interfaceNode, xmlModelFile);
		}
		
		SparkWindow mainWindow = windowManager.getMainWindow();
		if (mainWindow != null) {
			saveWindow(xmlModelDoc, windowsNode, mainWindow, true);
			
			ISparkPanel panel = mainWindow.getPanel();
			if (panel != null)
				panel.updateXML(mainWindow, xmlModelDoc, interfaceNode, xmlModelFile);
		}
	}
	
	
	/**
	 * Creates a node for the given window
	 * @param windowsNode
	 * @param win
	 */
	private static void saveWindow(Document doc, Node windowsNode, SparkWindow win, boolean main) {
		String name = win.getName();
		Rectangle loc = win.getLocation();
		
		// Create a new xml node
		Node winNode = doc.createElement("window");

		addAttr(doc, winNode, "name", name);
		addAttr(doc, winNode, "x", loc.x);
		addAttr(doc, winNode, "y", loc.y);
		addAttr(doc, winNode, "width", loc.width);
		addAttr(doc, winNode, "height", loc.height);
		addAttr(doc, winNode, "visible", win.isVisible());
		if (main)
			addAttr(doc, winNode, "main", main);
		
		windowsNode.appendChild(winNode);
	}
}
