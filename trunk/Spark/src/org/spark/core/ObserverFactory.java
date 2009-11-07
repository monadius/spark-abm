package org.spark.core;

//import com.spinn3r.log5j.Logger;

/**
 * Creates instances of Observer
 * @author Monad
 *
 */
public class ObserverFactory {
//	private static final Logger logger = Logger.getLogger();
	
	public static final String DEFAULT_OBSERVER_NAME = "Observer1";
	
	
	@SuppressWarnings("unchecked")
	static Observer create(String observerName, int executionMode) throws Exception
	{
		if (!observerName.startsWith("org.spark"))
			observerName = "org.spark.core." + observerName;
		
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
	 * @param observerName if null then a default observer is used
	 * @param executionMode
	 * @return
	 */
	public static Observer create(SparkModel model, String observerName, int executionMode) throws Exception {
		if (model == null)
			throw new Exception("Observer cannot be created for a null model");

		if (observerName == null) {
			observerName = model.getDefaultObserverName();
			executionMode = model.getDefaultExecutionMode();
		}
		
		Observer observer = create(observerName, executionMode);
		model.setObserver(observer);
		
		return observer;
	}
	
	
	/**
	 * Returns the list of names of all available observers
	 * @return
	 */
	public static String[] getObserversList() {
		return new String[] { "Observer1",
				"Observer2", "ObserverParallel" };
	}
}
