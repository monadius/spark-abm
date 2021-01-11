package parser.tree;

import parser.Symbol;
import javasrc.JavaEmitter;
import main.Command;
import main.SparkModel;
import main.type.Type;
import main.type.UnknownType;

public class AssignmentNode extends CommandNode {
	public AssignmentNode(Symbol symbol) {
		super(symbol, SparkModel.getInstance().getCommand("="));
	}
	
	
	public AssignmentNode(Symbol symbol, Command cmd) {
		super(symbol, cmd);
	}
	
	
	@Override
	public Type getType() throws Exception {
		return getNode(0).getType();
	}
	
	
	@Override
	public Type resolveType(Type expectedType, int flag) throws Exception {
		if (this.cmd != SparkModel.getInstance().getCommand("=")) {
			return super.resolveType(expectedType, flag);
		}
		
		if (expectedType != null)
			throw new Exception("Assignment node does not return any value: " + symbol);
		
		Type leftType = getNode(0).resolveType(null, flag | LVALUE);
		Type rightType = getNode(1).resolveType(leftType, flag | GET_VALUE);
		if (rightType == null || rightType instanceof UnknownType) {
			throw new Exception("Cannot assign unknown type: " + symbol);
		}
		
		// TODO: is it correct?
		if (leftType != null && !(leftType instanceof UnknownType)) {
			if (!rightType.instanceOf(leftType))
				getNode(0).resolveType(rightType, flag | LVALUE);
		}
		else {
			getNode(0).resolveType(rightType, flag | LVALUE);
		}
		return null;
	}
	
	
	@Override
	public void translate(JavaEmitter java, int flag) throws Exception {
		if (this.cmd != SparkModel.getInstance().getCommand("=")) {
			super.translate(java, flag);
			return;
		}
		
		// TODO: implement in the correct way
		java.addBuffer("@@lhs");
		java.addBuffer("@@value");
		
		java.setActiveBuffer("@@lhs");
		getNode(0).translate(java, flag | LVALUE);
		java.endBuffer();

//		java.print(" = ");
		
		java.setActiveBuffer("@@value");
		getNode(1).translate(java, flag | GET_VALUE);
		java.endBuffer();
		
		String translation = java.getBufferText("@@lhs");
		java.printText(translation);
		
		java.clearBuffers();
	}
}
