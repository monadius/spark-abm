package parser.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import main.annotation.InterfaceAnnotation;
import main.annotation.ObserverAnnotation;
import main.type.AgentType;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import parser.xml.modelfile.ModelFileLoader;

import static parser.xml.XmlDocUtils.*;

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
	private Node filesNode;

	/**
	 * A basic constructor which creates a new model file when a given file does
	 * not exist.
	 * 
	 * @param file
	 */
	public ModelFileWriter(File file, String tickTime) throws Exception {
		this.file = file;
		Node root = null;
		
		if (file.exists()) {
			try {
				doc = ModelFileLoader.loadModelFile(file);

				root = doc.getFirstChild();
				// Remove all #text nodes
				removeChildren(root, "#text");
				
				modelNode = getChildByTagName(root, "model");
				interfaceNode = getChildByTagName(root, "interface");
				filesNode = getChildByTagName(root, "files");
				
				if (modelNode == null)
					modelNode = root;
				
				if (interfaceNode == null)
					interfaceNode = root;
				
				if (filesNode == null)
					filesNode = root;
			}
			catch (Exception e) {
				doc = createNewDocument();
			}
		} else {
			doc = createNewDocument();
		}
		
		if (tickTime == null)
			tickTime = "1";
		
		removeChildren(modelNode, "#text");
		removeChildren(interfaceNode, "#text");
		removeChildren(filesNode, "#text");
		
		addAttr(doc, modelNode, "tick", tickTime);
	}
	
	
	/**
	 * Creates a new document
	 * @param file
	 * @return
	 */
	private Document createNewDocument() throws Exception {
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
										.newDocumentBuilder();
		Document doc = db.newDocument();
		Node root = doc.createElement("spark");
		addAttr(doc, root, "version", 1);
		doc.appendChild(root);
		
		filesNode = doc.createElement("files");
		modelNode = doc.createElement("model");
		interfaceNode = doc.createElement("interface");
		
		root.appendChild(filesNode);
		root.appendChild(modelNode);
		root.appendChild(interfaceNode);
		
		return doc;
	}
	

	/**
	 * Saves the created xml file
	 */
	public void save() throws Exception {
		ModelFileLoader.saveModelFile(doc, file);
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
	 * Adds a basic setup information into the xml file.
	 * 
	 * @param classPath
	 * @param setupClass
	 */
	public void addSetupInformation(String path, String setupClass, ObserverAnnotation observer) {
		Node setup;

		// First, remove old nodes if they exist
		removeChildren(modelNode, "setup");
		removeChildren(modelNode, "classpath");

		// Create new nodes
		if (observer != null)
			setup = observer.toNode(doc);
		else
			setup = doc.createElement("setup");
		
		// Fill in nodes' attributes
		addAttr(doc, filesNode, "path", path);
		addAttr(doc, filesNode, "all", true);

		setup.setTextContent(setupClass);

		// Add nodes to the document
		modelNode.insertBefore(setup, modelNode.getFirstChild());
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
		removeChildren(modelNode, "agents");
		removeChildren(modelNode, "agent");
		removeChildren(modelNode, "space-agents");

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
		// Manage windows first
		Node windowsNode = getChildByTagName(interfaceNode, "windows");
		ArrayList<Node> windows = getChildrenByTagName(windowsNode, "window");
		String location = null;
		
		for (Node window : windows) {
			if (getBooleanValue(window, "main", false)) {
				location = getStringValue(window, "name", null);
				break;
			}
		}
		
		// Create main window
		if (location == null) {
			if (windowsNode == null) {
				windowsNode = doc.createElement("windows");
				interfaceNode.appendChild(windowsNode);
			}
			
			Node window = doc.createElement("window");
			addAttr(doc, window, "x", 0);
			addAttr(doc, window, "y", 0);
			addAttr(doc, window, "width", 500);
			addAttr(doc, window, "height", 600);
			addAttr(doc, window, "main", true);
			
			location = "Main Window";
			addAttr(doc, window, "name", location);
			
			windowsNode.appendChild(window);
		}

		
		// Do nothing if 'mainframe' node exists
		Node mainFrame = getChildByTagName(interfaceNode, "mainframe");

		if (mainFrame == null) {
			mainFrame = doc.createElement("mainframe");
			interfaceNode.appendChild(mainFrame);
		}
		
		addAttr(doc, mainFrame, "location", location);
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
		removeChildren(interfaceNode, "datalayer");

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
		// Remove old nodes
		removeChildren(interfaceNode, "charts");
		removeChildren(interfaceNode, "chart");

		// Create new nodes
		for (InterfaceAnnotation annotation : annotations) {
			Node tmp = annotation.toNode(doc);
			if (tmp != null) {
				String location = getStringValue(tmp, "name", "Chart");
				addAttr(doc, tmp, "location", location);
				interfaceNode.appendChild(tmp);
			}
		}
	}
	
	
	
	/**
	 * Adds methods to the document
	 * @param annotations
	 */
	public void addMethods(ArrayList<InterfaceAnnotation> methods) {
		addAnnotations(methods, "methods", true, null);
		
		removeChildren(interfaceNode, "methods-panel");
		
		if (methods.size() > 0) {
			Node panel = doc.createElement("methods-panel");
			addAttr(doc, panel, "location", "Methods");
			interfaceNode.appendChild(panel);
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
	
	
}
