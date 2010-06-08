package javasrc;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Stack;

import main.Id;
import main.SparkModel;
import main.Variable;
import main.type.BuiltinType;
import main.type.Type;

/**
 * Very simple java source code emitter
 * @author Monad
 *
 */
public class JavaEmitter {
	/* Output stream */
	protected PrintStream out;
	/* Active line buffer frame */
	protected BufferFrame activeFrame;
	
	/* Hash map for temporary variable counters */
	protected final HashMap<String, Integer> tempVars;
	
	/* Globally defined buffers (like @@object) */
	protected final HashMap<String, Stack<LineBuffer>> globalBuffers;
	
	/**
	 * A frame of buffers 
	 */
	static class BufferFrame {
		HashMap<String, LineBuffer> buffers;
		LineBuffer activeBuffer;
		
		BufferFrame(LineBuffer activeBuffer) {
			this.activeBuffer = activeBuffer;
			this.buffers = new HashMap<String, LineBuffer>();
		}
	}
	
	/* A stack of frame buffers */
	protected final Stack<BufferFrame> frames;
	
	/* A stack of base buffers */
	protected final Stack<LineBuffer> baseBuffers;
	
	
	/* This variable is used for writing out method's arguments */
	private int argsNumber = 0;
	
	/**
	 * Creates an emitter which writes to the out stream
	 * @param out
	 */
	public JavaEmitter(PrintStream out) {
		this.out = out;
		this.frames = new Stack<BufferFrame>();
		this.baseBuffers = new Stack<LineBuffer>();
		this.tempVars = new HashMap<String, Integer>();
		this.globalBuffers = new HashMap<String, Stack<LineBuffer>>();
		
		activeFrame = frames.push(new BufferFrame(new LineBuffer(0)));
	}
	
	
	/**
	 * Prints everything into the output stream
	 */
	public void flush() {
		print(frames.get(0).activeBuffer);
	}
	
	
	/**
	 * Closes the output stream
	 */
	public void close() {
		out.close();
	}
	
	
	/**
	 * Activates a global buffer
	 * A new stack is created for new name
	 * @param name
	 */
	public void pushGlobalBuffer(String name) {
		if (!globalBuffers.containsKey(name)) {
			globalBuffers.put(name, new Stack<LineBuffer>());
		}
		
		Stack<LineBuffer> stack = globalBuffers.get(name);
		LineBuffer buffer = new LineBuffer(0);
		stack.push(buffer);
		
		BufferFrame frame = new BufferFrame(buffer);
		
		// Activate the new frame
		frames.push(frame);
		activeFrame = frame;
	}
	
	
	
	/**
	 * Deactivates a global buffer
	 * @param name
	 */
	// TODO: should be popGlobalBuffer() without arguments,
	// otherwise push A; push B; pop A; pop B; patterns are possible
	public void popGlobalBuffer(String name) throws Exception {
		if (globalBuffers.containsKey(name)) {
			globalBuffers.get(name).pop();
			
			return;
		}
		
		throw new Exception("Buffer " + name + " is not found");
	}
	
	
	/**
	 * Makes the current active buffer a base buffer
	 */
	public void pushBaseBuffer() {
		baseBuffers.push(activeFrame.activeBuffer);
	}
	
	
	/**
	 * Releases one base buffer
	 */
	public void popBaseBuffer() {
		baseBuffers.pop();
	}
	
	
	/**
	 * Clears all temporary variables
	 */
	public void clearTempVariables() {
		tempVars.clear();
	}
	
