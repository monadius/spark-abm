package parser.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import main.annotation.InterfaceAnnotation;
import main.annotation.ObserverAnnotation;
import main.type.AgentType;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Class for creating new model files and updating existing model files.
 * 
 * @author Monad
 */
public class ModelFileWriter {
	/* xml document */
	private Document doc;
	private File file;
//	private Node root;
	private Node modelNode;
	private Node interfaceNode;

	/**
	 * A basic constructor which creates a new model file when a given file does
	 * not exist.
	 * 
	 * @param file
	 */
	public ModelFileWriter(File file, String tickTime) throws Exception {
		this.file = file;

		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();

		Node root = null;
		
		if (file.exists()) {
			try {
				doc = db.parse(file);

				root = doc.getFirstChild();
				// Remove all #text nodes
				removeChildren(root, "#text");
				
				modelNode = getChildByTagName(root, "model");
				interfaceNode = getChildByTagName(root, "interface");
			}
			catch (Exception e) {
				doc = db.newDocument();
				root = doc.createElement("model");
				doc.appendChild(root);
			}
		} else {
			doc = db.newDocument();
			root = doc.createElement("model");
			doc.appendChild(root);
		}
		
		if (modelNode == null)
			modelNode = root;
		
		if (interfaceNode == null)
			interfaceNode = root;

		if (tickTime == null)
			tickTime = "1";
		
		addAttr(doc, modelNode, "tick", tickTime);
	}

	/**
	 * Saves the created xml file
	 */
	public void save() throws Exception {
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();

		transformer.setOutputProperty("indent", "yes");

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(file);
		transformer.transform(source, result);
	}

	/**
	 * Returns a list of children with the specified name
	 * 
	 * @param name
	 * @return
	 */
	private ArrayList<Node> getChildrenByTagName(Node node, String name) {
		ArrayList<Node> list = new ArrayList<Node>();
		if (node == null)
			return list;

		for (Node child = node.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			if (child.getNodeName().equals(name))
				list.add(child);
		}

		return list;
	}

	
	/**
	 * Returns a list of children with the specified name
	 * 
	 * @param name
	 * @return
	 */
	private Node getChildByTagName(Node node, String name) {
		if (node == null)
			return null;
		
		for (Node child = node.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			if (child.getNodeName().equals(name))
				return child;
		}

		return null;
	}

	
	
	/**
	 * Creates a hash map from the given list with keys obtained from the given
	 * attribute. It is assumed that attributes of all elements have different
	 * values (otherwise some elements will be omitted)
	 * 
	 * @param list
	 * @param attribute
	 * @return
	 */
	private HashMap<String, Node> listToMap(ArrayList<Node> list,
			String attribute) {
		HashMap<String, Node> map = new HashMap<String, Node>();

		for (Node item : list) {
			Node tmp = item.getAttributes().getNamedItem(attribute);
			String value = (tmp == null ? null : tmp.getNodeValue());

			map.put(value, item);
		}

		return map;
	}
	
	
	/**
	 * Removes all children nodes with the specified name of the given node
	 * @param node
	 */
	private void removeChildren(Node node, String name) {
		for (Node item : getChildrenByTagName(node, name)) {
			node.removeChild(item);
		}
	}
	

	/**
	 * Adds a basic setup information into the xml file.
	 * 
	 * @param classPath
	 * @param setupClass
	 */
	public void addSetupInformation(String path, String setupClass, ObserverAnnotation observer) {
		Node setup, classPath;

		// First, remove old nodes if they exist
		for (Node item : getChildrenByTagName(modelNode, "setup")) {
			modelNode.removeChild(item);
		}

		for (Node item : getChildrenByTagName(modelNode, "classpath")) {
			modelNode.removeChild(item);
		}

		// Create new nodes
		if (observer != null)
			setup = observer.toNode(doc);
		else
			setup = doc.createElement("setup");
		
		classPath = doc.createElement("classpath");

		// Fill in nodes' attributes
		Node attr = doc.createAttribute("path");
		attr.setNodeValue(path);
		classPath.getAttributes().setNamedItem(attr);

		setup.setTextContent(setupClass);

		// Add nodes to the document
		modelNode.insertBefore(setup, modelNode.getFirstChild());
		modelNode.insertBefore(classPath, setup);
	}
	
	
	/**
	 * Adds information file to the model
	 */
	public void addAboutInformation(String readmePath) {
		removeChildren(interfaceNode, "about");
		
		Node about = doc.createElement("about");
		Node attr = doc.createAttribute("path");
		
		attr.setNodeValue(readmePath);
		about.getAttributes().setNamedItem(attr);
		
		interfaceNode.insertBefore(about, interfaceNode.getFirstChild());
	}

