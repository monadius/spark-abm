package parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import main.Id;
import main.SparkModel;
import main.Variable;
import main.annotation.InterfaceAnnotation;
import main.type.AgentType;
import main.type.Method;
import main.type.ModelType;
import main.type.Type;
import main.type.UnknownType;
import main.type.UnresolvedType;

/**
 * Main parser class
 * 
 * @author Monad
 * 
 */
public class SparkLogoParser {
	private Scanner scanner;
	private File[] files;
	private SparkModel sparkModel;

	/**
	 * Creates a parser with the given input files and model's name
	 * 
	 * @param scanner
	 */
	public SparkLogoParser(File[] files) {
		this.files = files;
	}

	/**
	 * Reads out all symbols from the given string
	 * 
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public static SymbolList stringToSymbols(String str) throws Exception {
		SymbolFactory sf = new SymbolFactory();

		InputStream in = new ByteArrayInputStream(str.getBytes());
		Scanner scanner = new Scanner(in, sf);

		SymbolList list = new SymbolList();
		while (true) {
			Symbol s = scanner.nextToken();
			if (s.id == sym.EOF)
				break;

			list.add(s);
		}

		return list;
	}

	/**
	 * Creates an internal representation of source code
	 * 
	 * @return
	 * @throws Exception
	 */
	public SparkModel read() throws Exception {
		SymbolFactory sf = new SymbolFactory();
		sparkModel = SparkModel.getInstance();

		// Load all files
		for (int i = 0; i < files.length; i++) {
			FileInputStream in = new FileInputStream(files[i]);
			sf.setCurrentFileName(files[i].getName());
			scanner = new Scanner(in, sf);

			// Empty file
			if (scanner.peekToken().id == sym.EOF) {
				in.close();
				continue;
			}

			parseDeclarations();
			in.close();
		}

		sparkModel.resolveDeclarationTypes();
		return sparkModel;
	}

	/**
	 * Reads declarations without method parsing and type checking. Only source
	 * symbols for method are retrieved and new user types are created.
	 * 
	 * @throws Exception
	 */
	private void parseDeclarations() throws Exception {
		ArrayList<InterfaceAnnotation> annotations = new ArrayList<InterfaceAnnotation>();
		Symbol symbol;
		
		/* File can contain annotations at the beginning */
		while (true) {
			symbol = scanner.peekToken();
			
			if (symbol.id == sym.ANNOTATION) {
				annotations.add(parseAnnotation());
				continue;
			}
			
			break;
		}
		
		/* A type definition is required */
		Type type = parseNewTypeDeclaration();
		boolean agentFlag = type instanceof AgentType;
		
		for (InterfaceAnnotation a : annotations) {
			type.addAnnotation(a);
		}

		annotations.clear();		
		
		/* Read type fields and methods */
		while (true) {
			ArrayList<Variable> fields;
			Method method;
			symbol = scanner.peekToken();

			// First parse global variables
			if (symbol.id == sym.GLOBAL) {
				// TODO: remove it later
				if (agentFlag)
					throw new Exception(
							"Global variables can be defined only for models: "
									+ symbol);
				ArrayList<Variable> globals = parseGlobal();
				
				// TODO: move it to parseGlobal()
				for (Variable var : globals) {
					var.global = true;
					String typeName = type.getId().toJavaName();
					// TODO: move it into Variable class
					var.setTranslation(typeName + "." + var.getTranslation,
							typeName + "." + var.setTranslation);
					type.addField(var);
				}
				
				// Only the first variable of the group will have the annotation
				Variable var = globals.get(0);

				for (InterfaceAnnotation annotation : annotations) {
					var.addAnnotation(annotation);
				}
			
				annotations.clear();

				continue;
			}

			if (symbol.id == sym.ANNOTATION) {
				annotations.add(parseAnnotation());
				continue;
			}
			
			// Parse all other declarations
			switch (symbol.id) {
			case sym.AGENT:
			case sym.MODEL:
			case sym.CLASS:
				type = parseNewTypeDeclaration();
				agentFlag = type instanceof AgentType;
				
				for (InterfaceAnnotation a : annotations) {
					type.addAnnotation(a);
				}
				annotations.clear();

				break;

			case sym.SHARED:
				scanner.nextToken();
				fields = parseField();
				for (Variable var : fields) {
					var.shared = true;
					type.addField(var);
				}
				// String typeName = type.getId().toJavaName();
				// TODO: move it into Variable class
				// var.setTranslation(typeName + "." + var.getTranslation,
				// typeName + "." + var.setTranslation);

				break;

			case sym.VAR:
				fields = parseField();
				for (Variable var : fields) {
					type.addField(var);
				}
				break;

			case sym.TO:
				method = parseMethod();
				type.addMethod(method);
				
				for (InterfaceAnnotation a : annotations) {
					method.addAnnotation(a);
				}
				annotations.clear();
				
				break;

			case sym.SPACE:
				if (agentFlag)
					throw new Exception(
							"space keyword is not allowed for an agent type: "
									+ symbol);
				parseSpaceDeclaration((ModelType) type);
				break;

			case sym.EOF:
				return;

			default:
				throw new Exception("Unexpected symbol: " + symbol);
			}
			
			if (annotations.size() > 0) 
				throw new Exception("Annotations are illegal for " + symbol);

		}
	}

