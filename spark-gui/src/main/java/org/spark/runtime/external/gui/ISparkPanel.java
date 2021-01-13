package org.spark.runtime.external.gui;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Spark panel interface
 * @author Monad
 *
 */
public interface ISparkPanel {
	/**
	 * Updates information in the associated xml node
	 */
	public void updateXML(SparkWindow location, Document xmlModelDoc, Node interfaceNode, File xmlModelFile);
}
