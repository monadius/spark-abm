package org.spark.gui.render;

import java.awt.Dimension;

import javax.media.opengl.GLCanvas;

public class JOGLCanvas implements RenderCanvas {
	protected GLCanvas canvas;
	
	
	public JOGLCanvas(GLCanvas canvas) {
		this.canvas = canvas; 
	}
	
	
	public void display() {
		if (canvas != null)
			canvas.display();
	}
	
	
	public Dimension getSize() {
		if (canvas == null)
			return new Dimension(0, 0);
		
		return canvas.getSize();
	}

	
	public void setSize(Dimension dim) {
		if (canvas == null)
			return;
		
		canvas.setSize(dim);
	}

	
	public int getWidth() {
		if (canvas == null)
			return 0;

		return canvas.getWidth();
	}

	
	public int getHeight() {
		if (canvas == null)
			return 0;
		
		return canvas.getHeight();
	}

}
