package org.sparklogo.main.type;

import org.sparklogo.main.Id;


/**
 * Self type is the type of the current object
 *
 * @author Monad
 */
public class SelfType extends Type {
    private static SelfType instance = new SelfType();


    public static SelfType getInstance() {
        return instance;
    }


    public SelfType() {
        super(new Id("self"));
    }
}
