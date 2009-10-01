package main.type;

import main.Id;


/**
 * Represents a type which need to be identified later
 * @author Monad
 *
 */
public class UnknownType extends Type {
	public UnknownType() {
		// TODO: do we need id? Or it can be null (as before)
		super(new Id("$unknown"));
	}
	
	
	@Override
	public Type resolveDeclarationTypes() {
		return this;
	}
}
