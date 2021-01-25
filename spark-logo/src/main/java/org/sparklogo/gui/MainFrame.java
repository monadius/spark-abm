package org.sparklogo.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.File;
import java.util.List;

import javax.swing.*;

import org.sparkabm.utils.FileUtils;
import org.sparklogo.project.Project;
import org.sparklogo.project.ProjectFile;

class MainFrame extends JFrame implements ActionListener {
    private final JList<File> fileList;
    private final Console console;
    private final JButton addButton;
    private final JButton removeButton;
    private final JButton translateButton;
    private final JButton compileButton;
    private final JButton runInSparkButton;
    private final JButton startButton;

    protected final JTextField projectDirectoryField;
    protected final JTextField outputDirectoryField;
    protected final JTextField projectNameField;

//    private final JButton changeProjectDirectoryButton;
//    private final JButton changeOutputDirectoryButton;

    private final OptionsDialog options;

    /* Configuration */
    private final Configuration configuration;

    /* Current directory */
    private File currentDirectory;

    private JMenu fileMenu;
    /* Indicates where recent projects appear in the file menu */
    private int recentProjectsStart;

    /* Project instance */
    private final Project project;

    public MainFrame() {
        super("SPARK-PL");
        project = new Project();
        configuration = new Configuration();
        currentDirectory = configuration.getProgramDirectory();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    configuration.saveConfigFile();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        });

        // Create file list
        fileList = new JList<>(project.getListModel());
//		listPanel.add(fileList);

        // Create three panels and console
        JPanel projectPanel = new JPanel(new GridLayout(0, 2));
        JPanel buttonPanel = new JPanel(new GridLayout(6, 1));
        console = new Console();

        // Set panel sizes
        projectPanel.setMinimumSize(new Dimension(100, 100));
        projectPanel.setPreferredSize(new Dimension(400, 100));

        buttonPanel.setMinimumSize(new Dimension(200, 100));
        buttonPanel.setPreferredSize(new Dimension(200, 200));
        buttonPanel.setMaximumSize(new Dimension(200, 1000));

        console.setMinimumSize(new Dimension(600, 100));
        console.setPreferredSize(new Dimension(600, 100));

        // Create scroll pane for list
        JScrollPane listScroll = new JScrollPane(fileList);
        listScroll.setPreferredSize(new Dimension(400, 200));
        listScroll.setMinimumSize(new Dimension(100, 100));

        // Create splitters
        JSplitPane splitter3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                listScroll, projectPanel);
        JSplitPane splitter2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                splitter3, buttonPanel);
        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                splitter2, console);

        // Create buttons
        addButton = new JButton("Add");
        addButton.addActionListener(this);

        removeButton = new JButton("Remove");
        removeButton.addActionListener(this);

        translateButton = new JButton("Translate");
        translateButton.addActionListener(this);

        compileButton = new JButton("Compile");
        compileButton.addActionListener(this);

        runInSparkButton = new JButton("Run in SPARK");
        runInSparkButton.addActionListener(this);

        startButton = new JButton("Start");
        startButton.addActionListener(this);


        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(translateButton);
        buttonPanel.add(compileButton);
        buttonPanel.add(runInSparkButton);
        buttonPanel.add(startButton);

        // Set up project panel
        projectNameField = new JTextField();
        projectDirectoryField = new JTextField();
        outputDirectoryField = new JTextField();
        // TODO: make them editable
        projectNameField.setEditable(false);
        projectDirectoryField.setEditable(false);
        outputDirectoryField.setEditable(false);

//        changeProjectDirectoryButton = new JButton("...");
//        changeProjectDirectoryButton.addActionListener(this);
//
//        changeOutputDirectoryButton = new JButton("...");
//        changeOutputDirectoryButton.addActionListener(this);

        projectPanel.add(new JLabel("Project name: "));
        projectPanel.add(projectNameField);
//        projectPanel.add(new JLabel(""));

        projectPanel.add(new JLabel("Project directory: "));
        projectPanel.add(projectDirectoryField);
//        projectPanel.add(changeProjectDirectoryButton);

        projectPanel.add(new JLabel("Output directory: "));
        projectPanel.add(outputDirectoryField);
        // projectPanel.add(changeOutputDirectoryButton);
