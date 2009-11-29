package org.spark.utils;

import java.awt.Window;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;


public class FileUtils {
	/**
	 * Returns all files satisfying the given filter in the given directory
	 * and its sub-directories
	 * @param directory
	 * @param filter
	 * @param recurse
	 * @return
	 */
	public static ArrayList<File> findAllFiles(File directory, FilenameFilter filter, boolean recurse) {
		ArrayList<File> files = new ArrayList<File>();
		
		// Get files / directories in the directory
		File[] entries = directory.listFiles();
			
		// Go over entries
		for (File entry : entries)
		{
				// If there is no filter or the filter accepts the 
				// file / directory, add it to the list
				if (filter == null || filter.accept(directory, entry.getName()))
				{
					files.add(entry);
				}
				
				// If the file is a directory and the recurse flag
				// is set, recurse into the directory
				if (recurse && entry.isDirectory())
				{
					files.addAll(findAllFiles(entry, filter, recurse));
				}
		}
			
		return files;
	}
	

	/**
	 * Returns a relative path to the given file
	 * @param file
	 * @return
	 */
	public static String getRelativePath(File basePath, File file) {
		if (basePath == null)
			return file.getAbsolutePath();
		
		if (basePath.isFile())
			basePath = basePath.getParentFile();
		
		if (!file.isAbsolute())
			file = new File(basePath, file.getPath());
		
		String path = file.getName();
		
		for (File f = file.getParentFile(); f != null; f = f.getParentFile()) {
			if (f.equals(basePath))
				return path;
			
			path = f.getName() + "/" + path;
		}
		
		return file.getAbsolutePath();
	}
	

	/**
	 * Returns file's extension
	 * @param f
	 * @return
	 */
	public static String getExtension(File f) {
		return getExtension(f.getName());
	}
	

	/**
	 * Returns file's extension
	 * @param fname
	 * @return
	 */
	public static String getExtension(String fname) {
		String ext = "";
		int i = fname.lastIndexOf('.');

		if (i > 0 && i < fname.length() - 1) {
			ext = fname.substring(i + 1).toLowerCase();
		}
		
		return ext;
	}

	
	/**
	 * Creates a file chooser for the given file extension
	 * @param dir
	 * @param extension
	 * @return
	 */
	public static JFileChooser createFileChooser(File dir, final String extension) {
		final JFileChooser fc = new JFileChooser(dir);
		
		fc.setFileFilter(new FileFilter() {
			// Accept all directories and all files with the given extension
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}

				if (extension == null)
					return true;
				
				String ext = getExtension(f);
				if (ext != null) {
					if (ext.equals(extension))
						return true;
					else
						return false;
				}

				return false;
			}

			// The description of this filter
			public String getDescription() {
				if (extension == null)
					return "*.*";
				
				return "*." + extension;
			}
		});

		return fc;
	}
	
	
	/**
	 * Shows an open file dialog and returns a selected file
	 * @return
	 * @throws Exception
	 */
	public static File openFileDialog(File dir, String extension, Window parent) {
		JFileChooser fc = createFileChooser(dir, extension);
		int returnVal = fc.showOpenDialog(parent);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			return file;
		}
		
		return null;
	}
	
	
	
	/**
	 * Shows an open file dialog and returns a selected file
	 * @return
	 * @throws Exception
	 */
	public static File saveFileDialog(File dir, final String extension, Window parent) {
		JFileChooser fc = createFileChooser(dir, extension);
		int returnVal = fc.showSaveDialog(parent);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			return file;
		}
		
		return null;
	}

}
