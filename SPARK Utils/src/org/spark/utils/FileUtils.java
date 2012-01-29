package org.spark.utils;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.spark.math.Vector;

import com.spinn3r.log5j.Logger;


public class FileUtils {
	private static final Logger logger = Logger.getLogger();
	
	/* Collection of all file writers */
	private static final HashMap<String, PrintStream> writers = new HashMap<String, PrintStream>();
	
	/* Base directory for file operations */
	private static File baseDir = null;
	
	
	/**
	 * Sets the base directory for file operations
	 * @param baseDir
	 */
	public static void setBaseDir(File baseDir) {
		if (!baseDir.exists()) {
			logger.info("The directory: " + baseDir + " does not exists");
			return;
		}
		
		logger.info("New base directory: " + baseDir);
		FileUtils.baseDir = baseDir;
	}
	
	
	/**
	 * Appends the base directory to the given file name
	 * @param fname
	 * @return
	 */
	public static File getFile(String fname) {
		File file = new File(fname);
		if (baseDir != null) {
			if (!file.isAbsolute()) {
				file = new File(baseDir, fname);
			}
		}
		
		return file;
	}
	

	/**
	 * Returns a file reader
	 * @param fname
	 * @return
	 */
	public static BufferedReader getFileReader(String fname) {
		try {
			FileReader fr = new FileReader(getFile(fname));
			return new BufferedReader(fr);
		}
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	
	/**
	 * Closes the file reader
	 * @param r
	 */
	public static void close(BufferedReader r) {
		if (r == null)
			return;
		
		try {
			r.close();
		}
		catch (IOException e) {
			logger.error(e);
		}
	}
	
	
	/**
	 * Reads a line from the given reader
	 */
	public static String readLine(BufferedReader r) {
		if (r == null)
			return null;
		
		try {
			return r.readLine();
		}
		catch (IOException e) {
			logger.error(e);
			return null;
		}
	}
	
	
	/**
	 * Reads a double number
	 */
	public static double readDouble(BufferedReader r) {
		if (r == null)
			return 0.0;
		
		try {
			String line = r.readLine();
			return StringUtils.StringToDouble(line);
		}
		catch (IOException e) {
			logger.error(e);
			return 0.0;
		}
	}
	
	
	/**
	 * Reads an integer
	 */
	public static int readInteger(BufferedReader r) {
		if (r == null)
			return 0;
		
		try {
			String line = r.readLine();
			return StringUtils.StringToInteger(line);
		}
		catch (IOException e) {
			logger.error(e);
			return 0;
		}
	}
	
	
	/**
	 * Reads a vector
	 */
	public static Vector readVector(BufferedReader r, String separator) {
		if (r == null)
			return null;
		
		try {
			String line = r.readLine();
			return StringUtils.StringToVector(line, separator);
		}
		catch (IOException e) {
			logger.error(e);
			return null;
		}
	}
	
	
	/**
	 * Returns an existing file writer or opens a new file
	 * @param name
	 * @return null if there is an error
	 */
	public static PrintStream getFileWriter(String name) {
		PrintStream writer = writers.get(name);
		
		if (writer != null) {
			return writer;
		}
		
		try {
			File file = new File(name);
			if (baseDir != null) {
				if (!file.isAbsolute()) {
					file = new File(baseDir, name);
				}
			}
			writer = new PrintStream(new FileOutputStream(file, true));
		}
		catch (IOException e) {
			logger.error(e);
			return null;
		}
		
		writers.put(name, writer);
		return writer;
	}
	
	
	/**
	 * Creates a new file with the given name or erases an existing file
	 * @param name
	 */
	public static void createNew(String name) {
		closeFile(name);
		
		File file = new File(name);
		if (baseDir != null) {
			if (!file.isAbsolute()) {
				file = new File(baseDir, name);
			}
		}
		
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			else {
				if (file.isFile()) {
					if (file.delete())
						file.createNewFile();
				}
			}
			
		}
		catch (IOException e) {
			logger.error(e);
		}
	}
	
	
	/**
	 * Creates a new file with a unique name and with the given prefix
	 * @param name
	 */
	public static String createUniqueNew(String prefix) {
		String name = prefix;
		int counter = 2;

		File output;
		if (baseDir != null) {
			output = baseDir;
		}
		else {
			output = new File(".");
		}
		
		try	{
			while (true) {
				File file = new File(output, name);
			
				if (file.exists()) {
					name = prefix + counter;
					counter++;
				}
				else {
					file.createNewFile();
					break;
				}
			}
		}
		catch (IOException e) {
			logger.error(e);
			return null;
		}
		
		return name;
	}
	
	
	/**
	 * Closes the file associated with the given name
	 */
	public static void closeFile(String name) {
		PrintStream writer = writers.get(name);
		
		if (writer != null) {
			writer.close();
			writers.remove(name);
		}
	}
	
	
	/**
	 * Closes all open files
	 */
	public static void closeAllOpenFiles() {
		for (PrintStream writer : writers.values()) {
			writer.close();
		}
		
		writers.clear();
	}
	
	
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
		if (entries == null)
			return files;
		
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
	 * Returns all files with the given extension in the given directory
	 * and its sub-directories
	 * @param directory
	 * @param filter
	 * @param recurse
	 * @return
	 */
	public static ArrayList<File> findAllFiles(File directory, final String extension, boolean recurse) {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name == null)
					return false;
				
