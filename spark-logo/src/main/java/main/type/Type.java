package main.type;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import parser.Symbol;
import parser.SymbolList;
import parser.sym;

import main.AdhocImplementation;
import main.Command;
import main.Id;
import main.SparkModel;
import main.Variable;
import main.annotation.ExternalVariableAnnotation;
import main.annotation.InterfaceAnnotation;

import javasrc.JavaEmitter;
import javasrc.Translation;

/**
 * Type = class in the OOP sense. No implicit primitive types (with exceptions?)
 * 
 * @author Monad
 * 
 */
public class Type {
	protected final Id id;
	protected Type parentType;
	
	// TODO: remove this later
	public Id alias;
	
	// If true, then it is possible to extend this type
	private boolean partialFlag;

	/* Declaration translation strings */
	private String declarationLHS;
	private String declarationRHS;

	/* Flag indicating whether the type were resolved or not */
	protected boolean resolved = false;

	/* All fields */
	protected final HashMap<Id, Variable> fields;
	/* List of fields in the order of their declaration */
	protected final ArrayList<Variable> fieldList;

	/* All methods */
	// Note: name should reflect all parameters for overloading
	protected final HashMap<Id, Method> methods;
	/* List of methods in the order of their declaration:
	 * _init method is always the first followed by constructors
	 */
	protected final ArrayList<Method> methodList;

	/* Constructors */
	// TODO: now we have only one (explicit) constructor
//	protected final ArrayList<Method> constructors;

	/**
	 * Creates a new agent type
	 * 
	 * @param id
	 * @param parentType
	 * @return
	 */
	public static AgentType createAgentType(Id id, Type parentType, boolean partial) {
		return new AgentType(id, parentType, partial);
	}

	/**
	 * Creates a new model type
	 * 
	 * @param id
	 * @param parentType
	 * @return
	 */
	public static ModelType createModelType(Id id, Type parentType, boolean partial) {
		return new ModelType(id, parentType, partial);
	}

	/**
	 * Creates a new class type
	 * 
	 * @param id
	 * @param parentType
	 * @return
	 */
	public static Type createClassType(Id id, Type parentType, boolean partial) {
		if (parentType == null)
			parentType = new UnresolvedType(new Id("$Object"));
		return new Type(id, parentType, partial);
	}

	/**
	 * Returns type id (type name)
	 * 
	 * @return
	 */
	public Id getId() {
		return id;
	}
	
	/**
	 * Returns the partial flag
	 * @return
	 */
	public boolean isPartial() {
		return partialFlag;
	}

	/**
	 * Returns parent type
	 * 
	 * @return
	 */
	public Type getParentType() {
		return parentType;
	}

	/**
	 * Returns declaration LHS string (type)
	 */
	public String getDeclarationLHS() throws Exception {
		if (declarationLHS == null) {
			// default: type id
			declarationLHS = getTranslationString() + " @@id";
		}

		return declarationLHS;
	}
	
	/**
	 * Returns declaration RHS string (default value)
	 */
	public String getDeclarationRHS() {
		if (declarationRHS == null) {
			if (instanceOf(SparkModel.getInstance().getType(new Id("$Object"))))
				declarationRHS = "null";
		}
		
		return declarationRHS;
	}
	

	/**
	 * Sets declaration translation string
	 * 
	 * @param translation
	 */
	public void setDeclarationTranslation(String lhs, String rhs) {
		this.declarationLHS = lhs;
		this.declarationRHS = rhs;
	}

	/**
	 * Returns a translation string
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getTranslationString() throws Exception {
		return id.toJavaName();
	}

	/**
	 * Creates a type with the given name
	 * 
	 * @param id
	 */
	protected Type(Id id) {
		this(id, null, false);
	}

	/**
	 * Creates a type with the given name and parent type
	 * 
	 * @param id
	 * @param parent
	 */
	protected Type(Id id, Type parent, boolean partial) {
		this.id = id;
		this.parentType = parent;
		this.partialFlag = partial;

		fields = new HashMap<Id, Variable>();
		methods = new HashMap<Id, Method>();
//		constructors = new ArrayList<Method>();
		
		fieldList = new ArrayList<Variable>();
		methodList = new ArrayList<Method>();
	}
	
	
	public void addAnnotation(InterfaceAnnotation a) throws Exception {
		throw new Exception("Annotation " + a + " cannot be associated with " + this);
	}

