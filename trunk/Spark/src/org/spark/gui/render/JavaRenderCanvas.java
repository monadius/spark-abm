package org.spark.gui.render;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class JavaRenderCanvas extends Canvas {
	private static final long serialVersionUID = 1L;

	private JavaRender render;
	boolean reshape = true;
	
	private Image bufferImage;
	
	
	public JavaRenderCanvas(JavaRender render) {
		super();
		this.render = render;
	}
	
	
	public void takeSnapshot(String fname) {
		if (bufferImage != null) {
			try {
				BufferedImage buffer = new BufferedImage(bufferImage.getWidth(this),
									bufferImage.getHeight(this),
									BufferedImage.TYPE_3BYTE_BGR);
			
				Graphics g = buffer.getGraphics();
				if (g != null) {
					g.drawImage(bufferImage, 0, 0, this);
				}

				g.dispose();
				
				// FIXME: sometime picture is not saved or saved with errors
				ImageIO.write(buffer, "png", new File(fname + ".png"));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void display() {
		if (bufferImage == null) {
			if (getWidth() <= 0 || getHeight() <= 0)
				return;
			bufferImage = this.createImage(getWidth(), getHeight());
			if (bufferImage == null)
				return;
		}
		
//		Graphics g = this.getGraphics();
		Graphics g = bufferImage.getGraphics();
		display(g);
		if (g != null)
			g.dispose();

		g = this.getGraphics();
		if (g != null) {
			g.drawImage(bufferImage, 0, 0, this);
			g.dispose();
		}
	}
	
	
	public void display(Graphics g) {
		if (reshape || render.reshapeRequested) {
			render.reshapeRequested = false;
			reshape = false;
			render.reshape(0, 0, getWidth(), getHeight());
		}

		if (g == null)
			return;
		
		
		g.clearRect(0, 0, getWidth(), getHeight());
		render.display((Graphics2D)g);
	}
	
	
	@Override
	public void paint(Graphics g) {
		display(g);
	}
	
	
	@Override
	public void validate() {
		super.validate();
		reshape = true;
	}
	
	
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		
		if (width > 0 && height > 0)
			bufferImage = this.createImage(width, height);

		reshape = true;
	}
}
