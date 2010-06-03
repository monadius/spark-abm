package parser.xml.modelfile;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
	public static Document loadModelFile(File fileName) throws Exception {
		Document doc = DocumentBuilderFactory.newInstance()
							.newDocumentBuilder().parse(fileName);
		
		return converter.convert(doc);
	}
	
	
	/**
	 * Saves the document with model description into the given file
	 * @param modelDoc
	 * @param fileName
	 */
	public static void saveModelFile(Document modelDoc, File fileName) throws Exception {
		if (modelDoc == null || fileName == null)
			return;
		
		modelDoc = converter.convert(modelDoc);

		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();
		
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		DOMSource source = new DOMSource(modelDoc);
		StreamResult result = new StreamResult(fileName);
		transformer.transform(source, result);		
	}
}
