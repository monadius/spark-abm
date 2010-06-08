package javasrc;

import java.util.ArrayList;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import parser.tree.TreeNode;

/**
 * Class for translation rules
 * @author Monad
 *
 */
public class Translation {
	/* Main translation string */
	protected String translationString;
	
	/* Prelude text */
	protected String preludeString;
	
	/* Return text */
	protected String returnString;
	
	/**
	 * Temporary variable representation
	 */
	static class TemporaryVariable {
		/* Name tag */
		public final String nameTag;
		/* Type name (could contain arguments) */
		public final String typeNameString;
		
		public TemporaryVariable(String name, String type) {
			this.nameTag = name;
			this.typeNameString = type;
		}
	}
	
	/* Collection of temporary variables */
	protected final ArrayList<TemporaryVariable> tempVars;
	
	
	/**
	 * Creates a translation from a string
	 * @param translationString
	 */
	public Translation(String translationString) {
		this.tempVars = new ArrayList<TemporaryVariable>();
		this.translationString = translationString; 
	}
	
	/**
	 * Creates a translation from an xml-node
	 * @param node
	 */
	public Translation(Node node) throws Exception {
		tempVars = new ArrayList<TemporaryVariable>();
		
		NamedNodeMap attr;
		Node tmp;
		
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node node2 = list.item(i);

			// Get a temporary variable
			if (node2.getNodeName().equals("temp")) {
				attr = node2.getAttributes();

				String sType = (tmp = attr.getNamedItem("type")) != null ? tmp.getNodeValue() : null;
				String name = (tmp = attr.getNamedItem("name")) != null ? tmp.getNodeValue() : "";

				tempVars.add(new TemporaryVariable(name, sType));
				continue;
			}
			
			// Get prelude text
			if (node2.getNodeName().equals("prelude")) {
				preludeString = node2.getTextContent().trim();
				continue;
			}

			// Get prelude text
			if (node2.getNodeName().equals("return")) {
				returnString = node2.getTextContent().trim();
				continue;
			}

			// Get translation text
			if (node2.getNodeName().equals("#text")) {
				translationString = node2.getNodeValue().trim();
				continue;
			}

		}
		
		
	}
	
	
	// TODO: better solution to the problem
	/**
	 * Returns true if self variable is processed explicitly
	 * @return
	 */
//	public boolean explicitReturn() {
//		return returnString != null;
//	}
	
	
	
	/**
	 * Performs a translation transformation
	 * All buffers should be prepared before calling this method
	 * Do not forget to clear buffers after this method
	 * @param java
	 */
	public void doTranslation(JavaEmitter java, int flag) throws Exception {
		for (int i = 0; i < tempVars.size(); i++) {
			TemporaryVariable var = tempVars.get(i);
			String tempName = java.createTempVariable(var.typeNameString, var.nameTag);
		
			java.addBuffer("@" + var.nameTag);
			java.setActiveBuffer("@" + var.nameTag);
			java.print(tempName);
			java.endBuffer();
		}
		
		if (preludeString != null) {
			java.printTextToBaseBuffer(preludeString);
		}
		
		if (translationString != null && !translationString.equals(""))
			java.printText(translationString);
		
		if (returnString != null && (flag & TreeNode.GET_VALUE) != 0) {
			java.printText(returnString);
		}

	}
}
