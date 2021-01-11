package com.sparkabm.math.parser;

import java.util.ArrayList;

/**
 * Author: Solovyev Alexey Date: 01.11.2005 Time: 10:41:13
 */

/**
 * Describes a tree node
 */
abstract class TreeNode {
	// Branches
	protected TreeNode left, right;
	// Reference to the whole tree
	protected ParserTree tree;

	/**
	 * Constructor
	 */
	TreeNode(ParserTree tree) {
		this.tree = tree;
	}

	/**
	 * Sets left and right branches
	 */
	void setLeftRight(TreeNode left, TreeNode right) {
		this.left = left;
		this.right = right;
	}

	/**
	 * Compiles the node
	 */
	abstract void processNode(ArrayList<Integer> program) throws Exception;

	/**
	 * Optimizes the node
	 */
	TreeNode optimize() throws Exception {
		if (left != null)
			left = left.optimize();
		if (right != null)
			right = right.optimize();
		return this;
	}
}

/**
 * Defines a (double) constant node
 */
class ConstNode extends TreeNode {
	// Constant value
	private final double val;

	/**
	 * Constructor
	 */
	public ConstNode(ParserTree tree, double val) {
		super(tree);
		this.val = val;
	}

	/**
	 * Constructor
	 */
	public ConstNode(ParserTree tree, String str) throws Exception {
		this(tree, Double.parseDouble(str));
	}

	/**
	 * Returns the constant value of the node
	 */
	double getValue() {
		return val;
	}

	/**
	 * Compiles the node
	 */
	void processNode(ArrayList<Integer> program) throws Exception {
		program.add(new Integer(UserFunction.CLOAD));
		program.add(new Integer(tree.addConstant(val)));

		tree.changeStack(1);
	}

	@Override
	public String toString() {
		return String.valueOf(val);
	}
}

/**
 * Defines a node for a variable
 */
class VariableNode extends TreeNode {
	// Variable name
	private final String name;

	/**
	 * Constructor
	 */
	public VariableNode(ParserTree tree, String name) {
		super(tree);
		this.name = name;
	}

	/**
	 * Compiles the node
	 */
	void processNode(ArrayList<Integer> program) throws Exception {
		program.add(new Integer(UserFunction.LOAD));

		int n = tree.addVar(name);
		program.add(new Integer(n));
		tree.changeStack(1);
	}

	@Override
	public String toString() {
		return name;
	}
}

/**
 * Defines an operation node
 */
class OperationNode extends TreeNode {
	// Operation identifier
	private char op;

	/**
	 * Constructor
	 */
	public OperationNode(ParserTree tree, char op) {
		super(tree);
		this.op = op;
	}

	/**
	 * Performs operations on constants
	 */
	TreeNode optimize() throws Exception {
		// Optimize left and right branches
		super.optimize();

		double lval = 1;
		double rval = 1;

		if (left instanceof ConstNode) {
			// Negate a constant
			lval = ((ConstNode) left).getValue();
			if (op == 'm')
				return new ConstNode(tree, -lval);
		}

		if (right instanceof ConstNode) {
			rval = ((ConstNode) right).getValue();
		}

		// Perform binary operations on constants
		if (left instanceof ConstNode && right instanceof ConstNode) {
			switch (op) {
			case '+':
				lval += rval;
				break;
			case '-':
				lval -= rval;
				break;
			case '*':
				lval *= rval;
				break;
			case '/':
				lval /= rval;
				break;
			case '^':
				lval = Math.pow(lval, rval);
				break;
			default:
				throw new Exception("Undefined operation: " + op);
			}

			return new ConstNode(tree, lval);
		}

		// Optimize operations involving 0, 0^n = 0 for all real n by convention
		if (lval == 0.0) {
			switch (op) {
			case '+':
				return right;
			case '-': {
				TreeNode node = new OperationNode(tree, 'm');
				node.setLeftRight(right, null);
				return node;
			}

			case '*':
			case '/':
			case '^':
				return new ConstNode(tree, 0.0);
			}
		} else if (rval == 0) {
			switch (op) {
			case '+':
			case '-':
				return left;

			case '*':
				return new ConstNode(tree, 0.0);

			case '^':
				return new ConstNode(tree, 1.0);
			}
		}

		return this;
	}

	/**
	 * Compiles the node
	 */
	void processNode(ArrayList<Integer> program) throws Exception {
		if (op == 'm') {
			left.processNode(program);
			program.add(new Integer(UserFunction.NEG));
			return;
		}

		right.processNode(program);
		left.processNode(program);

		switch (op) {
		case '+':
			program.add(new Integer(UserFunction.ADD));
			break;
		case '-':
			program.add(new Integer(UserFunction.SUB));
			break;
		case '*':
			program.add(new Integer(UserFunction.MUL));
			break;
		case '/':
			program.add(new Integer(UserFunction.DIV));
			break;
		case '^':
			program.add(new Integer(UserFunction.POWER));
			break;
		default:
			throw new Exception("Undefined operation: " + op);
		}

		tree.changeStack(-1);
	}

	@Override
	public String toString() {
		if (op == 'm')
			return "-" + left.toString();
		return "(" + left.toString() + ' ' + op + ' ' + right.toString() + ')';
	}
}

/**
 * Defines a node for a function
 */
class FunctionNode extends TreeNode {
	private final String name;
	private final ArrayList<TreeNode> args;

	/**
	 * Constructor
	 */
	public FunctionNode(ParserTree tree, String name) {
		super(tree);
		this.name = name;
		this.args = new ArrayList<TreeNode>();
	}

	/**
	 * Adds an argument
	 */
	public void addArgument(TreeNode arg) {
		args.add(arg);
	}

	/**
	 * Optimizes the arguments
	 */
	TreeNode optimize() throws Exception {
		for (int i = 0; i < args.size(); i++) {
			TreeNode node = (TreeNode) args.get(i);
			node = node.optimize();
			args.set(i, node);
		}

		return this;
	}

	/**
	 * Compiles the node
	 */
	void processNode(ArrayList<Integer> program) throws Exception {
		int code = UserFunction.isBuiltin(name);

		if (code > 0 && args.size() != 1)
			throw new Exception("Bad args number for the built-in function: "
					+ name);

		// Compile arguments
		for (int i = 0; i < args.size(); i++) {
			((TreeNode) args.get(i)).processNode(program);
		}

		if (code > 0) {
			// Built-in function
			program.add(new Integer(code));
		} else {
			int n = UserFunction.isUserFunction(name);
			if (n < 0)
				throw new Exception("Undefined function: " + name);

			UserFunction f = UserFunction.getUserFunction(n);
			if (f.getArgsNumber() != args.size())
				throw new Exception("Bad number of arguments in function: "
						+ name);

			program.add(new Integer(UserFunction.INVOKE));
			program.add(new Integer(n));
		}

		tree.changeStack(-args.size() + 1);
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer(name);

		str.append('(');
		for (int i = 0; i < args.size(); i++) {
			str.append(args.get(i).toString());
			if (i == args.size() - 1)
				str.append(')');
			else
				str.append(", ");
		}

		return str.toString();
	}
}
