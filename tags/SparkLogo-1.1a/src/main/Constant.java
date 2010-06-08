package main;

import javasrc.JavaEmitter;
import main.type.Type;

/**
 * Represents a constant value
 * @author Monad
 *
 */
public class Constant {
	protected Type type;
	protected Object value;
	
	
	/**
	 * Creates a constant
	 * @param type
	 * @param value
	 */
	public Constant(Type type, Object value) {
		this.type = type;
		this.value = value;
	}
	
	
	/**
	 * Returns type
	 * @return
	 */
	public Type getType() {
		return type;
	}
	
	
	/**
	 * Translates to Java
	 */
	public void translate(JavaEmitter java) {
		java.print(value.toString());
	}
}
