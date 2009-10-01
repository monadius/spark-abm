package main.type;

import main.Id;

/**
 * An artificial type for representing a name (of a type)
 * @author Monad
 */
public class NameType extends Type {
	private static NameType instance = new NameType();
	
	public static NameType getInstance() {
		return instance;
	}
	
	/**
	 * Creates a predefined Spark type
	 * @param id
	 */
	private NameType() {
		super(new Id("Name"));
		resolved = true;
	}	
}