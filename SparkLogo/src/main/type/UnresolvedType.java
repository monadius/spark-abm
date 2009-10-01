package main.type;

import main.Id;
import main.SparkModel;

/**
 * Class for unresolved type: contains only type name
 * @author Monad
 *
 */
public class UnresolvedType extends Type {
	public UnresolvedType(Id id) {
		super(id);
	}
	
	
	@Override
	public Type resolveDeclarationTypes() throws Exception {
		resolved = true;
		
		// Resolve argument type
		if (id.name.startsWith("ArgumentType")) {
			String[] args = id.name.split(":");
			if (args.length < 2)
				throw new Exception("Cannot resolve ArgumentType: argument name is not given");
			
			String argumentName = args[1];
			boolean subtypeFlag = false;

			if (args.length > 2) {
				if (args[2].equals("subtype"))
					subtypeFlag = true;
			}
			
			return new ArgumentType(argumentName, subtypeFlag);
		}
		
		// Returns a type by its name
		return SparkModel.getInstance().getType(id);
	}
}
