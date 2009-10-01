package parser.xml;

import java.util.HashMap;

import main.Id;
import main.Variable;
import main.type.BuiltinType;
import main.type.CompositeType;
import main.type.Method;
import main.type.Type;
import main.type.UnresolvedType;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Class for loading xml-file containing type descriptions
 * @author Monad
 *
 */
public class TypeLoader {
	/**
	 * Loads the given xml-file into the given table
	 * @param fname
	 * @param types
	 * @throws Exception
	 */
	public static void load(String fname, HashMap<Id, Type> types) throws Exception {
		javax.xml.parsers.DocumentBuilder db = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = db.parse(fname);
	
		NodeList xmlTypes = doc.getElementsByTagName("type");
		
		for (int i = 0; i < xmlTypes.getLength(); i++) {
			Type type = loadType(xmlTypes.item(i));
			// TODO: name conflicts
			types.put(type.getId(), type);
			
			if (type.alias != null)
				types.put(type.alias, type);
		}
	}
	
	
	/**
	 * Creates a type from its description
	 * @param node
	 * @return
	 */
	private static BuiltinType loadType(Node node) throws Exception {
		Type parentType = null;
		BuiltinType type;
		Id typeId = null;
		Id alias = null;
		boolean interfaceFlag = false;
		boolean compositeFlag = false;
		
		NamedNodeMap attr = node.getAttributes();
		// Read name
		Node node2 = attr.getNamedItem("name");
		
		if (node2 == null)
			throw new Exception("Name attribute is not defined");
		
		typeId = new Id(node2.getNodeValue());
		
		// Parent type?
		node2 = attr.getNamedItem("parent");
		if (node2 != null)
			parentType = new UnresolvedType(new Id(node2.getNodeValue()));
		
		// Interface?
		node2 = attr.getNamedItem("interface");
		if (node2 != null && node2.getNodeValue().equals("true"))
			interfaceFlag = true;
		
		// Composite?
		node2 = attr.getNamedItem("composite");
		if (node2 != null && node2.getNodeValue().equals("true"))
			compositeFlag = true;
		
		// Alias?
		node2 = attr.getNamedItem("alias");
		if (node2 != null)
			alias = new Id(node2.getNodeValue());
		
		// Iterate through all subnodes
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			node2 = children.item(i);
			if (node2.getNodeName().equals("javaname")) {
				typeId.setJavaName(node2.getTextContent()); 
			}
		}
		
		if (typeId == null)
			throw new Exception("Java name is not defined for a type");

		if (compositeFlag)
			type = new CompositeType(typeId, parentType);
		else
			type = new BuiltinType(typeId, parentType, interfaceFlag);
		
		loadDeclarationConversion(node, type);
		loadFields(node, type);
		loadMethods(node, type);
		
		type.alias = alias;
		
		return type;
	}
	
	
	/**
	 * Loads all methods of the given type
	 * @param node
	 * @param type
	 */
	private static void loadMethods(Node node, Type type) throws Exception {
		NodeList list = node.getChildNodes();
		
		// TODO: translations
		for (int i = 0; i < list.getLength(); i++) {
			Node node2 = list.item(i);
			if (node2.getNodeName().equals("method")) {
//				Method method = Loader.loadCommand(node2).toMethod();
				Method method = Loader.loadMethod(node2);
				type.addMethod(method);
			}
		}
	}

	
	/**
	 * Loads all declaration/conversion stuff
	 * @param node
	 * @param type
	 */
	private static void loadDeclarationConversion(Node node, Type type) throws Exception {
		NodeList list = node.getChildNodes();
		
		// TODO: translations
		for (int i = 0; i < list.getLength(); i++) {
			Node node2 = list.item(i);
			if (node2.getNodeName().equals("declaration")) {
				String declaration = node2.getTextContent().trim();
				type.setDeclarationTranslation(declaration);
			}
		}
	}

	
	
	/**
	 * Loads all fields of the given type
	 * @param node
	 * @param type
	 */
	private static void loadFields(Node node, Type type) throws Exception {
		NodeList list = node.getChildNodes();
		
		// TODO: set/get translations
		for (int i = 0; i < list.getLength(); i++) {
			Node node2 = list.item(i);
			if (node2.getNodeName().equals("field")) {
				Variable field = Loader.getVariable(node2);
				type.addField(field);
			}
		}
	}

}
