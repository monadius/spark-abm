package org.spark.core;

import com.spinn3r.log5j.Logger;

/**
 * Creates instances of Observer
 * @author Monad
 *
 */
public class ObserverFactory {
	private static final Logger logger = Logger.getLogger();
	
	@SuppressWarnings("unchecked")
	static Observer create(String observerName, int executionMode) throws Exception
	{
		Class<ObserverImpl> cl = (Class<ObserverImpl>) Class.forName(observerName);
		if (cl == null)
				throw new Exception("Observer implementation class "
										+ observerName + " is not found");

		ObserverImpl impl = cl.newInstance();
		executionMode = impl.filterExecutionMode(executionMode);

		// Create a new observer
		Observer.instance = new Observer(impl, executionMode);

		return Observer.instance;
	}
	
	
	/**
	 * Creates an Observer for the given model
	 * @param model
	 * @param observerName
	 * @param executionMode
	 * @return
	 */
	public static Observer create(SparkModel model, String observerName, int executionMode) throws Exception {
		Observer observer = create(observerName, executionMode);
		
		if (model != null) {
			model.setObserver(observer);
		}
		else {
			logger.info("Creating observer without associated model");
		}
		
		return observer;
	}
	
	
	/**
	 * Returns the list of names of all available observers
	 * @return
	 */
	public static String[] getObserversList() {
		return new String[] { "org.spark.core.Observer1",
				"org.spark.core.Observer2", "org.spark.core.ObserverParallel" };
	}
}
