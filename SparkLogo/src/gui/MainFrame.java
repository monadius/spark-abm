package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements ActionListener {
	private static final int MAX_RECENT_PROJECTS = 10;

	
	
	private final JList fileList;
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

	private final JButton changeProjectDirectoryButton;
	private final JButton changeOutputDirectoryButton;
	
	private final OptionsDialog options;

	/* Current directory */
	private File currentDirectory = new File(".");

	/* Recent project files */
	private final ArrayList<File> recentProjects = new ArrayList<File>(
			MAX_RECENT_PROJECTS);

	private JMenu fileMenu;
	/* Indicates where recent projects appear in the file menu */
	private int recentProjectsStart;

	/* Project instance */
	private Project project;

	public MainFrame() throws Exception {
		super("SPARK-PL");
		project = new Project();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					saveConfigFile();
				}
				catch (Exception ex) {
				}
				System.exit(0);
			}
		});
		
		// Create three panels and console
		JPanel listPanel = new JPanel(new BorderLayout());
		JPanel projectPanel = new JPanel(new GridLayout(3, 2));
		JPanel buttonPanel = new JPanel(new GridLayout(6, 1));
		console = new Console();

		// Set panel sizes
		listPanel.setMinimumSize(new Dimension(100, 100));
		listPanel.setPreferredSize(new Dimension(400, 200));

		projectPanel.setMinimumSize(new Dimension(100, 100));
		projectPanel.setPreferredSize(new Dimension(400, 100));

		buttonPanel.setMinimumSize(new Dimension(200, 100));
		buttonPanel.setPreferredSize(new Dimension(200, 200));
		buttonPanel.setMaximumSize(new Dimension(200, 1000));

		console.setMinimumSize(new Dimension(600, 100));
		console.setPreferredSize(new Dimension(600, 100));

		// Create scroll pane for list
		JScrollPane listScroll = new JScrollPane(listPanel);
		listScroll.setMinimumSize(new Dimension(100, 100));

		// Create splitters
		JSplitPane splitter3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				listScroll, projectPanel);
		JSplitPane splitter2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				splitter3, buttonPanel);
		JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				splitter2, console);

		// Create file list
		fileList = new JList(project.getListModel());
		listPanel.add(fileList);

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

		changeProjectDirectoryButton = new JButton("...");
		changeProjectDirectoryButton.addActionListener(this);

		changeOutputDirectoryButton = new JButton("...");
		changeOutputDirectoryButton.addActionListener(this);

		projectPanel.add(new JLabel("Project name: "));
		projectPanel.add(projectNameField);
		projectPanel.add(new JLabel(""));

		projectPanel.add(new JLabel("Project directory: "));
		projectPanel.add(projectDirectoryField);
		projectPanel.add(changeProjectDirectoryButton);

		projectPanel.add(new JLabel("Output directory: "));
		projectPanel.add(outputDirectoryField);
		// projectPanel.add(changeOutputDirectoryButton);
		projectPanel.add(new JLabel(""));

		// Create options dialog
		options = new OptionsDialog(this);
		
		// Set up menu
		setupMenu();
		readConfigFile();

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
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Opens a project file");
		menuItem.addActionListener(this);
		menu.add(menuItem);

		// Save
		menuItem = new JMenuItem("Save project...", KeyEvent.VK_S);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Saves the project");
		menuItem.addActionListener(this);
		menu.add(menuItem);

		// Exit
		menuItem = new JMenuItem("Exit", KeyEvent.VK_X);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.CTRL_MASK));
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
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Options");
		menuItem.setActionCommand("options");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		
		menuBar.add(menu);

		// Finalize menu setup
		this.setJMenuBar(menuBar);
	}

	/**
	 * Reads a configuration file
	 */
	private void readConfigFile() {
		Document doc;

		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			doc = db.parse("config.xml");
		} catch (Exception e) {
			// No configure file or other problems
			return;
		}


		NodeList list = doc.getElementsByTagName("spark-path");
		if (list != null && list.getLength() > 0) {
			String sparkPath = list.item(0).getTextContent();
			options.setSparkPath(new File(sparkPath));
		}

		
		list = doc.getElementsByTagName("file");
		for (int i = 0; i < list.getLength(); i++) {
			String path = list.item(i).getTextContent();
			if (path == null || path.equals(""))
				continue;

			addRecentProject(new File(path));
		}
	}

	/**
	 * Saves a configuration file
	 */
	private void saveConfigFile() throws Exception {
		PrintStream out = new PrintStream(new File("config.xml"));

		out.println("<config>");
		
//		File rtPath = options.getRtPath();
//		if (rtPath != null) {
//			out.print("\t<rt-path>");
//			out.print(rtPath.getPath());
//			out.print("</rt-path>");
//			out.println();
//		}
		
		File sparkPath = options.getSparkPath();
		if (sparkPath != null) {
			out.print("\t<spark-path>");
			out.print(sparkPath.getPath());
			out.print("</spark-path>");
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

	/**
	 * Adds a project to the recent projects list
	 * 
	 * @param file
	 */
	private void addRecentProject(File file) {
		if (!file.exists())
			return;
		
		// Check duplicates
		for (int i = 0; i < recentProjects.size(); i++) {
			File f = recentProjects.get(i);
			if (f.equals(file)) {
				if (i == 0)
					return;

				// Move this project to the top
				recentProjects.remove(i);
				recentProjects.add(0, f);
				updateRecentMenu();

				return;
			}
		}

		// Add new project to the top
		recentProjects.add(0, file);
		updateRecentMenu();

		// Remove old 'recent' projects
		if (recentProjects.size() > MAX_RECENT_PROJECTS) {
			int n = recentProjects.size() - MAX_RECENT_PROJECTS;
			for (int i = 0; i < n; i++) {
				// Index should be the same because elements are automatically
				// shifted
				recentProjects.remove(MAX_RECENT_PROJECTS);
			}
		}
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

		if (recentProjects.size() > 0)
			fileMenu.addSeparator();

		// Insert all current components
		for (int i = 0; i < recentProjects.size(); i++) {
			JMenuItem menuItem;

			menuItem = new JMenuItem(recentProjects.get(i).getName());
			menuItem.setActionCommand("project" + i);
			menuItem.addActionListener(this);

			fileMenu.add(menuItem);
		}
	}

	/**
	 * Opens a project file
	 * 
	 * @throws Exception
	 */
	private void openProjectFile() throws Exception {
		File file = openFileDialog("xml");
	
		if (file != null) {
			openProjectFile(file);
			currentDirectory = file.getParentFile();
		}

	}

	
	/**
	 * Shows open file dialog and returns a selected file
	 * @param extension
	 * @return
	 * @throws Exception
	 */
	public File openFileDialog(final String ext) throws Exception {
		final JFileChooser fc = new JFileChooser(currentDirectory);

		// Create a filter
		fc.setFileFilter(new FileFilter() {
			// Accept all directories and all xml files
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}

				String extension = ProjectFile.getExtension(f);
				if (extension != null && ext != null) {
					if (extension.equals(ext))
						return true;
					else
						return false;
				}

				return false;
			}

			// The description of this filter
			public String getDescription() {
				return "*." + ext;
			}
		});

		int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			return file;
		}

		return null;
	}

	
	/**
	 * Opens a given project file
	 * 
	 * @param file
	 */
	public void openProjectFile(File file) throws Exception {
		ProjectFile.readProjectFile(file, project);
		project.synchronizeMainFrame(this);
		addRecentProject(file);
	}

	/**
	 * Saves a project file
	 * 
	 * @throws Exception
	 */
	private void saveProjectFile() throws Exception {
		final JFileChooser fc = new JFileChooser(currentDirectory);

		// Create a filter
		fc.setFileFilter(new FileFilter() {

			// Accept all directories and all xml files
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}

				String extension = ProjectFile.getExtension(f);
				if (extension != null) {
					if (extension.equals("xml"))
						return true;
					else
						return false;
				}

				return false;
			}

			// The description of this filter
			public String getDescription() {
				return "*.xml";
			}
		});

		int returnVal = fc.showSaveDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			// TODO: think about better solution: maybe it is intended
			// to save file with different extension
			if (!ProjectFile.getExtension(file).equals("xml"))
				file = new File(file.getPath() + ".xml");
				
			project.saveProject(file);

			addRecentProject(file);
			currentDirectory = fc.getCurrentDirectory();
		}

	}

	/**
	 * Add a file into a project file
	 * 
	 * @throws Exception
	 */
	private void addFileDialog() throws Exception {
		final JFileChooser fc = new JFileChooser(currentDirectory);

		int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			project.addFile(file);
			currentDirectory = fc.getCurrentDirectory();
		}

	}

	/**
	 * Add a file into a project file
	 * 
	 * @throws Exception
	 */
	private File getDirectoryDialog() throws Exception {
		final JFileChooser fc = new JFileChooser(currentDirectory);

		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			currentDirectory = fc.getCurrentDirectory();

			if (file.isDirectory()) {
				currentDirectory = file;
				return file;
			}
		}

		return null;
	}
	

	public void actionPerformed(ActionEvent arg0) {
		Object src = arg0.getSource();

		try {
			/* Project directory change */
			if (src == changeProjectDirectoryButton) {
				File dir = getDirectoryDialog();
				if (dir == null)
					dir = currentDirectory;

				// TODO: remove this
				projectNameField.setText("");
				outputDirectoryField.setText("");

				projectDirectoryField.setText(dir.getAbsolutePath());
				project.synchronizeWithMainFrame(this);
				return;
			}

			/* Add button */
			if (src == addButton) {
				addFileDialog();
				return;
			}

			/* Remove button */
			if (src == removeButton) {
				int[] indices = fileList.getSelectedIndices();
				for (int i = indices.length - 1; i >= 0; i--)
					project.removeFile(indices[i]);

				return;
			}

			/* Translate button */
			if (src == translateButton) {
				console.clearText();
				project.translate();
				return;
			}
			
			/* Compile button */
			if (src == compileButton) {
				console.clearText();
				project.compile(options.getSparkPath());
				return;
			}
			
			/* Run in SPARK button */
			if (src == runInSparkButton) {
				console.clearText();
				project.runInSpark(options.getSparkPath());
				return;
			}
			
			/* Start button */
			if (src == startButton) {
				console.clearText();
				project.translate();
				project.compile(options.getSparkPath());
				project.runInSpark(options.getSparkPath());
				return;
			}

			/* Menu commands */
			if (src instanceof JMenuItem) {
				String action = arg0.getActionCommand();
				if (action.startsWith("project")) {
					int i = Integer.parseInt(action.substring("project"
							.length()));
					currentDirectory = recentProjects.get(i).getParentFile();
					openProjectFile(recentProjects.get(i));
					return;
				}
				
				if (action.equals("options")) {
					options.setVisible(true);
					return;
				}

				// TODO: add action commands to all menu items
				String cmd = ((JMenuItem) src).getText();

				if (cmd.startsWith("Open"))
					openProjectFile();
				else if (cmd.startsWith("Save"))
					saveProjectFile();
				else if (cmd.startsWith("Exit")) {
					saveConfigFile();
					System.exit(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.toString());
		}
	}

}
