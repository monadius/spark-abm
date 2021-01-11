package parser.tree;

import javasrc.JavaEmitter;
import main.Id;
import main.SparkModel;
import main.Variable;
import main.type.Type;

/**
 * For control structure node
 * @author Monad
 */
public class ForNode extends CommandNode {
	protected Variable counter;

	/**
	 * Creates a new for node with the given counter variable
	 * @param counter
	 */
	public ForNode(Variable counter) {
		super(null, SparkModel.getInstance().getCommand("for"));
		this.counter = counter;
		counter.doNotDeclare = true;
	}
	
	@Override
	public Type getType() throws Exception {
		return null;
	}

	@Override
	public Type resolveType(Type expectedType, int flag) throws Exception {
		Type d = SparkModel.getInstance().getType(new Id("double"));
		// From should be double
		getNode(0).resolveType(d, flag);
		// To should be double
		getNode(1).resolveType(d, flag);
		// Step should be double
		getNode(2).resolveType(d, flag);
		// Block is already resolved
		
		// No type
		return null;
	}

	@Override
	public void translate(JavaEmitter java, int flag) throws Exception {
		java.addBuffer("@@counter");
		java.setActiveBuffer("@@counter");
		java.print(counter.id.toJavaName());
		java.endBuffer();
		
		super.translate(java, flag);
		
		java.clearBuffers();
	}

}
