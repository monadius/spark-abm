package org.spark.runtime.external.gui.menu;

import java.util.ArrayList;

/**
 * SPARK menu can be a submenu of another SPARK menu
 * @author Monad
 *
 */
public abstract class SparkMenu extends SparkMenuItem {
	/* Sub-items of the menu */
	private final ArrayList<SparkMenuItem> subItems;
	
	/**
	 * Default constructor
	 * @param name
	 */
	protected SparkMenu(String name, int group) {
		super(name, group);
		subItems = new ArrayList<SparkMenuItem>();
	}
	
	
	/**
	 * Returns a sub-menu with the given name
	 * @param name
	 * @return
	 */
	public SparkMenu getSubMenu(String name) {
		for (SparkMenuItem item : subItems) {
			if (!(item instanceof SparkMenu))
				continue;
			
			if (item.getName().equals(name))
				return (SparkMenu) item;
		}
		
		return null;
	}
	
	
	/**
	 * Adds a new item into the menu
	 * @param item
	 */
	public void addItem(SparkMenuItem item) {
		int id = item.getGroup();
		
		for (int i = subItems.size() - 1; i >= 0; i--) {
			int id2 = subItems.get(i).getGroup();
			
			if (id >= id2) {
				insertAfter0(subItems.get(i), item, id != id2);
				subItems.add(i + 1, item);
				return;
			}
		}
		
		insertAfter0(null, item, true);
		subItems.add(0, item);
	}
	
	
	/**
	 * Removes the given item from the menu
	 * @param item
	 */
	public void removeItem(SparkMenuItem item) {
		int index = subItems.indexOf(item);
		if (index == -1)
			return;
		
		removeAt0(item);
		subItems.remove(index);
	}
	
	
	/**
	 * Removes all items of the given group
	 * @param group
	 */
	public void removeGroup(int group) {
		ArrayList<SparkMenuItem> items = new ArrayList<SparkMenuItem>();
		
		for (SparkMenuItem item : subItems) {
			if (item.getGroup() == group)
				items.add(item);
		}
		
		for (SparkMenuItem item : items) {
			removeItem(item);
		}
	}
	
	
	
	/**
	 * Inserts a new item after the given index
	 * @param item
	 * @param separated if true then the new item will be separated from other items
	 */
	protected abstract void insertAfter0(SparkMenuItem before, SparkMenuItem item, boolean separated);
	
	
	/**
	 * Removes an item at the given position
	 * @param index
	 */
	protected abstract void removeAt0(SparkMenuItem item);
	
}
