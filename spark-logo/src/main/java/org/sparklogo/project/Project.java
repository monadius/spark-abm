package org.sparklogo.project;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.sparkabm.utils.FileUtils;
import org.sparklogo.main.SparkModel;
import org.sparklogo.parser.SparkLogoParser;

/**
 * SPARK Project class
 *
 * @author Monad
 */
public class Project {
    public final static String DEFAULT_NAME = "SPARK Model";

    private final static String[] LIBS = {
            "spark-core", "spark-math", "spark-utils"
    };
    private final static String SPARK_LIB = "spark-gui";

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
    private class ProjectListModel extends AbstractListModel<File> implements
            ListModel<File> {

        public File getElementAt(int n) {
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
        projectFiles = new ArrayList<>();
        listModel = new ProjectListModel();
        name = DEFAULT_NAME;
    }

    /**
     * Returns the list model associated with the project
     *
     */
    public ListModel<File> getListModel() {
        return listModel;
    }

    /**
     * Sets project's name
     */
    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            name = DEFAULT_NAME;
        }
        this.name = name;
    }

    /**
     * Returns project's name. The returned value is nonnull.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the project directory
     */
    public void setProjectDirectory(File dir) {
        // If it is not a directory then do nothing
        if (dir != null && !dir.isDirectory()) return;

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
     * Returns project's directory (the path to the project file).
     */
    public File getProjectDirectory() {
        return projectDirectory;
    }

    /**
     * Sets the output directory
     */
    public void setOutputDirectory(File dir) {
        outputDirectory = dir;
    }

    /**
     * Returns project's output directory
     */
    public File getOutputDirectory() {
        return outputDirectory;
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
     */
    public void addFile(File file) throws Exception {
        // Verify that there are no duplicates
        for (File f : projectFiles) {
            if (!f.isAbsolute() && projectDirectory != null) {
                f = new File(projectDirectory, f.getPath());
            }

            if (file.equals(f)) {
                throw new Exception("File " + file + " is already in the project");
            }
        }

        file = ProjectFile.getRelativePath(projectDirectory, file);
        projectFiles.add(file);
        listModel.fireChangeEvent();
    }

    /**
     * Removes a file at the given position
     */
    public void removeFile(int index) {
        if (index < 0 || index >= projectFiles.size()) return;

        projectFiles.remove(index);
        listModel.fireChangeEvent();
    }

    /**
     * Saves the project file
     */
    public void saveProject(File projectFile) throws Exception {
        if (projectFile != null && !projectFile.isDirectory()) {
            setProjectDirectory(projectFile.getParentFile());
            ProjectFile.saveProjectFile(projectFile, name, projectDirectory,
                    outputDirectory, projectFiles);
        }
    }

    private void checkPath(File path, FilenameFilter filter) throws Exception {
        if (path != null && path.isDirectory()) {
            File[] files = path.listFiles(filter);
            if (files != null && files.length > 0) {
                return;
            }
        }
        throw new Exception("Path to SPARK/lib is invalid: use options menu to set up this path");
    }

    private String getJars(File libPath, String[] names) {
        FilenameFilter filter = (dir, name) ->
                name.endsWith(".jar") && Arrays.stream(names).anyMatch(name::startsWith);

        return FileUtils.findAllFiles(libPath.getAbsoluteFile(), filter, false)
                        .stream()
                        .map(File::getPath)
                        .collect(Collectors.joining(File.pathSeparator));
    }

    /**
     * Translates the spark code into java code
     */
    public void translate(File sparkLibPath) throws Exception {
        if (projectFiles.size() == 0) {
            System.out.println("No files to translate");
            return;
        }
        File logoPath = new File(sparkLibPath != null ? sparkLibPath.getParentFile() : new File(""), "logo");
        checkPath(logoPath, (dir, name) -> "commands.xml".equals(name));

        File[] files = new File[projectFiles.size()];
        // Make all paths absolute
        for (int i = 0; i < files.length; i++) {
            files[i] = projectFiles.get(i);

            if (!files[i].isAbsolute() && projectDirectory != null) {
                files[i] = new File(projectDirectory, files[i].getPath());
            }
        }

        // Init spark model
        SparkModel.init(logoPath, name);

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
            if (outputDirectory.isAbsolute()) {
                output = outputDirectory;
            }
            else {
                output = new File(projectDirectory, outputDirectory.getPath());
            }
        } else {
            output = new File(projectDirectory, "output");
        }

        // Delete all java files before producing new files
        ProjectFile.deleteAll(model.getOutputPath(output), "java", false);

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
            if (!readme.equals(dst)) {
                ProjectFile.copy(readme, dst);
            }
        }
    }

    /**
     * Compiles java files into byte code inside the output directory
     */
    public void compile(File sparkLibPath) throws Exception {
        /*
         * if (rtPath == null || !rtPath.exists()) { throw new Exception(
         * "Path to rt.jar is invalid: use options menu to set up this path"); }
         */
        checkPath(sparkLibPath, (dir, name) -> name.startsWith(LIBS[0]) && name.endsWith(".jar"));

        // Arguments of the compiler
        ArrayList<String> compilerArgs = new ArrayList<>();

        // Set up output directory
        File output;

        if (outputDirectory != null) {
            if (outputDirectory.isAbsolute()) {
                output = outputDirectory;
            }
            else {
                output = new File(projectDirectory, outputDirectory.getPath());
            }
        } else {
            output = new File(projectDirectory, "output");
        }

        if (!output.exists()) {
            System.out.println("Output directory " + output.getPath() + " does not exist");
            return;
        }

        // Delete all .class files in the output directory
        ProjectFile.deleteAll(output, "class", true);

        // Get all java files in the output folder and its sub-folders
        ArrayList<File> javaFiles = FileUtils.findAllFiles(output, "java", true);

        // Create a compiler parameters string
        // compilerArgs.add("-verbose");

        compilerArgs.add("-Xlint:deprecation");

        compilerArgs.add("-target");
        compilerArgs.add("8");

        compilerArgs.add("-source");
        compilerArgs.add("8");

        compilerArgs.add("-classpath");
        compilerArgs.add(getJars(sparkLibPath, LIBS));

        for (File javaFile : javaFiles) {
            compilerArgs.add(javaFile.getPath());
        }

        // Get the javac compiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            // Try to use the compiler as an external process
            compilerArgs.add(0, "javac");
            System.out.println("Compiling java files...");
            System.out.println(String.join(" ", compilerArgs));

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
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new Exception("A Java compiler is not found. Please install JDK 8 or newer: https://adoptopenjdk.net/releases.html");
            }
        } else {
            // Run the compiler with standard input/output streams
            String[] args = new String[compilerArgs.size()];
            args = compilerArgs.toArray(args);

            System.out.println("Compiling java files...");
            System.out.println(String.join(" ", args));
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
    public void runInSpark(File sparkLibPath) throws Exception {
        checkPath(sparkLibPath, (dir, name) -> name.startsWith(SPARK_LIB) && name.endsWith(".jar"));

        // Set up output directory
        File output;

        if (outputDirectory != null) {
            if (outputDirectory.isAbsolute()) {
                output = outputDirectory;
            }
            else {
                output = new File(projectDirectory, outputDirectory.getPath());
            }
        } else {
            output = new File(projectDirectory, "output");
        }

        if (!output.exists()) {
            System.out.println("Output directory " + output.getPath()
                    + " does not exist");
            return;
        }

        // Get all xml files in the output folder (possible model files)
        ArrayList<File> xmlFiles = FileUtils.findAllFiles(output, "xml", false);
        if (xmlFiles.size() == 0) {
            throw new Exception("No model files in the output directory: " + output.getPath());
        }

//		StringBuilder cmd = new StringBuilder(
//				"java -Xmx512m -Xms128m -jar ");
//		cmd.append('"');
//		cmd.append(sparkExternalPath.getPath());
//		cmd.append('"');
//
//		cmd.append(" \"");
//		cmd.append(xmlFiles.get(0).getPath());
//		cmd.append('"');

//		System.out.println(cmd);
//        String[] cmd = {
//                String.join(File.separator, sparkExternalPath.getParentFile().getParent(), "bin", "spark-gui"),
//                xmlFiles.get(0).getPath()
//        };
        String[] cmd = {
                "java", "-jar",
                getJars(sparkLibPath, new String[] { SPARK_LIB }),
                xmlFiles.get(0).getPath()
        };

        System.out.println(String.join(" ", cmd));
        Process proc = Runtime.getRuntime().exec(cmd);

//				new String[] { "java", "-Xmx512m", "-Xms128m",
//						"-Dsun.java2d.d3d=false", "-jar", sparkExternalPath.getPath(),
//						xmlFiles.get(0).getPath() });

        InputStream inputStream = proc.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        inputStream = proc.getErrorStream();
        final BufferedReader errReader = new BufferedReader(
                new InputStreamReader(inputStream));

        // Read streams in separate threads
        new Thread(new LineReader(bufferedReader)).start();
        new Thread(new LineReader(errReader)).start();

    }

    private static class LineReader implements Runnable {
        private final BufferedReader reader;

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
