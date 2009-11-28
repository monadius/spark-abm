package org.spark.runtime.external.gui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.LinkedList;

import org.spark.runtime.external.gui.menu.SparkMenu;

/**
 * Represents a window in a SPARK user interface
 * @author Monad
 *
 */
public abstract class SparkWindow {
	/* Window's name */
	private String name;
	
	/* Panel inside the window */
	private ISparkPanel panel;
	
	/* Owner of the window */
	protected SparkWindow owner;
	

	/**
	 * Event class for dealing with name changes
	 * @author Monad
	 */
	abstract static class NameChangeEvent {
		public abstract String nameChanging(SparkWindow window, String newName);

		public abstract void nameChanged(SparkWindow window, String newName);
	}
	
	private final LinkedList<NameChangeEvent> nameChange;
	
	/**
	 * Event which is invoked when the visibility of the window changes
	 * @author Monad
	 *
	 */
	abstract static class VisibilityChangedEvent {
		public abstract void visibilityChanged(SparkWindow window, boolean visible);
	}
	
	private final LinkedList<VisibilityChangedEvent> visibilityChanged;
	
	
	/**
	 * Creates a new SPARK window
	 * @param owner
	 */
	protected SparkWindow(SparkWindow owner) {
		this.owner = owner;
		this.nameChange = new LinkedList<NameChangeEvent>();
		this.visibilityChanged = new LinkedList<VisibilityChangedEvent>();
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
		// Null names are not allowed
		if (name == null)
			return;
		
		if (this.name != null && this.name.equals(name))
			return;

		for (NameChangeEvent event : nameChange) {
			name = event.nameChanging(this, name);
		}
		
		this.name = name;
		
		for (NameChangeEvent event : nameChange) {
			event.nameChanged(this, this.name);
		}
	}
	
	
	/**
	 * Sets the handler for the name change event
	 * @param event
	 */
	void addNameChangeEvent(NameChangeEvent event) {
		nameChange.add(event);
	}
	
	/**
	 * Removes the handler for the name change event 
	 * @param event
	 */
	void removeNameChangeEvent(NameChangeEvent event) {
		nameChange.remove(event);
	}
	

	/**
	 * Adds the handler for the visibility change event
	 * @param event
	 */
	void addVisibilityChangedEvent(VisibilityChangedEvent event) {
		visibilityChanged.add(event);
	}

	
	/**
	 * Removes the handler for the visibility change event
	 * @param event
	 */
	void removeVisibilityChangedEvent(VisibilityChangedEvent event) {
		visibilityChanged.remove(event);
	}
	
	
	/**
	 * Invoked when the visibility changes
	 * @param visibile
	 */
	protected void onVisibilityChanged() {
		for (VisibilityChangedEvent event : visibilityChanged) {
			event.visibilityChanged(this, isVisible());
		}
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
	 * Returns a preferred size of the window
	 * @return
	 */
	public abstract Dimension getPreferredSize();
	
	
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
	 * Adds a panel into the window
	 * @param panel
	 */
	public final void addPanel(ISparkPanel panel) {
		if (addPanel0(panel))
			this.panel = panel;
	}
	

	/**
	 * Adds a SPARK panel to the given position
	 * @param panel
	 * @param position
	 */
	public final void addPanel(ISparkPanel panel, String position) {
		if (addPanel0(panel, position))
			this.panel = panel;
	}

	
	/**
	 * Returns a panel inside the window 
	 * @return
	 */
	protected ISparkPanel getPanel() {
		return panel;
	}
	
	
	/**
	 * Implementation of addPanel()
	 * @param panel
	 * @return false if the panel was not added
	 */
	protected abstract boolean addPanel0(ISparkPanel panel);
	
	
	/**
	 * Adds a SPARK panel to the given position
	 * @param panel
	 * @param position
	 */
	public abstract boolean addPanel0(ISparkPanel panel, String position);

}
