package org.spark.runtime.internal;

import java.lang.reflect.Method;

import org.spark.core.SparkModel;
import org.spark.runtime.AbstractChangeEvent;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

//import com.spinn3r.log5j.Logger;


/**
 * Represents a model variable for parameters and charts
 * @author Monad
 */
public class ModelVariable extends AbstractChangeEvent {
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
	
	/* Variable's type: should be consistent with set/get methods */
	private Class<?> type;
	
	/* Get method of the variable, null means that the variable is write only */
	private Method getMethod;
	
	/* Set method of the variable, null means that the variable is read only */
	private Method setMethod;
	
	/* Variable's value. Could be null */
	private Object value;
	
	/* True means that a new value was assigned to the variable
	 * and this value should be later synchronized with SPARK model.
	 */
	private boolean writeFlag;

	
	
	/**
	 * Returns variable's name
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Returns variable's type
	 * @return
	 */
	public Class<?> getType() {
		return type;
	}
	

	/**
	 * Private constructor ensures that variables can be created
	 * only by createVariable method.
	 */
	private ModelVariable() {
	}
	
	

	
	/**
	 * Creates a new model variable from the given xml-node.
	 * If a variable with the given name exists then an exception
	 * will be raised.
	 * @param node
	 * @return
	 */
	public static ModelVariable loadVariable(SparkModel model, Node node) throws Exception {
		NamedNodeMap attributes = node.getAttributes();
		Node tmp;
		
		// Read node's attributes
		String name = (tmp = attributes.getNamedItem("name")) != null ? tmp.getNodeValue() : "???";
		String sget = (tmp = attributes.getNamedItem("get")) != null ? tmp.getNodeValue() : "";
		String sset = (tmp = attributes.getNamedItem("set")) != null ? tmp.getNodeValue() : "";
		String stype = (tmp = attributes.getNamedItem("type")) != null ? tmp.getNodeValue() : "Double";

		// Create a new variable and add it to the table
		ModelVariable var = new ModelVariable();
		var.name = name;
		
//		if (!model.addMovelVariable(var))
//			throw new Exception("A variable " + name + " is already declared");
		
		// Load type and set/get methods
		try {
			var.type = Class.forName("java.lang." + stype);
			// TODO: uncomment?
//			var.getMethod = ModelManager.getInstance().getModel().getClass().getMethod(sget);
//			var.setMethod = ModelManager.getInstance().getModel().getClass().getMethod(sset);
//			var.getMethod = GUIModelManager.getModelClass().getMethod(sget);
//			var.setMethod = GUIModelManager.getModelClass().getMethod(sset, var.type);
			var.getMethod = model.getClass().getMethod(sget);
			var.setMethod = model.getClass().getMethod(sset, var.type);
		}
		catch (Exception e) {
			// TODO: do we need to process any exception here?
			throw e;
		}
		
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
			
				writeFlag = true;
				this.value = value;
			}
			
			fireStateChanged();
			return;
		}

		if (value.getClass() != type)
			throw new Exception("Wrong type for variable " + name);

		synchronized (this) {
			if (value.equals(this.value))
				return;
			
			writeFlag = true;
			this.value = value;
		}

		fireStateChanged();
	}
	
	
	/**
	 * Parses string and sets variable's value
	 * @param svalue
	 * @throws Exception
	 */
	public void setValue(String svalue) throws Exception {
		if (type == Boolean.class) {
			if (svalue.equals("true"))
				setValue(Boolean.TRUE);
			else
				setValue(Boolean.FALSE);
			
			return;
		}
		
		if (type == Integer.class) {
			setValue(Integer.parseInt(svalue));
			return;
		}
		
		if (type == Double.class) {
			setValue(Double.parseDouble(svalue));
			return;
		}
	}
	
	
	/**
	 * Synchronizes variable's value with the SPARK value.
	 * Call it synchronously with model run. 
	 */
	public synchronized void synchronizeValue() throws Exception {
		// New value was assigned (from an interface)
		if (writeFlag && setMethod != null && value != null) {
			// TODO: should it be model instance instead of null?
			writeFlag = false;
			setMethod.invoke(null, value);
			
			// do not return and get the value later: for some
			// variables it makes sense to set it first and then
			// read again to ensure that a new value was set properly.
		}
		
		if (getMethod != null) {
			// TODO: should it be model instance instead of null?
			Object newValue = getMethod.invoke(null);
			
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
	
}
