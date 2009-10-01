package parser.tree;

import java.util.ArrayList;

import main.AdhocImplementation;
import main.CodeBlock;
import main.Command;
import main.Id;
import main.SparkModel;
import main.Variable;
import main.type.BlockType;
import main.type.CompositeType;
import main.type.Type;
import main.type.UnknownType;
import parser.Symbol;
import parser.SymbolList;
import parser.sym;

public class SyntaxTreeBuilder {
	/* Active type */
	// static Type currentType;
	/* Active code block */
	static CodeBlock currentBlock;

	/**
	 * Auxiliary pair class
	 */
	static class Pair<A, B> {
		public A a;
		public B b;

		public Pair(A a, B b) {
			this.a = a;
			this.b = b;
		}
	}

	public static void setCurrentBlock(CodeBlock block) {
		currentBlock = block;
	}

	/**
	 * Returns a command with the given name from the global namespace or from
	 * the current class Also returns the reference to the calling object (self)
	 * 
	 * @param name
	 * @return
	 */
	public static Pair<Command, Variable> getCommand(String name) {
		Command command;
		Type currentType = null;

		if (currentBlock != null)
			currentType = currentBlock.getSelfType();

		if (currentType != null) {
			command = currentType.getCommand(name);
			if (command != null)
				return new Pair<Command, Variable>(command, currentBlock
						.getSelfVariable());
		}

		command = SparkModel.getInstance().getCommand(name);

		return new Pair<Command, Variable>(command, null);
	}

	/**
	 * Returns a variable with the given name from the global namespace, or from
	 * the current class, or from the local variable list Also returns the
	 * reference to the calling object (self)
	 * 
	 * @param name
	 * @return
	 */
	public static Pair<Variable, Variable> getVariable(String name) {
		Variable var;
		Type currentType = null;

		if (currentBlock != null)
			currentType = currentBlock.getSelfType();

		// Look at the local variables first
		if (currentBlock != null) {
			var = currentBlock.getLocalVariable(name);
			if (var != null)
				return new Pair<Variable, Variable>(var, null);
		}

		// Look at the local fields
		if (currentType != null) {
			var = currentType.getField(new Id(name));
			if (var != null)
				return new Pair<Variable, Variable>(var, currentBlock
						.getSelfVariable());
		}

		// Look at global variables
		var = SparkModel.getInstance().getGlobalVariable(name);

		// Global variables are treated as local ones:
		// they have no reference to the objects which contain them
		return new Pair<Variable, Variable>(var, null);
	}

	/**
	 * Parses a local variable declaration
	 * 
	 * @param codeBlock
	 * @throws Exception
	 */
	public static ArrayList<Variable> parseLocalVariableDeclaration(
			SymbolList source) throws Exception {
		Symbol s = source.next();
		if (s.id != sym.VAR)
			throw new Exception("var keyword is expected: got " + s);

		ArrayList<Variable> vars = new ArrayList<Variable>();

		while (true) {
			s = source.next();
			if (s.id != sym.IDENTIFIER)
				throw new Exception("An identifier is expected: " + s);

			Id id = new Id((String) s.value);
			TreeNode node = null;
			Type type = new UnknownType();

			s = source.peek();
			if (s.id == sym.EQ) {
				source.next();
				// Parse initialization
				node = expressionNode(source);
				// TODO: explicit type?
			} else if (s.id == sym.COLON) {
				// Parse explicit type declaration
				source.next();
				s = source.next();
				if (s.id != sym.IDENTIFIER)
					throw new Exception("An identifier is expected: " + s);

				type = SparkModel.getInstance().getType(
						new Id((String) s.value));
				if (type == null)
					throw new Exception("Type " + s + " is not defined");
			}

			Variable var = new Variable(id, type);
			var.initializationExpression = node;

			// TODO: does it do anything?
			var.resolveDeclarationTypes();
			vars.add(var);

			// Quit after explicit type declaration
			if (!(type instanceof UnknownType)) {
				for (Variable var2 : vars) {
					if (var2.type instanceof UnknownType)
						var2.type = type;

					// TODO: does it do anything?
					var2.resolveDeclarationTypes();
				}

				break;
			}

			// Other variables?
			s = source.peek();
			if (s.id != sym.COMMA)
				break;

			source.next();
		}

		return vars;
	}

