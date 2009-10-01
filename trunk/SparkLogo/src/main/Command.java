package main;

import java.util.ArrayList;

import javasrc.Translation;

import main.type.BlockType;
import main.type.Method;
import main.type.NameType;
import main.type.Type;

import parser.SymbolList;
import parser.tree.CommandNode;
import parser.tree.SyntaxTreeBuilder;
import parser.tree.TreeNode;


public class Command {
	/**
	 * Argument description
	 * @author Monad
	 *
	 */
	protected static class Argument {
		public boolean optional;
		/* type and name description */
		public Variable var;
		
		public Argument(Variable var, boolean opt) {
			this.var = var;
			this.optional = opt;
		}
		
		public Argument(Variable var) {
			this.var = var;
			this.optional = false;
		}
	}
	
	
	protected final String name;
	protected final ArrayList<Argument> arguments = new ArrayList<Argument>();
	/* Return type */
	protected Type	returnType;
	/* Sub-type for composite return type */
	// FIXME: remove this: sub-type should be a part of the composite type
	protected Type	returnSubtype;
	
	/* Command properties */
	private boolean infix;
	private int		precedence;
	
	/* Translation */
	protected Translation translation;
	
	
	/**
	 * Sets the translation string
	 * @param str
	 */
	public void setTranslation(Translation translation) {
		this.translation = translation;
	}
	
	
	/**
	 * Returns the translation string
	 * @return
	 */
	public Translation getTranslation() {
		return translation;
	}

	/**
	 * Sets the infix flag
	 * @param infix
	 */
	public void setInfix(boolean infix) {
		this.infix = infix;
	}
	
	
	/**
	 * Returns true if the command is infix
	 * @return
	 */
	public boolean isInfix() {
		return infix;
	}
	
	
	/**
	 * Sets the precedence
	 * @param n
	 * @throws Exception
	 */
	public void setPrecedence(int n) throws Exception {
		if (n < 0 || n > 8)
			throw new Exception("setPrecedence(): precedence should be between 0 and 8 for command " + name);
		this.precedence = n;
	}
	

	/**
	 * Returns the precedence
	 * @return
	 */
	public int getPrecedence() {
		return precedence;
	}
	
	
	/**
	 * Sets the return type
	 * @param type
	 */
	public void setReturnType(Type type) {
		this.returnType = type;
	}
	
	
	/**
	 * Sets the return sub-type
	 * @param type
	 */
	public void setReturnSubtype(Type type) {
		this.returnSubtype = type;
	}
	
	
	/**
	 * Gets the return type
	 * @return
	 */
	public Type getReturnType() {
		return returnType;
	}

	
	/**
	 * Gets the return sub-type
	 * @return
	 */
	public Type getReturnSubtype() {
		return returnSubtype;
	}

	
	/**
	 * Returns the name
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	
	

	/**
	 * Creates a command
	 * @param name
	 */
	public Command(String name) {
		this.name = name;
		this.returnType = null;
		this.infix = false;
		this.precedence = 7;
	}
	
	
	/**
	 * Returns true if the return type is not null
	 * @return
	 */
//	public boolean isReporter() {
//		return returnType != null;
//	}
	

	/**
	 * Adds an argument
	 * @param arg
	 */
	public void addArgument(Variable arg) {
		addArgument(arg, false);
	}


	/**
	 * Adds an optional argument
	 * @param arg
	 * @param optional
	 */
	public void addArgument(Variable arg, boolean optional) {
		// TODO: name conflict
		arguments.add(new Argument(arg, optional));
	}
	
	
	/**
	 * Returns the i-th argument
	 * @param i
	 * @return
	 */
	public Variable getArgument(int i) {
		return arguments.get(i).var;
	}
	
	
	/**
	 * Return all arguments as an array list
	 * @return
	 */
	public ArrayList<Variable> getArguments() {
		ArrayList<Variable> args = new ArrayList<Variable>(arguments.size());
		
		for (int i = 0; i < arguments.size(); i++) {
			args.add(arguments.get(i).var);
		}
		
		return args;
	}
	
	
	/**
	 * Returns the number of arguments
	 * @return
	 */
	public int getArgumentsNumber() {
		return arguments.size();
	}
	
	

	/**
	 * Parses arguments of an infix reporter
	 * @param list
	 * @param node
	 * @param first first argument should be already parsed
	 * @throws Exception
	 */
	public void parseInfixCommandArguments(SymbolList list, CommandNode node, TreeNode first) throws Exception {
		node.addNode(first);
		
		for (int i = 1; i < arguments.size(); i++) {
			Argument arg = arguments.get(i);
			
			if (arg.var.type instanceof BlockType) {
				if (arg.optional)
					node.addNode(SyntaxTreeBuilder.optionalBlockNode(list, (BlockType) arg.var.type, first));
				else
					node.addNode(SyntaxTreeBuilder.blockNode(list, (BlockType) arg.var.type, null, first));
			}
			else if (arg.var.type instanceof NameType) {
				node.addNode(SyntaxTreeBuilder.nameNode(list));
			}
			else {
				if (arg.optional)
					throw new Exception("Non-block parameters cannot be optional");
				node.addNode(SyntaxTreeBuilder.operationNode(list, precedence + 1));
			}
		}
		
	}

	
	
	public void parseCommandArguments(SymbolList list, CommandNode node) throws Exception {
		for (int i = 0; i < arguments.size(); i++) {
			Argument arg = arguments.get(i);
			
			if (arg.var.type instanceof BlockType) {
				if (arg.optional)
					node.addNode(SyntaxTreeBuilder.optionalBlockNode(list, (BlockType) arg.var.type, node.getNode(0)));
				else
					node.addNode(SyntaxTreeBuilder.blockNode(list, (BlockType) arg.var.type, null, node.getNode(0)));
			}
			else if (arg.var.type instanceof NameType) {
				node.addNode(SyntaxTreeBuilder.nameNode(list));
			}
			else {
				if (arg.optional)
					throw new Exception("Non-block parameters cannot be optional");
				
				// Non-void return type requires different arguments treatment
				// to comply with NetLogo syntax
				if (returnType != null)
					node.addNode(SyntaxTreeBuilder.operationNode(list, precedence + 1));
				else
					node.addNode(SyntaxTreeBuilder.expressionNode(list));
			}
		}
	}
	
	
	/**
	 * Converts a command to a method
	 * @return
	 */
	public Method toMethod() {
		// TODO: add cmd field into Method class
		
		Method method = new Method(new Id(name));
		for (int i = 0; i < arguments.size(); i++) {
			Variable var = arguments.get(i).var;
			method.addArgument(var);
		}
		
		method.setReturnType(returnType);
		method.setReturnSubtype(returnSubtype);
		method.setTranslation(translation);
		
		return method;
	}

}
