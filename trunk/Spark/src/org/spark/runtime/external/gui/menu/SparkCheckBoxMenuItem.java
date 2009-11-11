package org.spark.runtime.external.gui.menu;

/**
 * A menu item with a check box
 * @author Monad
 *
 */
public abstract class SparkCheckBoxMenuItem extends SparkMenuItem {
	/**
	 * Default constructor
	 * @param name
	 * @param group
	 */
	protected SparkCheckBoxMenuItem(String name, int group) {
		super(name, group);
	}
	
	
	/**
	 * Sets the selection flag of the check box
	 * @param selected
	 */
	public abstract void setSelected(boolean selected);
	
	
	/**
	 * Returns the selection flag
	 * @return
	 */
	public abstract boolean isSelected();
}
