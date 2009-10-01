package main.type;

import main.Id;


/**
 * Myself type is the type of a calling object
 * @author Monad
 */
public class MyselfType extends Type {
	private static MyselfType instance = new MyselfType();
	
	
	public static MyselfType getInstance() {
		return instance;
	}
	
	
	public MyselfType() {
		super(new Id("myself"));
	}
}
