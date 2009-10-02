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
	private Node root;

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

		if (file.exists()) {
			try {
				doc = db.parse(file);

				root = doc.getFirstChild();
				// Remove all #text nodes
				removeChildren(root, "#text");
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

		if (tickTime == null)
			tickTime = "1";
		
		Node tick = doc.createAttribute("tick");
		tick.setNodeValue(tickTime);
		root.getAttributes().setNamedItem(tick);
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

		for (Node child = node.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			if (child.getNodeName().equals(name))
				list.add(child);
		}

		return list;
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
		for (Node item : getChildrenByTagName(root, "setup")) {
			root.removeChild(item);
		}

		for (Node item : getChildrenByTagName(root, "classpath")) {
			root.removeChild(item);
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
		root.insertBefore(setup, root.getFirstChild());
		root.insertBefore(classPath, setup);
	}
	
	
	/**
	 * Adds information file to the model
	 */
	public void addAboutInformation(String readmePath) {
		removeChildren(root, "about");
		
		Node about = doc.createElement("about");
		Node attr = doc.createAttribute("path");
		
		attr.setNodeValue(readmePath);
		about.getAttributes().setNamedItem(attr);
		
		root.insertBefore(about, root.getFirstChild());
	}

	/**
	 * Adds agents into the document
	 * 
	 * @param agents
	 */
	public void addAgents(String packageName, ArrayList<AgentType> agentTypes) {
		// Remove old nodes
		for (Node item : getChildrenByTagName(root, "agents")) {
			root.removeChild(item);
		}

		// For compatibility remove also 'agent' and 'space-agents' nodes
		// appended to the root directly
		for (Node item : getChildrenByTagName(root, "agent")) {
			root.removeChild(item);
		}

		for (Node item : getChildrenByTagName(root, "space-agents")) {
			root.removeChild(item);
		}
		
		// Create a new 'space-agents' node
		Node agents = doc.createElement("agents");
		root.appendChild(agents);

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
		if (getChildrenByTagName(root, "mainframe").size() > 0)
			return;

		Node mainFrame = doc.createElement("mainframe");
		Node x = doc.createAttribute("x");
		Node y = doc.createAttribute("y");
		Node width = doc.createAttribute("width");
		Node height = doc.createAttribute("height");

		x.setNodeValue("0");
		y.setNodeValue("0");
		width.setNodeValue("500");
		height.setNodeValue("600");

		mainFrame.getAttributes().setNamedItem(x);
		mainFrame.getAttributes().setNamedItem(y);
		mainFrame.getAttributes().setNamedItem(width);
		mainFrame.getAttributes().setNamedItem(height);

		root.appendChild(mainFrame);
	}

	/**
	 * Adds data layers to the document
	 * 
	 * @param annotations
	 */
	public void addDataLayers(ArrayList<InterfaceAnnotation> annotations) {
		HashMap<String, Node> oldDataLayers = new HashMap<String, Node>();
		
		// Remove old nodes
		for (Node item : getChildrenByTagName(root, "data-layers")) {
			oldDataLayers = listToMap(getChildrenByTagName(item, "datalayer"), "name");
			root.removeChild(item);
		}

		// For compatibility remove also 'datalayer' nodes
		// appended to the root directly
		for (Node item : getChildrenByTagName(root, "datalayer")) {
			root.removeChild(item);
		}

		// Create a new 'data-layers' node
		Node dataLayers = doc.createElement("data-layers");
		root.appendChild(dataLayers);

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
		for (Node item : getChildrenByTagName(root, "charts")) {
			oldCharts = listToMap(getChildrenByTagName(item, "chart"), "method");
			root.removeChild(item);
		}

		// For compatibility remove also 'chart' nodes
		// appended to the root directly
		for (Node item : getChildrenByTagName(root, "chart")) {
			root.removeChild(item);
		}

		// Create a new 'charts' node
		Node charts = doc.createElement("charts");
		root.appendChild(charts);

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

				charts.appendChild(tmp);
			}
		}
	}
	
	
	
	
	/**
	 * Adds given annotations as sub-nodes of a specific node
	 * @param annotations
	 * @param parentName name of a parent node
	 */
	public void addAnnotations(ArrayList<InterfaceAnnotation> annotations, String parentName) {
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
}
