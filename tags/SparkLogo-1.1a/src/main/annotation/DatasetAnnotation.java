package main.annotation;

import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * Dataset annotation
 * @author Monad
 */
class DatasetAnnotation extends VariableAnnotation {
	
	protected DatasetAnnotation() {
		annotationId = "dataset";
		
		items.put("$variable", new StringElement("variable"));
		items.put("name", new StringElement("name"));
	}

	@Override
	public String toString() {
		if (variable != null) {
			if (items.get("name").value == null)
				items.get("name").value = variable.id.name;
			
			items.get("$variable").value = variable.id.name;
		}
		
		String str = "<item ";
		str += super.toString();
		str += "/>";
		
		return str;
	}
	
	
	@Override
	public Node toNode(Document doc) {
		if (variable != null) {
			if (items.get("name").value == null)
				items.get("name").value = variable.id.name;

			items.get("$variable").value = variable.id.name;
		}
		
		return super.toNode(doc, "item");
	}
}