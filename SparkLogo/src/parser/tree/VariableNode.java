package parser.tree;

import parser.Symbol;
import javasrc.JavaEmitter;
import main.AdhocImplementation;
import main.Id;
import main.SparkModel;
import main.Variable;
import main.type.MethodType;
import main.type.NameType;
import main.type.Type;
import main.type.UnknownType;



public class VariableNode extends TreeNode {
	protected Variable var;
	
	public VariableNode(Symbol symbol, Variable var) {
		super(symbol);
		this.var = var;
	}

	
	@Override
	public void debugPrint(java.io.PrintStream out) throws Exception {
		out.println("<variable name = \"" + var.id.name + "\" type = \"" + var.type.getId().name + "\" />");
	}

	
	@Override
	public Type getType() throws Exception {
		return var.type;
	}
	
	
	@Override
	public Type resolveType(Type expectedType, int flag) throws Exception {
		Type myType = var.type;
		
		// FIXME: remove this as soon as possible
		// make natural implicit type conversion
		AdhocImplementation.flag = false;
		if (myType.getId() != null) {
			Type doubleType = SparkModel.getInstance().getType(new Id("double"));
			
			// Always use double type for GET_VALUE
			if ((flag & GET_VALUE) != 0) {
				if (myType.getId().name.equals("$integer") ||
					myType.getId().name.equals("$long") ||
					myType.getId().name.equals("$time"))
				{
					myType = doubleType;
					AdhocImplementation.flag = true;
				}
			}
			
			if (doubleType.instanceOf(expectedType))
				if (myType.getId().name.equals("$integer") ||
					myType.getId().name.equals("$long") ||
					myType.getId().name.equals("$time")) {
				
					myType = doubleType;
					AdhocImplementation.flag = true;
				}
	/*		else if (myType.getId().name.equals("grid")) {
				if (expectedType == null || expectedType instanceof UnknownType
						|| expectedType.getId().name.equals("double"))
					if ((flag & DOTLVALUE) == 0) {
						DotNode dotNode = new DotNode();
						dotNode.addNode(this);
						Type gridType = SparkModel.getInstance().getType(new Id("grid"));
						Command getCommand = gridType.getCommand("value-here");
						Command setCommand = gridType.getCommand("set-value-here");
						
						if ((flag & LVALUE) == 0) {
							dotNode.addNode(new CommandNode(getCommand));
						}
						else {
							dotNode.addNode(new CommandNode(setCommand));
						}
						
						
						
						myType = SparkModel.getInstance().getType(new Id("double"));
						AdhocImplementation.flag = true;
					}
			}
	*/
		}
		
		if (expectedType instanceof MethodType) {
			expectedType = currentBlock().getMethod().getReturnType();
			if (expectedType == null)
				throw new Exception("Method " + currentBlock().getMethod() + " does not have return type: " + symbol);
		}
		
		if (expectedType != null 
			&& !(expectedType instanceof UnknownType)
			&& !(expectedType instanceof NameType)) {
			// TODO: types should be unique
			if (myType instanceof UnknownType) {
				myType = expectedType;
			}
			
			if (!myType.instanceOf(expectedType))
				throw new Exception("Types mismatch for variable " + var.id + 
						" with type " + var.type.getId() + " and type " + expectedType.getId() + ": " + symbol);
		}

		if (!AdhocImplementation.flag)
			var.type = myType;
		
		return myType;
	}
	
	
	@Override
	public void translate(JavaEmitter java, int flag) throws Exception {
		// TODO: better implementation
		
//		java.print(var.id.toJavaName());
		java.addBuffer("@@self");
		java.setActiveBuffer("@@self");
		java.print(currentBlock().getSelfVariable().id.toJavaName());
		java.endBuffer();

		java.addBuffer("@@myself");
		java.setActiveBuffer("@@myself");
		java.print(currentBlock().getMyselfVariable().id.toJavaName());
		java.endBuffer();

		java.printText(var.getTranslationString(flag));
		
		java.clearBuffers();
	}

}
