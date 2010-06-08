package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import main.type.AgentType;
import main.type.ModelType;
import main.type.Type;

import parser.xml.Loader;
import parser.xml.TypeLoader;

/**
 * Global namespace of the model
 * @author Monad
 *
 */
public class SparkModel {
	/* The only model instance */
	private static SparkModel theModel;	
	
	/* Model's name */
	protected Id name;
	
	/* All user defined types: agents and models */
	protected HashMap<Id, Type> userTypes;
	
	/* All predefined spark types */
	protected HashMap<Id, Type> sparkTypes;
	
	/* All commands */
	protected HashMap<String, Command> commands;
	
	
	/**
	 * Returns the only model instance
	 * @return
	 */
	public static SparkModel getInstance() {
		return theModel;
	}
	
	
	/**
	 * Creates a new model
	 * @param name
	 */
	public static void init(String name) throws Exception {
//		if (theModel != null) {
//			throw new Error("Model is already created");
		
		// Create the main class
		theModel = new SparkModel(name);
		
		// Load all predefined types
		TypeLoader.load("logo/SparkTypes.xml", theModel.sparkTypes);
		// Load all predefined commands
		Loader.load("logo/commands.xml");
	}
	
	
	public Id getName() {
		return name;
	}
	
	
	/**
	 * Creates a new spark model
	 * @param name
	 */
	private SparkModel(String name) {
		this.name = new Id(name);
		
		userTypes = new HashMap<Id, Type>();
		sparkTypes = new HashMap<Id, Type>();
		commands = new HashMap<String, Command>();
	}
	
	
	/**
	 * Adds a new global command
	 * @param command
	 */
	public void addCommand(Command command) throws Exception {
		// TODO: overloading
		if (commands.containsKey(command.name)) {
			Command cmd = commands.get(command.name);
			if (cmd instanceof OverloadedCommand) {
				((OverloadedCommand) cmd).addCommand(command);
			}
			else {
				OverloadedCommand cmd2 = new OverloadedCommand(cmd);
				cmd2.addCommand(command);
				commands.put(command.name, cmd2);
			}

			return;
//			throw new Exception("Command " + command.name + " is already defined");
		}
		
		commands.put(command.name, command);
	}
	
	
	/**
	 * Adds a new global command with the given alias name
	 * @param command
	 */
	public void addCommand(String alias, Command command) throws Exception {
		// TODO: overloading
		if (commands.containsKey(alias))
			throw new Exception("Command " + alias + " is already defined");
		
		commands.put(alias, command);
	}
	
	
	/**
	 * Creates a new user type
	 * @param type
	 * @return
	 */
	public Type createUserType(Id id, Type parentType, boolean agentFlag, boolean modelFlag) throws Exception {
		if (userTypes.containsKey(id) || sparkTypes.containsKey(id))
			throw new Exception("The type " + id + " is already defined");

		Type type;
		if (agentFlag)
			type = Type.createAgentType(id, parentType);
		else if (modelFlag)
			type = Type.createModelType(id, parentType);
		else
			type = Type.createClassType(id, parentType);
		
		userTypes.put(type.getId(), type);
		
		return type;
	}
	
	
	
	/**
	 * Checks whether the given global name exists
	 * @param id
	 * @return true if the given name exists
	 */
	public boolean checkGlobalNameConflicts(Id id) {
		for (Type type : userTypes.values()) {
			Variable var = type.getField(id);
			
			if (var != null) {
				if (var.global)
					return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Returns a globally defined command
	 * @param name
	 * @return null if no command found
	 */
	public Command getCommand(String name) {
		return commands.get(name);
	}
	
	
	
	/**
	 * Returns a type by id
	 * @param id
	 * @return null if no such type
	 */
	public Type getType(Id id) {
		if (sparkTypes.containsKey(id))
			return sparkTypes.get(id);
		else
			return userTypes.get(id);
	}
	
	
	/**
	 * Returns a global variable (a field or a constant)
	 * @param name
	 * @return null if no variable found
	 */
	public Variable getGlobalVariable(String name) {
		Id id = new Id(name);
		for (Type type : userTypes.values()) {
			Variable var = type.getField(id);
			if (var != null && var.global)
				return var;
		}

		// TODO: constant
		
		return null;
	}
	
	
	
	/**
	 * Tries to resolve types of fields, methods, etc.
	 */
	public void resolveDeclarationTypes() throws Exception {
		for (Type type : sparkTypes.values()) {
			type.resolveDeclarationTypes();
		}
		
		for (Type type : userTypes.values()) {
			type.resolveDeclarationTypes();
		}
	}
	
	
	
	/**
	 * Parses all methods for all types
	 */
	public void parseMethods() throws Exception {
		// First parse all models
		for (Type type : userTypes.values()) {
			if (type instanceof ModelType)
				type.parseMethods();
		}
		
		for (Type type : userTypes.values()) {
			if (type instanceof ModelType)
				continue;
			type.parseMethods();
		}
	}
	
	
	/**
	 * Creates java files
	 * @param outputPath
	 * @throws Exception
	 */
	public void translateToJava(File outputPath) throws Exception {
		outputPath = new File(outputPath, getName().toJavaName());
		outputPath.mkdirs();
		
		for (Type type : userTypes.values()) {
			type.translateToJava(outputPath);
		}
	}

	
	
	/**
	 * Creates xml model description files
	 * @param outputPath
	 * @throws Exception
	 */
	public void createXMLFiles(File outputPath) throws Exception {
		ArrayList<AgentType> agents = new ArrayList<AgentType>();
		ArrayList<ModelType> models = new ArrayList<ModelType>();
		
		for (Type type : userTypes.values()) {
			if (type instanceof AgentType)
				agents.add((AgentType) type);
			else if (type instanceof ModelType)
				models.add((ModelType) type);
		}

		for (ModelType model : models) {
			File file = new File(outputPath, model.getId().toJavaName() + ".xml");
			model.createXMLModelFile(file, agents);
		}
	}
	

	
}
