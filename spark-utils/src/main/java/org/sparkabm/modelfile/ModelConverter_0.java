package org.sparkabm.modelfile;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A model converter for the first version of a model file
 *
 * @author Monad
 */
class ModelConverter_0 extends ModelConverter {
    /**
     * Creates a 0-level model converter
     */
    ModelConverter_0() {
        super(null);
    }

    @Override
    public CheckResult checkDocument(Document doc) {
        Node root = doc.getFirstChild();

        if (root == null)
            return CheckResult.OLDER_VERSION;

        if (!"model".equals(root.getNodeName()))
            return CheckResult.OLDER_VERSION;

        return CheckResult.GOOD_VERSION;
    }

    @Override
    protected Document convert0(Document doc) throws Exception {
        // Do nothing
        return doc;
    }

}
