package main.annotation;

import org.w3c.dom.Document;
import org.w3c.dom.Node;



/**
 * DataLayer annotation
 * @author Monad
 */
class DataLayerAnnotation extends VariableAnnotation {
	
	protected DataLayerAnnotation() {
		annotationId = "datalayer";
		
		items.put("min", new DoubleElement("val1"));
		items.put("max", new DoubleElement("val2"));
		items.put("color", new ColorElement("color2"));
		items.put("$name", new StringElement("name"));
		// TODO: do we need explicit color1 as well?
	}

	@Override
	public String toString() {
		if (variable == null)
			return "";

		items.get("$name").value = variable.id.name;
		
		String str = "<datalayer color1 = \"0;0;0\" ";
		
		str += super.toString();
		str += "/>";
		
		return str;
	}
	
	
	@Override
	public Node toNode(Document doc) {
		if (variable == null)
			return null;

		items.get("$name").value = variable.id.name;
		
		return super.toNode(doc, "datalayer");
	}
}

