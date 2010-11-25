package org.spark.runtime.external.gui.menu;


/**
 * Item in a SPARK menu
 * @author Monad
 *
 */
abstract public class SparkMenuItem {
	/* Item's name */
	private String name;
	
	/* Items can be grouped together */
	private final int group;
	
	
	/**
	 * Default constructor
	 * @param name
	 */
	protected SparkMenuItem(String name, int group) {
		this.name = name;
		this.group = group;
	}
	
	
	/**
	 * Sets the shortcut for the menu item
	 * @param key
	 * @param action
	 */
	public void setShortcut(int key, int action) {
	}
	
	
	/**
	 * Returns item's name
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Returns group's index
	 * @return
	 */
	public int getGroup() {
		return group;
	}
	
	
	/**
	 * Invoked when some parameters have been changed
	 */
	protected abstract void stateChanged();
	
	
	/**
	 * Sets the action listener for item's events
	 * @param listener
	 */
	public abstract void setActionListener(ISparkMenuListener listener);
}
