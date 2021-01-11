package org.sparklogo.parser.tree;

import org.sparklogo.parser.Symbol;
import org.sparklogo.javasrc.JavaEmitter;
import org.sparklogo.main.type.Type;

public class DotNode extends CommandNode {

	public DotNode(Symbol symbol) {
		super(symbol, null);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Type getType() throws Exception {
		if (children.size() < 2)
			return null;
		
		return getNode(1).getType();
	}

	@Override
	public Type resolveType(Type expectedType, int flag) throws Exception {
		return getNode(1).resolveType(expectedType, flag);
	}

	@Override
	public void translate(JavaEmitter java, int flag) throws Exception {
		// TODO: elaborate
		// TODO: hierarchical buffers or global variable (better)
		 java.pushGlobalBuffer("@@object");
		// java.popGlobalBuffer("@@object");
//		java.addBuffer("@@object");
//		java.setActiveBuffer("@@object");
		getNode(0).translate(java, (flag & ~LVALUE) | GET_VALUE);
//		java.endBuffer();
		java.endBuffer();
//		java.print(".");
		getNode(1).translate(java, flag);
		java.popGlobalBuffer("@@object");
	}

}
