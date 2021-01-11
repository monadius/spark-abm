package main.type;

import main.Id;

/**
 * Refers to the return type of a method
 * @author Monad
 *
 */
public class MethodType extends Type {
	private static MethodType instance = new MethodType();
	
	/**
	 * Returns the instance of this type
	 */
	public static MethodType getInstance() {
		return instance;
	}
	
	/**
	 * Creates an instance
	 */
	private MethodType() {
		super(new Id("MethodType"));
	}

}
