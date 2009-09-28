package org.spark.startup;

import java.io.File;

import javax.swing.JOptionPane;

import org.spark.core.Observer;
import org.spark.gui.ModelManager;


public class StartupGUI {
	public static void main(String[] args) {
		try {
			Observer.init("org.spark.core.Observer1");
//			Observer.init("org.spark.core.ObserverParallel");
 
			ModelManager.init();
			if (args.length > 0) {
				File modelFile = new File(args[0]);
				if (modelFile.exists()) {
					ModelManager.getInstance().loadModel(modelFile);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.toString());
		}
	}
}
