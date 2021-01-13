package org.spark.runtime.external.gui.menu;


/**
 * Creates SPARK menu items
 * @author Monad
 *
 */
abstract public class SparkMenuFactory {
	/**
	 * Creates a new sub-menu
	 * @param name
	 * @param group
	 * @return
	 */
	public abstract SparkMenu createMenu(String name, int group);
	
	/**
	 * Creates a menu item
	 * @param name
	 * @param group
	 * @return
	 */
	public abstract SparkMenuItem createItem(String name, int group);
	
	
	/**
	 * Creates a check box menu item
	 * @param name
	 * @param group
	 * @return
	 */
	public abstract SparkCheckBoxMenuItem createCheckBoxItem(String name, int group);
	
	
	/**
	 * Returns a factory for creating menu items
	 * @return
	 */
	public static SparkMenuFactory getFactory() {
		// TODO: better implementation is required
		return new Swing_SparkMenuFactory();
	}
}