	/**
	 * Parses a command or declaration
	 * 
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public static void parse(SymbolList list, BlockNode code) throws Exception {
		Symbol s = list.peek();

		if (s.id == sym.VAR) {
			ArrayList<Variable> vars = SyntaxTreeBuilder
					.parseLocalVariableDeclaration(list);

			for (Variable var : vars) {
				code.getCodeBlock().addLocalVariable(var);
				if (var.initializationExpression != null) {
					AssignmentNode node = new AssignmentNode(s);
					node.addNode(new VariableNode(s, var));
					node.addNode(var.initializationExpression);
					code.addCommand(node);
				}
			}

			return;
		}

		TreeNode node = null;

		if (s.id == sym.FOR)
			node = forNode(list);
		else
			node = assignmentNode(list);

		if (node instanceof CommandNode) {
			code.addCommand((CommandNode) node);
		} else {
			throw new Exception("A command is expected: " + s);
		}
	}

	/**
	 * Parses an assignment (if any)
	 * 
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public static TreeNode assignmentNode(SymbolList list) throws Exception {
		TreeNode left = expressionNode(list);
		AssignmentNode node;

		Symbol s = list.peek();

		// Standard assignment
		if (s.id == sym.EQ) {
			list.next();
			node = new AssignmentNode(s);
			node.addNode(left);
			node.addNode(expressionNode(list));

			return node;
		}

		// Assignment with an operation
		if (s.id == sym.OPERATOR) {
			String name = (String) s.value;
			Command cmd = SparkModel.getInstance().getCommand(name);
			
			// We are interested only in assignment commands
			// which have the lowest precedence
			if (cmd.getPrecedence() != 0)
				return left;
			
			list.next();
			
			// TODO: better solution is required
			// for resolving the += problem
			if (left instanceof DotNode) {
				TreeNode right = ((DotNode) left).getNode(1);
				if (right instanceof VariableNode) {
					Variable var = ((VariableNode) right).var;
					
					if (var.specialTranslation) {
						String opName = name.substring(0, 1);
						
						CommandNode op = new CommandNode(s, 
								SparkModel.getInstance().getCommand(opName));
						
						op.addNode(left);
						op.addNode(expressionNode(list));
						node = new AssignmentNode(s);
						node.addNode(left);
						node.addNode(op);
						
						return node;
					}
				}
			}
			
			node = new AssignmentNode(s, cmd);
			node.addNode(left);
			node.addNode(expressionNode(list));
			
			return node;
		}

		return left;
	}

	/**
	 * Parses an expression
	 * 
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public static TreeNode expressionNode(SymbolList list) throws Exception {
		return operationNode(list, 1);
	}

	/**
	 * Parses an operation with the given precedence level
	 * 
	 * @param list
	 * @param level
	 * @return
	 * @throws Exception
	 */
	public static TreeNode operationNode(SymbolList list, int level)
			throws Exception {
		if (level < 1 || level > 9)
			throw new Exception(
					"operationNode(): level should be between 1 and 9");
		if (level == 9)
			return unaryOperationNode(list);

		TreeNode left = operationNode(list, level + 1);

		while (true) {
			Symbol s = list.peek();
			if (s.id != sym.IDENTIFIER && s.id != sym.OPERATOR)
				return left;

			Command cmd = getCommand((String) s.value).a;
			if (cmd == null || !cmd.isInfix())
				return left;

			if (cmd.getPrecedence() != level)
				return left;

			list.next();
			CommandNode node = new CommandNode(s, cmd);
			cmd.parseInfixCommandArguments(list, node, left);
			left = node;
		}

	}

