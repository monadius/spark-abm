package org.spark.modelfile;

import static org.spark.utils.XmlDocUtils.*;

import java.util.ArrayList;

import org.spark.utils.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Moves parameters from <interface> to <model>
 */
public class ModelConverter_1_1 extends ModelConverter {
	private static final Version VERSION = new Version(1, 1);

	ModelConverter_1_1() {
		super(new ModelConverter_1());
	}

	@Override
	public CheckResult checkDocument(Document doc) {
		Node root = doc.getFirstChild();

		if (root == null)
			return CheckResult.OLDER_VERSION;

		if (!"spark".equals(root.getNodeName()))
			return CheckResult.OLDER_VERSION;

		Version version = getNodeVersion(root);
		int cmp = VERSION.compare(version);

		if (cmp < 0)
			return CheckResult.NEWER_VERSION;
		if (cmp > 0)
			return CheckResult.OLDER_VERSION;

		return CheckResult.GOOD_VERSION;
	}

	@Override
	protected Document convert0(Document doc) throws Exception {
		// Modify the version of the document
		Node root = doc.getFirstChild();
		addAttr(doc, root, "version", VERSION);
		
		Node modelNode = getChildByTagName(root, "model");
		Node interfaceNode = getChildByTagName(root, "interface");
		
		Node parametersNode = doc.createElement("parameters");
		Node parframeNode = getChildByTagName(interfaceNode, "parameterframe");
		
		// Nothing else to do if there is no parameters
		if (parframeNode == null)
			return doc;
		
		ArrayList<Node> parameters = getChildrenByTagName(parframeNode, "parameter");
		for (Node par : parameters) {
			parframeNode.removeChild(par);
			parametersNode.appendChild(par);
		}
		
		modelNode.appendChild(parametersNode);
		return doc;
	}

}
