package gui;

import java.io.*;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import main.SparkModel;
import parser.SparkLogoParser;

/**
 * SPARK Project class
 * 
 * @author Monad
 * 
 */
public class Project {
	/* Project name */
	private String name;

	/* Main project directory */
	private File projectDirectory;

	/* Output path. Default value projectDirectory/output */
	private File outputDirectory;

	/* List of all porject files */
	private final ArrayList<File> projectFiles;

	/**
	 * Customized list model for user interface
	 */
	private class ProjectListModel extends AbstractListModel implements
			ListModel {
		private static final long serialVersionUID = 1L;

		public Object getElementAt(int n) {
			if (n >= 0 && n < projectFiles.size())
				return projectFiles.get(n);

			return null;
		}

		public int getSize() {
			return projectFiles.size();
		}

		public void fireChangeEvent() {
			fireContentsChanged(this, 0, getSize());
		}
	}

	/* The instance of the project list model */
	private final ProjectListModel listModel;

	/**
	 * Default constructor
	 */
	public Project() {
		projectFiles = new ArrayList<File>();
		listModel = new ProjectListModel();
		name = "SPARK Model";
	}

	/**
	 * Returns the list model associated with the project
	 * 
	 * @return
	 */
	public ListModel getListModel() {
		return listModel;
	}

	/**
	 * Sets project's name
	 * 
	 * @param name
	 */
	public void setName(String name) {
		if (name == null || name.equals(""))
			name = "SPARK Model";
		this.name = name;
	}

	/**
	 * Sets the project directory
	 * 
	 * @param dir
	 */
	public void setProjectDirectory(File dir) {
		// If it is not a directory then do nothing
		if (dir != null && !dir.isDirectory())
			return;

		if (projectDirectory != null) {
			// Make all paths absolute
			for (int i = 0; i < projectFiles.size(); i++) {
				File f = projectFiles.get(i);

				if (f.isAbsolute())
					continue;

				f = new File(projectDirectory, f.getPath());
				projectFiles.set(i, f);
			}
		}

		// Change project directory
		projectDirectory = dir;

		// Modify paths
		for (int i = 0; i < projectFiles.size(); i++) {
			File f = projectFiles.get(i);

			// Path is already absolute
			f = ProjectFile.getRelativePath(projectDirectory, f);
			projectFiles.set(i, f);
		}

		listModel.fireChangeEvent();
	}

	/**
	 * Sets the output directory
	 * 
	 * @param dir
	 */
	public void setOutputDirectory(File dir) {
		outputDirectory = dir;
	}

	/**
	 * Synchronizes the data with the main frame
	 * 
	 * @param frame
	 */
	public void synchronizeWithMainFrame(MainFrame frame) {
		String name = frame.projectNameField.getText();
		String projectDir = frame.projectDirectoryField.getText();
		String outputDir = frame.outputDirectoryField.getText();

		// Set project directory
		if (projectDir != null && !projectDir.equals(""))
			setProjectDirectory(new File(projectDir));
		else
			setProjectDirectory(null);

		if (name == null || name.equals("")) {
			setName(projectDirectory.getAbsoluteFile().getName());
		} else {
			setName(name);
		}

		if (outputDir == null || outputDir.equals("")) {
			outputDirectory = new File("output");
		} else {
			outputDirectory = new File(outputDir);
			// TODO: test output directory file (is it acceptable or not)
		}

		// Synchronize MainFrame data
		synchronizeMainFrame(frame);
	}

	/**
	 * Synchronizes text fields with the project data
	 * 
	 * @param frame
	 */
	public void synchronizeMainFrame(MainFrame frame) {
		if (name != null)
			frame.projectNameField.setText(this.name);
		else
			frame.projectNameField.setText("");

		if (projectDirectory != null)
			frame.projectDirectoryField.setText(projectDirectory.getPath());
		else
			frame.projectDirectoryField.setText("");

		if (outputDirectory != null)
			frame.outputDirectoryField.setText(outputDirectory.getPath());
		else
			frame.outputDirectoryField.setText("");
	}

	/**
	 * Removes all files from the project
	 */
	public void clearFileList() {
		projectFiles.clear();
		listModel.fireChangeEvent();
	}

