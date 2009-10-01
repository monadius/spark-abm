package main.type;

import java.util.ArrayList;

import main.AdhocImplementation;
import main.CodeBlock;
import main.Id;
import main.Variable;
import main.annotation.InterfaceAnnotation;
import main.annotation.MethodAnnotation;

import javasrc.JavaEmitter;
import javasrc.Translation;

import parser.Symbol;
import parser.SymbolList;
import parser.sym;
import parser.tree.BlockNode;
import parser.tree.CommandNode;
import parser.tree.DotNode;
import parser.tree.SyntaxTreeBuilder;

/**
 * Basic method representation class
 * @author Monad
 *
 */
public class Method {
	/* Reference to the parent type */
	// It is set in Type.addMethod()
	protected Type parentType;
	/* Indicates whether the method is a constructor */
	protected boolean constructorFlag;
	/* Indicates that the method is an abstract method */
	protected boolean abstractFlag;
	/* Method's id */
	protected Id id;
	/* Source code of the method */
	protected final SymbolList sourceCode;
	/* A list of arguments */
	protected final ArrayList<Variable> arguments;
	/* Return type */
	protected Type returnType;
	/* Return sub-type */
	// FIXME: remove this
	protected Type returnSubtype;
	/* Translation transform */
	protected Translation translation;

	/* Annotations associated with the method */
	protected final ArrayList<MethodAnnotation> annotations;
	
	/* Auxiliary code block */
	protected CodeBlock parentCodeBlock;
	/* Parsed code */
	protected BlockNode methodCode;
	
	
	
	/**
	 * Creates a method with the given name
	 * @param id
	 */
	public Method(Id id) {
		this.id = id;
		sourceCode = new SymbolList();
		arguments = new ArrayList<Variable>();
		annotations = new ArrayList<MethodAnnotation>();
	}
	
	
	/**
	 * Returns method's id
	 * @return
	 */
	public Id getId() {
		return id;
	}
	
	
	public void addAnnotation(InterfaceAnnotation annotation) throws Exception {
		if (!(annotation instanceof MethodAnnotation))
			throw new Exception("Annotation " + annotation.getId() + " cannot be associated with the method " + this.id);

		MethodAnnotation a = (MethodAnnotation) annotation;
		annotations.add(a);
		a.associateMethod(this);
	}
	
	
	/**
	 * Returns the type to which the method belongs
	 * @return
	 */
	public Type getParentType() {
		return parentType;
	}
	
	
	/**
	 * Sets the abstract flag
	 * @param flag
	 */
	public void setAbstractFlag(boolean flag) {
		abstractFlag = flag;
	}
	
	
	/**
	 * Sets the method's return type
	 * @param type
	 */
	public void setReturnType(Type type) {
		returnType = type;
	}
	
	
	/**
	 * Returns the number of arguments
	 * @return
	 */
	public int getArgumentsNumber() {
		return arguments.size();
	}
	
	
	/**
	 * Gets the method's return type
	 * @return
	 */
	public Type getReturnType() {
		return returnType;
	}
	
	/**
	 * Sets the method's return sub-type
	 * @param type
	 */
	public void setReturnSubtype(Type type) {
		returnSubtype = type;
	}
	
	
	/**
	 * Gets the method's return sub-type
	 * @return
	 */
	public Type getReturnSubtype() {
		return returnSubtype;
	}
	
	
	/**
	 * Sets a translation transform
	 * @param translation
	 */
	public void setTranslation(Translation translation) {
		this.translation = translation;
	}
	
	
	/**
	 * Adds an argument
	 * @param var
	 */
	public void addArgument(Variable var) {
		arguments.add(var);
	}
	
	
	/**
	 * Adds a source code symbol
	 * @param symbol
	 */
	public void addSourceCodeSymbol(Symbol symbol) {
		sourceCode.add(symbol);
	}
	
	
	/**
	 * Tries to resolve all types
	 */
	public void resolveDeclarationTypes() throws Exception {
		for (int i = 0; i < arguments.size(); i++) {
			arguments.get(i).resolveDeclarationTypes();
		}
		
		if (returnType != null) {
			returnType = returnType.resolveDeclarationTypes();
		}
	}
	
	
	
	/**
	 * Translates to java source code
	 */
	public void translateToJava(JavaEmitter java) throws Exception {
		java.clearTempVariables();
		String typeName = returnType != null ? returnType.getTranslationString() : null;
		if (constructorFlag)
			typeName = "";
		java.beginMethod("public", typeName, id.toJavaName(), arguments.size());
		
		for (int i = 0; i < arguments.size(); i++) {
			Variable arg = arguments.get(i);
			java.addArgument(arg.type, arg.id);
		}
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!		
		// FIXME: remove this, find better solution
		if (constructorFlag) {
			AdhocImplementation.flag = true;
			ArrayList<CommandNode> commands = methodCode.getCodeBlock().getCommands();
			
			if (commands.size() > 0 && commands.get(0) instanceof DotNode) {
				DotNode node = (DotNode) commands.get(0);
				if (node.getNode(1) instanceof CommandNode) {
					CommandNode node2 = (CommandNode) node.getNode(1);
					if (node2.getCommand().getName().equals("super")) {
						node2.pushCodeBlock(methodCode.getCodeBlock());
						node2.translate(java, 0);
						node2.popCodeBlock();
						java.endJavaLine();
						commands.remove(0);
					}
				}
			}
		}
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		if (constructorFlag) {
			// FIXME: add _init call inside method source code or translated code
			java.println("_init();");
		}
		else if (parentType instanceof ModelType && id.name.equals("setup")) {
			// FIXME: it is assumed that the first command is space creation
			AdhocImplementation.flag = true;
			ArrayList<CommandNode> commands = methodCode.getCodeBlock().getCommands();

			commands.add(1, 
					new CommandNode(null, parentType.getCommand("_init"))
			);
		}
		methodCode.translate(java, 0);
		java.endMethod();
	}
	
	
	

	/**
	 * Parses the method's source code
	 * @throws Exception
	 */
	public void parse() throws Exception {
		// Create a code block for arguments
		parentCodeBlock = new CodeBlock(null);
		parentCodeBlock.setMethod(this);
		parentCodeBlock.setDefaultSelf(parentType);
		
		for (int i = 0; i < arguments.size(); i++) {
			parentCodeBlock.addLocalVariable(arguments.get(i));
		}
		
		// Create a code block (block node) for method's body
		methodCode = new BlockNode(null, parentCodeBlock);
		SyntaxTreeBuilder.setCurrentBlock(methodCode.getCodeBlock());
		
		// Parse all symbols
		for (Symbol s = sourceCode.peek(); s.id != sym.END; s = sourceCode.peek()) {
			SyntaxTreeBuilder.parse(sourceCode, methodCode);
		}
	}
}
