package org.spark.runtime.internal;

import java.lang.reflect.Method;

import org.spark.core.SparkModel;
import org.spark.gui.GUIModelManager;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Represents a method in a model which can be invoked during runtime
 * @author Monad
 */
public class ModelMethod {
//	private static final Logger logger = Logger.getLogger();
	
	/* Method's name */
	private String name;
	
	/* Reference to the method inside model class */
	private Method method;
	
	/* The number of times this method to be invoked during synchronization */
	private int callsCount;

	
	/**
	 * Returns method's name
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Private constructor
	 * @param name
	 * @param method
	 */
	private ModelMethod(String name, Method method) {
		this.name = name;
		this.method = method;
		this.callsCount = 0;
	}
	
	/**
	 * Creates a new model method and returns a reference to it
	 * @param node
	 * @return null if method was not created
	 */
	public static ModelMethod loadMethod(SparkModel model, Node node) {
		NamedNodeMap attributes = node.getAttributes();
		Node tmp;
		
		String name = (tmp = attributes.getNamedItem("name")) != null ? tmp.getNodeValue() : "???";
		String smethod = (tmp = attributes.getNamedItem("method")) != null ? tmp.getNodeValue() : "";
		
		Method method = null;
		
		try {
			// FIXME: Change it to ModelManager later
			// method = ModelManager.getModel().getClass().getMethod(smethod);
			// Look at a static method first
			method = model.getClass().getMethod(smethod);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		if (method != null) {
			ModelMethod mm = new ModelMethod(name, method);
			return mm;
		}
		
		return null;
	}
	
	
	/**
	 * Use this method to invoke the model method
	 */
	public synchronized void invoke() {
		callsCount++;
	}
	

	/**
	 * Invokes method
	 * @throws Exception
	 */
	public synchronized void synchronize(SparkModel model) throws Exception {
		for (int i = 0; i < callsCount; i++) {
			method.invoke(model);
			// TODO: remove later
			GUIModelManager.getInstance().requestUpdate();
		}
		
		callsCount = 0;
	}
	

}
