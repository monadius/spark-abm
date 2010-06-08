package parser.xml;

import javasrc.Translation;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import main.Command;
import main.Id;
import main.SparkModel;
import main.Variable;
import main.type.*;

import org.w3c.dom.*;



/**
 * Command loader
 * @author Monad
 *
 */
public class Loader {
	/**
	 * Loads all commands
	 * @param fname
	 * @throws Exception
	 */
	public static void load(String fname) throws Exception {
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = db.parse(fname);
		
		loadReporters(doc);
		loadCommands(doc);
	}
	
	
	/**
	 * Returns the type by its name
	 * @param node
	 * @return
	 * @throws Exception
	 */
	private static Type getType(Node node, String attributeName) throws Exception {
		NamedNodeMap attr = node.getAttributes();
		Node tmp;

		String sType = (tmp = attr.getNamedItem(attributeName)) != null ? tmp.getNodeValue() : "";
		
		// TODO: do as follows
		// type = SparkModel.getType(new Id(sType))
		// with sType = $name, $method, $self, $myself, $parent, $this
		// Be sure that the types are initialized and added to SparkModel
		
		// Resolve NameType
		if (sType.equals("NameType"))
			return NameType.getInstance();
		
		// Resolve MethodType
		if (sType.equals("MethodType"))
			return MethodType.getInstance(); 
		
		// Resolve SelfType
		if (sType.equals("SelfType"))
			return SelfType.getInstance();
		
		// Resolve MyselfType
		if (sType.equals("MyselfType"))
			return MyselfType.getInstance();

		// Resolve ParentType
		if (sType.equals("ParentType"))
			return ParentType.getInstance();
		
		// Resolve ThisType
		if (sType.equals("ThisType"))
			return ThisType.getInstance();
		
		// Resolve ArgumentType
		if (sType.startsWith("ArgumentType")) {
			return new UnresolvedType(new Id(sType)).resolveDeclarationTypes();
		}
		
		Type type = SparkModel.getInstance().getType(new Id(sType));
	
		if (type == null) {
			type = new UnresolvedType(new Id(sType));
		}
		
		return type;
	}
	

	/**
	 * Creates a variable
	 * @param node
	 * @return
	 * @throws Exception
	 */
	protected static Variable getVariable(Node node) throws Exception {
		NamedNodeMap attr = node.getAttributes();
		
		Type type = getType(node, "type");
		String name = attr.getNamedItem("name").getNodeValue();
//		String sValue = (node = attr.getNamedItem("value")) != null ? node.getNodeValue() : null;
		
//		Object value = (sValue != null) ? type.parse(sValue) : null;

		// Load set/get translations
		String setTranslation = null;
		String getTranslation = null;

		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node node2 = list.item(i);
			if (node2.getNodeName().equals("set")) {
				setTranslation = node2.getTextContent();
			}
			else if (node2.getNodeName().equals("get")) {
				getTranslation = node2.getTextContent();
			}
		}
		
		Variable var = new Variable(new Id(name), type);
		if (setTranslation != null || getTranslation != null)
			var.setTranslation(getTranslation, setTranslation);
		else {
			var.setDefaultFieldTranslation();
		}
		
