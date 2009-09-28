package org.spark.runtime;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.spark.gui.GUIModelManager;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.spinn3r.log5j.Logger;

/**
 * Represents a method in a model which can be invoked during runtime
 * @author Monad
 */
public class ModelMethod {
	private static final Logger logger = Logger.getLogger();
	
	/* All methods */
	private final static HashMap<String, ModelMethod> methods;
	
	static {
		methods = new HashMap<String, ModelMethod>();
	}
	
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
	public static ModelMethod createMethod(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		Node tmp;
		
		String name = (tmp = attributes.getNamedItem("name")) != null ? tmp.getNodeValue() : "???";
		String smethod = (tmp = attributes.getNamedItem("method")) != null ? tmp.getNodeValue() : "";
		
		if (methods.containsKey(name)) {
			logger.error("Method " + name + " is already defined");
			return null;
		}
		
		Method method = null;
		
		try {
			// FIXME: Change it to ModelManager later
			// method = ModelManager.getModel().getClass().getMethod(smethod);
			// Look at a static method first
			method = GUIModelManager.getModelClass().getMethod(smethod);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		if (method != null) {
			ModelMethod mm = new ModelMethod(name, method);
			methods.put(name, mm);
			
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
	private synchronized void synchronize() throws Exception {
		for (int i = 0; i < callsCount; i++) {
			// TODO: change to ModelManager later
			method.invoke(GUIModelManager.getInstance().getModel());
			GUIModelManager.getInstance().requestUpdate();
		}
		
		callsCount = 0;
	}
	
	
	/**
	 * Synchronizes method calls
	 */
	public static void synchronizeMethods() {
		for (ModelMethod method : methods.values()) {
			try {
				method.synchronize();
			}
			catch (Exception e) {
				logger.error("Error during external method invokation: " + method.name);
				logger.error(e.getMessage());
			}
		}
	}
}
