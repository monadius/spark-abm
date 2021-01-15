package org.sparklogo.main.annotation;

import org.sparklogo.main.Id;
import org.sparklogo.main.SparkModel;

import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * Parameter annotation
 *
 * @author Monad
 */
class ParameterAnnotation extends VariableAnnotation {
    protected ParameterAnnotation() {
        super(PARAMETER_ANNOTATION);

        items.put("default", new ValueElement("default"));
        items.put("min", new DoubleElement("min", 0.0));
        items.put("max", new DoubleElement("max", 10.0));
        items.put("step", new DoubleElement("step", 0.1));
        items.put("name", new StringElement("name"));
        items.put("values", new StringElement("values"));
        items.put("$type", new StringElement("type"));
        items.put("$variable", new StringElement("variable"));

        items.get("default").optional = true;
        items.get("values").optional = true;

        // Set up the default type
        items.get("$type").value = "Double";
    }


    private boolean setDefaultValues() {
        try {
            if (variable != null) {
                items.get("$variable").value = variable.id.name;

                if (variable.type == SparkModel.getInstance().getType(new Id("double"))) {
                    items.get("$type").value = "Double";
                } else if (variable.type == SparkModel.getInstance().getType(new Id("boolean"))) {
                    items.get("$type").value = "Boolean";
                } else {
                    // Unsupported variable type
                    return false;
                }


                if (items.get("name").value == null)
                    items.get("name").value = variable.id.name;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        if (setDefaultValues()) {
            String str = "<parameter ";
            str += super.toString();
            str += "/>";

            return str;
        } else {
            return "";
        }
    }


    @Override
    public Node toNode(Document doc) {
        if (setDefaultValues()) {
            return super.toNode(doc, "parameter");
        } else {
            return null;
        }
    }
}


