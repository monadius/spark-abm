package org.spark.runtime.external.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.spark.runtime.external.gui.menu.ISparkMenuListener;
import org.spark.runtime.external.gui.menu.SparkCheckBoxMenuItem;
import org.spark.runtime.external.gui.menu.SparkMenu;
import org.spark.runtime.external.gui.menu.SparkMenuFactory;
import org.spark.runtime.external.gui.menu.SparkMenuItem;

/**
 * Manages all windows in the SPARK user interface
 * 
 * @author Monad
 * 
 */
public abstract class WindowManager {
	/* Collection of all windows */
	private final ArrayList<SparkWindow> windows;
	
	/* Main window */
	private final SparkWindow mainWindow;
	
	/* Window menu */
	private SparkMenu windowMenu;

	/**
	 * Default protected constructor
	 */
	protected WindowManager() {
		windows = new ArrayList<SparkWindow>();
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
		HashSet<String> existingNames = new HashSet<String>();
		
		for (SparkWindow win : windows)
			existingNames.add(win.getName());

		for (int i = 1; i < 1000; i++) {
			if (existingNames.contains(result))
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
		SparkWindow[] result = new SparkWindow[windows.size()];
		result = windows.toArray(result);
		
		return result;
	}
	
	
	/**
	 * Returns a SPARK panel of the given type
	 * @param <T>
	 * @param type
	 * @return
	 */
	// TODO: not a good method
	@SuppressWarnings("unchecked")
	public <T extends ISparkPanel> T getSparkPanel(Class<T> type) {
		for (SparkWindow win : windows) {
			ISparkPanel panel = win.getPanel();
			if (panel != null)
				if (panel.getClass() == type)
					return (T) panel;
		}
		
		return null;
	}
	
	
	/**
	 * Destroys all windows (except the main window)
	 */
	public void disposeAll() {
		for (SparkWindow win : windows) {
			win.dispose();
		}
		
		windows.clear();
		updateWindowMenu();
	}
	
	
	// TODO: think about using removeWindow(String name)
	// and name is synchronized using event listeners
	public boolean removeWindow(SparkWindow window) {
		if (windows.contains(window)) {
			// TODO: remove event listeners
			windows.remove(window);
			updateWindowMenu();
			
			window.dispose();
			return true;
		}
		
		return false;
	}
	

	/**
	 * Adds a window to the manager
	 * 
	 * @param window
	 */
	protected final void addWindow(final SparkWindow window) {
		// Window is already in the table
		if (windows.contains(window))
			return;
		
		if (window == mainWindow)
			return;

		String name = window.getName();
		name = getGoodName(name);
		window.setName(name);

		// Visibility changed event
		window.addVisibilityChangedEvent(new SparkWindow.VisibilityChangedEvent() {
			@Override
			public void visibilityChanged(SparkWindow window, boolean visible) {
				updateWindowMenu();
			}
		});
		
		// Name changed event
		window.addNameChangeEvent(new SparkWindow.NameChangeEvent() {
			@Override
			public String nameChanging(SparkWindow win, String newName) {
				String name = getGoodName(newName);
				return name;
			}
			
			@Override
			public void nameChanged(SparkWindow win, String newName) {
				updateWindowMenu();
			}
		});

		windows.add(window);
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
		
		for (final SparkWindow win : windows) {
			SparkMenuFactory factory = SparkMenuFactory.getFactory();

			String name = win.getName();
			boolean visible = win.isVisible();
			
			final SparkCheckBoxMenuItem item = factory.createCheckBoxItem(name, 1);
			item.setSelected(visible);
			
			item.setActionListener(new ISparkMenuListener() {
				public void onClick(SparkMenuItem menuItem) {
					win.setVisible(item.isSelected());
				}
			});

			// Add window to the menu
			windowMenu.addItem(item);
		}
	}
	
	
	/**
	 * Moves the given panel into the specific location (window, container, etc.)
	 * @param panel
	 * @param location
	 */
	public SparkWindow setLocation(ISparkPanel panel, String location) {
		if (location == null)
			location = "Untitled";
		
		HashMap<String, SparkWindow> windowsByName = new HashMap<String, SparkWindow>();
		for (SparkWindow win : windows) {
			windowsByName.put(win.getName(), win);
		}
		
		if (windowsByName.containsKey(location)) {
			SparkWindow win = windowsByName.get(location);
			win.addPanel(panel);
			return win;
		}
		else if (location.equals(mainWindow.getName())) {
			mainWindow.addPanel(panel);
			return mainWindow;
		}
		else {
			SparkWindow win = getWindowFactory().createWindow(location, 0, 0, 200, 200);
			win.addPanel(panel);
			win.setVisible(true);
			return win;
		}
	}
	
	
	
	/**
	 * Tiles open windows
	 */
	public void tileWindows() {
		ArrayList<SparkWindow> charts = new ArrayList<SparkWindow>();
		ArrayList<SparkWindow> views = new ArrayList<SparkWindow>();
		ArrayList<SparkWindow> others = new ArrayList<SparkWindow>();
		SparkWindow parameters = null;
		
		for (SparkWindow win : windows) {
			if (!win.isVisible())
				continue;

			ISparkPanel panel = win.getPanel();
			
			if (panel instanceof SparkParameterPanel)
				parameters = win;
			else if (panel instanceof SparkChartPanel)
				charts.add(win);
			else if (panel instanceof SparkViewPanel)
				views.add(win);
			else
				others.add(win);
		}
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		
		int mainLength = dim.width / 3 + 50;
		int mainHeight = mainLength + 100;
		int parametersLength = dim.width * 5 / 12;
		int parametersHeight = mainHeight / 2;
		int othersLength = dim.width - mainLength - parametersLength;
		
		// MainFrame
		mainWindow.setLocation(0, 0, mainLength, mainHeight);
		
		// Parameters frame
		if (parameters != null) {
			Dimension parDim = parameters.getPreferredSize();
			parametersHeight = parDim.height;
			if (parametersHeight > (mainHeight * 2) / 3)
				parametersHeight = (mainHeight * 2) / 3;
			
			parameters.setLocation(mainLength, 0, parametersLength, parametersHeight);
		}
		
		// Other frames
		tileWindows(others, parametersHeight, 1, 
				dim.width - othersLength, 0, othersLength, parametersHeight);
		
		// Chart frames
		int x = mainLength;
		int y = parameters != null ? parametersHeight : 0;
		int w = parameters != null ? dim.width - x : parametersLength;
		int h = dim.height - y;
		
		tileWindows(charts, 300, 1.5, x, y, w, h);
		
		// View frames
		x = 0;
		y = mainHeight;
		w = mainLength;
		h = dim.height - y;
		
		tileWindows(views, mainLength, 1, x, y, w, h);		
	}
	
	
	/**
	 * Computes the length of a tiling
	 * @param k number of windows
	 * @param w width of a rectangular region
	 * @param h height of a rectangular region
	 * @param preferredLength preferred length of a tiling
	 * @param lengthToHeight ratio length : height
	 * @return
	 */
	private int computeTilingLength(int k, int w, int h, 
			int preferredLength, double lengthToHeight) {
		
		int length = preferredLength;

		// Number of windows along x axis
		int n = w / length;
		if (n == 0) {
			n = 1;
			length = w;
		}
		
		// Region is too small
		if (w * h < k)
			return 1;
		
		while (true) {
			int height = (int)(length / lengthToHeight);
			if (height <= 0)
				return 1;
			
			// Number of windows along y axis
			int m = h / height;
			
			if (m * n >= k)
				break;
			
			n += 1;
			length = w / n;
			
			if (length <= 0)
				return 1;
		}
		
		return length;
	}
	
	
	/**
	 * Tiles given windows in a specific rectangular region
	 * @param frames
	 * @param preferredLength
	 * @param lengthToHeight
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	private void tileWindows(ArrayList<SparkWindow> wins, 
			int preferredLength, double lengthToHeight, 
			int x, int y, int w, int h) {
		if (lengthToHeight < 1e-3 || w <= 1 || h <= 1)
			return;
		
		int k = wins.size();
		
		if (k == 0)
			return;
		
		int length = computeTilingLength(k, w, h, preferredLength, lengthToHeight);
		if (length <= 0)
			return;
		
		int height = (int)(length / lengthToHeight);
		if (height <= 0)
			return;
		
		int n = w / length;
		int m = h / height;
		
		int index = 0; 
		
		for (int j = 0; j < m; j++) {
			for (int i = 0; i < n; i++) {
				if (index >= k)
					return;
				
				SparkWindow frame = wins.get(index);
				frame.setLocation(x + i * length, y + j * height, length, height);
				
				index++;
			}
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
