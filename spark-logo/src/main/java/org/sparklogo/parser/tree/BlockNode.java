package org.sparklogo.parser.tree;

import java.util.*;

import org.sparklogo.parser.Symbol;

import org.sparklogo.javasrc.JavaEmitter;

import org.sparklogo.main.CodeBlock;
import org.sparklogo.main.Variable;
import org.sparklogo.main.type.BlockType;
import org.sparklogo.main.type.Type;


public class BlockNode extends TreeNode {
	protected CodeBlock	code;
	
	
	public CodeBlock getCodeBlock() {
		return code;
	}
	
	
	public BlockNode(Symbol symbol, CodeBlock parent) {
		super(symbol);
		code = new CodeBlock(parent);
	}
	
	public void addCommand(CommandNode node) throws Exception {
		code.addCommand(node);
	}
	
	
	public void addLocalVariable(Variable var) {
		code.addLocalVariable(var);
	}
	
	
	public void debugPrint(java.io.PrintStream out) throws Exception {
		out.println("<block>");
		ArrayList<CommandNode> code = this.code.getCommands();
		for (int i = 0; i < code.size(); i++) {
			code.get(i).debugPrint(out);
		}
		out.println("</block>");
	}


	@Override
	public void translate(JavaEmitter java, int flag) throws Exception {
		pushCodeBlock(code);
		// TODO: flag = 0? Otherwise it leads to errors with GET_VALUE
		code.translate(java, 0);
		popCodeBlock();
	}
	
	
	@Override
	public Type getType() throws Exception {
		return BlockType.getInstance();
	}
	
	
	@Override
	public Type resolveType(Type expectedType, int flag) throws Exception {
		return BlockType.getInstance();
	}
}
