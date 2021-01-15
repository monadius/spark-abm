package org.sparklogo.main.type;

import org.sparklogo.main.Id;
import org.sparklogo.main.SparkModel;

/**
 * Class for unresolved type: contains only type name
 *
 * @author Monad
 */
public class UnresolvedType extends Type {
    // Contains the id of the subtype
    private Id subtype;

    public UnresolvedType(Id id) {
        super(id);
    }

    public UnresolvedType(Id id, Id subtype) {
        super(id);
        this.subtype = subtype;
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
        Type type = SparkModel.getInstance().getType(id);
        if (type instanceof CompositeType) {
            Type subtype = SparkModel.getInstance().getType(this.subtype);
            if (subtype != null)
                type = new CompositeType((CompositeType) type, subtype);
        }

        return type;
    }
}