	/**
	 * Creates a translational temporary variable
	 * @param typeString can contain variables
	 * @param name
	 * @return name of the created variable
	 */
	public String createTempVariable(String typeString, String name) throws Exception {
		String tmpName = "_" + name;
		Integer n = tempVars.get(tmpName);
		if (n != null) {
			int nn = n.intValue() + 1;
			tempVars.put(tmpName, nn);
			tmpName += nn;
		}
		else {
			tempVars.put(tmpName, 0);
		}

		// Null type means that we are declaring variable manually
		if (typeString != null) {
			String typeName = makeSubstitutions(typeString);
			// TODO: it is not a good practice to work with types in this class
			Type type = SparkModel.getInstance().getType(new Id(typeName));
			if (type == null)
				throw new Exception("Cannot create a temporary variable " + name + " because its type " + type + " is not defined");
		
		
			LineBuffer buffer = baseBuffers.peek();
			field(buffer, "", type.getId().toJavaName(), tmpName, null);
		}
		
		return tmpName;
	}
	
	
	/**
	 * Begins a new buffer frame
	 */
	public void setActiveBuffer(String name) throws Exception {
		// Find a new active buffer
		LineBuffer buffer = activeFrame.buffers.get(name);
		
		if (buffer == null)
			throw new Exception("Buffer " + name + " is not found");

		// Create a new frame
		BufferFrame frame = new BufferFrame(buffer);
		
		// Activate the new frame
		frames.push(frame);
		activeFrame = frame;
	}
	
	
	/**
	 * Clears substituion buffers
	 */
	public void clearBuffers() {
		activeFrame.buffers.clear();
	}
	
	
	/**
	 * Adds a new named buffer
	 * @param name
	 * @throws Exception
	 */
	public void addBuffer(String name) throws Exception {
		LineBuffer buffer = activeFrame.buffers.get(name);
		if (buffer != null)
			throw new Exception("Buffer " + name + " is already defined");
		
		buffer = new LineBuffer(0);
		activeFrame.buffers.put(name, buffer);
	}
	
	
	/**
	 * Returns text from the named buffer
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public String getBufferText(String name) throws Exception {
		LineBuffer buffer = activeFrame.buffers.get(name);
		
		if (buffer == null)
			throw new Exception("Buffer " + name + " is not found");
		
		return buffer.toString();
	}

	
	/**
	 * Ends work with the current active buffer
	 * @throws Exception
	 */
	public void endBuffer() throws Exception {
		frames.pop();
		BufferFrame frame = frames.peek();
		if (frame == null)
			throw new Exception("Stack is empty");
		
		activeFrame = frame;
	}
	
	
	/**
	 * Prints out a string buffer
	 * @param buffer
	 */
	public void print(LineBuffer buffer) {
		int indent = buffer.indent;
		String indentString = "";
		
		for (int i = 0; i < indent; i++)
			indentString += "\t";
		
		for (int i = 0; i < buffer.lines.size(); i++) {
			String str = buffer.lines.get(i).toString().trim();

			if (str.equals("}")) {
				indent--;
				if (indent > 0) 
					indentString = indentString.substring(0, indent);
				else
					indentString = "";
			}
			
			out.print(indentString);
			out.println(str);
			
			if (str.equals("{")) {
				indent++;
				indentString += "\t";
			}
		}
	}
	
	
	/**
	 * Prints out a string
	 * @param str
	 */
	public void println(String str) {
		println(activeFrame.activeBuffer, str);
	}
	
	
	/**
	 * Prints out a string
	 * @param str
	 */
	public void print(String str) {
		print(activeFrame.activeBuffer, str);
	}

	
	/**
	 * Prints out a string
	 * @param str
	 */
	public void println(LineBuffer buffer, String str) {
		buffer.println(str);
	}
	
	
	/**
	 * Prints out a string
	 * @param str
	 */
	public void print(LineBuffer buffer, String str) {
		buffer.print(str);
	}

	
	/**
	 * Prints several lines of a text and makes substitutions
	 * @param text
	 */
	public void printText(String text) {
		text = makeSubstitutions(text);
		
		String[] lines = text.split("\n");
		for (int i = 0; i < lines.length; i++) {
			print(lines[i]);
			if (i != lines.length - 1)
				endLine();
		}
	}

	
	/**
	 * Prints several lines of a text into a base buffer
	 * and makes substitutions (from the current active frame)
	 * @param text
	 */
	public void printTextToBaseBuffer(String text) {
		text = makeSubstitutions(text);
		
		LineBuffer buffer = baseBuffers.peek();
		
		String[] lines = text.split("\n");
		for (int i = 0; i < lines.length; i++) {
			buffer.print(lines[i]);
//			if (i != lines.length - 1)
			buffer.endLine();
		}
	}

	
	/**
	 * Makes substitutions
	 * @param str
	 * @return
	 */
	public String makeSubstitutions(String str) {
		for (String key : globalBuffers.keySet()) {
			Stack<LineBuffer> stack = globalBuffers.get(key);
			if (stack.size() > 0) {
				LineBuffer buffer = stack.peek();
				str = str.replaceAll(key, buffer.toString());
			}
		}
		
		for (String key : activeFrame.buffers.keySet()) {
			str = str.replaceAll(key, activeFrame.buffers.get(key).toString());
		}
		
		return str;
	}
	
	
	/**
	 * Prints an empty line
	 */
	public void emptyLine() {
		activeFrame.activeBuffer.println();
	}
	
	
	/**
	 * Ends a line
	 */
	public void endLine() {
		activeFrame.activeBuffer.endLine();
	}
	
	
	/**
	 * Ends a line and puts ; if necessary
	 */
	public void endJavaLine() {
		activeFrame.activeBuffer.endJavaLine();
	}
	
	
	/**
	 * Begins a block {}
	 */
	public void beginBlock() {
		activeFrame.activeBuffer.println("{");
	}
	
	
	/**
	 * Ends a block
	 */
	public void endBlock() {
		activeFrame.activeBuffer.println("}");
	}
	
	
	/**
	 * Begins a class declaration
	 * @param modifier
	 * @param className
	 * @param parentName
	 */
	public void beginClass(String modifier, Id className, Type parentType) {
		String str = modifier + " class " + className.toJavaName();
		if (parentType != null) {
			// TODO: find more elegant solution
			if (parentType instanceof BuiltinType && ((BuiltinType) parentType).isInterface())
				str += " implements ";
			else
				str += " extends ";
			
			try {
				str += parentType.getId().toJavaName();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		println(str);
		beginBlock();
	}
	
	
	/**
	 * Ends a class declaration
	 */
	public void endClass() {
		endBlock();
	}
	
	
	/**
	 * Creates a field declaration
	 * @param modifier
	 * @param typeName
	 * @param fieldName
	 * @param initialValue
	 */
	public void field(String modifier, String typeName, String fieldName, String initialValue) {
		field(activeFrame.activeBuffer, modifier, typeName, fieldName, initialValue);
	}
	
	
	/**
	 * Creates a field declaration
	 * @param modifier
	 * @param typeName
	 * @param fieldName
	 * @param initialValue
	 */
	public void field(LineBuffer buffer, String modifier, String typeName, String fieldName, String initialValue) {
		String str = modifier + " " + typeName + " " + fieldName;
		if (initialValue != null)
			str += " = " + initialValue;
		
		str += ";";
		println(buffer, str);
	}
	
	/**
	 * Creates a field declaration
	 * @param field
	 */
	public void field(Variable field) {
		String modifier;
		
		if (field.global)
			modifier = "public static";
		else if (field.shared)
			modifier = "protected static";
		else
			modifier = "protected";
		
		String declaration = "// Error!";
		
		try {
			declaration = field.getDeclarationTranslation();
//			Id typeId = field.type.getId();
//			if (typeId == null)
//				typeName = "unknown";
//			else
//				typeName = typeId.toJavaName();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		println(modifier + " " + declaration);
//		field(modifier, typeName, field.id.toJavaName(), null);
	}
	
	
	
	/**
	 * Creates a method declaration
	 * @param modifier
	 * @param typeName null => void
	 * @param methodName
	 * @param argsNumber
	 */
	public void beginMethod(String modifier, String typeName, String methodName, int argsNumber) {
		this.argsNumber = argsNumber;
		
		String str = modifier;
		
		if (typeName == null)
			str += " void";
		else
			str += " " + typeName;
		
		str += " " + methodName;
		str += "(";
		
		if (argsNumber <= 0) {
			str += ")";
			print(str);
			beginBlock();
		}
		else {
			print(str);
		}
	}
	
	
	/**
	 * Adds a method argument
	 * @param typeName
	 * @param argName
	 */
	public void addArgument(Type type, Id argId) {
		String typeName = "";
		
		try {
			if (type.getId() != null)
				typeName = type.getTranslationString();
			else
				typeName = "unknown";
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		String argName = argId.toJavaName();
		
		String str = typeName + " " + argName;
		argsNumber--;
		if (argsNumber <= 0) {
			print(str + ")");
			beginBlock();
		}
		else {
			print(str + ", ");
		}
	}
	
	
	/**
	 * Finished a method declaration
	 */
	public void endMethod() {
		endBlock();
	}
}