	/**
	 * Parses an annotation
	 * 
	 * @return
	 */
	private InterfaceAnnotation parseAnnotation() throws Exception {
		Symbol symbol = scanner.nextToken();

		if (symbol.id != sym.ANNOTATION)
			throw new Exception("'@' is expected: " + symbol);

		symbol = scanner.nextToken();
		InterfaceAnnotation annotation = InterfaceAnnotation
				.beginParsing(symbol);

		symbol = scanner.peekToken();
		// Default annotation elements
		if (symbol.id != sym.LPAREN)
			return annotation;

		scanner.nextToken();
		while (true) {
			symbol = scanner.nextToken();
			if (symbol.id == sym.EOF)
				throw new Exception("Unexpected end of file: " + symbol);

			if (symbol.id == sym.RPAREN)
				break;

			Symbol s2 = scanner.nextToken();
			if (s2.id != sym.EQ)
				throw new Exception("'=' is expected: " + s2);

			s2 = scanner.nextToken();
			annotation.processElement(symbol, s2);

			if (scanner.peekToken().id == sym.COMMA)
				scanner.nextToken();
		}

		return annotation;
	}

	/**
	 * Parses a space declaration
	 * 
	 * @param model
	 */
	private void parseSpaceDeclaration(ModelType model) throws Exception {
		Symbol symbol = scanner.nextToken();

		if (symbol.id != sym.SPACE)
			throw new Exception("'space' keyword is expected: " + symbol);

		symbol = scanner.nextToken();
		if (symbol.id != sym.IDENTIFIER)
			throw new Exception("Space name is expected: " + symbol);

		model.beginSpaceDeclaration((String) symbol.value);

		while (true) {
			symbol = scanner.peekToken();

			switch (symbol.id) {
			case sym.CLASS:
			case sym.SPACE:
			case sym.SHARED:
			case sym.AGENT:
			case sym.GLOBAL:
			case sym.VAR:
			case sym.MODEL:
			case sym.TO:
			case sym.ANNOTATION:
			case sym.EOF:
				return;
			}

			model.addSpaceDeclarationSymbol(symbol);
			scanner.nextToken();
		}
	}