	/**
	 * Adds a file to the project
	 * 
	 * @param file
	 * @return modified file (with relative/full path)
	 */
	public void addFile(File file) throws Exception {
		// Verify that there are no duplicates
		for (File f : projectFiles) {
			if (!f.isAbsolute() && projectDirectory != null)
				f = new File(projectDirectory, f.getPath());

			if (file.equals(f))
				throw new Exception("File " + file
						+ " is already in the project");
		}

		file = ProjectFile.getRelativePath(projectDirectory, file);
		projectFiles.add(file);
		listModel.fireChangeEvent();
	}

	/**
	 * Removes a file at the given position
	 * 
	 * @param index
	 */
	public void removeFile(int index) {
		if (index < 0 || index >= projectFiles.size())
			return;

		projectFiles.remove(index);
		listModel.fireChangeEvent();
	}

	/**
	 * Saves the project file
	 * 
	 * @param projectFile
	 */
	public void saveProject(File projectFile) throws Exception {
		ProjectFile.saveProjectFile(projectFile, name, projectDirectory,
				outputDirectory, projectFiles);
	}

	/**
	 * Translates the spark code into java code
	 * 
	 * @throws Exception
	 */
	public void translate() throws Exception {
		if (projectFiles.size() == 0) {
			System.out.println("No files to translate");
			return;
		}

		File[] files = new File[projectFiles.size()];
		// Make all paths absolute
		for (int i = 0; i < files.length; i++) {
			files[i] = projectFiles.get(i);

			if (!files[i].isAbsolute() && projectDirectory != null)
				files[i] = new File(projectDirectory, files[i].getPath());
		}

		if (name == null)
			name = "SPARK Model";

		// Init spark model
		SparkModel.init(name);

		// Read project files

		System.out.println("Reading files...");

		SparkLogoParser parser = new SparkLogoParser(files);
		SparkModel model = parser.read();

		// Parse code

		System.out.println("Parsing files...");
		model.parseMethods();

		// Set up output directory
		File output;

		if (outputDirectory != null) {
			if (outputDirectory.isAbsolute())
				output = outputDirectory;
			else
				output = new File(projectDirectory, outputDirectory.getPath());
		} else {
			output = new File(projectDirectory, "output");
		}

		// Translate model
		System.out.println("Creating Java code...");
		model.translateToJava(output);
		model.createXMLFiles(output);

		System.out.println("SUCCESSFUL");

		// TODO: do we need to add description files as source files?

		// Copy readme.txt
		// TODO: projectDirectory could be null
		File readme = new File(projectDirectory, "readme.txt");
		if (readme.exists()) {
			File dst = new File(output, "readme.txt");
			if (!readme.equals(dst))
				ProjectFile.copy(readme, dst);
		}
	}