	/**
	 * Unary operations node (of the highest priority)
	 * 
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public static TreeNode unaryOperationNode(SymbolList list) throws Exception {
		// TODO: check this code
		Symbol s = list.peek();

		if (s.id == sym.OPERATOR) {
			if (s.value.equals("-")) {
				list.next();
				Command cmd = getCommand("@-").a;
				if (cmd == null)
					throw new Exception(
							"Unary minus command '@-' is not defined");
				CommandNode node = new CommandNode(s, cmd);
				cmd.parseCommandArguments(list, node);

				return node;
			}
		}

		return dotNode(list);
	}

	/**
	 * Parses a dot operator
	 * 
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public static TreeNode dotNode(SymbolList list) throws Exception {
		TreeNode left = atomNode(list);

		while (true) {
			Symbol s = list.peek();
			if (s.id != sym.DOT)
				break;

			list.next();
			if (currentBlock == null)
				throw new Exception("Dot command is not allowed here: " + s);
			left.pushCodeBlock(currentBlock);
			Type dottedType = left.resolveType(null, TreeNode.DOTLVALUE);
			left.popCodeBlock();

			DotNode node = new DotNode(s);
			node.addNode(left);
			node.addNode(dotAtomNode(list, dottedType));
			left = node;
		}

		return left;
	}

	/**
	 * Parses an atom expression on the right hand side of a dot
	 * 
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public static TreeNode dotAtomNode(SymbolList list, Type dottedType)
			throws Exception {
		Symbol s = list.peek();

		if (s.id == sym.END)
			throw new Exception("dotAtomNode(): no symbols");

		Variable var;

		switch (s.id) {
		case sym.IDENTIFIER:
			String name = (String) s.value;
			Command cmd = dottedType.getCommand(name);
			if (cmd != null) {
				list.next();
				CommandNode node = new CommandNode(s, cmd);
				cmd.parseCommandArguments(list, node);

				return node;
			}

			list.next();
			var = dottedType.getField(new Id(name));
			if (var == null) {
				throw new Exception("dotAtomNode(): Identifier " + name
						+ " is not recognized: " + s);
			}

			return new VariableNode(s, var);

		default:
			throw new Exception("dotAtomNode(): Illegal symbol - " + s);
		}
	}

	/**
	 * Parses an atom expression
	 * 
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public static TreeNode atomNode(SymbolList list) throws Exception {
		Symbol s = list.peek();

		if (s.id == sym.END)
			throw new Exception("atomNode(): no symbols");

		TreeNode node;
		Variable var;

		switch (s.id) {
		case sym.LPAREN:
			list.next();
			node = expressionNode(list);

			s = list.next();
			if (s.id != sym.RPAREN)
				throw new Exception("atomNode(): ')' is expected - " + s);

			return node;

		case sym.DOUBLE:
			list.next();
			return new ConstantNode(s);

		case sym.STRING:
			list.next();
			return new ConstantNode(s);

		case sym.LBRACK:
			return parseVectorConstant(list);

		case sym.IDENTIFIER:
		case sym.OPERATOR:
			String name = (String) s.value;
			if (name.equals("<"))
				return parseVectorConstant(list);

			Command cmd = getCommand(name).a;
			if (cmd != null) {
				return commandNode(list);
			}

			list.next();
			Pair<Variable, Variable> varPair = getVariable(name);
			var = varPair.a;
			if (var == null) {
				throw new Exception("atomNode(): Identifier " + name
						+ " is not recognized: " + s);
			}

			// TODO: explicit 'self' translation is not considered here
			if (varPair.b != null) {
				DotNode dotNode = new DotNode(s);
				dotNode.addNode(new VariableNode(s, varPair.b));
				dotNode.addNode(new VariableNode(s, varPair.a));

				return dotNode;
			}

			return new VariableNode(s, var);

		default:
			throw new Exception("atomNode(): Illegal symbol - " + s);
		}
	}

	/**
	 * Parses an optional code block
	 * 
	 * @param list
	 * @return
	 * @throws Exception
	 */
	// FIXME: remove the third argument
	public static TreeNode optionalBlockNode(SymbolList list,
			BlockType blockType, TreeNode first) throws Exception {
		Symbol s = list.peek();

		if (s.id != sym.LBRACK)
			return null;
		else
			return blockNode(list, blockType, null, first);
	}

