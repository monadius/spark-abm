package main;

import java.util.ArrayList;

import parser.SymbolList;
import parser.tree.TreeNode;
import main.annotation.InterfaceAnnotation;
import main.annotation.VariableAnnotation;
import main.type.Type;
import main.type.UnknownType;

/**
 * Represents a variable: field, local variable, global variable, etc.
 * @author Monad
 */
public class Variable {
	public Type type;
	public final Id	id;
	// Global means public static
	public boolean global;
	// Shared means protected static
	public boolean shared;
	// True means that the variable should not be declared explicitly
	// TODO: move this flag into CodeBlock local variables definition
	public boolean doNotDeclare;
	public final ArrayList<VariableAnnotation> annotations;
	
	/* Initialization source code */
	public SymbolList initializationSource; 
	
	/* Initialization expression */
	public TreeNode initializationExpression; 
	
	/* Set translation string */
	public String setTranslation;
	/* Get translation string */
	public String getTranslation;
	
	/* This flag indicates that special translation strings are used */
	public boolean specialTranslation;
	
	// TODO: variable should not be copied
	// only one copy of each variable should exist
	public Variable(Id id, Type type) {
		if (type == null || id == null)
			throw new Error("Type and id could not be null");

		this.id = id;
		this.type = type;
		
		// Default translation strings
		// TODO: should be derived from the type
		setTranslation = id.toJavaName() + " = @@value";
		getTranslation = id.toJavaName();
		
		// TODO: better solution is required
		AdhocImplementation.flag = true;
		if (type.instanceOf(SparkModel.getInstance().getType(new Id("$time")))) {
			setTranslation = null;
			getTranslation = id.toJavaName() + ".getTick()";
		}
		
		annotations = new ArrayList<VariableAnnotation>();
	}
	
	
	/**
	 * Adds a new annotation to the variable
	 * @param annotation
	 */
	public void addAnnotation(InterfaceAnnotation annotation) throws Exception {
		if (annotation instanceof VariableAnnotation) {
			VariableAnnotation varAnnotation = (VariableAnnotation) annotation;
			
			annotations.add(varAnnotation);
			varAnnotation.associateVariable(this);
			return;
		}
	
		throw new Exception("Annotation " + annotation.getId() + " cannot be associated with a variable " + this);
	}
	

	/**
	 * Sets get/set translation strings
	 * @param getTranslation
	 * @param setTranslation
	 */
	public void setTranslation(String getTranslation, String setTranslation) {
		this.setTranslation = setTranslation;
		this.getTranslation = getTranslation;
		
		// TODO: better solution is required.
		// This is for resolving += problem with special fields.
		if (!global)
			specialTranslation = true;
	}
	
	
	/**
	 * Sets a default translation strings
	 */
	public void setDefaultFieldTranslation() {
		// TODO: should be derived from the type
		setTranslation = "@@object." + id.toJavaName() + " = @@value";
		getTranslation = "@@object." + id.toJavaName();
		
		specialTranslation = false;
	}
	
	
	/**
	 * Tries to resolve type
	 */
	public void resolveDeclarationTypes() throws Exception {
		type = type.resolveDeclarationTypes();
		if (type == null)
			throw new Exception("Type not found for variable: " + id);
	}
	
	
	/**
	 * Translates into the java code
	 * @param java
	 * @param lvalue
	 */
	public String getTranslationString(int flag) throws Exception {
		// Set
		if ((flag & TreeNode.LVALUE) == 1) {
			if (setTranslation == null)
				throw new Exception("The variable " + id + " cannot be assigned");
			
			return setTranslation;
		}

		if (getTranslation == null)
			throw new Exception("The variable " + id + " cannot be read");
		
		return getTranslation;
	}
	
	
	/**
	 * Returns the declaration translation
	 * @return
	 */
	public String getDeclarationTranslation() throws Exception {
		if (type == null || type instanceof UnknownType) 
			throw new Exception("Type is not defined for: " + id);
		
		String declaration = type.getDeclarationTranslation();
		if (declaration == null) {
			declaration = type.getTranslationString() + " " + id.toJavaName();
			if (type.instanceOf(SparkModel.getInstance().getType(new Id("$Object")))) {
				declaration += " = null;";
			}
			else {
				declaration += ";";
			}
		}
		else {
			declaration = declaration.replaceAll("@@id", id.toJavaName());
		}
		
		return declaration;
	}
}
