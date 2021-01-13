package org.spark.runtime.external;

import java.util.HashMap;

import org.sparkabm.runtime.data.DataRow;
import org.spark.runtime.external.data.IDataConsumer;
import org.sparkabm.utils.AbstractChangeEvent;
import org.sparkabm.utils.XmlDocUtils;
import org.w3c.dom.Node;

//import com.spinn3r.log5j.Logger;


/**
 * Represents an interface to a model variable
 * @author Monad
 */
public class ProxyVariable extends AbstractChangeEvent implements IDataConsumer {
	// Possible types
	public static final int DOUBLE_TYPE = 1;
	public static final int BOOL_TYPE = 2;
	
	// Contains type names and the corresponding types
	private final static HashMap<String, Integer> types;
	
	/* Variable's name */
	private final String name;
	
	/* Variable's type */
	private final int type;
	
	/* Variable's type name */
	private final String typeName;
	
	/* Variable's value. Could be null */
	private Object value;
	
	
	/**
	 * Static initializer
	 */
	static {
		types = new HashMap<String, Integer>();
		types.put("double", DOUBLE_TYPE);
		types.put("boolean", BOOL_TYPE);
	}
	
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
	public int getType() {
		return type;
	}
	
	
	/**
	 * Returns the name of the type of this variable
	 * @return
	 */
	public String getTypeName() {
		return typeName;
	}
	

	/**
	 * Private constructor ensures that variables can be created
	 * only by createVariable method.
	 */
	private ProxyVariable(String name, int type, String typeName) {
		this.name = name;
		this.type = type;
		this.typeName = typeName;
		this.value = null;
	}
	
	

	
	/**
	 * Creates a new model variable from the given xml-node.
	 * If a variable with the given name exists then an exception
	 * will be raised.
	 * @param node
	 * @return
	 */
	static ProxyVariable loadVariable(Node node) throws Exception {
		// Read node's attributes
		String name = XmlDocUtils.getValue(node, "name", "???");
		String typeName = XmlDocUtils.getValue(node, "type", "double").toLowerCase();
		
		Integer type = types.get(typeName);
		if (type == null)
			throw new Exception("Type " + typeName + " is not supported for the variable " + name);

		// Create a new variable
		ProxyVariable var = new ProxyVariable(name, type, typeName);
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

		String typeName = value.getClass().getSimpleName().toLowerCase().intern();
		Integer type = types.get(typeName);
		if (type == null || type != this.type)
			throw new Exception("Wrong type of the variable " + name);

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
		if (type == BOOL_TYPE) {
			if ("true".equals(svalue))
				setValue(Boolean.TRUE);
			else
				setValue(Boolean.FALSE);
			
			return;
		}
		
		if (type == DOUBLE_TYPE) {
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
		
		switch (type) {
		// double
		case DOUBLE_TYPE:
			newValue = row.getVarDoubleValue(name);
			break;
		
		// boolean
		case BOOL_TYPE:
			newValue = row.getVarBooleanValue(name);
			break;
		}
		
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
