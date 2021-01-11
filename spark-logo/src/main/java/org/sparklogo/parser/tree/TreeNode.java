package org.sparklogo.parser.tree;

import java.util.Stack;

import org.sparklogo.parser.Symbol;

import org.sparklogo.main.CodeBlock;
import org.sparklogo.main.type.Type;
import org.sparklogo.javasrc.JavaEmitter;


public abstract class TreeNode {
	/* Indicates that an lvalue is requested */
	public final static int LVALUE = 0x1;
	/* Indicates that a lvalue of the dot operator is requested */
	public final static int DOTLVALUE = 0x2;
	/* Indicates that a value should be returned */
	public final static int GET_VALUE = 0x4;
	
	/* Code block stack (used in translation) */
	private static Stack<CodeBlock> currentBlocks = new Stack<CodeBlock>();
	
	
	public static interface Visitor {
		public abstract boolean visit(TreeNode node);
	}
	
	
	/* Flags of the node */
	protected int nodeFlags;
	
	
	/* Symbol associated with this node (for error reports) */
	protected Symbol symbol;
	
	/**
	 * Creates a new node associated with the given symbol
	 * @param symbol
	 */
	public TreeNode(Symbol symbol) {
		this.symbol = symbol;
	}
	
	/**
	 * Sets the active code block
	 * @param block
	 */
	public void pushCodeBlock(CodeBlock block) {
		currentBlocks.push(block);
	}
	
	
	/**
	 * Returns to the previous code block
	 * @return
	 */
	public CodeBlock popCodeBlock() {
		return currentBlocks.pop();
	}
	
	
	/**
	 * Returns the active code block
	 * @return
	 */
	protected CodeBlock currentBlock() {
		return currentBlocks.peek();
	}
	

	public void debugPrint(java.io.PrintStream out) throws Exception {
	}
	
	public abstract void translate(JavaEmitter java, int flag) throws Exception;

	/**
	 * Returns a type associated with a tree node.
	 * @return
	 */
	public abstract Type getType() throws Exception;

	/**
	 * Resolves types of a node and its children 
	 * @return
	 * @throws Exception
	 */
	public abstract Type resolveType(Type expectedType, int flag) throws Exception;
	
	
	/**
	 * Visits all nodes and calls the visitor's function for each node
	 * @param visitor
	 * @return
	 */
	public boolean visitAll(Visitor visitor) {
		return visitor.visit(this);
	}
}
