package main.type;

import java.util.ArrayList;

import main.Id;
import main.Variable;

/**
 * Represents a type derived from an argument
 * @author Monad
 */
public class ArgumentType extends Type {
	/* Name of the argument from which the type should be derived */
	protected String argumentName;
	/* True tells to look into subtype of a composite type */
	protected boolean subtypeFlag;
	
	
	/**
	 * Returns an argument id
	 * @return
	 */
	public Id getArgumentId() {
		return new Id(argumentName);
	}
	
	
	/**
	 * Creates a type which will be derived from the specific named argument
	 * @param argumentName
	 */
	public ArgumentType(String argumentName) {
		super(new Id("ArgumentType"));
		
		this.argumentName = argumentName;
	}
	
	
	/**
	 * Creates a type which will be derived from the specific named argument
	 * that may have a composite type
	 * @param argumentName
	 * @param subtypeFlag
	 */
	public ArgumentType(String argumentName, boolean subtypeFlag) {
		this(argumentName);
		this.subtypeFlag = subtypeFlag;
	}

	
	/**
	 * Derives a type from the given arguments and resolved types
	 * @param arguments
	 * @param types
	 * @return
	 */
	public Type deriveType(ArrayList<Variable> arguments, Type[] types) throws Exception {
		if (arguments.size() != types.length)
			throw new Exception("Inconsistent arguments");
		
		for (int i = 0; i < arguments.size(); i++) {
			Variable var = arguments.get(i);
			
			if (var.id.name.equals(argumentName)) {
				if (subtypeFlag) {
					// Look at a sub-type 
					if (!(types[i] instanceof CompositeType))
						throw new Exception("A composite type is expected");
					
					CompositeType t = (CompositeType) types[i];
					if (t.subtype == null)
						throw new Exception("A subtype is not defined for the composite type " + t);
					
					return t.subtype;
				}
				
				// Return type
				return types[i];
			}
		}
		
		throw new Exception("Argument " + argumentName + " is not found");
	}
}
