package main.type;

import main.Id;

/**
 * An artificial type for representing block arguments
 * @author Monad
 */
public class BlockType extends Type {
	/* The reference to the 'self' type of the block */
	protected Type selfType;
	
	private static BlockType instance = new BlockType();
	
	public static BlockType getInstance() {
		return instance;
	}
	
	/**
	 * Creates a predefined Spark type
	 * @param id
	 */
	private BlockType() {
		super(new Id("Block"));
		resolved = true;
	}
	
	
	/**
	 * Creates a block node with the specific 'self' type
	 * @param selfType
	 */
	public BlockType(Type selfType) throws Exception {
		this();
		this.selfType = selfType.resolveDeclarationTypes();
	}
	
	
	/**
	 * Returns a 'self' type
	 * @return is null if no specific 'self' type
	 */
	public Type getSelfType() {
		return selfType;
	}
	
	
	@Override
	public boolean instanceOf(Type type) {
		return type instanceof BlockType;
	}
}
