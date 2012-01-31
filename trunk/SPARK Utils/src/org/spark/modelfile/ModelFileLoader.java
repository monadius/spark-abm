package org.spark.modelfile;

import java.io.File;

import org.spark.utils.XmlDocUtils;
import org.w3c.dom.Document;

/**
 * Loads a model file 
 * @author Monad
 */
public class ModelFileLoader {
	/* Current model converter */
	private static final ModelConverter converter = new ModelConverter_1();
	
	
	/**
	 * Loads the given file as a spark model file and converts it to the most current version
	 * @param fileName
	 * @return
	 */
	public static Document loadModelFile(File file) throws Exception {
		Document doc = XmlDocUtils.loadXmlFile(file);
		if (doc == null)
			throw new Exception("Error: cannot open xml-file " + file);
		
		Document doc2 = converter.convert(doc);
		if (converter.checkDocument(doc2) != ModelConverter.CheckResult.GOOD_VERSION)
			throw new Exception("Unsupported xml-file " + file);
		
		return doc2;
	}
	
	
	/**
	 * Saves the document with model description into the given file
	 * @param modelDoc
	 * @param fileName
	 */
	public static void saveModelFile(Document modelDoc, File file) throws Exception {
		if (modelDoc == null || file == null)
			return;
		
		modelDoc = converter.convert(modelDoc);
		XmlDocUtils.saveDocument(modelDoc, file);
	}
}
