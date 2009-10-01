package main.type;

import main.Id;


/**
 * Parent type is the type of the parent of an object
 * @author Monad
 */
public class ParentType extends Type {
	private static ParentType instance = new ParentType();
	
	
	public static ParentType getInstance() {
		return instance;
	}
	
	
	public ParentType() {
		super(new Id("$parent"));
	}
}