	/**
	 * Adds agents into the document
	 * 
	 * @param agents
	 */
	public void addAgents(String packageName, ArrayList<AgentType> agentTypes) {
		// Remove old nodes
		for (Node item : getChildrenByTagName(modelNode, "agents")) {
			modelNode.removeChild(item);
		}

		// For compatibility remove also 'agent' and 'space-agents' nodes
		// appended to the root directly
		for (Node item : getChildrenByTagName(modelNode, "agent")) {
			modelNode.removeChild(item);
		}

		for (Node item : getChildrenByTagName(modelNode, "space-agents")) {
			modelNode.removeChild(item);
		}
		
		// Create a new 'space-agents' node
		Node agents = doc.createElement("agents");
		modelNode.appendChild(agents);

		// Create sub-nodes for all agents
		for (int i = 0; i < agentTypes.size(); i++) {
//			Type spaceAgent = SparkModel.getInstance().getType(
//					new Id("SpaceAgent"));
			
//			Type spaceLink = SparkModel.getInstance().getType(
//					new Id("Link"));

			AgentType agent = agentTypes.get(i);
//			if (agent.instanceOf(spaceAgent) || agent.instanceOf(spaceLink)) {
//			Node agentNode = doc.createElement("agent");
			Node agentNode = null;
			// Process agent's annotations
			for (InterfaceAnnotation ann : agent.getAnnotations()) {
				if (ann.getId().equals("step"))
					agentNode = ann.toNode(doc);
			}
			
			if (agentNode == null)
				agentNode = doc.createElement("agent");
			
			Node nameAttr = doc.createAttribute("name");

			nameAttr.setNodeValue(agent.getId().name);
			agentNode.getAttributes().setNamedItem(nameAttr);
			agentNode.setTextContent(packageName + "."
					+ agent.getId().toJavaName());

			agents.appendChild(agentNode);
//			}
		}
	}

	/**
	 * Adds a main frame to the document if it does not exist
	 */
	public void addMainFrame() {
		// Do nothing if 'mainframe' node exists
		if (getChildrenByTagName(interfaceNode, "mainframe").size() > 0)
			return;

		Node mainFrame = doc.createElement("mainframe");
		
		addAttr(doc, mainFrame, "x", 0);
		addAttr(doc, mainFrame, "y", 0);
		addAttr(doc, mainFrame, "width", 500);
		addAttr(doc, mainFrame, "height", 600);
		addAttr(doc, mainFrame, "location", "Main Window");

		interfaceNode.appendChild(mainFrame);
	}

	/**
	 * Adds data layers to the document
	 * 
	 * @param annotations
	 */
	public void addDataLayers(ArrayList<InterfaceAnnotation> annotations) {
		HashMap<String, Node> oldDataLayers = new HashMap<String, Node>();
		
		// Remove old nodes
		for (Node item : getChildrenByTagName(interfaceNode, "data-layers")) {
			oldDataLayers = listToMap(getChildrenByTagName(item, "datalayer"), "name");
			interfaceNode.removeChild(item);
		}

		// For compatibility remove also 'datalayer' nodes
		// appended to the root directly
		for (Node item : getChildrenByTagName(interfaceNode, "datalayer")) {
			interfaceNode.removeChild(item);
		}

		// Create a new 'data-layers' node
		Node dataLayers = doc.createElement("data-layers");
		interfaceNode.appendChild(dataLayers);

		for (InterfaceAnnotation annotation : annotations) {
			Node tmp = annotation.toNode(doc);
			if (tmp != null) {
				// Always add manually created annotation
				if (!annotation.isAutoGenerated()) {
					dataLayers.appendChild(tmp);
					continue;
				}
				
				// TODO: namedItem == null?
				String name = tmp.getAttributes().getNamedItem("name").getNodeValue();
				
				// Do not replace already defined data layer
				if (oldDataLayers.containsKey(name)) {
					dataLayers.appendChild(oldDataLayers.get(name));
				}
				else {
					dataLayers.appendChild(tmp);
				}
			}
		}
	}
	
	
	
