package org.spark.math.parser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Author: Solovyev Alexey
 * Date: 01.11.2005
 * Time: 10:16:53
 */

/**
 * A tree which represents a simple math expression
 */
class ParserTree {
	// The root
	TreeNode root;
	
	// Stack size
	int maxStack;
	int curStack;
	
	// List of all constants
	final ArrayList<Double> constants;
	
	// Table of all variables (contains names and the corresponding indices)
	final HashMap<String, Integer> vars;

	/**
	 * Constructor
	 */
	public ParserTree() {
		constants = new ArrayList<Double>();
		vars = new HashMap<String, Integer>();
	}

	/**
	 * Sets the root node
	 */
	public void setRoot(TreeNode root) {
		this.root = root;
	}

	/**
	 * Sets variables
	 */
	public void setVars(String[] vars) throws Exception {
		if (vars == null)
			return;
		
		// Do not set variables if there are already other variables
		// in order to avoid incorrect indices for variables.
		if (this.vars.size() != 0)
			throw new Exception("Cannot set variables: vars.size() > 0");

		for (int i = 0; i < vars.length; i++)
			this.vars.put(vars[i], new Integer(i));
	}

	/**
	 * Optimizes the tree
	 */
	public void optimize() throws Exception {
		root = root.optimize();
	}

	/**
	 * Compiles the tree into a program
	 */
	public ArrayList<Integer> compile() throws Exception {
		if (root == null)
			throw new Exception("Error: root is null");

		optimize();
		constants.clear();
		maxStack = 0;
		curStack = 0;

		ArrayList<Integer> program = new ArrayList<Integer>();
		root.processNode(program);

		return program;
	}

	void changeStack(int size) {
		curStack += size;
		if (curStack > maxStack)
			maxStack = curStack;
	}

	/**
	 * Adds a constant and returns its index
	 */
	int addConstant(double constant) {
		Double val = new Double(constant);

		int n = constants.indexOf(val);
		if (n < 0) {
			n = constants.size();
			constants.add(val);
		}

		return n;
	}

	/**
	 * Returns the index of the given variable.
	 * Returns -1 if the variable is not defined.
	 */
	int getVar(String name) {
		if (vars.containsKey(name))
			return vars.get(name);

		return -1;
	}
	
	/**
	 * Adds a variable and returns its index.
	 * If the variable with the given name is already defined,
	 * then its index will be returned
	 */
	int addVar(String name) {
		int index = getVar(name);
		if (index < 0) {
			index = vars.size();
			vars.put(name, index);
		}
		
		return index;
	}

	@Override
	public String toString() {
		return root.toString();
	}
}