		return var;
	}
	
	
	/**
	 * Loads command description
	 */
	private static void loadArgumentsAndTranslation(Node node, Command cmd) throws Exception {
		Node tmp;
		NamedNodeMap attr;
		
		NodeList nodes2 = node.getChildNodes();
		for (int j = 0; j < nodes2.getLength(); j++) {
			tmp = nodes2.item(j);
			/* argument node */
			if (tmp.getNodeName().equals("argument")) {
				attr = tmp.getAttributes();
				String optional = "false";
				if (attr.getNamedItem("optional") != null)
					optional = attr.getNamedItem("optional").getNodeValue();

				cmd.addArgument(getVariable(tmp), Boolean.parseBoolean(optional));
			}
			/* block node */
			else if (tmp.getNodeName().equals("block")) {
				attr = tmp.getAttributes();
				String optional = "false";
				if (attr.getNamedItem("optional") != null)
					optional = attr.getNamedItem("optional").getNodeValue();
			
				String name = "block";
				if (attr.getNamedItem("name") != null) {
					name = attr.getNamedItem("name").getNodeValue();
				}
				
				Type selfType = null;
				if (attr.getNamedItem("selftype") != null) {
					selfType = getType(tmp, "selftype");
				}

				Type type;
				if (selfType != null)
					type = new BlockType(selfType);
				else
					type = BlockType.getInstance();

				cmd.addArgument(new Variable(new Id(name), type), Boolean.parseBoolean(optional));
			}
			/* translation node */
			else if (tmp.getNodeName().equals("translation")) {
				cmd.setTranslation(new Translation(tmp));
			}
		}
		
	}
	

	/**
	 * Loads all reporters
	 * @param doc
	 * @throws Exception
	 */
	private static void loadReporters(Document doc) throws Exception {
		NodeList nodes = doc.getElementsByTagName("reporter");

		for (int i = 0; i < nodes.getLength(); i++) {
			Node tmp, node = nodes.item(i);
			NamedNodeMap attr = node.getAttributes();
			
			String name = attr.getNamedItem("name").getNodeValue();
			String alias = (tmp = attr.getNamedItem("alias")) != null ? tmp.getNodeValue() : null;
			String infix = (tmp = attr.getNamedItem("infix")) != null ? tmp.getNodeValue() : "false";
			String precedence = (tmp = attr.getNamedItem("precedence")) != null ? tmp.getNodeValue() : "7";
			Type type = getType(node, "type");
			Type subtype = getType(node, "subtype"); 
			
			Command reporter = new Command(name);
			reporter.setReturnType(type);
			reporter.setReturnSubtype(subtype);
			reporter.setInfix(Boolean.parseBoolean(infix));
			reporter.setPrecedence(Integer.parseInt(precedence));

			loadArgumentsAndTranslation(node, reporter);
			
			SparkModel.getInstance().addCommand(reporter);
			if (alias != null) 
				SparkModel.getInstance().addCommand(alias, reporter);
		}

	}


	/**
	 * Loads all commands
	 * @param doc
	 * @throws Exception
	 */
	private static void loadCommands(Document doc) throws Exception {
		NodeList nodes = doc.getElementsByTagName("command");
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Node tmp, node = nodes.item(i);
			NamedNodeMap attr = node.getAttributes();
			
			String name = attr.getNamedItem("name").getNodeValue();
			String alias = (tmp = attr.getNamedItem("alias")) != null ? tmp.getNodeValue() : null;
			String infix = (tmp = attr.getNamedItem("infix")) != null ? tmp.getNodeValue() : "false";
			String precedence = (tmp = attr.getNamedItem("precedence")) != null ? tmp.getNodeValue() : "7";
			
			Command cmd = new Command(name);
			cmd.setInfix(Boolean.parseBoolean(infix));
			cmd.setPrecedence(Integer.parseInt(precedence));

			loadArgumentsAndTranslation(node, cmd);
			
			SparkModel.getInstance().addCommand(cmd);
			if (alias != null) 
				SparkModel.getInstance().addCommand(alias, cmd);
		}
	}
	
	
	/**
	 * Loads a command
	 * @param node
	 * @throws Exception
	 */
	public static Method loadMethod(Node node) throws Exception {
		NamedNodeMap attr = node.getAttributes();
		Node tmp;
		
		String name = attr.getNamedItem("name").getNodeValue();
		String infix = (tmp = attr.getNamedItem("infix")) != null ? tmp.getNodeValue() : "false";
		String precedence = (tmp = attr.getNamedItem("precedence")) != null ? tmp.getNodeValue() : "7";
		String sabstract = (tmp = attr.getNamedItem("abstract")) != null ? tmp.getNodeValue() : "false";
		Type type = getType(node, "type");
		Type subtype = getType(node, "subtype");
		
		Command cmd = new Command(name);
		cmd.setReturnType(type);
		cmd.setReturnSubtype(subtype);
		cmd.setInfix(Boolean.parseBoolean(infix));
		cmd.setPrecedence(Integer.parseInt(precedence));
		
		loadArgumentsAndTranslation(node, cmd);

		Method method = cmd.toMethod();
		if (sabstract.equals("true"))
			method.setAbstractFlag(true);
		
		return method;
	}
	
	
}
