package org.spark.runtime.external.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.gui.SparkViewPanel;
import org.spark.runtime.external.gui.SparkWindow;
import org.spark.runtime.external.gui.WindowManager;
import org.spark.runtime.external.gui.dialogs.BatchRunDialog;
import org.spark.runtime.external.gui.dialogs.DataLayersDialog;
import org.spark.runtime.external.gui.dialogs.ModelPropertiesDialog;
import org.spark.runtime.external.gui.dialogs.SparkPreferencesDialog;
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
	public static SparkMenu create(WindowManager manager) {
		SparkMenuFactory factory = SparkMenuFactory.getFactory();
		SparkMenu menu = factory.createMenu("SPARK Menu", 0);
		
		SparkMenu file = createFileMenu(factory, manager);
		SparkMenu model = createModelMenu(factory, manager);
		SparkMenu window = createWindowMenu(factory, manager);
		SparkMenu help = createHelpMenu(factory, manager);
		
		menu.addItem(file);
		menu.addItem(model);
		menu.addItem(window);
//		menu.addItem(help);
		
		return menu;
	}
	
	
	/**
	 * Creates 'File' menu
	 * @return
	 */
	private static SparkMenu createFileMenu(SparkMenuFactory factory, WindowManager manager) {
		SparkMenu file = factory.createMenu("File", 0);
		
		// TODO: action handlers
		SparkMenuItem open = factory.createItem("Open...", 0);
		SparkMenuItem close = factory.createItem("Close", 0);
		SparkMenuItem preferences = factory.createItem("Preferences", 1);
//		SparkMenuItem load = factory.createItem("Load state...", 1);
//		SparkMenuItem save = factory.createItem("Save state...", 1);
		SparkMenuItem exit = factory.createItem("Exit", 2);

		// Open action
		open.setShortcut(KeyEvent.VK_O, ActionEvent.CTRL_MASK);
		
		open.setActionListener(new ISparkMenuListener() {
			public void onClick(SparkMenuItem item) {
				Coordinator c = Coordinator.getInstance();
				if (c == null)
					return;
				
				try {
					File file = FileUtils.openFileDialog(c.getCurrentDir(), "xml", null);
					if (file != null) {
						c.loadModel(file);
						c.startLoadedModel(Long.MAX_VALUE, true);
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
		
		
		// Preferences action
		preferences.setShortcut(KeyEvent.VK_P, ActionEvent.CTRL_MASK);
		preferences.setActionListener(new ISparkMenuListener() {
			private SparkPreferencesDialog dialog;
			
			public void onClick(SparkMenuItem item) {
				if (dialog == null) {
					Coordinator c = Coordinator.getInstance();
					dialog = new SparkPreferencesDialog(null, c.getConfiguration());
				}
				
				dialog.init();
				dialog.setVisible(true);
			}
		});
		
		
		// Exit action
		exit.setActionListener(new ISparkMenuListener() {
			public void onClick(SparkMenuItem item) {
				Coordinator c = Coordinator.getInstance();
				if (c == null)
					return;
				
				// TODO: make sure that the simulation is completely stopped
				// and all results are saved
				c.unloadModel();
				System.exit(0);
			}
		});
		
		// Add all items to the menu
		file.addItem(open);
		file.addItem(close);
		file.addItem(preferences);
//		file.addItem(load);
//		file.addItem(save);
		file.addItem(exit);
		
		return file;
	}
	
	
	/**
	 * Creates 'File' menu
	 * @return
	 */
	private static SparkMenu createModelMenu(SparkMenuFactory factory, WindowManager manager) {
		SparkMenu model = factory.createMenu("Model", 0);
		
		SparkMenuItem dataLayers = factory.createItem("Data Layer Properties", 0);
		SparkMenuItem modelProperties = factory.createItem("Model Properties", 0);
		SparkMenuItem batchRun = factory.createItem("Batch run", 1);
		
		// Data layer properties
		dataLayers.setActionListener(new ISparkMenuListener() {
			private DataLayersDialog dialog;
			
			public void onClick(SparkMenuItem item) {
				Coordinator c = Coordinator.getInstance();
				if (c == null)
					return;
			
				if (dialog == null)
					dialog = new DataLayersDialog(null);
				
				dialog.init(c.getDataLayerStyles());
				dialog.setVisible(true);
				dialog.toFront();
			}
		});
		
		// Model properties
		modelProperties.setActionListener(new ISparkMenuListener() {
			private ModelPropertiesDialog dialog;
			
			public void onClick(SparkMenuItem item) {
				if (dialog == null)
					dialog = new ModelPropertiesDialog(null);
				
				dialog.init();
				dialog.setVisible(true);
			}
		});
		
		// Batch run
		batchRun.setActionListener(new ISparkMenuListener() {
			private BatchRunDialog dialog;
			
			public void onClick(SparkMenuItem item) {
				if (dialog == null) {
					dialog = new BatchRunDialog(null);
				}
				
				dialog.init();
				dialog.setVisible(true);
			}
		});
		
		model.addItem(modelProperties);
		model.addItem(dataLayers);
		model.addItem(batchRun);
		
		return model;
	}
	
	
	/**
	 * Creates 'File' menu
	 * @return
	 */
	private static SparkMenu createWindowMenu(SparkMenuFactory factory, final WindowManager manager) {
		SparkMenu window = factory.createMenu("Window", 0);
		
		SparkMenuItem newView = factory.createItem("New View", 0);
		SparkMenuItem tile = factory.createItem("Tile Windows", 0);
		
		// New view
		newView.setActionListener(new ISparkMenuListener() {
			public void onClick(SparkMenuItem item) {
				// TODO: do not create a new window when a model is not loaded
				Coordinator c = Coordinator.getInstance();
				
				if (c == null || !c.isModelLoaded())
					return;
				
				SparkWindow win = manager.getWindowFactory().createWindow("View", 100, 100, 300, 300);
				new SparkViewPanel(win, c.getConfiguration().getRenderType());
				win.setVisible(true);
			}
		});
		
		// Tile
		tile.setActionListener(new ISparkMenuListener() {
			public void onClick(SparkMenuItem item) {
				manager.tileWindows();
			}
		});
		
		window.addItem(newView);
		window.addItem(tile);
		
		return window;
	}
	
	
	/**
	 * Creates 'File' menu
	 * @return
	 */
	private static SparkMenu createHelpMenu(SparkMenuFactory factory, WindowManager manager) {
		SparkMenu help = factory.createMenu("Help", 0);
		
		return help;
	}
	
	
	
	
}
