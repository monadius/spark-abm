package gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Simple xml-based project file of the form
 * <files>
 * 	<file>name relative to this file</file>
 *  ...
 * </files>
 * @author Monad
 *
 */
public class ProjectFile {
	/**
	 * Converts the file path relatively to the parent
	 * @param parent
	 * @param file
	 * @return
	 */
	public static File getRelativePath(File parent, File file) {
		if (parent == null || file == null)
			return file;
		
		parent = parent.getAbsoluteFile();
		if (!file.isAbsolute())
			file = new File(parent, file.getPath());
		String path = file.getName();
		
		for (File f = file.getParentFile(); f != null; f = f.getParentFile()) {
			if (f.equals(parent))
				return new File(path);
			
			path = f.getName() + File.separator + path;
				
		}
		
		return file;
	}
	
	
	/**
	 * Reads the project file
	 * @param path
	 * @param fname
	 * @return
	 * @throws Exception
	 */
	public static void readProjectFile(File projectFile, Project project) throws Exception {
		File defaultPath = projectFile.getAbsoluteFile().getParentFile();
		
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = db.parse(projectFile);

		NodeList elements = doc.getChildNodes();
		if (elements.getLength() < 1 || !elements.item(0).getNodeName().equals("spark-project"))
			throw new Exception("The file " + projectFile.getName() + " is not a valid SPARK project file");
		
		elements = elements.item(0).getChildNodes();
		
		String name = null;
//		String projectDir = null;
		String outputDir = null;

		for (int i = 0; i < elements.getLength(); i++) {
			Node node = elements.item(i);
			
			if (node.getNodeName().equals("name")) {
				name = node.getTextContent();
				continue;
			}

			if (node.getNodeName().equals("project-directory")) {
//				projectDir = node.getTextContent();
				continue;
			}
			
			if (node.getNodeName().equals("output-directory")) {
				outputDir = node.getTextContent();
				continue;
			}
		}

		// Set name
		if (name == null || name.equals(""))
			name = "SPARK Model";
		
		project.setName(name);
		
		// Set project directory
		File projectDirectory;

		// TODO: introduce a checkbox for selecting absolute project path
//		if (projectDir == null || projectDir.equals(""))
			projectDirectory = defaultPath;
//		else
//			projectDirectory = new File(projectDir);
			
		
		if (projectDirectory.exists())
			defaultPath = projectDirectory;

		project.setProjectDirectory(defaultPath);
		
		// Set output directory
		if (outputDir != null && !outputDir.equals("")) {
			project.setOutputDirectory(new File(outputDir));
		}

	
		// Parse file list
		NodeList fileList = doc.getElementsByTagName("file");
		project.clearFileList();
		
		for (int i = 0; i < fileList.getLength(); i++) {
			String fname = fileList.item(i).getTextContent();
			File file = new File(fname);
			project.addFile(file);
		}
	}

	
	
	/**
	 * Prints out a tag with the given inner text
	 * @param out
	 * @param tag
	 * @param text
	 * @param indent
	 */
	private static void printTag(PrintStream out, String tag, String text, int indent) {
		for (int i = 0; i < indent; i++)
			out.append('\t');
		
		out.print("<" + tag + ">");
		out.print(text);
		out.print("</" + tag + ">");
		out.println();
	}
	
	/**
	 * Saves files into a project file
	 * @param projectFile
	 * @param files
	 * @throws FileNotFoundException 
	 */
	public static void saveProjectFile(File projectFile, 
			String projectName, 
			File projectDirectory,
			File outputDirectory, 
			ArrayList<File> files) throws FileNotFoundException {
		PrintStream out = new PrintStream(projectFile);
		
		
		out.println("<spark-project>");

		// Print out name
		if (projectName != null && !projectName.equals("")) {
			printTag(out, "name", projectName, 1);
		}
		
		// Print out project directory
		if (projectDirectory != null) {
			printTag(out, "project-directory", projectDirectory.getAbsolutePath(), 1);
		}
		
		// Print out output directory
		if (outputDirectory != null) {
			printTag(out, "output-directory", outputDirectory.getPath(), 1);
		}
		
		out.println();
		out.println("\t<files>");
		
		for (int i = 0; i < files.size(); i++) {
			printTag(out, "file", files.get(i).getPath(), 2);
		}

		out.println("\t</files>");
		out.println("</spark-project>");

	}
	
	
	/**
	 * Auxiliary function
	 * @param f
	 * @return
	 */
	public static String getExtension(File f) {
        String ext = "";
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        
        return ext;
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
	 * Copies src file to dst file.
	 * @param src
	 * @param dst
	 * @throws IOException
	 */
    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
    
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        
        in.close();
        out.close();
    }

	
}
