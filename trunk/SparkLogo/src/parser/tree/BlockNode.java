package parser.tree;

import java.util.*;

import parser.Symbol;

import javasrc.JavaEmitter;

import main.CodeBlock;
import main.Variable;
import main.type.BlockType;
import main.type.Type;


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
