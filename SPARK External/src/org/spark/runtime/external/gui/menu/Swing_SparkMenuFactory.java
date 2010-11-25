package org.spark.runtime.external.gui.menu;

/**
 * Implements the SparkMenuFactory class
 * @author Monad
 *
 */
public class Swing_SparkMenuFactory extends SparkMenuFactory {

	@Override
	public SparkCheckBoxMenuItem createCheckBoxItem(String name, int group) {
		return new Swing_SparkCheckBoxMenuItem(name, group);
	}

	@Override
	public SparkMenuItem createItem(String name, int group) {
		return new Swing_SparkMenuItem(name, group);
	}

	@Override
	public SparkMenu createMenu(String name, int group) {
		return new Swing_SparkMenu(name, group);
	}

}
