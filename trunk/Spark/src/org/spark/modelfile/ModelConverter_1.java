package org.spark.modelfile;

import static org.spark.utils.XmlDocUtils.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Level 1 model converter
 * @author Monad
 *
 */
class ModelConverter_1 extends ModelConverter {
	public ModelConverter_1() {
		super(new ModelConverter_0());
	}
	
	
	@Override
	public boolean checkDocument(Document doc) {
		Node root = doc.getFirstChild();
		
		if (root == null)
			return false;

		if (!"spark".equals(root.getNodeName()))
			return false;

		int version = getIntegerValue(root, "version", 0);
		if (version != 1)
			return false;
		
		return true;
	}

	
	@Override
	protected Document convert0(Document doc) throws Exception {
		// Create a new document
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document result = db.newDocument();

		// Create a new root node
		Node newRoot = result.createElement("spark");
		Node nameAttr = result.createAttribute("version");
		nameAttr.setNodeValue("1");
		
		newRoot.getAttributes().setNamedItem(nameAttr);
		result.appendChild(newRoot);
		
		// Create 'model' and 'interface' nodes
		Node model = result.createElement("model");
		Node interfaceNode = result.createElement("interface");
		
		newRoot.appendChild(model);
		newRoot.appendChild(interfaceNode);

		// Iterate over all nodes in 'doc'
		Node root = doc.getFirstChild();
		NodeList nodes = root.getChildNodes();
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String name = node.getNodeName().intern();

			node = result.importNode(node, true);
			
			if (name == "classpath" ||
					name == "setup" ||
					name == "agents" ||
					name == "variables" ||
					name == "methods") {
				model.appendChild(node);
			}
			else {
				interfaceNode.appendChild(node);
			}
		}
		
		return result;
	}

}
