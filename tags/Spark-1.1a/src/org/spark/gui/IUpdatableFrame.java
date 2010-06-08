package org.spark.gui;

import org.w3c.dom.Document;


public interface IUpdatableFrame {
	public void writeXML(Document doc);
	
	public void updateData(long tick);
	
	public void updateData();
	
	public void reset();
}
