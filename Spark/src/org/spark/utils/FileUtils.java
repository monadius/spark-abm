package org.spark.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

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
		String ext = "";
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		
		return ext;
	}

}
