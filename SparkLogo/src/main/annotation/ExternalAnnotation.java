package main.annotation;

import main.type.Method;
import main.type.ModelType;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Methods declared with an external annotation
 * will be available in the user interface.
 * @author Monad
 */
class ExternalAnnotation extends MethodAnnotation {

	protected ExternalAnnotation() {
		annotationId = "external";
		
		items.put("$method", new StringElement("method"));
		items.put("name", new StringElement("name"));
	}
	
	
	private void setDefaultValues() {
		if (method != null) {
			if (items.get("name").value == null)
				items.get("name").value = method.getId().name;
			
			if (method.getArgumentsNumber() > 0)
				throw new Error("@external annotation can be applied only to methods without arguments: " + method);
			
			if (method.getId().name.equals("setup"))
				throw new Error("@external annotation cannot be applied to the 'setup' method");
			
			items.get("$method").value = method.getId().toJavaName();
		}
	}
	
	
	@Override
	public void associateMethod(Method method) throws Exception {
		if (!(method.getParentType() instanceof ModelType))
			throw new Exception("Annotation " + annotationId + " cannot be applied to a non-model method " + method);

		super.associateMethod(method);
	}
	

	@Override
	public String toString() {
		setDefaultValues();

		String str = "<method ";
		
		str += super.toString();
		str += "/>";
		
		return str;
	}
	
	
	@Override
	public Node toNode(Document doc) {
		setDefaultValues();
		Node node = super.toNode(doc, "method");
		return node;
	}

}
