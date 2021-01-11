package main.type;

import main.Id;


/**
 * ThisType is the type of the current class
 * @author Monad
 */
public class ThisType extends Type {
	private static ThisType instance = new ThisType();
	
	
	public static ThisType getInstance() {
		return instance;
	}
	
	
	public ThisType() {
		super(new Id("$this"));
	}
}