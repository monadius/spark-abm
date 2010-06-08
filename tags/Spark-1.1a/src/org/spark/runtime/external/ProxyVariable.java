package org.spark.runtime.external;

import org.spark.runtime.AbstractChangeEvent;
import org.spark.runtime.data.DataRow;
import org.spark.runtime.external.data.IDataConsumer;
import org.spark.utils.XmlDocUtils;
import org.w3c.dom.Node;

//import com.spinn3r.log5j.Logger;


/**
 * Represents an interface to a model variable
 * @author Monad
 */
public class ProxyVariable extends AbstractChangeEvent implements IDataConsumer {
//	private static final Logger logger = Logger.getLogger();	

	/* Table of all created variables */
//	private final static HashMap<String, ModelVariable> variables;
	
/*	static
	{
		variables = new HashMap<String, ModelVariable>();
	}
*/	
	/* Variable's name */
	private String name;
	
	/* Variable's type name */
	private String typeName;
	
	/* Variable's value. Could be null */
	private Object value;
	
	
	
	/**
	 * Returns variable's name
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Returns variable's type name
	 * @return
	 */
	public String getTypeName() {
		return typeName;
	}
	

	/**
	 * Private constructor ensures that variables can be created
	 * only by createVariable method.
	 */
	private ProxyVariable(String name, String typeName) {
		this.name = name;
		this.typeName = typeName.toLowerCase().intern();
		this.value = null;
	}
	
	

	
	/**
	 * Creates a new model variable from the given xml-node.
	 * If a variable with the given name exists then an exception
	 * will be raised.
	 * @param node
	 * @return
	 */
	static ProxyVariable loadVariable(Node node) {
		// Read node's attributes
		String name = XmlDocUtils.getValue(node, "name", "???");
		String typeName = XmlDocUtils.getValue(node, "type", "double");

		// Create a new variable and add it to the table
		ProxyVariable var = new ProxyVariable(name, typeName);
		
		return var;
	}
	

	
	/**
	 * Returns variable's value
	 * @return
	 */
	public synchronized Object getValue() {
		return value;
	}
	
	
	/**
	 * Sets variable's value
	 * @param value
	 */
	public void setValue(Object value) throws Exception {
		if (value == null) {
			synchronized (this) {
				if (this.value == null)
					return;

				this.value = value;
				Coordinator.getInstance().changeVariable(name, value);
			}
			
			fireStateChanged();
			return;
		}

		String type = value.getClass().getSimpleName().toLowerCase().intern();
		if (type != typeName)
			throw new Exception("Wrong type for variable " + name);

		synchronized (this) {
			if (value.equals(this.value))
				return;

			this.value = value;
			Coordinator.getInstance().changeVariable(name, value);
		}

		fireStateChanged();
	}
	
	
	/**
	 * Parses string and sets variable's value
	 * @param svalue
	 * @throws Exception
	 */
	public void setValue(String svalue) throws Exception {
		if (typeName == "boolean") {
			if (svalue.equals("true"))
				setValue(Boolean.TRUE);
			else
				setValue(Boolean.FALSE);
			
			return;
		}
		
		if (typeName == "integer") {
			setValue(Integer.parseInt(svalue));
			return;
		}
		
		if (typeName == "double") {
			if (svalue == null || svalue.equals("null"))
				setValue((Object)null);
			else
				setValue(Double.parseDouble(svalue));
			return;
		}
	}
	
	
	/**
	 * Synchronizes variable's value with the SPARK value
	 */
	public synchronized void consume(DataRow row) {
		Object newValue = null;
		
		if (typeName == "double")
			newValue = row.getVarDoubleValue(name);
		else if (typeName == "boolean")
			newValue = row.getVarBooleanValue(name);
		else if (typeName == "integer")
			newValue = row.getVarIntegerValue(name);
		
		if (newValue == null) {
			if (value != null) {
				value = newValue;
				fireStateChanged();
			}
		}
		else if (!newValue.equals(value)) {
			value = newValue;
			fireStateChanged();
		}
	}
	
}