	/**
	 * Returns true if this type is an instance of the given type
	 * 
	 * @param type
	 * @return
	 */
	public boolean instanceOf(Type type) {
		// NullType is an instance of any Object type
		Type nullType = SparkModel.getInstance().getType(new Id("$NullObject"));
		Type objType = SparkModel.getInstance().getType(new Id("$Object"));

		if (this == nullType) {
			if (type == nullType || type == objType)
				return true;
			
			return type.instanceOf(objType);		
		}
		
		// FIXME: there can be copies of the same type
		for (Type currentType = this; currentType != null; currentType = currentType.parentType) {
			if (currentType == type)
				return true;
		}

		return false;
	}

	/**
	 * Returns a field with the given name. If field does not exist in the
	 * current type then the parent type is checked.
	 * 
	 * @param id
	 * @return null if no such field
	 */
	public Variable getField(Id id) {
		if (fields.containsKey(id))
			return fields.get(id);
		else if (parentType != null)
			return parentType.getField(id);

		return null;
	}
	
	
	/**
	 * Returns all fields in the type
	 * @return
	 */
	public ArrayList<Variable> getAllFields() {
		ArrayList<Variable> result = new ArrayList<Variable>();
		result.addAll(fieldList);
		
		return result;
	}
	

	/**
	 * Returns a method with the given name
	 * 
	 * @param id
	 * @param searchParent
	 * @return
	 */
	public Method getMethod(Id id, boolean searchParent) {
		if (methods.containsKey(id))
			return methods.get(id);
		else if (searchParent && parentType != null)
			return parentType.getMethod(id, searchParent);

		return null;
	}

	/**
	 * Adds a new field
	 * 
	 * @param field
	 */
	public void addField(Variable field) throws Exception {
		if (SparkModel.getInstance().checkGlobalNameConflicts(field.id)) {
			throw new Exception("Field " + field.id + " is already defined as a global variable");
		}
		
		if (getField(field.id) != null) {
			throw new Exception("Field " + field.id + " is already defined");
		}

		// TODO: do not add twice for fields with predefined object translation
		// if (field.getTranslation != null)
		// field.getTranslation = "@@object." + field.getTranslation;

		// if (field.setTranslation != null)
		// field.setTranslation = "@@object." + field.setTranslation;

		fields.put(field.id, field);
		fieldList.add(field);
	}
	
	
	/**
	 * Removes a field from the type
	 * @param field
	 */
	public void removeField(Variable field) {
		fields.remove(field.id);
		fieldList.remove(field);
	}
	

	/**
	 * Adds a new method
	 * 
	 * @param field
	 */
	public final void addMethod(Method method) throws Exception {
		// TODO: for built-in types 'create' should be available method name
		AdhocImplementation.flag = true;
		if (!id.name.equals("Space"))  {
			// Change name
			AdhocImplementation.flag = true;
			if (method.id.name.equals("create")) {
				method.id = new Id("_create");
			}
		}
		
		if (getMethod(method.id, false) != null) {
			throw new Exception("Method " + method.id
					+ " is already defined for class " + id);
		}

		method.parentType = this;
		methods.put(method.id, method);
		
		// Put all special method at the beginning
		if (method.id.name.startsWith("_") || method.id.name.startsWith("$"))
			methodList.add(0, method);
		else
			methodList.add(method);
	}

	/**
	 * Marks a method as a constructor
	 * 
	 * @param method
	 * @throws Exception
	 */
	private final void addConstructor(Method method) throws Exception {
		// TODO: only one constructor without arguments is allowed now
//		if (constructors.size() > 0)
//			throw new Exception("Only one constructor is currently allowed");

//		if (method.arguments.size() > 0)
//			throw new Exception("No arguments are allowed for a constructor");

		method.parentType = this;
		method.constructorFlag = true;
		method.id = new Id(getTranslationString());
//		constructors.add(method);
		
		// Add constructor right after _init method
		// TODO: better implementation: without direct reference to _init
		if (methodList.size() > 0 && methodList.get(0).id.name.equals("_init"))
			methodList.add(1, method);
		else
			methodList.add(0, method);
	}
	
	
	/**
	 * Adds a special constructor for a type derived from SpaceAgent
	 */
	private final void createSpaceAgentConstructor() throws Exception {
		// Verify type
		Type spaceAgent = SparkModel.getInstance().getType(new Id("SpaceAgent"));
		if (!this.instanceOf(spaceAgent))
			return;
		
		Type intType = SparkModel.getInstance().getType(new Id("$integer"));
		Type numberType = SparkModel.getInstance().getType(new Id("number"));
		
		Method ctor = new Method(new Id("ctor"));
		ctor.addArgument(new Variable(new Id("radius"), numberType));
		ctor.addArgument(new Variable(new Id("shape"), intType));
		
		ctor.addSourceCodeSymbol(new Symbol("super", sym.IDENTIFIER, "super", -1, -1));
		ctor.addSourceCodeSymbol(new Symbol("radius", sym.IDENTIFIER, "radius", -1, -1));
		ctor.addSourceCodeSymbol(new Symbol("shape", sym.IDENTIFIER, "shape", -1, -1));

		addConstructor(ctor);
	}
	
	
	
