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
		
		items.put("interval", new IntegerElement("interval"));
		items.put("$variable", new StringElement("variable"));
		items.put("name", new StringElement("name"));
		items.put("label", new StringElement("label"));
	}
	
	
	private boolean setDefaultValues() {
		try {
			if (variable != null) {
				items.get("$variable").value = variable.id.name; 

				if (items.get("name").value == null)
					items.get("name").value = variable.id.name;
				
				if (items.get("label").value == null)
					items.get("label").value = variable.id.name;
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

			String str = "<chart ";
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
			Node node = super.toNode(doc, "chart");
			return node;
		}
		else {
			return null;
		}
	}
}
