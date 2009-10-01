package main.annotation;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Chart annotation
 * @author Monad
 */
class ChartAnnotation extends VariableAnnotation {

	protected ChartAnnotation() {
		annotationId = "chart";
		
		items.put("$get", new StringElement("method"));
		items.put("interval", new IntegerElement("interval"));
		items.put("name", new StringElement("name"));
		
	}

	@Override
	public String toString() {
		if (variable != null) {
			if (items.get("name").value == null)
				items.get("name").value = variable.id.name;
		}

		String str = "<chart width = \"300\" height = \"200\" x = \"" + 0 + "\" y = \"" + 0 + "\" ";
		
		str += super.toString();
		str += "/>";
		
		return str;
	}
	
	
	@Override
	public Node toNode(Document doc) {
		Node node = super.toNode(doc, "chart");
		return node;
	}
}