	/**
	 * Adds a default constructor
	 * @throws Exception
	 */
	private final void createDefaultConstructor() throws Exception {
		Method ctor = new Method(new Id("ctor"));
		
		// SpaceAgent type needs a special attention:
		// if there is the "super" command in the "_create" method,
		// then it should be moved to the default constructor
		Type spaceAgent = SparkModel.getInstance().getType(new Id("SpaceAgent"));

		Method _create = getMethod(new Id("_create"), false);
		if (_create != null) {
			if (this.instanceOf(spaceAgent)) {
				//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!		
				// FIXME: remove this, find better solution
				AdhocImplementation.flag = true;
				SymbolList src = _create.sourceCode;
				src.reset();
				Symbol symbol = src.peek();
				
				if (symbol.id == sym.IDENTIFIER && "super".equals(symbol.value)) {
					ctor.addSourceCodeSymbol(src.next());
					ctor.addSourceCodeSymbol(src.next());
					ctor.addSourceCodeSymbol(src.next());
					
					src.RemoveFirstAndReset();
					src.RemoveFirstAndReset();
					src.RemoveFirstAndReset();
				}
				//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!			
			}
		}
		else
		{
			// For each type we need to declare a "create" method
			_create = new Method(new Id("_create"));
			addMethod(_create);
		}
		
		addConstructor(ctor);
	}
	
	
	

	/**
	 * Tries to resolve types of fields and methods
	 */
	public Type resolveDeclarationTypes() throws Exception {
		if (resolved)
			return this;

		// Preventing recurrence
		resolved = true;

		// Resolve parent type
		if (parentType != null) {
			parentType = parentType.resolveDeclarationTypes();
			if (parentType == null)
				throw new Exception("Parent type does not exist for type: "
						+ id);
		}

		// Resolve fields
		for (Variable var : fieldList) {
			var.resolveDeclarationTypes();
		}

		// Resolve methods
		for (Method method : methodList) {
			method.resolveDeclarationTypes();
		}

		return this;
	}

	/**
	 * Verifies that all interface methods are created
	 */
	protected void createInterfaceMethods() throws Exception {
		if (parentType == null)
			return;

		if (!(parentType instanceof BuiltinType)
				|| !((BuiltinType) parentType).isInterface())
			return;

		// TODO: get all methods recursively from parent, etc.
		for (Method method : parentType.methodList) {
			if (!method.abstractFlag)
				continue;

			Id id = method.id;
			Method myMethod = getMethod(id, false);

			// TODO: Instead of throwing an exception create an empty method
			if (myMethod == null)
				throw new Exception("Abstract method " + id
						+ " is not implemented in " + this.id);

			if (method.arguments.size() != myMethod.arguments.size())
				throw new Exception("Abstract method " + id
						+ " has wrong number of arguments in " + this.id);

			for (int i = 0; i < method.arguments.size(); i++) {
				// TODO: call something like resolve type for a variable
				Type type = method.arguments.get(i).type;
				Type myType = myMethod.arguments.get(i).type;

				if (myType == null || myType instanceof UnknownType) {
					myMethod.arguments.get(i).type = type;
					continue;
				}

				// FIXME: be careful here
				if (myType != type)
					throw new Exception(
							"Incompatible types for an abstract method " + id
									+ " in " + this.id);
			}

			// TODO: be careful, also look into sub-types
			// (Or better remove sub-types at all before)
			if (method.getReturnType() != myMethod.getReturnType())
				throw new Exception(
						"Return types are incompatible for the implementation of an abstract method "
								+ id + " in " + this.id);
		}
	}

	/**
	 * Parses all methods
	 */
	public void parseMethods() throws Exception {
		createFieldInitializationMethod();
		createConstructors();

		createInterfaceMethods();

//		for (Method constructor : constructors) {
//			constructor.parse();
//		}

		// Constructors are inside methodList
		for (Method method : methodList) {
			method.parse();
		}
	}

	/**
	 * Creates the source code for the method for initialization of fields
	 */
	protected void createFieldInitializationMethod() throws Exception {
		Id id = new Id("_init");

		Method init = new Method(id);
		// TODO: better solution?
		String thisName = getId().toJavaName();
		init.setTranslation(new Translation("_init" + thisName + "()"));
		addMethod(init);

		for (Variable field : fieldList) {
			SymbolList src = field.initializationSource;
			// No initialization expression
			if (src == null || src.size() == 0)
				continue;

			init.addSourceCodeSymbol(new Symbol(field.id.name, sym.IDENTIFIER,
					field.id.name, -1, -1));
			init.addSourceCodeSymbol(new Symbol("=", sym.EQ, -1, -1));
			for (int i = 0; i < src.size(); i++)
				init.addSourceCodeSymbol(src.get(i));
		}
	}

