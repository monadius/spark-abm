package com.sparkabm.math.parser;

/**
 * Author: Solovyev Alexey
 * Date: 01.11.2005
 * Time: 10:15:36
 */

/**
 * Builds a parser tree
 */
class ParserTreeBuilder {
	private SourceTokenizer src;
	private ParserTree tree;

	/**
	 * Constructor
	 */
	public ParserTreeBuilder() {
	}

	/**
	 * Creates a parser tree from the given string
	 */
	public ParserTree create(String str) throws Exception {
		return create(new SourceTokenizer(str));
	}

	/**
	 * Creates a parser tree from the given stream of tokens
	 */
	public ParserTree create(SourceTokenizer src) throws Exception {
		this.src = src;
		this.tree = new ParserTree();

		tree.setRoot(level1());
		return tree;
	}

	/**
	 * Parses +, -
	 */
	private TreeNode level1() throws Exception {
		TreeNode left = level2();

		while (true) {
			int token = src.nextToken();
			if (token != Token.PLUS && token != Token.MINUS)
				break;

			TreeNode node;
			if (token == Token.PLUS)
				node = new OperationNode(tree, '+');
			else
				node = new OperationNode(tree, '-');

			// +, - are left associative
			TreeNode right = level2();
			node.setLeftRight(left, right);
			left = node;
		}

		src.putBackLastToken();
		return left;
	}

	/**
	 * Parses *, /
	 */
	private TreeNode level2() throws Exception {
		TreeNode left = level3();

		while (true) {
			int token = src.nextToken();
			if (token != Token.MUL && token != Token.DIV)
				break;

			TreeNode node;
			if (token == Token.MUL)
				node = new OperationNode(tree, '*');
			else
				node = new OperationNode(tree, '/');

			// *, / are left associative
			TreeNode right = level3();
			node.setLeftRight(left, right);
			left = node;
		}

		src.putBackLastToken();
		return left;
	}

	/**
	 * Parses unary -, +
	 */
	private TreeNode level3() throws Exception {
		int token = src.nextToken();

		if (token == Token.MINUS) {
			TreeNode node = new OperationNode(tree, 'm');
			node.setLeftRight(level3(), null);
			return node;
		}

		if (token == Token.PLUS)
			return level3();

		src.putBackLastToken();
		return level4();
	}

	/**
	 * Parses ^
	 */
	private TreeNode level4() throws Exception {
		TreeNode left = atom();

		if (src.nextToken() == Token.POWER) {
			// ^ is right associative
			TreeNode right = level3();
			TreeNode node = new OperationNode(tree, '^');
			node.setLeftRight(left, right);
			return node;
		}

		src.putBackLastToken();
		return left;
	}

	/**
	 * Parses parentheses, numbers, variables, function applications
	 */
	private TreeNode atom() throws Exception {
		int token = src.nextToken();

		switch (token) {
		// number
		case Token.NUMBER:
			return new ConstNode(tree, src.getValue());

			// (...)
		case Token.LBRACKET:
			TreeNode node = level1();
			if (src.nextToken() != Token.RBRACKET)
				throw new Exception(") is missing");
			return node;

			// identifier: variable or function
		case Token.IDENTIFIER:
			String name = src.getValue();

			token = src.nextToken();
			src.putBackLastToken();

			if (token == Token.LBRACKET)
				return function(name);
			else
				return new VariableNode(tree, name);
		}

		throw new Exception("Unknown atomic expression");
	}

	/**
	 * Parses a function application
	 */
	private TreeNode function(String name) throws Exception {
		if (src.nextToken() != Token.LBRACKET)
			throw new Exception("Error ( in function");

		FunctionNode f = new FunctionNode(tree, name);

		if (src.nextToken() == Token.RBRACKET)
			return f;

		src.putBackLastToken();

		while (true) {
			f.addArgument(level1());

			int token = src.nextToken();

			if (token == Token.RBRACKET)
				break;
			if (token != Token.COMMA)
				throw new Exception("Error: , expected");
		}

		return f;
	}

}