	/**
	 * Parses a code block
	 * 
	 * @param list
	 * @param predefinedBlock
	 *            is used for predefined block with given local variables
	 * @return
	 * @throws Exception
	 */
	// FIXME: remove the fourth argument
	public static BlockNode blockNode(SymbolList list, BlockType blockType,
			BlockNode predefinedBlock, TreeNode first) throws Exception {
		Symbol s = list.next();
		if (s.id != sym.LBRACK)
			throw new Exception("blockNode(): '[' is expected: " + s);

		if (currentBlock == null)
			throw new Exception("Block [] is not allowed here: " + s);

		BlockNode node = predefinedBlock;
		if (node == null)
			node = new BlockNode(s, currentBlock);

		CodeBlock oldBlock = currentBlock;
		currentBlock = node.getCodeBlock();

		if (blockType.getSelfType() != null) {

			// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			// FIXME: find better solution of the 'ask problem'
			AdhocImplementation.flag = true;
			first.pushCodeBlock(oldBlock);
			Type type = first.resolveType(null, 0);
			first.popCodeBlock();

			if (type instanceof CompositeType) {
				type = ((CompositeType) type).getSubtype();
				if (type == null)
					throw new Exception("Sub-type is not defined");
			}

			// currentBlock.setSelf(blockType.getSelfType());
			currentBlock.setSelf(type);

			// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		}

		while (list.peek().id != sym.END) {
			s = list.peek();
			if (s.id == sym.RBRACK) {
				list.next();
				currentBlock = oldBlock;
				return node;
			}

			parse(list, node);
		}

		throw new Exception("blockNode(): ']' is expected: " + s);
	}

	/**
	 * Parses a name (identifier)
	 * 
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public static VariableNode nameNode(SymbolList list) throws Exception {
		Symbol s = list.next();

		if (s.id != sym.IDENTIFIER)
			throw new Exception("Identifier expected:" + s);

		Id typeId = new Id((String) s.value);
		Type type = SparkModel.getInstance().getType(typeId);
		if (type == null)
			throw new Exception("Type is not recognized: " + s);

		Variable var = new Variable(new Id(type.getId().toJavaName()), type);
		return new VariableNode(s, var);
	}

	/**
	 * Parses a 'for' control structure
	 * 
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public static ForNode forNode(SymbolList list) throws Exception {
		Symbol s = list.next();

		if (s.id != sym.FOR)
			throw new Exception("'for' is expected: " + s);

		s = list.next();
		if (s.id != sym.IDENTIFIER)
			throw new Exception("An identifier is expected: " + s);

		Variable counter = new Variable(new Id((String) s.value), SparkModel
				.getInstance().getType(new Id("double")));

		s = list.next();
		if (s.id != sym.EQ)
			throw new Exception("'=' is expected: " + s);

		TreeNode from = null;
		TreeNode to = null;
		TreeNode step = null;

		// From expression
		from = expressionNode(list);

		s = list.next();
		if (s.id != sym.COLON)
			throw new Exception("':' is expected: " + s);

		// To/Step expression
		TreeNode toOrStep = expressionNode(list);

		s = list.peek();
		// Step is specified
		if (s.id == sym.COLON) {
			list.next();
			step = toOrStep;
			to = expressionNode(list);
		} else {
			// No step : default step = 1
			to = toOrStep;
			step = new ConstantNode(new Symbol("DOUBLE", sym.DOUBLE,
					new Double(1), -1, -1));
		}

		// Block
		BlockNode block = new BlockNode(s, currentBlock);
		block.addLocalVariable(counter);

		block = blockNode(list, BlockType.getInstance(), block, null);

		ForNode node = new ForNode(counter);
		node.addNode(from);
		node.addNode(to);
		node.addNode(step);
		node.addNode(block);

		return node;
	}

	/**
	 * Parses a pure (void return type) command
	 * 
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public static CommandNode commandNode(SymbolList list) throws Exception {
		Symbol s = list.next();
		if (s.id != sym.IDENTIFIER)
			throw new Exception("commandNode(): IDENTIFIER is expected - " + s);

		Pair<Command, Variable> commandPair = getCommand((String) s.value);
		Command cmd = commandPair.a;
		if (cmd == null)
			throw new Exception("Command " + s.value + " is not defined");

		CommandNode node = new CommandNode(s, cmd);
		cmd.parseCommandArguments(list, node);

		if (commandPair.b != null) {
			DotNode dotNode = new DotNode(s);
			dotNode.addNode(new VariableNode(s, commandPair.b));
			dotNode.addNode(node);

			return dotNode;
		}

		return node;
	}

	/**
	 * Parses a vector constant
	 * 
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public static ConstantNode parseVectorConstant(SymbolList list)
			throws Exception {
		Symbol s = list.next();

		if (!(s.id == sym.LBRACK) && !s.value.equals("<"))
			throw new Exception("< or [ is expected: " + s);

		double[] xyz = new double[3];

		// TODO: 2-d vectors
		for (int i = 0; i < 3; i++) {
			// TODO: expressions in vector declaration
			s = list.next();
			if (s.id != sym.DOUBLE)
				throw new Exception("A number is expected: " + s);

			xyz[i] = (Double) s.value;

			if (i == 2)
				continue;

			s = list.next();
			if (s.id != sym.COMMA)
				throw new Exception(", is expected: " + s);

		}

		s = list.next();

		if (!(s.id == sym.RBRACK) && !s.value.equals(">"))
			throw new Exception("> or ] is expected: " + s);

		return new ConstantNode(xyz);
	}

}
