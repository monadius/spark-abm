package org.sparklogo.main.annotation;

import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.sparklogo.parser.Symbol;
import org.sparklogo.parser.sym;


/**
 * Describes an annotation element
 *
 * @author Monad
 */
abstract class AnnotationElement {
    public String name;
    public Object value;
    public Object defaultValue;
    /* True means that no default value will be assigned */
    public boolean optional;

    /**
     * Parses an annotation element value
     *
     * @param s
     * @throws Exception
     */
    public abstract void parseValue(Symbol s) throws Exception;

    /**
     * Returns a default value of the element
     *
     * @return
     */
//	public abstract Object getDefaultValue();
    @Override
    public String toString() {
        if (optional && value == null)
            return "";

        Object value = this.value;

        if (value == null)
            value = defaultValue;//getDefaultValue();

        String str = name + " = \"";
        if (value != null) {
            str += value.toString();
        }
        str += "\"";

        return str;
    }


    /**
     * Creates an xml attribute
     *
     * @param doc
     * @return null if optional without value
     */
    public final Node toAttribute(Document doc) {
        if (optional && value == null)
            return null;

        Object value = this.value;

        if (value == null)
            value = defaultValue; //getDefaultValue();

        if (value == null)
            value = "";

        Node attr = doc.createAttribute(name);
        attr.setNodeValue(value.toString());

        return attr;
    }
}


/**
 * Integer-valued element
 */
class IntegerElement extends AnnotationElement {
    public IntegerElement(String name) {
        this.name = name;
        this.defaultValue = Integer.valueOf(0);
        this.value = null;
    }

    public void parseValue(Symbol s) throws Exception {
        if (s.id != sym.DOUBLE)
            throw new Exception("A number is expected: " + s);

        this.value = (int) ((Double) s.value).doubleValue();
    }
}


/**
 * Double-valued element
 */
class DoubleElement extends AnnotationElement {
    public DoubleElement(String name, double defaultValue) {
        this.name = name;
        this.defaultValue = Double.valueOf(defaultValue);
        this.value = null;
    }

    public DoubleElement(String name) {
        this(name, Double.valueOf(0));
    }

    public void parseValue(Symbol s) throws Exception {
        if (s.id != sym.DOUBLE)
            throw new Exception("A number is expected: " + s);

        this.value = (Double) s.value;
    }
}


/**
 * String-valued element
 */
class StringElement extends AnnotationElement {
    public StringElement(String name) {
        this.name = name;
        this.defaultValue = "";
    }

    public void parseValue(Symbol s) throws Exception {
        if (s.id != sym.STRING)
            throw new Exception("A string is expected: " + s);

        this.value = (String) s.value;
    }
}


/**
 * Represents a value: a number, a boolean, or a string
 */
class ValueElement extends AnnotationElement {
    public ValueElement(String name) {
        this.name = name;
        this.value = null;
        this.defaultValue = "";
    }

    public void parseValue(Symbol s) throws Exception {
        switch (s.id) {
            // string
            case sym.STRING:
                this.value = (String) s.value;
                break;

            // number
            case sym.DOUBLE:
                this.value = (Double) s.value;
                break;

            // true, false, or something else
            case sym.IDENTIFIER:
                this.value = (String) s.value;
                break;

            default:
                throw new Exception("A value is expected: " + s);
        }
    }
}


/**
 * Color-valued element
 */
class ColorElement extends StringElement {
    static HashMap<String, String> colors;

    static {
        colors = new HashMap<String, String>();
        colors.put("red", "1;0;0");
        colors.put("green", "0;1;0");
        colors.put("blue", "0;0;1");
        colors.put("white", "1;1;1");
        colors.put("black", "0;0;0");
        colors.put("cyan", "0;1;1");
        colors.put("yellow", "1;1;0");
        colors.put("magenta", "1;0;1");
        colors.put("brown", "0.8;0.4;0");
        colors.put("orange", "1;0.6;0");
        colors.put("violet", "0.5;0;0.5");
    }

    public ColorElement(String name) {
        super(name);
    }


    public void parseValue(Symbol s) throws Exception {
        super.parseValue(s);

        String color = colors.get(value);
        if (color != null) {
            value = color;
            return;
        }

        // TODO: verify that the format is correct, i.e. r;g;b
        // Or convert other formats to it
    }
}

