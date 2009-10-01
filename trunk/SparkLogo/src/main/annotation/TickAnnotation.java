package main.annotation;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Defines the length of a tick
 * @author Monad
 *
 */
public class TickAnnotation extends ModelAnnotation {

	protected TickAnnotation() {
		annotationId = "tick";
		
		items.put("time", new StringElement("tick"));

		items.get("time").optional = true;
	}

	@Override
	public String toString() {
		String str = "<model ";
		
		str += super.toString();
		str += "/>";
		
		return str;
	}
	
	
	public String getTime() {
		return (String) items.get("time").value;
	}
	
	
	@Override
	public Node toNode(Document doc) {
		Node node = super.toNode(doc, "model");
		return node;
	}
}
