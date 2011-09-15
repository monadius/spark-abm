package org.spark.runtime.external.render.images;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.media.opengl.GL;

import com.spinn3r.log5j.Logger;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;


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
		
		// OpenGL texture
		private Texture texture;
		
		/**
		 * Constructor
		 */
		protected TileImage(BufferedImage image, boolean xReflect, boolean yReflect) {
			this.image = image;
			this.xReflect = xReflect;
			this.yReflect = yReflect;
		}
		
		/**
		 * Returns the corresponding OpenGL texture
		 * @return
		 */
		public Texture getTexture() {
			if (texture != null)
				return texture;

			if (image == null)
				return null;
			
			texture = TextureIO.newTexture(image, false);
/*			texture.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER,
					GL.GL_NEAREST);
			texture.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER,
					GL.GL_NEAREST);
*/
			texture.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER,
					GL.GL_LINEAR);
			texture.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER,
					GL.GL_LINEAR);

			
			return texture;
		}
	}
	
	
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
}


