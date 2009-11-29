package main.annotation;

import main.Id;
import main.SparkModel;

import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * Parameter annotation
 * @author Monad
 */
class ParameterAnnotation extends VariableAnnotation {
	protected ParameterAnnotation() {
		annotationId = "parameter";
		
		// TODO: what to do with other types?
		items.put("default", new DoubleElement("default"));
		items.put("min", new DoubleElement("min"));
		items.put("max", new DoubleElement("max"));
		items.put("step", new DoubleElement("step"));
		items.put("name", new StringElement("name"));
		items.put("values", new StringElement("values"));
		items.put("$type", new StringElement("type"));
		items.put("$widget", new StringElement("widget"));
		items.put("$variable", new StringElement("variable"));
		
		items.get("default").optional = true;
		items.get("values").optional = true;
		
		// TODO: think about other numerical types
		items.get("$type").value = "Double";
		items.get("$widget").value = "Slider";
	}
	
	
	private boolean setDefaultValues() {
		try {
			if (variable != null) {
				items.get("$variable").value = variable.id.name; 

				if (variable.type == SparkModel.getInstance().getType(new Id("double"))) {
					items.get("$type").value = "Double";
				}
				else if (variable.type == SparkModel.getInstance().getType(new Id("boolean"))) {
					items.get("$type").value = "Boolean";
					items.get("$widget").value = "OnOff";
				}
				else {
					// Unsupported variable type
					return false;
				}
				
				
				if (items.get("name").value == null)
					items.get("name").value = variable.id.name;
			}
			else {
				return false;
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
			String str = "<parameter ";
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
			return super.toNode(doc, "parameter");
		}
		else {
			return null;
		}
	}
}


