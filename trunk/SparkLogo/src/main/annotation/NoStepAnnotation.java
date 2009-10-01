package main.annotation;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Agents with this annotation are not processed
 * by 'step' method
 * @author Monad
 */
class NoStepAnnotation extends AgentAnnotation {

	protected NoStepAnnotation() {
		annotationId = "nostep";
	}
	
	
	@Override
	public String toString() {
		return "nostep";
	}
	
	
	@Override
	public Node toNode(Document doc) {
		Node node = super.toNode(doc, "nostep");
		return node;
	}

}
