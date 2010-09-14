package org.spark.runtime.commands;

import java.io.File;

import org.spark.core.SparkModel;
import org.spark.modelfile.ModelFileLoader;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;
import org.spark.runtime.internal.manager.IModelManager;
import org.spark.utils.XmlDocUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Command for loading a model
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_LoadModel extends ModelManagerCommand {
//	private Document xmlDoc;
	private File modelFilePath;
	private File rootPath;
	
	/* Description of a model */
	private Document modelXmlDoc;
	
	/* A file transfer object */
	private FileTransfer fileTransfer;
	
	/**
	 * Creates a load command for the given model file
	 * @param modelFilePath
	 * @param manager
	 */
	public Command_LoadModel(File modelFilePath, IModelManager manager) {
		try {
			this.modelFilePath = modelFilePath;
			this.rootPath = null;
			this.modelXmlDoc = ModelFileLoader.loadModelFile(modelFilePath);
		
			Node filesNode = XmlDocUtils.getChildByTagName(modelXmlDoc.getFirstChild(), "files");
			this.fileTransfer = manager.createFileTransfer(filesNode, modelFilePath);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	public Document getXmlDoc() {
//		return xmlDoc;
//	}
	
	/**
	 * Returns model's document
	 */
	public Document getModelDocument() throws Exception {
		if (modelXmlDoc == null) {
			modelXmlDoc = ModelFileLoader.loadModelFile(modelFilePath);
		}
		
		return modelXmlDoc;
	}
	
	
	/**
	 * Returns a path to the model files
	 * @return
	 */
	public File getRootPath(File tmpPath) {
		if (fileTransfer != null) {
			String tmpName = String.valueOf(System.currentTimeMillis());
			
			try {
				rootPath = new File(tmpPath, tmpName);
				rootPath.mkdirs();
				fileTransfer.restoreFiles(rootPath);
			}
			catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
			return rootPath;
		}
		
		if (rootPath == null) {
			try {
				Document doc = getModelDocument();
				Node files = XmlDocUtils.getChildByTagName(doc.getFirstChild(), "files");
				
				if (files == null)
					return null;
				
				rootPath = new File(modelFilePath.getParentFile(), XmlDocUtils.getValue(files, "path", null));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return rootPath;
	}
	
	
	/**
	 * Does nothing
	 */
	public void execute(SparkModel model, AbstractSimulationEngine engine) {
	}
}
