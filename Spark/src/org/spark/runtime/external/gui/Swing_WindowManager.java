package org.spark.runtime.external.gui;

/**
 * Swing implementation of the WindowManager class
 * @author Monad
 *
 */
public class Swing_WindowManager extends WindowManager {
	/**
	 * Default constructor
	 */
	public Swing_WindowManager() {
	}
	
	
	/**
	 * Swing window factory
	 * @author Monad
	 *
	 */
	private class Swing_WindowFactory extends SparkWindowFactory {
		/* Main window instance */
		private SparkWindow mainWindow;

		
		/**
		 * Initializes the main window
		 */
		private void initMainWindow() {
			if (mainWindow != null)
				return;
			
			mainWindow = new Swing_SparkWindow(null);
			mainWindow.setVisible(true);
		}
		
		
		@Override
		public SparkWindow createWindow(String name, int x, int y, int width,
				int height) {
			initMainWindow();
			
			SparkWindow win = new Swing_SparkWindow(mainWindow);
			win.setName(name);
			win.setLocation(x, y, width, height);
			
			// Add to window manager
			addWindow(win);
			
			return win;
		}

		
		@Override
		public SparkWindow getMainWindow() {
			initMainWindow();
			return mainWindow;
		}
	}
	
	
	/* Factory instance */
	private static Swing_WindowFactory factory; 
	
	
	@Override
	public SparkWindowFactory getWindowFactory() {
		if (factory == null)
			factory = new Swing_WindowFactory();
		
		return factory;
	}

}