				return extension.equals(getExtension(name));
			}
		};
		
		return findAllFiles(directory, filter, recurse);
	}
	
	
	/**
	 * Splits the given file path into a list of all parent directories
	 * starting from the root
	 * @param file
	 * @return
	 */
	public static ArrayList<String> splitFilePath(File file) {
		ArrayList<String> result = new ArrayList<String>();

		while (file != null) {
			result.add(file.getName());
			file = file.getParentFile();
		}
		
		// Reverse the resulting list
		int n = result.size();
		for (int i = 0; i < n / 2; i++) {
			String tmp = result.get(i);
			result.set(i, result.get(n - i - 1));
			result.set(n - i - 1, tmp);
		}
		
		return result;
	}
	

	/**
	 * Returns a relative path to the given file
	 * @param file
	 * @return
	 */
	public static String getRelativePath(File base, File file) {
		if (file == null)
			return null;
		
		if (base == null)
			return file.getAbsolutePath();
		
		if (base.isFile())
			base = base.getParentFile();

		// Get canonical paths
		File canonicalBase;
		File canonicalFile;
		
		try {
			canonicalBase = new File(base.getCanonicalPath());
			canonicalFile = new File(file.getCanonicalPath());
		}
		catch (Exception e) {
			logger.error(e);
			return file.getAbsolutePath();
		}
		
		ArrayList<String> baseList = splitFilePath(canonicalBase);
		ArrayList<String> fileList = splitFilePath(canonicalFile);

		int bn = baseList.size();
		int fn = fileList.size();
		int counter = 0;
		
		// Skip all common base directories
		while (true) {
			if (counter >= bn || counter >= fn)
				break;

			String name1 = baseList.get(counter);
			String name2 = fileList.get(counter);
			
			if (!name1.equals(name2))
				break;
			
			counter++;
		}

		StringBuilder str = new StringBuilder();
		for (int i = counter; i < bn; i++) {
			str.append("..");
			str.append("/");
		}
		
		for (int i = counter; i < fn; i++) {
			str.append(fileList.get(i));
			if (i < fn - 1)
				str.append("/");
		}
		
		return str.toString();
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
	
	
	/**
	 * Shows a dialog for selecting directories
	 */
	public static File selectDirDialog(File baseDir, Window parent) {
		final JFileChooser fc = new JFileChooser(baseDir);
		FileFilter filter = new FileFilter() {
			// Accept all directories and all files with the given extension
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}

				return false;
			}

			// The description of this filter
			public String getDescription() {
				return "directories";
			}
		};
		
		fc.setFileFilter(filter);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int returnVal = fc.showDialog(parent, "Select");
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			return file;
		}
		
		return null;
	}


}
