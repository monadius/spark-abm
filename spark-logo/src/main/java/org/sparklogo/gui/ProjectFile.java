package org.sparklogo.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.sparkabm.utils.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Simple xml-based project file of the form
 * <files>
 * <file>name relative to this file</file>
 * ...
 * </files>
 *
 * @author Monad
 */
public class ProjectFile {
    /**
     * Converts the file path relatively to the parent
     */
    public static File getRelativePath(File parent, File file) {
        if (parent == null || file == null) return file;

        parent = parent.getAbsoluteFile();
        if (!file.isAbsolute()) {
            file = new File(parent, file.getPath());
        }

        try {
            return parent.toPath().relativize(file.toPath()).toFile();
        }
        catch (IllegalArgumentException e) {
            // Return the file path if it cannot be relativized
            return file;
        }
//        return new File(FileUtils.getRelativePath(parent, file));
    }


    /**
     * Reads the project file
     */
    public static void readProjectFile(File projectFile, Project project) throws Exception {
        File defaultPath = projectFile.getAbsoluteFile().getParentFile();

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(projectFile);

        NodeList elements = doc.getChildNodes();
        if (elements.getLength() < 1 || !elements.item(0).getNodeName().equals("spark-project")) {
            throw new Exception("The file " + projectFile.getName() + " is not a valid SPARK project file");
        }

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
        if (name == null || name.equals("")) {
            name = "SPARK Model";
        }

        project.setName(name);

        // Set project directory
        File projectDirectory;

        // TODO: introduce a checkbox for selecting absolute project path
//		if (projectDir == null || projectDir.equals(""))
        projectDirectory = defaultPath;
//		else
//			projectDirectory = new File(projectDir);


        if (projectDirectory.exists()) {
            defaultPath = projectDirectory;
        }

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
//		if (projectDirectory != null) {
//			printTag(out, "project-directory", projectDirectory.getAbsolutePath(), 1);
//		}

        // Print out output directory
        if (outputDirectory != null) {
            printTag(out, "output-directory", outputDirectory.getPath(), 1);
        }

        out.println();
        out.println("\t<files>");

        for (File file : files) {
            String path = file.getPath();
            // Use '/' as the separator even on windows machines
            path = path.replace('\\', '/');
            printTag(out, "file", path, 2);
        }

        out.println("\t</files>");
        out.println("</spark-project>");
    }


    /**
     * Deletes all files in the given directory with the given extension
     */
    public static void deleteAll(File directory, final String extension, boolean recursive) {
        ArrayList<File> files = FileUtils.findAllFiles(directory, extension, recursive);
        for (File file : files) {
            file.delete();
        }
    }


    /**
     * Copies src file to dst file.
     */
    public static void copy(File src, File dst) throws IOException {
        Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
