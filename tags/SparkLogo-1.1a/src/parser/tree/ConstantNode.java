package parser.tree;

import parser.Symbol;
import parser.sym;
import main.Constant;
import main.Id;
import main.SparkModel;
import main.type.MethodType;
import main.type.Type;
import main.type.UnknownType;
import javasrc.JavaEmitter;

/**
 * Node for a constant value
 * @author Monad
 *
 */
public class ConstantNode extends TreeNode {
	protected Constant constant;
	
	
	/**
	 * Creates a constant from a description symbol
	 * @param s
	 * @throws Exception
	 */
	public ConstantNode(Symbol s) throws Exception {
		super(s);
		Type type = null;
		// TODO: other types, vector
		
		if (s.id == sym.DOUBLE) {
			type = SparkModel.getInstance().getType(new Id("double"));
		}
		else if (s.id == sym.STRING) {
			type = SparkModel.getInstance().getType(new Id("string"));
			s.value = "\"" + s.value + "\"";
		}
		
		if (type == null)
			throw new Exception("Type of constant is not defined: " + s);
		
		constant = new Constant(type, s.value);
	}
	
	
	/**
	 * Creates a vector constant
	 * @param xyz
	 */
	public ConstantNode(double[] xyz) {
		super(null);
		// TODO: improve
		String str = "new Vector(";
		for (int i = 0; i < xyz.length; i++) {
			str += Double.toString(xyz[i]);
			if (i != xyz.length - 1)
				str += ", ";
		}
		
		str += ")";
		
		constant = new Constant(SparkModel.getInstance().getType(new Id("vector")), str);
	}
	
	
	@Override
	public Type getType() throws Exception {
		return constant.getType();
	}
	
	
	@Override
	public Type resolveType(Type expectedType, int flag) throws Exception {
		Type myType = constant.getType();

		if (expectedType instanceof MethodType) {
			expectedType = currentBlock().getMethod().getReturnType();
			if (expectedType == null)
				throw new Exception("Method " + currentBlock().getMethod() + " does not have return type: " + symbol);
		}
		
		if (expectedType != null && !(expectedType instanceof UnknownType)) {
			if (!myType.instanceOf(expectedType))
				throw new Exception("Types mismatch: myType: " + myType.getId() + "; expected type: " + expectedType.getId() + symbol);
		}
		
		return myType;
	}
	
	
	@Override
	public void translate(JavaEmitter java, int flag) throws Exception {
		constant.translate(java);
	}

}
