package org.spark.runtime;

import java.util.ArrayList;

import org.spark.gui.DatasetFrame;
import org.spark.gui.GUIModelManager;
import org.spark.gui.render.Render;

import com.spinn3r.log5j.Logger;

/**
 * Class for controlling the SPARK runtime from a model
 * @author Monad
 */
public class SPARKController {
	private static final Logger logger = Logger.getLogger();
	
	/**
	 * Resets the collected data
	 */
	public static void resetDataset() {
		logger.error("No effect");
/*		GUIModelManager manager = GUIModelManager.getInstance();
		if (manager == null)
			return;
		
		DatasetFrame data = manager.getDatasetFrame();
		if (data == null)
			return;
		
		data.reset();
*/		
	}
	

	/**
	 * Saves the collected data in the given file
	 * @param fname
	 */
	public static void saveDataset(String fname) {
		logger.error("No effect");
/*		if (fname == null)
			return;
		
		GUIModelManager manager = GUIModelManager.getInstance();
		if (manager == null)
			return;
		
		DatasetFrame data = manager.getDatasetFrame();
		if (data == null)
			return;

		data.saveData(fname);*/
	}
	
	
	private static boolean invoked = false;
	
	/**
	 * Saves the snapshots of all active views
	 * @param fname
	 */
	public static void saveSnapshots() {
		logger.error("No effect");
/*		if (!invoked) {
			invoked = true;
			
			GUIModelManager manager = GUIModelManager.getInstance();
			if (manager == null)
				return;
			
			final ArrayList<Render> renders = manager.getAllRenders();

			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					for (Render render : renders) {
						render.takeSnapshot();
					}
					invoked = false;
				}
			});
		}*/		
	}
	
	
	/**
	 * Save Model state which possible reRun later. 
	 * Output file format: "state"+Tick+".dat"
	 */
	public static void saveState() {
		logger.error("No effect");
		
//		saveState(null);
	}
	
	/**
	 * Save Model state which possible reRun later. 
	 * Output file format: fileName+Tick+".dat"
	 * @param fileName for example: "backup"
	 */
	public static void saveState(String fileName) {
		logger.error("No effect");
/*
		GUIModelManager manager = GUIModelManager.getInstance();
		
		if (manager == null)
			return;
		
		manager.saveModelState(fileName);*/
	}
	
	/**
	 * Make snapshot for one render.
	 * @param renderName
	 * @param fileName
	 */
	public static void saveSnapshot(String renderName, final String fileName) {
		logger.error("No effect");
/*		if (!invoked) {
			invoked = true;
			
			GUIModelManager manager = GUIModelManager.getInstance();
			if (manager == null)
				return;
			
			final ArrayList<Render> renders = manager.getAllRenders();

			for (int i = 0; i< renders.size();i++)
			{
				final Render render = renders.get(i);
				if (renderName == render.getName()) {
					java.awt.EventQueue.invokeLater(new Runnable() {
						public void run() {
							render.takeSnapshot(fileName);
							invoked = false;
						}
					});
				}
			}			
		}*/		
	}
}
