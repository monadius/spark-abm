package org.sparklogo.parser.tree;

import java.util.*;

import org.sparklogo.parser.Symbol;


public abstract class MultiNode extends TreeNode {
	protected ArrayList<TreeNode> children = new ArrayList<TreeNode>(10);
	
	public MultiNode(Symbol symbol) {
		super(symbol);
	}
	
	public MultiNode(Symbol symbol, TreeNode node) {
		super(symbol);
		children.add(node);
	}
	
	public void addNode(TreeNode node) {
		children.add(node);
	}
	
	public TreeNode getNode(int i) {
		return children.get(i);
	}
	
	@Override
	public boolean visitAll(Visitor visitor) {
		for (TreeNode child : children) {
			if (!child.visitAll(visitor))
				return false;
		}
		
		return visitor.visit(this);
	}
}
