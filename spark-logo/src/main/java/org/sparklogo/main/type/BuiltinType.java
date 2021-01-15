package org.sparklogo.main.type;

import org.sparklogo.main.Id;

/**
 * Represents a predefined Spark type
 *
 * @author Monad
 */
public class BuiltinType extends Type {
    /* Indicates whether the underlying type is an interface */
    protected boolean isInterface;

    /**
     * Creates a predefined Spark type
     *
     * @param id
     * @param isInterface
     */
    public BuiltinType(Id id, Type parentType, boolean isInterface) {
        // A built-in time is never partial (extendable)
        super(id, parentType, false);
        this.isInterface = isInterface;
    }


    /**
     * Returns true if the underlying type is an interface
     *
     * @return
     */
    public boolean isInterface() {
        return isInterface;
    }


    @Override
    public Type resolveDeclarationTypes() throws Exception {
        super.resolveDeclarationTypes();
        // Nothing to change here
        return this;
    }
}
