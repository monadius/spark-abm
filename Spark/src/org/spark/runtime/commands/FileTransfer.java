package org.spark.runtime.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.ArrayList;

import org.spark.utils.FileUtils;
import org.spark.utils.XmlDocUtils;
import org.w3c.dom.Node;


/**
 * Transfer files between a client and server
 * @author Alexey
 */
public class FileTransfer implements Serializable {
	/**
	 * Serial Version UID 
	 */
	private static final long serialVersionUID = 1152208933067463431L;
	
	
	/**
	 * Description of a file
	 * @author Alexey
	 *
	 */
	private static class FileData implements Serializable {
		private static final long serialVersionUID = 7737543313716350645L;

		/* File's name */
		private String name;
		/* File's data */
		private byte[] data;
		
		/**
		 * Default constructor
		 * @param file
		 */
		public FileData(File rootPath, File file) {
			if (file == null || !file.isFile() || !file.exists())
				return;
			
			this.name = FileUtils.getRelativePath(rootPath, file).toString();
			
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				long size = file.length();
				
				if (size > 1000000)
					throw new Exception("File " + name + " cannot be transferred because it is too big: " + size);
				
				this.data = new byte[(int) size];
				fis.read(data);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				try {
					if (fis != null)
						fis.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		
		/**
		 * Restores the file at the given location
		 * @param path
		 */
		public void restoreFile(File path) throws Exception {
			if (name == null)
				return;
			
			if (path == null || !path.isDirectory())
				throw new Exception("Invalid path");
			
			File file = new File(path, name);
			file.getParentFile().mkdirs();
			
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(data);
			fos.close();
		}
	}
	
	
	/* Data for all files */
	private final ArrayList<FileData> fileData = new ArrayList<FileData>(); 

	
	/**
	 * Creates a file transfer object from the given xml node
	 * @param filesNode
	 * @param path
	 */
	public FileTransfer(Node filesNode, File path) {
		String path0 = XmlDocUtils.getValue(filesNode, "path", ".");
		path = new File(path, path0);
		
		ArrayList<File> files;
		if (XmlDocUtils.getBooleanValue(filesNode, "all", false)) {
			// Load all files
			files = FileUtils.findAllFiles(path, new FilenameFilter() {
				public boolean accept(File dir, String name) {
					String ext = FileUtils.getExtension(name).intern();
					if (ext == "class" || ext == "jar")
						return true;
					else
						return false;
				}
			}, true);
		}
		else {
			// Load specific files
			files = new ArrayList<File>();
			ArrayList<Node> nodes = XmlDocUtils.getChildrenByTagName(filesNode, "file");
		
			for (Node node : nodes) {
				String name = XmlDocUtils.getValue(node, "name", null);
				if (name != null)
					files.add(new File(path, name));
			}
		}
		
		// Read and save data for all files
		for (File file : files) {
			fileData.add(new FileData(path, file));
		}
	}
	
	
	/**
	 * Restores all files at the given location
	 * @param path
	 * @throws Exception
	 */
	public void restoreFiles(File path) throws Exception {
		for (FileData data : fileData) {
			data.restoreFile(path);
		}
	}
}
