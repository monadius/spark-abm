package org.sparklogo.parser.tree;

import java.util.regex.Matcher;

import org.sparklogo.parser.Symbol;
import org.sparklogo.parser.sym;
import org.sparklogo.main.Constant;
import org.sparklogo.main.Id;
import org.sparklogo.main.SparkModel;
import org.sparklogo.main.type.MethodType;
import org.sparklogo.main.type.Type;
import org.sparklogo.main.type.UnknownType;
import org.sparklogo.javasrc.JavaEmitter;

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
			String str = (String) s.value;
			String replacement = Matcher.quoteReplacement("\\\"");
			str = str.replaceAll("\"", replacement);
			s.value = "\"" + str + "\"";
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
