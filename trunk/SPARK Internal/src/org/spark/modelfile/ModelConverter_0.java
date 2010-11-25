package org.spark.modelfile;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A model converter for the first version of a model file
 * @author Monad
 *
 */
class ModelConverter_0 extends ModelConverter {
	/**
	 * Creates a 0-level model converter
	 */
	public ModelConverter_0() {
		super(null);
	}

	@Override
	public boolean checkDocument(Document doc) {
		Node root = doc.getFirstChild();
		
		if (root == null)
			return false;
		
		if (!"model".equals(root.getNodeName()))
			return false;
		
		return true;
	}

	@Override
	protected Document convert0(Document doc) throws Exception {
		// Do nothing
		return doc;
	}

}
