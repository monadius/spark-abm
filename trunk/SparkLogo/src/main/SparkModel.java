package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import main.annotation.InterfaceAnnotation;
import main.annotation.VariableAnnotation;
import main.type.AgentType;
import main.type.ModelType;
import main.type.Type;
import main.type.UnknownType;

import parser.Symbol;
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
	protected final Id name;
	
	/* All user defined types: agents and models */
	protected final HashMap<Id, Type> userTypes;
	
	/* All predefined spark types */
	protected final HashMap<Id, Type> sparkTypes;
	
	/* All commands */
	protected final HashMap<String, Command> commands;

	// The main model
	protected ModelType mainModel;
	
	
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
	public Type createUserType(boolean partialFlag, Id id, Type parentType, 
				boolean agentFlag, boolean modelFlag, Symbol token) throws Exception {
		// Cannot redefine a global type
		if (sparkTypes.containsKey(id))
			throw new Exception("The type " + id + " is globally defined: " + token);
		
		Type type = userTypes.get(id);
		
		// User types can be extended using the "partial" keyword
		if (type != null) {
			if (!type.isPartial() || !partialFlag)
				throw new Exception("The type " + id + 
						" is already defined and cannot be extended (use 'partial' keyword): " + token);
			
			if (agentFlag ^ type instanceof AgentType)
				throw new Exception("Agent type " + id + " is extended by a non-agent type: " + token);
			
			if (modelFlag ^ type instanceof ModelType)
				throw new Exception("Model type " + id + " is extended by a non-model type: " + token);
			
			// TODO: simplify
			boolean sameParentFlag = false;
			Type parentType2 = type.getParentType();
			
			Id parentId1 = (parentType == null) ? null : parentType.getId();
			Id parentId2 = (parentType2 == null) ? null : parentType2.getId();
			
			if (parentId1 == null) {
				if (parentId2 == null)
					sameParentFlag = true;
				else if (parentId2.equals(new Id("$Object")))
					sameParentFlag = true;
			}
			else {
				sameParentFlag = parentId1.equals(parentId2);
			}

			if (!sameParentFlag)
				throw new Exception("Extendable types should have the same parent type: " + token);
			
			return type;
		}

		// Create a new type
		if (agentFlag)
			type = Type.createAgentType(id, parentType, partialFlag);
		else if (modelFlag) {
			if (mainModel != null)
				throw new Exception("Only one model file is allowed: " + token);
			
			this.mainModel = Type.createModelType(id, parentType, partialFlag);
			type = mainModel;
		}
		else
			type = Type.createClassType(id, parentType, partialFlag);
		
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
	 * Returns a globally defined command with the specific arguments
	 */
	public Command getCommand(String name, Type ... types) {
		Command cmd = commands.get(name);
		if (cmd == null)
			return null;
		
		if (cmd instanceof OverloadedCommand) {
			cmd = ((OverloadedCommand) cmd).findCommand(types);
		}
		else {
			if (types.length != cmd.getArgumentsNumber())
				return null;
			
			for (int i = 0; i < cmd.getArgumentsNumber(); i++) {
				Type arg = cmd.getArgument(i).type;
					
				if (!types[i].instanceOf(arg))
					return null;
			}
		}
		
		return cmd;
	}
	

	/**
	 * Returns a list of all agent types
	 * @return
	 */
	public ArrayList<AgentType> getAgentTypes() {
		ArrayList<AgentType> agents = new ArrayList<AgentType>();
		
		for (Type type : userTypes.values()) {
			if (type instanceof AgentType)
				agents.add((AgentType) type);
		}
		
		return agents;
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
	 * Adds an automatically generated global parameter
	 * @param var
	 */
	public Variable addAutoGlobalParameter(Id id) throws Exception {
		Variable var = getGlobalVariable(id.name);
		boolean existingVariable = var != null;
		boolean parameterFlag = false;
		
		if (existingVariable) {
			// Find if a parameter annotation is defined for the variable
			for (VariableAnnotation ann : var.annotations) {
				if (ann.getType() == InterfaceAnnotation.PARAMETER_ANNOTATION) {
					parameterFlag = true;
					break;
				}
			}
		}
		else {
			// Create a new variable with the undefined type
			var = new Variable(id, new UnknownType());
			var.global = true;
		}
		
		// Add a parameter annotation if necessary
		if (!parameterFlag) {
			Symbol token = Symbol.createIdentifier("parameter");
			try { 
				InterfaceAnnotation ann = InterfaceAnnotation.beginParsing(token);
				var.addAnnotation(ann);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// Add the (new) variable to the main model as a global variable
		if (!existingVariable) {
			// TODO: move it into Variable class
			String typeName = mainModel.getId().toJavaName();
			var.setTranslation(typeName + "." + var.getTranslation,
					typeName + "." + var.setTranslation);
			
			mainModel.addField(var);
		}
		
		return var;
	}
	
	
	/**
	 * Moves all global variables to the model file
	 */
	public void resolveGlobalVariables() throws Exception {
		if (mainModel == null)
			throw new Exception("No model file in the project");
		
		ArrayList<Variable> globals = new ArrayList<Variable>();
		ArrayList<Variable> fields = mainModel.getAllFields();
		
		// First, add global variables from the main model
		for (Variable var : fields) {
			if (var.global)
				globals.add(var);
		}
		
		// Then, add global variables from other types
		for (Type type : userTypes.values()) {
			if (type == mainModel)
				continue;
			
			ArrayList<Variable> localGlobals = new ArrayList<Variable>();
			fields = type.getAllFields();
			for (Variable var : fields) {
				if (var.global)
					localGlobals.add(var);
			}
			
			for (Variable var : localGlobals) {
				// Delete all global variable from the current type
				type.removeField(var);
				// Add all global variables to the model type
				mainModel.addField(var);
			}
			
			globals.addAll(localGlobals);
		}

		// Set up translation strings
		for (Variable var : globals) {
			// TODO: move it into Variable class
			String typeName = mainModel.getId().toJavaName();
			var.setTranslation(typeName + "." + var.getTranslation,
					typeName + "." + var.setTranslation);
		}
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
	 * Returns the output path where translated files will be created
	 * @param basePath
	 * @return
	 */
	public File getOutputPath(File basePath) {
		return new File(basePath, getName().toJavaName());
	}
	
	
	/**
	 * Creates java files
	 * @param outputPath
	 * @throws Exception
	 */
	public void translateToJava(File outputPath) throws Exception {
		outputPath = getOutputPath(outputPath);
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
		ArrayList<AgentType> agents = getAgentTypes();
		
		if (mainModel == null)
			throw new Exception("No model file");

//		for (ModelType model : models) {
//			File file = new File(outputPath, model.getId().toJavaName() + ".xml");
//			model.createXMLModelFile(file, agents);
//		}
		File file = new File(outputPath, mainModel.getId().toJavaName() + ".xml");
		mainModel.createXMLModelFile(file, agents);
	}
	

	
}
