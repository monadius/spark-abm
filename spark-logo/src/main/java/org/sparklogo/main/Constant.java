package org.sparklogo.main;

import org.sparklogo.javasrc.JavaEmitter;
import org.sparklogo.main.type.Type;

/**
 * Represents a constant value
 *
 * @author Monad
 */
public class Constant {
    protected Type type;
    protected Object value;


    /**
     * Creates a constant
     *
     * @param type
     * @param value
     */
    public Constant(Type type, Object value) {
        this.type = type;
        this.value = value;
    }


    /**
     * Returns type
     *
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