	/**
	 * Creates default constructor or modifies the existing constructor(s) in
	 * order to call field initialization method
	 */
	protected void createConstructors() throws Exception {
		// TODO: find whether there is a default constructor (without arguments)
//		if (constructors.size() > 0)
//			return;
		
		createDefaultConstructor();
		createSpaceAgentConstructor();

//		addMethod(new Method(new Id("create")));
	}

	/**
	 * Returns a command related to methods of this type
	 * 
	 * @return null if no command found
	 */
	public Command getCommand(String name) {
		Id id = new Id(name);

		Method method = methods.get(id);
		if (method == null) {
			if (parentType != null)
				return parentType.getCommand(name);

			return null;
		}

		return new CommandFromMethod(method);
	}

	/**
	 * Translates the type definition into java source code
	 * 
	 * @param outputPath
	 */
	public void translateToJava(File outputPath) throws Exception {
		String name = id.toJavaName() + ".java";
		File outFile = new File(outputPath, name);

		PrintStream out = new PrintStream(outFile);
		JavaEmitter java = new JavaEmitter(out);

		java.println("package "
				+ SparkModel.getInstance().getName().toJavaName() + ";");
		java.emptyLine();

		java.println("import java.util.ArrayList;");
		java.println("import java.io.*;");
		java.println("import org.spark.core.*;");
		java.println("import org.spark.data.*;");
		java.println("import org.spark.space.*;");
		java.println("import org.spark.utils.*;");
		java.println("import org.spark.math.*;");
		java.emptyLine();

		// TODO: write those imports which are required

		java.beginClass("public", id, parentType);

		for (Variable field : fieldList) {
			java.field(field);
		}

		java.emptyLine();

//		for (Method constructor : constructors) {
//			java.println("");
//			constructor.translateToJava(java);
//		}

		// All constructors are inside methodList
		for (Method method : methodList) {
			java.println("");
			method.translateToJava(java);
		}

		java.emptyLine();

		// Create get/set methods for global fields
		for (Variable field : fieldList) {
			if (field.global) {
				java.emptyLine();
				createGetSetMethods(java, field);
			}
		}

		java.endClass();
		java.flush();
		java.close();
	}

	/**
	 * Creates get/set methods for a field of types double or integer
	 * 
	 * @param field
	 */
	protected void createGetSetMethods(JavaEmitter java, Variable field) {
		String modifier = "public";
		if (field.global)
			modifier += " static";

		// TODO: better implementation
		String javaObjectTypeName = null;
		String javaPrimitiveTypeName = null;

		if (field.type == SparkModel.getInstance().getType(new Id("double"))) {
			javaObjectTypeName = "Double";
			javaPrimitiveTypeName = "double";
		} else if (field.type == SparkModel.getInstance().getType(
				new Id("integer"))) {
			javaObjectTypeName = "Integer";
			javaPrimitiveTypeName = "int";
		} else if (field.type == SparkModel.getInstance().getType(
				new Id("boolean"))) {
			javaObjectTypeName = "Boolean";
			javaPrimitiveTypeName = "boolean";
		} else {
			// TODO: do we need to throw an exception?
			return;
		}

		String name = "_" + field.id.toJavaName();

		String getName = "get" + name;
		String setName = "set" + name;

		// Add external variable annotation
		try {
			field.addAnnotation(new ExternalVariableAnnotation());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// Complete annotation description
		for (InterfaceAnnotation annotation : field.annotations) {
			annotation.setGetSetValues(getName, setName);
		}

		// TODO: get/set methods should be created inside type as real methods.
		// The difficulties are 'Double' and 'Integer' types and static methods

		// Create get method
		java.beginMethod(modifier, javaPrimitiveTypeName, getName, 0);
		java.print("return ");
		// TODO: translation string
		java.print(field.id.toJavaName());
		java.endJavaLine();
		java.endMethod();

		java.emptyLine();

		// Create set method
		java.beginMethod(modifier, "void", setName, 1);
		java.addArgument(new UnresolvedType(new Id(javaObjectTypeName)),
				new Id("value"));
		java.print(field.id.toJavaName());
		java.print(" = ");
		java.print("value");
		java.endJavaLine();
		java.endMethod();
	}

	@Override
	public String toString() {
		String str = id.name;
		return str;
	}
}
