package org.spark.runtime.external.render.images;

import static org.spark.utils.XmlDocUtils.getIntegerValue;
import static org.spark.utils.XmlDocUtils.getValue;

import java.awt.image.BufferedImage;

import org.w3c.dom.Node;

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
	
	/**
	 * Returns an image
	 */
	public BufferedImage getImage(String name) {
		BufferedImage image = src.getImage(name);
		int width = (w < 0) ? image.getWidth() : w;
		int height = (h < 0) ? image.getHeight() : h;
			
		return image.getSubimage(x, y, width, height);
	}
	
	
	// Reads information about a subimage
	public static SubimageSrc loadXml(ImageSrc src, Node subimageNode) {
		String id = getValue(subimageNode, "id", "tiles");
		int x = getIntegerValue(subimageNode, "x", 0);
		int y = getIntegerValue(subimageNode, "y", 0);
		int width = getIntegerValue(subimageNode, "width", -1);
		int height = getIntegerValue(subimageNode, "height", -1);
		
		return new SubimageSrc(id, src, x, y, width, height);
	}

}