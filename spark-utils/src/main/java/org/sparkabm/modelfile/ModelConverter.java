package org.sparkabm.modelfile;

import org.w3c.dom.Document;

/**
 * Converts a model document to the most recent format
 *
 * @author Monad
 */
abstract class ModelConverter {
    // Possible results of the checkDocument() function
    protected enum CheckResult {
        OLDER_VERSION,
        GOOD_VERSION,
        NEWER_VERSION
    }

    /* Converter to the previos version document */
    private ModelConverter previousVersionConverter;


    /**
     * Default constructor
     *
     * @param previous
     */
    public ModelConverter(ModelConverter previous) {
        this.previousVersionConverter = previous;
    }


    /**
     * Returns true if the document has the right version
     *
     * @param doc
     * @return
     */
    public abstract CheckResult checkDocument(Document doc);


    /**
     * Converts the given model document
     *
     * @param doc
     * @return
     */
    public final Document convert(Document doc) throws Exception {
        // Check the version of the document
        CheckResult result = checkDocument(doc);

        switch (result) {
            // Good version or newer version
            case GOOD_VERSION:
            case NEWER_VERSION:
                return doc;

            // Older version:
            case OLDER_VERSION:
                if (previousVersionConverter == null)
                    throw new Exception("Document is not supported");

                Document doc2 = previousVersionConverter.convert(doc);
                return convert0(doc2);
        }

        throw new Exception("Document is not supported");
    }


    /**
     * Converts the given model document which satisfies the previous version rules
     *
     * @param doc
     * @return
     * @throws Exception
     */
    protected abstract Document convert0(Document doc) throws Exception;
}
