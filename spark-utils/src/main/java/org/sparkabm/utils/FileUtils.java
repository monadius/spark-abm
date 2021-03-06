package org.sparkabm.utils;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.sparkabm.math.Vector;


public class FileUtils {
    private static final Logger logger = Logger.getLogger(FileUtils.class.getName());

    /* Collection of all file writers */
    private static final Map<String, PrintStream> writers = new HashMap<>();

    /* Base directory for file operations */
    private static File baseDir = null;


    /**
     * Sets the base directory for file operations
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
     */
    public static File getFile(String fname) {
        final File file = new File(fname);
        if (baseDir != null) {
            if (!file.isAbsolute()) {
                return new File(baseDir, fname);
            }
        }

        return file;
    }


    /**
     * Returns a file reader
     */
    public static BufferedReader getFileReader(String fname) {
        try {
            FileReader fr = new FileReader(getFile(fname));
            return new BufferedReader(fr);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception", e);
            return null;
        }
    }


    /**
     * Returns a file writer
     */
    public static PrintWriter getPrintWriter(String fname) {
        try {
            return new PrintWriter(getFile(fname));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception", e);

            return null;
        }
    }


    /**
     * Closes the file reader
     */
    public static void close(BufferedReader r) {
        if (r == null) return;

        try {
            r.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "exception", e);
        }
    }


    /**
     * Closes the file writer
     */
    public static void close(PrintWriter w) {
        if (w == null) return;

        try {
            w.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception", e);
        }
    }


    /**
     * Reads a line from the given reader
     */
    public static String readLine(BufferedReader r) {
        if (r == null) return null;

        try {
            return r.readLine();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "exception", e);
            return null;
        }
    }

    /**
     * Writes a line to the given writer
     */
    public static void writeLine(PrintWriter w, String line) {
        if (w == null) return;

        try {
            w.println(line);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception", e);
        }
    }


    /**
     * Reads a double number
     */
    public static double readDouble(BufferedReader r) {
        if (r == null) return 0.0;

        try {
            String line = r.readLine();
            return StringUtils.StringToDouble(line);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "exception", e);
            return 0.0;
        }
    }

    /**
     * Writes a double number to the given writer
     */
    public static void writeDouble(PrintWriter w, double n) {
        if (w == null) return;

        try {
            w.println(n);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception", e);
        }
    }

    /**
     * Reads a boolean value
     */
    public static boolean readBool(BufferedReader r) {
        if (r == null) return false;

        try {
            String line = r.readLine();
            return StringUtils.stringToBoolean(line);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "exception", e);
            return false;
        }
    }

    /**
     * Writes a boolean value to the given writer
     */
    public static void writeBool(PrintWriter w, boolean b) {
        if (w == null) return;

        try {
            w.println(b);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception", e);
        }
    }


    /**
     * Reads an integer
     */
    public static int readInteger(BufferedReader r) {
        if (r == null) return 0;

        try {
            String line = r.readLine();
            return StringUtils.StringToInteger(line);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "exception", e);
            return 0;
        }
    }


    /**
     * Reads a vector
     */
    public static Vector readVector(BufferedReader r, String separator) {
        if (r == null) return null;

        try {
            String line = r.readLine();
            return StringUtils.StringToVector(line, separator);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "exception", e);
            return null;
        }
    }

    /**
     * Writes a vector to the given writer
     */
    public static void writeVector(PrintWriter w, Vector v) {
        if (w == null) return;

        try {
            w.println(v);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception", e);
        }
    }


    /**
     * Returns an existing file writer or opens a new file
     *
     * @return null if there is an error
     */
    public static PrintStream getFileWriter(String name) {
        PrintStream writer = writers.get(name);
        if (writer != null) return writer;

        try {
            File file = getFile(name);
            writer = new PrintStream(new FileOutputStream(file, true));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "exception", e);
            return null;
        }

        writers.put(name, writer);
        return writer;
    }


    /**
     * Creates a new file with the given name or erases an existing file
     */
    public static boolean createNew(String name) {
        closeFile(name);
        File file = getFile(name);

        try {
            if (file.createNewFile()) {
                return true;
            }
            else if (file.isFile() && file.delete()) {
                return file.createNewFile();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "exception", e);
        }
        return false;
    }


    /**
     * Creates a new file with a unique name and with the given prefix
     */
    public static String createUniqueNew(String prefix) {
        String name = prefix;
        int counter = 2;

        File output;
        if (baseDir != null) {
            output = baseDir;
        } else {
            output = new File(".");
        }

        try {
            while (true) {
                File file = new File(output, name);

                if (file.exists()) {
                    name = prefix + counter;
                    counter++;
                } else {
                    file.createNewFile();
                    break;
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "exception", e);
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
     */
    public static ArrayList<File> findAllFiles(File directory, FilenameFilter filter, boolean recurse) {
        ArrayList<File> files = new ArrayList<>();

        // Get files / directories in the directory
        File[] entries = directory.listFiles();
        if (entries == null) return files;

        // Go over entries
        for (File entry : entries) {
            // If there is no filter or the filter accepts the
            // file / directory, add it to the list
            if (filter == null || filter.accept(directory, entry.getName())) {
                files.add(entry);
            }

            // If the file is a directory and the recurse flag
            // is set, recurse into the directory
            if (recurse && entry.isDirectory()) {
                files.addAll(findAllFiles(entry, filter, recurse));
            }
        }

        return files;
    }


    /**
     * Returns all files with the given extension in the given directory
     * and its sub-directories
     */
    public static ArrayList<File> findAllFiles(File directory, final String extension, boolean recurse) {
        FilenameFilter filter = (dir, name) -> getExtension(name).equals(extension);
        return findAllFiles(directory, filter, recurse);
    }

    /**
     * Returns a relative path to the given file.
     * Both path are canonicalized before relativization.
     */
    public static String getRelativePath(File base, File file) {
        if (file == null) return null;
        if (base == null) return file.getAbsolutePath();
        if (base.isFile()) base = base.getParentFile();

        // Get canonical paths
        File canonicalBase;
        File canonicalFile;

        try {
            canonicalBase = base.getCanonicalFile();
            canonicalFile = file.getCanonicalFile();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception", e);
            return file.getAbsolutePath();
        }

        try {
            return canonicalBase.toPath().relativize(canonicalFile.toPath()).toString();
        }
        catch (IllegalArgumentException e) {
            // Return the file path if it cannot be relativized
            return file.getAbsolutePath();
        }
    }


    /**
     * Returns file's extension
     */
    public static String getExtension(File f) {
        return getExtension(f.getName());
    }


    /**
     * Returns file's extension
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
     */
    public static JFileChooser createFileChooser(File dir, final String extension) {
        final JFileChooser fc = new JFileChooser(dir);

        fc.setFileFilter(new FileFilter() {
            // Accept all directories and all files with the given extension
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                return extension == null || extension.equals(getExtension(f));
            }

            // The description of this filter
            public String getDescription() {
                return extension == null ? "*.*" : "*." + extension;
            }
        });

        return fc;
    }


    /**
     * Shows an open file dialog and returns a selected file
     */
    public static File openFileDialog(Window parent, File dir, String extension) {
        JFileChooser fc = createFileChooser(dir, extension);
        int returnVal = fc.showOpenDialog(parent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
        }

        return null;
    }


    /**
     * Shows an open file dialog and returns a selected file
     */
    public static File saveFileDialog(Window parent, File dir, String extension) {
        JFileChooser fc = createFileChooser(dir, extension);
        int returnVal = fc.showSaveDialog(parent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
        }

        return null;
    }


    /**
     * Shows a dialog for selecting directories
     */
    public static File selectDirDialog(Window parent, File baseDir) {
        final JFileChooser fc = new JFileChooser(baseDir);
        FileFilter filter = new FileFilter() {
            // Accept all directories
            public boolean accept(File f) {
                return f.isDirectory();
            }

            // The description of this filter
            public String getDescription() {
                return "directories";
            }
        };

        fc.setFileFilter(filter);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnVal = fc.showOpenDialog(parent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
        }

        return null;
    }


}
