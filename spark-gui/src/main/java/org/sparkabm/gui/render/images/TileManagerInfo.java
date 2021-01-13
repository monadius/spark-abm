package org.sparkabm.gui.render.images;

import static org.sparkabm.utils.XmlDocUtils.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sparkabm.math.Vector;
import org.w3c.dom.Node;

/**
 * Describes sources of all images
 */
public class TileManagerInfo {
	private static final Logger logger = LogManager.getLogger();
	
	// Collection of all tile manager descriptions
	private static final HashMap<File, TileManagerInfo> tileManagers = new HashMap<File, TileManagerInfo>();
	
	// The corresponding tile manager
	private TileManager manager;
	
	// Root sources of images
	private ArrayList<ImageSrc> rootSources;

	
	/**
	 * Creates an empty tile manager description
	 */
	public TileManagerInfo() {
		manager = null;
		rootSources = new ArrayList<ImageSrc>();
	}
	
	
	/**
	 * Returns the corresponding tile manager
	 */
	public TileManager getTileManager() {
		return manager;
	}
	
	
	/**
	 * Loads a tile manager description from the given file
	 * @param xmlFile
	 * @return
	 */
	public static TileManagerInfo loadFromXml(File xmlFile) throws Exception {
		TileManagerInfo info = tileManagers.get(xmlFile);
		if (info != null)
			return info;
		
		info = new TileManagerInfo();
		info.loadXml(xmlFile);
		
		tileManagers.put(xmlFile, info);
		return info;
	}
			
	
	/**
	 * Loads a tile manager description from the given xml file
	 */
	private void loadXml(File xmlFile) throws Exception {
		Node root = loadXmlFile(xmlFile).getFirstChild();
		
		if (!root.getNodeName().equals("tiles")) {
			throw new Exception("Unknow file format: " + xmlFile.getName());
		}
		
		// Read main parameters
		File baseDir = xmlFile.getParentFile();
		String name = getValue(root, "name", xmlFile.getName());
		
		// Create a new tile manager
		this.manager = new TileManager(name);

		// Read (root) data sources
		this.rootSources = readDataSrc(baseDir, root);
		HashMap<String, ImageSrc> dataSrcMap = new HashMap<String, ImageSrc>();
		
		for (ImageSrc src : rootSources) {
			dataSrcMap.put(src.getId(), src);
		}
		
		// Read references
		Node refNode = getChildByTagName(root, "references");
		if (refNode == null)
			return;
		
		ArrayList<Node> sets = getChildrenByTagName(refNode, "set");
		for (Node node : sets) {
			readSet(manager, dataSrcMap, node);
		}
	}
	
	
	// Reads information about images in a set
	private static void readSet(TileManager manager, HashMap<String, ImageSrc> dataSrcMap, Node setNode) {
		String tileSet = getValue(setNode, "name", "???");
		
		ArrayList<Node> images = getChildrenByTagName(setNode, "image");
		for (Node node : images) {
			// Name
			String name = getValue(node, "name", "???");
			// Data source
			String dataSrc = getValue(node, "data", null);
			if (dataSrc == null) {
				logger.error("No data source: " + tileSet + "$" + name);
				continue;
			}
			
			// Reference within data source
			String ref = getValue(node, "ref", "");
			
			// Reflections
			boolean xReflect = getBooleanValue(node, "x-reflect", false);
			boolean yReflect = getBooleanValue(node, "y-reflect", false);
			
			ImageSrc src = dataSrcMap.get(dataSrc);
			if (src == null) {
				logger.error("Data source " + dataSrc + " is not defined for " + tileSet + "$" + name);
				continue;
			}
			
			// Load the image
			try {
				BufferedImage img = src.getImage(ref);
				if (img == null) {
					logger.error("Image haven't been loaded: " + tileSet + "$" + name);
					continue;
				}

				// Add the loaded image
				manager.addImage(tileSet, name, img, xReflect, yReflect);
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
	}
	
	
	// Reads information about data sources
	private static ArrayList<ImageSrc> readDataSrc(File baseDir, Node root) {
		ArrayList<ImageSrc> result = new ArrayList<ImageSrc>();
		
		Node dataNode = getChildByTagName(root, "data");
		if (dataNode == null)
			return result;
		
		ArrayList<Node> nodes = getAllChildren(dataNode);
		// Iterate over all nodes
		for (Node node : nodes) {
			String name = node.getNodeName().intern();
			if (name == "file") {
				result.addAll(readFileSrc(baseDir, node));
				continue;
			}
		}
		
		return result;
	}
	
	
	// Reads information about data sources associated with a file
	private static ArrayList<ImageSrc> readFileSrc(File baseDir, Node fileNode) {
		ArrayList<ImageSrc> result = new ArrayList<ImageSrc>();

		// File name
		String fname = getValue(fileNode, "name", null);
		if (fname == null) {
			logger.error("Name attribute is not defined in a file source description");
			return result;
		}
		
		File file = new File(baseDir, fname);
		if (!file.exists()) {
			file = new File(fname);
			if (!file.exists()) {
				logger.error("File " + fname + " does not exists");
				return result;
			}
		}
		
		// Id
		String id = getValue(fileNode, "id", fname);
		
		// Key color
		Vector keyColor = getVectorValue(fileNode, "key-color", ";", null);
		
		// Create the top level image source
		ImageSrc top = null;
		try {
			if (keyColor == null)
				top = new FileSrc(id, file);
			else
				top = new FileSrc(id, file, keyColor.toAWTColorInt());
		}
		catch (IOException e) {
			logger.error(e);
			return result;
		}
		
		// Iterate over all children nodes
		result.addAll(readChildrenSrc(top, fileNode));
		return result;
	}
	
	
	/**
	 * Reads in all children image sources
	 */
	private static ArrayList<ImageSrc> readChildrenSrc(ImageSrc top, Node baseNode) {
		ArrayList<ImageSrc> result = new ArrayList<ImageSrc>();
		if (top == null || baseNode == null)
			return result;
		
		ArrayList<Node> nodes = getAllChildren(baseNode);
		
		for (Node node : nodes) {
			String name = node.getNodeName().intern();
			ImageSrc src = null;

			// Tileset
			if (name == "tileset") {
				src = TileSrc.loadXml(top, node);
				if (src != null)
					result.add(src);
			}
			
			// Subimage
			if (name == "subimage") {
				src = SubimageSrc.loadXml(top, node); 
				if (src != null)
					result.add(src);
			}
			
			if (src == null)
				continue;
			
			// Recursively read sub-nodes
			result.addAll(readChildrenSrc(src, node));
		}
		
		return result;
	}

}
