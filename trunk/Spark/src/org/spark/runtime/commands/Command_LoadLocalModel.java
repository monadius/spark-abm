package org.spark.runtime.commands;

import java.io.File;

import org.spark.core.SparkModel;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;
import org.w3c.dom.Document;

/**
 * Command for loading a model
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_LoadLocalModel extends ModelManagerCommand {
	private Document xmlDoc;
	private File path;
	
	public Command_LoadLocalModel(Document xmlDoc, File path) {
		this.xmlDoc = xmlDoc;
		this.path = path;
	}
	
	public Document getXmlDoc() {
		return xmlDoc;
	}
	
	public File getPath() {
		return path;
	}
	
	public void execute(SparkModel model, AbstractSimulationEngine engine) {
	}
}