//        projectPanel.add(new JLabel(""));

        // Create options dialog
        options = new OptionsDialog(this);

        // Set up menu
        setupMenu();
        configuration.readConfigFile();
        updateRecentMenu();

        // Finalize setup
        this.add(splitter, BorderLayout.CENTER);
        this.pack();
        this.validate();

    }

    /**
     * Sets up a menu
     */
    private void setupMenu() {
        JMenuBar menuBar;
        JMenu menu;
        JMenuItem menuItem;

        // Create the menu bar
        menuBar = new JMenuBar();

        // Menu "File"
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.getAccessibleContext().setAccessibleDescription("File menu");
        menuBar.add(menu);

        // Open
        menuItem = new JMenuItem("Open project...", KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Opens a project file");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // Save
        menuItem = new JMenuItem("Save project...", KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Saves the project");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // Exit
        menuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Exit");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        fileMenu = menu;
        recentProjectsStart = menu.getMenuComponentCount();

        // Menu "Options"
        menu = new JMenu("Options");
        menu.getAccessibleContext().setAccessibleDescription("Options menu");

        // Options dialog
        menuItem = new JMenuItem("Options", KeyEvent.VK_P);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Options");
        menuItem.setActionCommand("options");
        menuItem.addActionListener(this);
        menu.add(menuItem);


        menuBar.add(menu);

        // Finalize menu setup
        this.setJMenuBar(menuBar);
    }

    /**
     * Returns the configuration object
     */
    Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Adds a project to the recent projects list
     */
    private void addRecentProject(File file) {
        configuration.addRecentProject(file);
        updateRecentMenu();
    }

    /**
     * Updates menu of recent projects
     */
    private void updateRecentMenu() {
        int n = fileMenu.getMenuComponentCount() - recentProjectsStart;

        // Remove all components
        for (int i = 0; i < n; i++) {
            fileMenu.remove(recentProjectsStart);
        }

        List<File> recentProjects = configuration.getRecentProjects();
        if (recentProjects.size() > 0) {
            fileMenu.addSeparator();
        }

        // Insert all current components
        for (int i = 0; i < recentProjects.size(); i++) {
            JMenuItem menuItem;

            menuItem = new JMenuItem(recentProjects.get(i).getName());
            menuItem.setActionCommand("project" + i);
            menuItem.addActionListener(this);

            fileMenu.add(menuItem);
        }
    }

    private void synchronizeWithProject() {
        projectNameField.setText(project.getName());
        File projectDirectory = project.getProjectDirectory();
        File outputDirectory = project.getOutputDirectory();

        if (projectDirectory != null) {
            projectDirectoryField.setText(projectDirectory.getPath());
        }
        else {
            projectDirectoryField.setText("");
        }

        if (outputDirectory != null) {
            outputDirectoryField.setText(outputDirectory.getPath());
        }
        else {
            outputDirectoryField.setText("");
        }
    }

    /**
     * Opens a project file with an open file dialog
     */
    private void openProjectFile() throws Exception {
        File file = FileUtils.openFileDialog(this, currentDirectory, "xml");

        if (file != null) {
            openProjectFile(file);
            currentDirectory = file.getParentFile();
        }
    }

    /**
     * Opens the given project file
     */
    public void openProjectFile(File file) throws Exception {
        ProjectFile.readProjectFile(file, project);
        synchronizeWithProject();
        addRecentProject(file);
    }

    /**
     * Saves a project file
     */
    private void saveProjectFile() throws Exception {
        File file = FileUtils.saveFileDialog(this, currentDirectory, "xml");
        if (file != null) {
            // TODO: think about a better solution: maybe it is intended
            //       to save file with a different extension
            if (!FileUtils.getExtension(file).equals("xml")) {
                file = new File(file.getPath() + ".xml");
            }

            project.saveProject(file);
            addRecentProject(file);
            currentDirectory = file.getParentFile();
        }
    }

    /**
     * Adds a file into a project file with a file open dialog
     */
    private void addFileDialog() throws Exception {
        File file = FileUtils.openFileDialog(this, currentDirectory, null);
        if (file != null) {
            project.addFile(file);
            currentDirectory = file.getParentFile();
        }
    }

    /**
     * Returns the current project directory
     */
    public File getCurrentDirectory() {
        return currentDirectory;
    }

    /**
     * Shows a directory selection dialog and sets the current directory
     */
    private File getDirectoryDialog()  {
        File file = FileUtils.selectDirDialog(this, currentDirectory);
        if (file != null) {
            currentDirectory = file;
        }
        return file;
    }

    public void actionPerformed(ActionEvent arg0) {
        Object src = arg0.getSource();

        try {
            /* Project directory change */
//            if (src == changeProjectDirectoryButton) {
//                File dir = getDirectoryDialog();
//                if (dir == null) dir = currentDirectory;
//
//                // TODO: remove this
//                projectNameField.setText("");
//                outputDirectoryField.setText("");
//
//                projectDirectoryField.setText(dir.getAbsolutePath());
//                project.synchronizeWithMainFrame(this);
//                return;
//            }

            /* Add button */
            if (src == addButton) {
                addFileDialog();
                return;
            }

            /* Remove button */
            if (src == removeButton) {
                int[] indices = fileList.getSelectedIndices();
                for (int i = indices.length - 1; i >= 0; i--) {
                    project.removeFile(indices[i]);
                }

                return;
            }

            /* Translate button */
            if (src == translateButton) {
                console.clearText();
                project.translate(configuration.getLibPath());
                return;
            }

            /* Compile button */
            if (src == compileButton) {
                console.clearText();
                project.compile(configuration.getLibPath());
                return;
            }

            /* Run in SPARK button */
            if (src == runInSparkButton) {
                console.clearText();
                project.runInSpark(configuration.getLibPath());
                return;
            }

            /* Start button */
            if (src == startButton) {
                console.clearText();
                File libPath = configuration.getLibPath();
                project.translate(libPath);
                project.compile(libPath);
                project.runInSpark(libPath);
                return;
            }

            /* Menu commands */
            if (src instanceof JMenuItem) {
                String action = arg0.getActionCommand();
                if (action.startsWith("project")) {
                    int i = Integer.parseInt(action.substring("project"
                            .length()));
                    File recentProject = configuration.getRecentProject(i);
                    if (recentProject != null) {
                        currentDirectory = recentProject.getParentFile();
                        openProjectFile(recentProject);
                    }
                    return;
                }

                if (action.equals("options")) {
                    options.setSparkLibPath(configuration.getLibPath());
                    options.setVisible(true);
                    return;
                }

                // TODO: add action commands to all menu items
                String cmd = ((JMenuItem) src).getText();

                if (cmd.startsWith("Open")) {
                    openProjectFile();
                }
                else if (cmd.startsWith("Save")) {
                    saveProjectFile();
                }
                else if (cmd.startsWith("Exit")) {
                    configuration.saveConfigFile();
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.toString());
        }
    }

}
