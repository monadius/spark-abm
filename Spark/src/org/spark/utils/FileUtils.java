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
}
