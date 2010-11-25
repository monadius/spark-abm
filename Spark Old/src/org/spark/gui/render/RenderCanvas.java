package org.spark.gui.render;

import java.awt.Dimension;

public interface RenderCanvas {
	public void display();
	
	public Dimension getSize();
	
	public void setSize(Dimension dim);
	
	public int getWidth();
	
	public int getHeight();
}
