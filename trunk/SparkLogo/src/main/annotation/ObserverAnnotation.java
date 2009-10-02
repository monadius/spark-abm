package main.annotation;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Defines the observer for the model
 * @author Monad
 *
 */
public class ObserverAnnotation extends ModelAnnotation {

	protected ObserverAnnotation() {
		annotationId = "observer";
		
		items.put("observer", new StringElement("observer"));
		items.put("mode", new StringElement("mode"));

		items.get("observer").optional = true;
		items.get("mode").optional = true;
	}

	@Override
	public String toString() {
		String str = "<setup ";
		
		str += super.toString();
		str += "/>";
		
		return str;
	}
	
	
	@Override
	public Node toNode(Document doc) {
		Node node = super.toNode(doc, "setup");
		return node;
	}
}