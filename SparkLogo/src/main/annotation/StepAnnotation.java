package main.annotation;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Step annotation
 * @author Monad
 */
class StepAnnotation extends AgentAnnotation {

	protected StepAnnotation() {
		annotationId = "step";
		
		items.put("priority", new IntegerElement("priority"));
		items.put("time", new StringElement("time"));

		items.get("priority").optional = true;
		items.get("time").optional = true;
	}

	@Override
	public String toString() {
		String str = "<agent ";
		
		str += super.toString();
		str += "/>";
		
		return str;
	}
	
	
	@Override
	public Node toNode(Document doc) {
		Node node = super.toNode(doc, "agent");
		return node;
	}
}

