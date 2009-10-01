package main.annotation;

import main.Id;
import main.SparkModel;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A special annotation used for creating a description of external variables.
 * @author Monad
 *
 */
public class ExternalVariableAnnotation extends VariableAnnotation {
	public ExternalVariableAnnotation() {
		annotationId = "ExternalVariable";
		
		items.put("$get", new StringElement("get"));
		items.put("$set", new StringElement("set"));
		items.put("$name", new StringElement("name"));
		items.put("$type", new StringElement("type"));
	}

	
	private boolean setDefaultValues() {
		try {
			if (variable != null) {
				if (variable.type == SparkModel.getInstance().getType(new Id("double"))) {
					items.get("$type").value = "Double";
				}
				else if (variable.type == SparkModel.getInstance().getType(new Id("boolean"))) {
					items.get("$type").value = "Boolean";
				}
				else {
					// Unsupported variable type
					return false;
				}
				
				if (items.get("$name").value == null)
					items.get("$name").value = variable.id.name;
			}
		}
		catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	
	@Override
	public String toString() {
		if (setDefaultValues()) {
			String str = "<variable ";
			str += super.toString();
			str += "/>";
		
			return str;
		}
		else {
			return "";
		}
	}
	
	
	@Override
	public Node toNode(Document doc) {
		if (setDefaultValues()) {
			return super.toNode(doc, "variable");
		}
		else {
			return null;
		}
	}

}
