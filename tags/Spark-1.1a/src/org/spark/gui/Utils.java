package org.spark.gui;

import java.awt.Window;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.spark.utils.FileUtils;

public class Utils {



	public static File openFile(Window owner, final String extension) throws Exception {
		final JFileChooser fc = new JFileChooser(GUIModelManager.getInstance()
				.getCurrentDirectory());

		fc.setFileFilter(new FileFilter() {

			// Accept all directories and all xml files
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				
				if (extension == null)
					return true;

				String extension = FileUtils.getExtension(f);
				if (extension != null) {
					if (extension.equals(extension))
						return true;
					else
						return false;
				}

				return false;
			}

			// The description of this filter
			public String getDescription() {
				return "*." + extension;
			}
		});

		int returnVal = fc.showOpenDialog(owner);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			return file;
		}
		
		return null;
	}
	
	
}
