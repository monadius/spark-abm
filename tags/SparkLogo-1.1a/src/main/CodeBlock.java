package main;

import java.util.ArrayList;
import java.util.HashMap;

import main.type.Method;
import main.type.Type;

import javasrc.JavaEmitter;

import parser.tree.CommandNode;

/**
 * A block of code inside method containing all commands
 * @author Monad
 *
 */
public class CodeBlock {
	/* A reference to a parent block */
	protected CodeBlock parentBlock;
	/* A reference to the underlying method */
	protected Method method;
	/* All local variables defined in this block */
	protected HashMap<Id, Variable> localVariables;
	/* Reference to the active object */
	protected Variable self;
	/* Reference to the calling object */
	protected Variable myself;
	/* Auxiliary variable for defining local self variables */
	protected int level;
	
	/* Code itself */
	protected ArrayList<CommandNode> commands;
	
	/**
	 * Creates a code block
	 * @param parent
	 */
	public CodeBlock(CodeBlock parent) {
		parentBlock = parent;
		localVariables = new HashMap<Id, Variable>();
		commands = new ArrayList<CommandNode>();
		
		// Set default self and method references
		if (parentBlock != null) {
			self = parentBlock.self;
			myself = parentBlock.myself;
			method = parentBlock.method;
			level = parentBlock.level + 1;
		}
	}
	
	
	/**
	 * Sets the method reference
	 * @param method
	 */
	public void setMethod(Method method) {
		this.method = method;
	}
	
	
	/**
	 * Returns a method to which the block belongs
	 * @return
	 */
	public Method getMethod() {
		return method;
	}
	
	
	/**
	 * Returns current active (self) type
	 * @return
	 */
	public Type getSelfType() {
		if (self != null)
			return self.type;
		return null;
	}
	
	
	/**
	 * Returns 'myself' type
	 * @return
	 */
	public Type getMyselfType() {
		if (myself != null)
			return myself.type;
		
		return null;
	}
	
	
	/**
	 * Returns the self variable
	 * @return
	 */
	public Variable getSelfVariable() {
		return self;
	}
	
	
	/**
	 * Returns the myself variable
	 * @return
	 */
	public Variable getMyselfVariable() {
		return myself;
	}
	
	
	/**
	 * Sets default self reference based on the given type
	 * @param type
	 */
	public void setDefaultSelf(Type type) {
		self = new Variable(new Id("this"), type);
		myself = self;
	}
	
	
	/**
	 * Sets self reference to a temporary variable with the given type
	 * @param type
	 */
	public void setSelf(Type type) {
		myself = self;
		self = new Variable(new Id("__agent" + level), type);
//		addLocalVariable(self);
	}
	
	
	/**
	 * Adds a local variable
	 * @param var
	 */
	public void addLocalVariable(Variable var) {
		// TODO: name conflicts
		localVariables.put(var.id, var);
	}
	
	
	/**
	 * Adds a command and resolves its type
	 * @param command
	 */
	public void addCommand(CommandNode command) throws Exception {
		command.pushCodeBlock(this);
		command.resolveType(null, 0);
		command.popCodeBlock();
		commands.add(command);
	}
	
	
	/**
	 * Returns commands
	 * @return
	 */
	public ArrayList<CommandNode> getCommands() {
		return commands;
	}
	
	
	/**
	 * Returns a local variable from this block or from
	 * the parent block
	 * @param name
	 * @return null if no variable found
	 */
	public Variable getLocalVariable(String name) {
		Id id = new Id(name);
		Variable var = localVariables.get(id);
		
		if (var != null)
			return var;
		
		if (parentBlock != null)
			return parentBlock.getLocalVariable(name);
		
		return null;
	}
	
	
	
	/**
	 * Translates code into Java
	 * @param java
	 */
	public void translate(JavaEmitter java, int flag) {
		// Declare local variables
		for (Variable var : localVariables.values()) {
			try {
//				java.field("", var.type.getTranslationString(), var.id.toJavaName(), null);
				if (var.doNotDeclare)
					continue;
				
				java.println(var.getDeclarationTranslation());
			}
			catch (Exception e) {
				e.printStackTrace();
				java.println("//Error during local variable declaration");
			}
		}

		if (localVariables.size() != 0) {
			java.emptyLine();
		}
		
		// Set base buffer
		java.pushBaseBuffer();
		
		// Translate commands
		for (int i = 0; i < commands.size(); i++) {
			try {
				commands.get(i).translate(java, flag);
				java.endJavaLine();
			}
			catch (Exception e) {
				e.printStackTrace();
				java.println("//Error during translation");
			}
		}
		
		java.popBaseBuffer();
	}
}
