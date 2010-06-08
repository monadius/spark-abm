package org.spark.startup;

import java.io.File;

import javax.swing.JOptionPane;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.spark.gui.ModelManager;

import com.spinn3r.log5j.Logger;


public class StartupGUI {
	private static final Logger logger = Logger.getLogger();
	
	public static void main(String[] args) {
		// The first thing to do is to set up the logger
		try {
			if (new File("spark.log4j.properties").exists()) {
				PropertyConfigurator.configure("spark.log4j.properties");
			} else {
				BasicConfigurator.configure();
				logger.error("File spark.log4j.properties is not found: using default output streams for log information");
			}
		} catch (Exception e) {
			e.printStackTrace();
			BasicConfigurator.configure();
		}

		
		try {
//			Observer.init("org.spark.core.Observer1");
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
