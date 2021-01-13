package org.sparkabm.gui.render.images;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

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