	/**
	 * Compiles java files into byte code inside the output directory
	 * 
	 * @param rtPath
	 * @param sparkPath
	 */
	public void compile(File sparkPath) throws Exception {
		/*
		 * if (rtPath == null || !rtPath.exists()) { throw new Exception(
		 * "Path to rt.jar is invalid: use options menu to set up this path"); }
		 */
		if (sparkPath == null || !sparkPath.exists()) {
			throw new Exception(
					"Path to spark.jar is invalid: use options menu to set up this path");
		}

		// Arguments of the compiler
		ArrayList<String> compilerArgs = new ArrayList<String>();

		// Set up output directory
		File output;

		if (outputDirectory != null) {
			if (outputDirectory.isAbsolute())
				output = outputDirectory;
			else
				output = new File(projectDirectory, outputDirectory.getPath());
		} else {
			output = new File(projectDirectory, "output");
		}

		if (!output.exists()) {
			System.out.println("Output directory " + output.getPath()
					+ " does not exist");
			return;
		}

		// Get all java files in the output folder and its sub-folders
		ArrayList<File> javaFiles = ProjectFile.findAllFiles(output,
				new FilenameFilter() {
					public boolean accept(File dir, String fname) {
						if (fname.endsWith(".java"))
							return true;
						else
							return false;
					}

				}, true);

		// Create a compiler parameters string
		// compilerArgs.add("-verbose");

		compilerArgs.add("-Xlint:deprecation");
		
		compilerArgs.add("-target");
		compilerArgs.add("1.5");

		compilerArgs.add("-classpath");
		compilerArgs.add(sparkPath.getPath());

		for (int i = 0; i < javaFiles.size(); i++) {
			compilerArgs.add(javaFiles.get(i).getPath());
		}

		// Get the javac compiler
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			// Try to use the compiler as an external process
			compilerArgs.add(0, "java");
			compilerArgs.add(1, "-jar");
			compilerArgs.add(2, "lib/javac.jar");
			compilerArgs.add(3, "-verbose");
			
/*			Process proc = Runtime.getRuntime().exec(
					new String[] { "java", "-jar lib/javac.jar",
							 "-jar", sparkPath.getPath(),
							xmlFiles.get(0).getPath() });
*/
			try {
				Process proc = Runtime.getRuntime().exec(
					compilerArgs.toArray(new String[0]));

				InputStream inputStream = proc.getInputStream();
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				final BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);

				inputStream = proc.getErrorStream();
				final BufferedReader errReader = new BufferedReader(
						new InputStreamReader(inputStream));

				// Read streams in separate threads
//				new Thread(new LineReader(bufferedReader)).start();
				new Thread(new LineReader(errReader)).start();
				
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					System.out.println(line);
				}
			}
			catch (Exception e) {
				throw e;
//				throw new Exception("Java compiler is not found");
			}
		}
		else {
			// Run the compiler with standard input/output streams
			String[] args = new String[compilerArgs.size()];
			args = compilerArgs.toArray(args);

			System.out.println("Compiling java files...");
			int result = compiler.run(null, null, null, args);
		

			if (result == 0) {
				System.out.println("SUCCESSFUL");
			} else {
				System.out.println("FAILED");
				throw new Exception("Compilation failed");
			}
		}
	}

	/**
	 * Runs the project in SPARK
	 */
	public void runInSpark(File sparkPath) throws Exception {
		if (sparkPath == null || !sparkPath.exists()) {
			throw new Exception(
					"Path to spark.jar is invalid: use options menu to set up this path");
		}

		// Set up output directory
		File output;

		if (outputDirectory != null) {
			if (outputDirectory.isAbsolute())
				output = outputDirectory;
			else
				output = new File(projectDirectory, outputDirectory.getPath());
		} else {
			output = new File(projectDirectory, "output");
		}

		if (!output.exists()) {
			System.out.println("Output directory " + output.getPath()
					+ " does not exist");
			return;
		}

		// Get all xml files in the output folder (possible model files)
		ArrayList<File> xmlFiles = ProjectFile.findAllFiles(output,
				new FilenameFilter() {
					public boolean accept(File dir, String fname) {
						if (fname.endsWith(".xml"))
							return true;
						else
							return false;
					}

				}, false);

		if (xmlFiles.size() == 0)
			throw new Exception("No model files in the output directory: "
					+ output.getPath());

		StringBuilder cmd = new StringBuilder(
				"java -Xmx512m -Xms128m -Dsun.java2d.d3d=false -jar ");
		cmd.append('\"');
		cmd.append(sparkPath.getPath());
		cmd.append('\"');

		cmd.append(" \"");
		cmd.append(xmlFiles.get(0).getPath());
		cmd.append('\"');

		System.out.println(cmd);

		Process proc = Runtime.getRuntime().exec(
				new String[] { "java", "-Xmx512m", "-Xms128m",
						"-Dsun.java2d.d3d=false", "-jar", sparkPath.getPath(),
						xmlFiles.get(0).getPath() });

		InputStream inputStream = proc.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		final BufferedReader bufferedReader = new BufferedReader(
				inputStreamReader);

		inputStream = proc.getErrorStream();
		final BufferedReader errReader = new BufferedReader(
				new InputStreamReader(inputStream));

		// Read streams in separate threads
		new Thread(new LineReader(bufferedReader)).start();
		new Thread(new LineReader(errReader)).start();

	}

	private static class LineReader implements Runnable {
		private BufferedReader reader;

		public LineReader(BufferedReader reader) {
			this.reader = reader;
		}

		public void run() {
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
