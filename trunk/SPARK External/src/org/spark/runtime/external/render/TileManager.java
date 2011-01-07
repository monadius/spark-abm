package org.spark.runtime.external.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.spark.math.Vector;
import org.w3c.dom.Node;

import com.spinn3r.log5j.Logger;

import static org.spark.utils.XmlDocUtils.*;

/**
 * Manages images and tiles
 * @author Alexey
 */
public class TileManager {
	private final static Logger logger = Logger.getLogger();
	
	/**
	 * Describes a tile image
	 */
	static public class TileImage {
		public final BufferedImage image;
		public final boolean xReflect;
		public final boolean yReflect;
		
		protected TileImage(BufferedImage image, boolean xReflect, boolean yReflect) {
			this.image = image;
			this.xReflect = xReflect;
			this.yReflect = yReflect;
		}
	}
	
	// Collection of all tile managers
	private static final HashMap<File, TileManager> tileManagers = new HashMap<File, TileManager>();
	
	// Name
	private String name;
	
	// All images
	private final HashMap<String, TileImage> images;
	
	/**
	 * Default constructor
	 * @param name
	 */
	public TileManager(String name) {
		this.name = name;
		this.images = new HashMap<String, TileImage>();
	}
	
	/**
	 * Returns a tile manager with the given name
	 * @param name
	 * @return
	 */
	public static TileManager getTileManger(String name) {
		return tileManagers.get(name);
	}
	

	/**
	 * Name property
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Adds an image to the set of images
	 * @param tileSet
	 * @param tileName
	 * @param image
	 */
	public void addImage(String tileSet, String tileName, BufferedImage image, boolean xReflect, boolean yReflect) {
		if (image == null) {
			logger.error("Image cannot be null");
			return;
		}
		
		String name = tileSet + "$" + tileName;
		images.put(name, new TileImage(image, xReflect, yReflect));
	}
	
	
	/**
	 * Returns an image within the given tile set and with the given name
	 * @param tileSet
	 * @param tileName
	 * @return null if no image found
	 */
	public TileImage getImage(String tileSet, String tileName) {
		String name = tileSet + "$" + tileName;
		return images.get(name);
	}
	
	
	/**
	 * Returns all images
	 * @return
	 */
	public TileImage[] getAllImages() {
		TileImage[] tmp = new TileImage[images.size()];
		return images.values().toArray(tmp);
	}
	
	
	/**
	 * Loads a tile manager from the given file
	 * @param xmlFile
	 * @return
	 */
	public static TileManager loadFromXml(File xmlFile) throws Exception {
		TileManager manager = tileManagers.get(xmlFile);
		if (manager != null)
			return manager;
			
		
		Node root = loadXmlFile(xmlFile).getFirstChild();
		
		if (!root.getNodeName().equals("tiles")) {
			throw new Exception("Unknow file format: " + xmlFile.getName());
		}
		
		// Read main parameters
		File baseDir = xmlFile.getParentFile();
		String name = getValue(root, "name", xmlFile.getName());
		
		// Create a new tile manager
		manager = new TileManager(name);

		// Read data sources
		ArrayList<ImageSrc> dataSrc = readDataSrc(baseDir, root);
		HashMap<String, ImageSrc> dataSrcMap = new HashMap<String, ImageSrc>();
		
		for (ImageSrc src : dataSrc) {
			dataSrcMap.put(src.getId(), src);
		}
		
		// Read references
		Node refNode = getChildByTagName(root, "references");
		if (refNode == null)
			return manager;
		
		ArrayList<Node> sets = getChildrenByTagName(refNode, "set");
		for (Node node : sets) {
			readSet(manager, dataSrcMap, node);
		}
		
		// Add the loaded tile manager to the collection of loaded tile managers
		tileManagers.put(xmlFile, manager);
		
		return manager;
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
			BufferedImage img = src.getImage(ref);
			if (img == null) {
				logger.error("Image haven't been loaded: " + tileSet + "$" + name);
				continue;
			}
			
			// Add the loaded image
			manager.addImage(tileSet, name, img, xReflect, yReflect);
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
	
	
	// Reads information about data sources associates with a file
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
		ArrayList<Node> nodes = getAllChildren(fileNode);
		for (Node node : nodes) {
			String name = node.getNodeName().intern();

			// Tileset
			if (name == "tileset") {
				ImageSrc src = readTilesetSrc(top, node);
				if (src != null)
					result.add(src);
				continue;
			}
			
			// Subimage
			if (name == "subimage") {
				ImageSrc src = readSubimageSrc(top, node);
				if (src != null)
					result.add(src);
				continue;
			}
		}
		
		return result;
	}
	
