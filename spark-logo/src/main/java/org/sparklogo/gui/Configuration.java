package org.sparklogo.gui;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * SPARK Manager configuration
 */
public class Configuration {
    private static final int MAX_RECENT_PROJECTS = 10;

    /* Program directory (the parent directory of the running jar file) */
    private final File programDirectory;

    /* Path to SPARK/lib */
    private File libPath;

    /* Recent project files */
    private final ArrayList<File> recentProjects = new ArrayList<>(MAX_RECENT_PROJECTS);

    public Configuration() {
        File path;
        try {
            path = new File(MainFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
        }
        catch (URISyntaxException ex) {
            path = new File(".");
        }

        if ("lib".equals(path.getName())) {
            programDirectory = path.getParentFile();
        } else {
            programDirectory = path;
        }
    }

    public File getProgramDirectory() {
        return programDirectory;
    }

    public List<File> getRecentProjects() {
        return recentProjects;
    }

    public File getRecentProject(int index) {
        if (index >= 0 && index < recentProjects.size()) {
            return recentProjects.get(index);
        }
        return null;
    }

    /**
     * Returns the SPARK/lib path or the program path if SPARK/lib is invalid
     */
    public File getLibPath() {
        if (libPath == null || !libPath.isDirectory()) {
            return new File(programDirectory, "lib");
        }
        return libPath;
    }

    /**
     * Sets the SPARK/lib path
     */
    public void setLibPath(File path) {
        if (path != null && path.isDirectory()) {
            libPath = path;
        }
    }

    /**
     * Adds a project to the recent projects list
     */
    void addRecentProject(File file) {
        if (!file.exists()) return;

        // Check duplicates
        for (int i = 0; i < recentProjects.size(); i++) {
            File f = recentProjects.get(i);
            if (f.equals(file)) {
                if (i == 0) return;

                // Move this project to the top
                recentProjects.remove(i);
                recentProjects.add(0, f);
                return;
            }
        }

        // Add new project to the top
        recentProjects.add(0, file);

        // Remove old 'recent' projects
        if (recentProjects.size() > MAX_RECENT_PROJECTS) {
            recentProjects.subList(MAX_RECENT_PROJECTS, recentProjects.size()).clear();
        }
    }

    /**
     * Reads a configuration file
     */
    void readConfigFile() {
        Document doc;

        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            doc = db.parse(new File(programDirectory, "config.xml"));
        } catch (Exception e) {
            return;
        }

        NodeList list = doc.getElementsByTagName("spark-lib-path");
        if (list != null && list.getLength() > 0) {
            libPath = new File(list.item(0).getTextContent());
        }

        list = doc.getElementsByTagName("file");
        for (int i = 0; i < list.getLength(); i++) {
            String path = list.item(i).getTextContent();
            if (path == null || path.isEmpty()) continue;
            addRecentProject(new File(path));
        }
    }

    /**
     * Saves a configuration file
     */
    void saveConfigFile() throws Exception {
        PrintStream out = new PrintStream(new File(programDirectory, "config.xml"));

        out.println("<config>");

        if (libPath != null) {
            out.print("\t<spark-lib-path>");
            out.print(libPath.getPath());
            out.print("</spark-lib-path>");
            out.println();
        }

        out.println("\t<recent-projects>");
        for (int i = recentProjects.size() - 1; i >= 0; i--) {
            out.print("\t\t<file>");
            out.print(recentProjects.get(i).getPath());
            out.print("</file>");
            out.println();
        }
        out.println("\t</recent-projects>");

        out.println("</config>");
        out.close();
    }

}