	/**
	 * Adds charts to the document
	 * @param annotations
	 */
	public void addCharts(ArrayList<InterfaceAnnotation> annotations) {
		HashMap<String, Node> oldCharts = new HashMap<String, Node>();
		
		// Remove old nodes
		for (Node item : getChildrenByTagName(interfaceNode, "charts")) {
			oldCharts = listToMap(getChildrenByTagName(item, "chart"), "method");
			interfaceNode.removeChild(item);
		}

		for (Node item : getChildrenByTagName(interfaceNode, "chart")) {
			interfaceNode.removeChild(item);
			String method = getStringValue(item, "method", null);
			if (method != null)
				oldCharts.put(method, item);
		}

		// Create a new 'charts' node
//		Node charts = doc.createElement("charts");
//		root.appendChild(charts);

		for (InterfaceAnnotation annotation : annotations) {
			Node tmp = annotation.toNode(doc);
			if (tmp != null) {
				// TODO: namedItem == null?
				String method = tmp.getAttributes().getNamedItem("method").getNodeValue();
				
				// Add position and size information from the old definition
				if (oldCharts.containsKey(method)) {
					NamedNodeMap attrs = tmp.getAttributes();
					tmp = oldCharts.get(method);
					for (int i = 0; i < attrs.getLength(); i++) {
						tmp.getAttributes().setNamedItem(attrs.item(i).cloneNode(false));
					}
				}

				interfaceNode.appendChild(tmp);
			}
		}
	}
	
	
	
	
	/**
	 * Adds given annotations as sub-nodes of a specific node
	 * @param annotations
	 * @param parentName name of a parent node
	 * @param modelFlag indicates that the annotations should be added to the model node
	 */
	public void addAnnotations(ArrayList<InterfaceAnnotation> annotations, String parentName, boolean modelFlag, String parentLocation) {
		Node root = modelFlag ? modelNode : interfaceNode;
		
		ArrayList<Node> list = getChildrenByTagName(root, parentName);
		Node parent = null;

		if (annotations.size() == 0) {
			// No annotations
			removeChildren(root, parentName);
			return;
		}

		boolean oldNodesRemoved = false;
		
		if (list.size() > 0) {
			parent = list.get(0);

			// Remove old nodes
			removeChildren(parent, "#text");
		}
		else {
			parent = doc.createElement(parentName);
			root.appendChild(parent);
		}
		
		if (parentLocation != null)
			addAttr(doc, parent, "location", parentLocation);
		
		for (InterfaceAnnotation annotation : annotations) {
			Node tmp = annotation.toNode(doc);
			if (tmp != null) {
				// Remove old nodes if they were not removed before
				if (!oldNodesRemoved) {
					String name = tmp.getNodeName();
					removeChildren(parent, name);
					oldNodesRemoved = true;
				}
				
				parent.appendChild(tmp);
			}
		}
	}
	
	
	/**
	 * Adds the attribute to the given node
	 * @param doc
	 * @param node
	 * @param attrName
	 * @param attrValue
	 */
	public static void addAttr(Document doc, Node node, String attrName, Object attrValue) {
		Node attr = doc.createAttribute(attrName);
		attr.setNodeValue(attrValue.toString());
		node.getAttributes().setNamedItem(attr);
	}

	
	
	/**
	 * Gets a string value of the given attribute
	 * @param node
	 * @param attrName
	 * @param defaultValue
	 * @return
	 */
	public static String getStringValue(Node node, String attrName, String defaultValue) {
		Node tmp;
		
		String value = (tmp = node.getAttributes().getNamedItem(attrName)) != null ? 
				tmp.getNodeValue()
				: null;
				
		if (value == null)
			return defaultValue;
		
		return value;
	}

	
	/**
	 * Gets a boolean value of the given attribute
	 * @param node
	 * @param attrName
	 * @param defaultValue
	 * @return
	 */
	public static boolean getBooleanValue(Node node, String attrName, boolean defaultValue) {
		String value = getStringValue(node, attrName, null);
				
		if (value == null)
			return defaultValue;
		
		return Boolean.valueOf(value);
	}

	
	/**
	 * Gets an integer value of the given attribute
	 * @param node
	 * @param attrName
	 * @param defaultValue
	 * @return
	 */
	public static int getIntegerValue(Node node, String attrName, int defaultValue) {
		String value = getStringValue(node, attrName, null);
				
		if (value == null)
			return defaultValue;
		
		try {
			return Integer.valueOf(value);
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}


	/**
	 * Gets a float value of the given attribute
	 * @param node
	 * @param attrName
	 * @param defaultValue
	 * @return
	 */
	public static float getFloatValue(Node node, String attrName, float defaultValue) {
		String value = getStringValue(node, attrName, null);
				
		if (value == null)
			return defaultValue;
		
		try {
			return Float.valueOf(value);
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	

	/**
	 * Gets a double value of the given attribute
	 * @param node
	 * @param attrName
	 * @param defaultValue
	 * @return
	 */
	public static double getDoubleValue(Node node, String attrName, double defaultValue) {
		String value = getStringValue(node, attrName, null);
				
		if (value == null)
			return defaultValue;
		
		try {
			return Double.valueOf(value);
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}
}
