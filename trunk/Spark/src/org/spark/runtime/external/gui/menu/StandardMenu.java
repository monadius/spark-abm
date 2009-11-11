package org.spark.runtime.external.gui.menu;

import java.io.File;

import org.spark.runtime.external.Coordinator;
import org.spark.utils.FileUtils;



/**
 * Creates SPARK's main menu
 * @author Monad
 *
 */
public class StandardMenu {
	/**
	 * Creates SPARK's main menu
	 * @return
	 */
	public static SparkMenu create() {
		SparkMenuFactory factory = SparkMenuFactory.getFactory();
		SparkMenu menu = factory.createMenu("SPARK Menu", 0);
		
		SparkMenu file = createFileMenu(factory);
		SparkMenu options = createOptionsMenu(factory);
		SparkMenu window = createWindowMenu(factory);
		SparkMenu help = createHelpMenu(factory);
		
		menu.addItem(file);
		menu.addItem(options);
		menu.addItem(window);
		menu.addItem(help);
		
		return menu;
	}
	
	
	/**
	 * Creates 'File' menu
	 * @return
	 */
	private static SparkMenu createFileMenu(SparkMenuFactory factory) {
		SparkMenu file = factory.createMenu("File", 0);
		
		// TODO: action handlers
		SparkMenuItem open = factory.createItem("Open...", 0);
		SparkMenuItem close = factory.createItem("Close", 0);
		SparkMenuItem load = factory.createItem("Load state...", 1);
		SparkMenuItem save = factory.createItem("Save state...", 1);
		SparkMenuItem exit = factory.createItem("Exit", 2);

		// Open action
		open.setActionListener(new ISparkMenuListener() {
			public void onClick(SparkMenuItem item) {
				Coordinator c = Coordinator.getInstance();
				if (c == null)
					return;
				
				try {
					File file = FileUtils.openFileDialog(c.getCurrentDir(), "xml", null);
					if (file != null) {
						c.loadModel(file);
						c.startLoadedModel();
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		// Close action
		close.setActionListener(new ISparkMenuListener() {
			public void onClick(SparkMenuItem item) {
				Coordinator c = Coordinator.getInstance();
				if (c == null)
					return;
				
				c.unloadModel();
			}
		});
		
		// Add all items to the menu
		file.addItem(open);
		file.addItem(close);
		file.addItem(load);
		file.addItem(save);
		file.addItem(exit);
		
		return file;
	}
	
	
	/**
	 * Creates 'File' menu
	 * @return
	 */
	private static SparkMenu createOptionsMenu(SparkMenuFactory factory) {
		SparkMenu options = factory.createMenu("Options", 0);
		
		return options;
	}
	
	
	/**
	 * Creates 'File' menu
	 * @return
	 */
	private static SparkMenu createWindowMenu(SparkMenuFactory factory) {
		SparkMenu window = factory.createMenu("Window", 0);
		
		SparkMenuItem newView = factory.createItem("New View", 0);
		SparkMenuItem tile = factory.createItem("Tile Windows", 0);
		
		window.addItem(newView);
		window.addItem(tile);
		
		return window;
	}
	
	
	/**
	 * Creates 'File' menu
	 * @return
	 */
	private static SparkMenu createHelpMenu(SparkMenuFactory factory) {
		SparkMenu help = factory.createMenu("Help", 0);
		
		return help;
	}
	
	
	
	
}