	/**
	 * Auxiliary procedure for filling up a symbol list with all symbols until
	 * new declaration
	 * 
	 * @param list
	 */
	private void fillSymbolList(SymbolList list) throws Exception {
		int flag = 0;
		while (true) {
			Symbol symbol = scanner.peekToken();
			
			// FIXME: think about better solution
			// We need somehow treat vector initialization containing commas
			if (symbol.id == sym.LBRACK || 
					(symbol.id == sym.OPERATOR && symbol.value.equals("<"))) {
				flag++;
			}

			if (symbol.id == sym.RBRACK || 
					(symbol.id == sym.OPERATOR && symbol.value.equals(">"))) {
				flag--;
			}

			switch (symbol.id) {
			case sym.COMMA:
				if (flag == 0)
					return;
				break;
				
			case sym.SPACE:
			case sym.SHARED:
			case sym.COLON:
			case sym.AGENT:
			case sym.CLASS:
			case sym.GLOBAL:
			case sym.VAR:
			case sym.MODEL:
			case sym.TO:
			case sym.ANNOTATION:
			case sym.EOF:
				return;
			}

			list.add(symbol);
			scanner.nextToken();
		}
	}

	/**
	 * Parses a string of the type: id [= constant][: type-id]
	 * 
	 * @param fieldFlag
	 *            indicates whether the parsed variable is a field
	 * @return
	 */
	private Variable parseVariableDeclaration(boolean fieldFlag)
			throws Exception {
		Symbol symbol = scanner.nextToken();
		Id id;
		Type type = new UnknownType();

		if (symbol.id != sym.IDENTIFIER)
			throw new Exception("Identifier is expected: " + symbol);

		id = new Id((String) symbol.value);
		SymbolList initializationSource = null;

		// Parse initialization string
		symbol = scanner.peekToken();
		if (symbol.id == sym.EQ) {
			scanner.nextToken();
			initializationSource = new SymbolList();
			fillSymbolList(initializationSource);
		}

		// Parse explicit type declaration
		symbol = scanner.peekToken();
		if (symbol.id == sym.COLON) {
			scanner.nextToken();
			type = parseTypeDeclaration();
		}

		Variable var = new Variable(id, type);
		var.initializationSource = initializationSource;
		if (fieldFlag)
			var.setDefaultFieldTranslation();
		return var;
	}

	/**
	 * Parses a type declaration
	 * 
	 * @return
	 */
	// TODO: composite types
	private Type parseTypeDeclaration() throws Exception {
		Symbol s = scanner.nextToken();

		if (s.id != sym.IDENTIFIER)
			throw new Exception("Type name is expected: " + s);

		Id typeId = new Id((String) s.value);
		Type type = new UnresolvedType(typeId);

		return type;
	}

	/**
	 * Parses a new type declaration. Each source file declares one type.
	 * 
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	private Type parseNewTypeDeclaration() throws IOException, Exception {
		Type parentType = null;
		Id typeId;
		Id parentId;

		Symbol symbol = scanner.nextToken();

		// Read type class: agent or model
		if (symbol.id != sym.AGENT && symbol.id != sym.MODEL
				&& symbol.id != sym.CLASS)
			throw new Exception(
					"Each SparkLogo-file should start with agent or model, or class keywords");

		boolean agentFlag = symbol.id == sym.AGENT;
		boolean modelFlag = symbol.id == sym.MODEL;

		// Read type name
		symbol = scanner.nextToken();
		if (symbol.id != sym.IDENTIFIER)
			throw new Exception("Identifier is expected");

		typeId = new Id((String) symbol.value);

		symbol = scanner.peekToken();
		if (symbol.id == sym.COLON) {
			// Read parent type
			scanner.nextToken();
			symbol = scanner.nextToken();
			if (symbol.id != sym.IDENTIFIER)
				throw new Exception("Identifier is expected");

			parentId = new Id((String) symbol.value);
			parentType = new UnresolvedType(parentId);
		}

		return SparkModel.getInstance().createUserType(typeId, parentType,
				agentFlag, modelFlag);
	}

	/**
	 * Reads a field declaration
	 * 
	 * @param type
	 * @throws Exception
	 */
	private ArrayList<Variable> parseField() throws Exception {
		Symbol symbol = scanner.nextToken();
		if (symbol.id != sym.VAR)
			throw new Exception("parseField(): expected 'var': " + symbol);

		ArrayList<Variable> fields = new ArrayList<Variable>();

		while (true) {
			Variable var = parseVariableDeclaration(true);
			fields.add(var);

			// If the type is explicitly declared, then assign the same
			// type to all variable in the group
			if (!(var.type instanceof UnknownType)) {
				for (int i = 0; i < fields.size(); i++) {
					Variable v = fields.get(i);
					if (v.type instanceof UnknownType)
						v.type = var.type;
				}

				// Stop after first explicit type declaration
				return fields;
			}

			symbol = scanner.peekToken();
			if (symbol.id == sym.COMMA) {
				scanner.nextToken();
				continue;
			}
			
			return fields;
		}
	}

