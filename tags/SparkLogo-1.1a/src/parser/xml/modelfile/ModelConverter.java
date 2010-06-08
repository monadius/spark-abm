package parser.xml.modelfile;

import org.w3c.dom.Document;

/**
 * Converts a model document to the most recent format
 * @author Monad
 *
 */
abstract class ModelConverter {
	/* Converter to the previos version document */
	private ModelConverter previousVersionConverter;
	
	
	/**
	 * Default constructor
	 * @param previous
	 */
	public ModelConverter(ModelConverter previous) {
		this.previousVersionConverter = previous;
	}
	
	
	/**
	 * Returns true if the document has the right version
	 * @param doc
	 * @return
	 */
	public abstract boolean checkDocument(Document doc);
	
	
	
	/**
	 * Converts the given model document
	 * @param doc
	 * @return
	 */
	public final Document convert(Document doc) throws Exception {
		// Document is in the right version
		if (checkDocument(doc))
			return doc;
		
		if (previousVersionConverter == null)
			throw new Exception("Document is not supported");
		
		Document result = previousVersionConverter.convert(doc);
		
		return convert0(result);
	}
	
	
	/**
	 * Converts the given model document which satisfies the previous version rules
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	protected abstract Document convert0(Document doc) throws Exception;
}
