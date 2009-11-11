package org.spark.runtime.external.gui;

import java.util.HashMap;

import org.spark.runtime.external.gui.menu.SparkCheckBoxMenuItem;
import org.spark.runtime.external.gui.menu.SparkMenu;
import org.spark.runtime.external.gui.menu.SparkMenuFactory;

/**
 * Manages all windows in the SPARK user interface
 * 
 * @author Monad
 * 
 */
public abstract class WindowManager {
	/* Tables of windows and their names (all windows have unique names) */
	private final HashMap<String, SparkWindow> windowsByName;
	private final HashMap<SparkWindow, String> namesByWindow;
	
	/* Main window */
	private final SparkWindow mainWindow;
	
	/* Window menu */
	private SparkMenu windowMenu;

	/**
	 * Default protected constructor
	 */
	protected WindowManager() {
		windowsByName = new HashMap<String, SparkWindow>();
		namesByWindow = new HashMap<SparkWindow, String>();
		
		mainWindow = getWindowFactory().getMainWindow();
	}
	
	
	/**
	 * Sets main menu
	 * @param menu
	 */
	public void setMainMenu(SparkMenu menu) {
		this.windowMenu = menu.getSubMenu("Window");
		mainWindow.setMenu(menu);		
	}
	
	
	/**
	 * Returns main window
	 * @return
	 */
	public SparkWindow getMainWindow() {
		return mainWindow;
	}
	

	/**
	 * Returns a name which is not defined yet (based on the given name) Can
	 * return null if no good name can be found
	 * 
	 * @param name
	 * @return
	 */
	public String getGoodName(String name) {
		if (name == null)
			name = "";

		String result = name;

		for (int i = 1; i < 1000; i++) {
			if (windowsByName.containsKey(result))
				result = name + " " + String.valueOf(i);
			else
				return result;
		}

		return null;
	}
	
	
	/**
	 * Returns an array of all windows
	 * @return
	 */
	public SparkWindow[] getWindows() {
		SparkWindow[] result = new SparkWindow[windowsByName.size()];
		result = windowsByName.values().toArray(result);
		
		return result;
	}
	
	
	/**
	 * Destroys all windows (except the main window)
	 */
	public void disposeAll() {
		for (SparkWindow win : windowsByName.values()) {
			win.dispose();
		}
		
		windowsByName.clear();
		namesByWindow.clear();
		
		updateWindowMenu();
	}
	

	/**
	 * Adds a window to the manager
	 * 
	 * @param window
	 */
	protected final void addWindow(final SparkWindow window) {
		// Window is already in the table
		if (namesByWindow.containsKey(window))
			return;
		
		if (window == mainWindow)
			return;

		String name = window.getName();
		name = getGoodName(name);
		window.setName(name);

		window.setNameChangedEvent(new SparkWindow.NameChangedEvent() {
			@Override
			public void nameChanged(String newName) {
				String oldName = namesByWindow.get(window);
				if (oldName.equals(newName))
					return;
				
				namesByWindow.remove(window);
				windowsByName.remove(oldName);

				String name = getGoodName(newName);

				namesByWindow.put(window, name);
				windowsByName.put(name, window);
				
				window.setName(name);
				updateWindowMenu();
			}
		});

		windowsByName.put(name, window);
		namesByWindow.put(window, name);
		
		updateWindowMenu();
	}
	
	
	/**
	 * Updates window menu
	 */
	private void updateWindowMenu() {
		if (windowMenu == null)
			return;
		
		// Remove all windows from the window menu
		windowMenu.removeGroup(1);
		
		for (SparkWindow win : windowsByName.values()) {
			SparkMenuFactory factory = SparkMenuFactory.getFactory();

			String name = win.getName();
			boolean visible = win.isVisible();
			
			SparkCheckBoxMenuItem item = factory.createCheckBoxItem(name, 1);
			item.setSelected(visible);

			// Add window to the menu
			windowMenu.addItem(item);
		}
	}
	
	
	/**
	 * Moves the given panel into the specific location (window, container, etc.)
	 * @param panel
	 * @param location
	 */
	public void setLocation(ISparkPanel panel, String location) {
		if (location == null)
			location = "Untitled";
		
		if (windowsByName.containsKey(location)) {
			SparkWindow win = windowsByName.get(location);
			win.addPanel(panel);
		}
		else if (location.equals(mainWindow.getName())) {
			mainWindow.addPanel(panel);
		}
		else {
			SparkWindow win = getWindowFactory().createWindow(location, 0, 0, 200, 200);
			win.addPanel(panel);
			win.setVisible(true);
		}
	}
	
	
	/**
	 * Factory for creating SPARK windows
	 * @author Monad
	 *
	 */
	public abstract class SparkWindowFactory {
		/**
		 * Creates a new SPARK window
		 * 
		 * @param name
		 * @param x
		 * @param y
		 * @param width
		 * @param height
		 * @return
		 */
		public abstract SparkWindow createWindow(String name, int x, int y,
				int width, int height);
		
		
		/**
		 * Returns the main window
		 * @return
		 */
		public abstract SparkWindow getMainWindow();
	}
	
	
	/**
	 * Returns a factory for creating windows
	 * @return
	 */
	public abstract SparkWindowFactory getWindowFactory();
}
