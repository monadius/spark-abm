package org.spark.runtime.external.gui;

import java.awt.Rectangle;

import org.spark.runtime.external.gui.menu.SparkMenu;

/**
 * Represents a window in a SPARK user interface
 * @author Monad
 *
 */
public abstract class SparkWindow {
	/* Window's name */
	private String name;
	
	/* Owner of the window */
	protected SparkWindow owner;
	

	/**
	 * Event class for dealing with name changes
	 * @author Monad
	 */
	abstract static class NameChangedEvent {
		public abstract void nameChanged(String newName);
	}
	
	private NameChangedEvent nameChanged;
	
	
	/**
	 * Creates a new SPARK window
	 * @param owner
	 */
	protected SparkWindow(SparkWindow owner) {
		this.owner = owner;
	}
	
	
	/**
	 * Returns window's name
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Sets a new name
	 * @param name
	 */
	public void setName(String name) {
		if (this.name != null && this.name.equals(name))
			return;
		
		this.name = name;
		
		if (nameChanged != null)
			nameChanged.nameChanged(name);
	}
	
	
	/**
	 * Sets a handler for the name change event
	 * @param event
	 */
	void setNameChangedEvent(NameChangedEvent event) {
		this.nameChanged = event;
	}
	
	
	/**
	 * Sets window's location and dimensions
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public abstract void setLocation(int x, int y, int width, int height);
	
	
	/**
	 * Returns window's space information
	 * @return
	 */
	public abstract Rectangle getLocation();
	
	
	/**
	 * Makes the window visible or hides it
	 * @param visible
	 */
	public abstract void setVisible(boolean visible);
	
	
	/**
	 * Returns true if the window is visible
	 * @return
	 */
	public abstract boolean isVisible();
	
	
	/**
	 * Sets the window menu
	 * @param menu
	 */
	public abstract void setMenu(SparkMenu menu);
	
	
	/**
	 * Destroys the window
	 */
	public abstract void dispose();
	
	
	/**
	 * Adds a SPARK panel inside the window
	 * @param panel
	 */
	public abstract void addPanel(ISparkPanel panel);
	
	
	/**
	 * Adds a SPARK panel to the given position
	 * @param panel
	 * @param position
	 */
	public abstract void addPanel(ISparkPanel panel, String location);

}