	/**
	 * Reads a global variable declaration
	 * 
	 * @param type
	 * @throws Exception
	 */
	private ArrayList<Variable> parseGlobal() throws Exception {
		Symbol symbol = scanner.nextToken();
		if (symbol.id != sym.GLOBAL)
			throw new Exception("parseGlobal(): expected 'global': " + symbol);

		ArrayList<Variable> fields = new ArrayList<Variable>();

		while (true) {
			Variable var = parseVariableDeclaration(false);
			fields.add(var);

			// If the type is explicitly declared, then assign the same
			// type to all variable in the group
			if (!(var.type instanceof UnknownType)) {
				for (int i = 0; i < fields.size(); i++) {
					Variable v = fields.get(i);
					if (v.type instanceof UnknownType)
						v.type = var.type;
				}

				// Stop after first explicit type declaration
				return fields;
			}

			symbol = scanner.peekToken();
			if (symbol.id == sym.COMMA) {
				scanner.nextToken();
				continue;
			}
			
			return fields;
		}
	}

	/**
	 * Reads method's arguments
	 * 
	 * @param method
	 * @throws Exception
	 */
	private void readArgs(Method method) throws Exception {
		Symbol symbol = scanner.peekToken();
		if (symbol.id != sym.LBRACK)
			return;

		scanner.nextToken();
		while (true) {
			symbol = scanner.peekToken();

			if (symbol.id == sym.RBRACK) {
				scanner.nextToken();
				break;
			}

			if (symbol.id != sym.IDENTIFIER)
				throw new Exception("readArgs(): expected ']' or IDENTIFIER - "
						+ symbol);

			// TODO: no initialization allowed?
			method.addArgument(parseVariableDeclaration(false));
		}

	}

	/**
	 * Reads method's body
	 * 
	 * @param method
	 * @throws Exception
	 */
	private void readBody(Method method) throws Exception {
		while (true) {
			Symbol symbol = scanner.peekToken();
			if (symbol.id == sym.EOF)
				return;
			if (symbol.id == sym.END)
				return;

			symbol = scanner.nextToken();
			method.addSourceCodeSymbol(symbol);
		}
	}

	/**
	 * Reads a method
	 * 
	 * @return
	 * @throws Exception
	 */
	private Method parseMethod() throws Exception {
		Id methodId;
		Method method;
		Symbol symbol = scanner.nextToken();

		if (symbol.id != sym.TO)
			throw new Exception("parseMethod(): expected TO - " + symbol);

		symbol = scanner.nextToken();

		if (symbol.id != sym.IDENTIFIER)
			throw new Exception("parseMethod(): expected IDENTIFIER - "
					+ symbol);

		methodId = new Id((String) symbol.value);
		method = new Method(methodId);

		// Parse arguments
		readArgs(method);

		// Parse return type
		symbol = scanner.peekToken();
		if (symbol.id == sym.COLON) {
			scanner.nextToken();
			Type type = parseTypeDeclaration();

			// TODO: no composite types now
			method.setReturnType(type);
		}

		// Read all source code
		readBody(method);

		symbol = scanner.nextToken();
		if (symbol.id != sym.END)
			throw new Exception("parseMethod(): expected END - " + symbol);

		return method;
	}

}