	// Reads information about a subimage
	private static ImageSrc readSubimageSrc(ImageSrc src, Node subimageNode) {
		String id = getValue(subimageNode, "id", "tiles");
		int x = getIntegerValue(subimageNode, "x", 0);
		int y = getIntegerValue(subimageNode, "y", 0);
		int width = getIntegerValue(subimageNode, "width", -1);
		int height = getIntegerValue(subimageNode, "height", -1);
		
		return new SubimageSrc(id, src, x, y, width, height);
	}
	
	// Reads information about a tile set
	private static ImageSrc readTilesetSrc(ImageSrc src, Node tilesetNode) {
		String id = getValue(tilesetNode, "id", "tiles");
		int xSize = getIntegerValue(tilesetNode, "x-size", 1);
		int ySize = getIntegerValue(tilesetNode, "y-size", 1);
		int xTiles = getIntegerValue(tilesetNode, "x-tiles", 1);
		int yTiles = getIntegerValue(tilesetNode, "y-tiles", 1);
		
		return new TileSrc(id, src, xSize, ySize, xTiles, yTiles);
	}
}


/**
 * Source of images
 * @author Alexey
 *
 */
abstract class ImageSrc {
	// Image source identifier
	private String id;
	
	/**
	 * Protected constructor
	 * @param id
	 */
	protected ImageSrc(String id) {
		this.id = id;
	}
	
	/**
	 * Image source identifier
	 */
	public String getId() {
		return id;
	}

	/**
	 * Makes all pixels of the image with the given color transparent
	 */
	public static BufferedImage makeTransparent(Image img, Color keyColor) {
		BufferedImage alpha = new BufferedImage(img.getWidth(null), img.getHeight(null), 
				BufferedImage.TYPE_4BYTE_ABGR); 
	
		Graphics2D g = alpha.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.drawImage(img, 0, 0, null);
		g.dispose();
	
		for (int y = 0; y < alpha.getHeight(); y++) {
			for (int x = 0; x < alpha.getWidth(); x++) {
				int col = alpha.getRGB(x, y);
				if (col == keyColor.getRGB()) {
					// make transparent
					alpha.setRGB(x, y, col & 0x00ffffff);
				}
			}
		}
	
		return alpha;
	}

	
	/**
	 * Returns image with the given name
	 * @param name
	 * @return
	 */
	public abstract BufferedImage getImage(String name);
}


/**
 * Images from a file
 * @author Alexey
 *
 */
class FileSrc extends ImageSrc {
	private BufferedImage image;
	
	/**
	 * Reads an image from the given file
	 * @param file
	 * @throws IOException
	 */
	public FileSrc(String id, File file) throws IOException {
		super(id);
		this.image = ImageIO.read(file);
	}
	
	
	/**
	 * Reads an image from the given file with the given transparent color
	 */
	public FileSrc(String id, File file, Color keyColor) throws IOException {
		this(id, file);
		image = makeTransparent(image, keyColor);
	}
	
	
	public BufferedImage getImage(String name) {
		// Ignore the argument
		return image;
	}
}


/**
 * Returns a sub-image of another image
 * @author Alexey
 *
 */
class SubimageSrc extends ImageSrc {
	private ImageSrc src;
	private int x, y;
	private int w, h;
	
	/**
	 * Negative values of w or h indicates that the full dimension should be used
	 */
	public SubimageSrc(String id, ImageSrc src, int x, int y, int w, int h) {
		super(id);
		this.src = src;
		this.x = x;
		this.y = y;
		this.h = h;
		this.w = w;
	}
	
	public BufferedImage getImage(String name) {
		BufferedImage image = src.getImage(name);
		int width = (w < 0) ? image.getWidth() : w;
		int height = (h < 0) ? image.getHeight() : h;
			
		return image.getSubimage(x, y, width, height);
	}
}


/**
 * Returns images from a tile grid
 * @author Alexey
 *
 */
class TileSrc extends ImageSrc {
	private ImageSrc src;
	private int xSize, ySize;
	private int xTiles, yTiles;
	
	public TileSrc(String id, ImageSrc src, int xSize, int ySize, int xTiles, int yTiles) {
		super(id);
		this.src = src;
		this.xSize = xSize;
		this.ySize = ySize;
		
		if (xTiles < 1)
			xTiles = 1;
		
		if (yTiles < 1)
			yTiles = 1;
		
		this.xTiles = xTiles;
		this.yTiles = yTiles;
	}
	
	public BufferedImage getImage(String name) {
		BufferedImage image = src.getImage(name);
		
		String[] els = name.split(",");
		if (els == null || els.length != 2)
			return null;
		
		int x = 0;
		int y = 0;
		try {
			x = Integer.parseInt(els[0]);
			y = Integer.parseInt(els[1]);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		if (x < 0)
			x = 0;
		
		if (y < 0)
			y = 0;
		
		if (x >= xTiles)
			x = xTiles - 1;
		
		if (y >= yTiles)
			y = yTiles - 1;
		
		image = image.getSubimage(x * xSize, y * ySize, xSize, ySize);
		return image;
	}
}
