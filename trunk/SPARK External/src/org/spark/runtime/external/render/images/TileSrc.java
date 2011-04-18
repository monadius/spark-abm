package org.spark.runtime.external.render.images;

import static org.spark.utils.XmlDocUtils.getIntegerValue;
import static org.spark.utils.XmlDocUtils.getValue;

import java.awt.image.BufferedImage;

import org.w3c.dom.Node;

/**
 * Returns images from a tile grid
 * @author Alexey
 *
 */
class TileSrc extends ImageSrc {
	private ImageSrc src;
	private int xSize, ySize;
	private int xTiles, yTiles;
	
	/**
	 * Constructs a tile source
	 */
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

	/**
	 * Returns an image
	 */
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
	
	
	// Reads information about a tile set
	public static TileSrc loadXml(ImageSrc src, Node tilesetNode) {
		String id = getValue(tilesetNode, "id", "tiles");
		int xSize = getIntegerValue(tilesetNode, "x-size", 1);
		int ySize = getIntegerValue(tilesetNode, "y-size", 1);
		int xTiles = getIntegerValue(tilesetNode, "x-tiles", 1);
		int yTiles = getIntegerValue(tilesetNode, "y-tiles", 1);
		
		return new TileSrc(id, src, xSize, ySize, xTiles, yTiles);
	}
}
