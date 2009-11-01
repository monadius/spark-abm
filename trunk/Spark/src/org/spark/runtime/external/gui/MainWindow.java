package org.spark.runtime.external.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.spark.runtime.data.DataRow;
import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.data.IDataConsumer;
import org.spark.runtime.external.gui.dialogs.DataLayersDialog;
import org.spark.runtime.external.gui.dialogs.RandomSeedDialog;
import org.spark.runtime.external.gui.dialogs.RenderProperties;
import org.spark.runtime.external.render.Render;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MainWindow extends JFrame implements IDataConsumer, 
	ActionListener, ChangeListener {

	private static final long serialVersionUID = 2800524835363109821L;

	private JButton startButton, setupButton;
	private JButton saveButton;
	private JCheckBox synchButton;
	private JLabel tickLabel;
	private JPopupMenu popup;
	private JMenu windowMenu;

	private RandomSeedDialog randomSeedDialog;
	
	private Canvas canvas;
	private Render render;
	private RenderProperties renderDialog;
	private Node mainFrameNode;

	/* Specifies which render to use: Java2D or JOGL */
	protected int renderType;
	
	/* Recent project files */
	private final ArrayList<File> recentProjects = new ArrayList<File>(
			MAX_RECENT_PROJECTS);

	private static final int MAX_RECENT_PROJECTS = 10;

	private JMenu fileMenu;
	/* Indicates where recent projects appear in the file menu */
	private int recentProjectsStart;
	
	private int windowMenuStart;

	private static final int[] delaySize = new int[] { -100, -50, -20, -10, -5,
			-4, -3, -2, 0, 10, 20, 50, 100, 200, 500 };

	static {
		javax.swing.ToolTipManager.sharedInstance().setLightWeightPopupEnabled(
				false);
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	}
	
	public Render getRender() {
		return render;
	}

	public MainWindow() {
		super("SPARK Main Window");

		// this.canvas = new GLCanvas();
		this.render = null;

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// System.exit(0);

				try {
					Coordinator.getInstance().unloadModel();
					saveConfigFile();
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				System.exit(0);
			}
		});

		JPanel panel = new JPanel();
		panel.setMinimumSize(new Dimension(100, 100));

		synchButton = new JCheckBox("Sychronized", null, true);
		startButton = new JButton("Start");
		setupButton = new JButton("Setup");
		saveButton = new JButton("Save");
		tickLabel = new JLabel("");
		tickLabel.setMinimumSize(new Dimension(100, 80));

		synchButton.addActionListener(this);
		startButton.addActionListener(this);
		setupButton.addActionListener(this);
		saveButton.addActionListener(this);

		JSlider framesPerSecond = new JSlider(JSlider.HORIZONTAL, 0, 14, 0);
		framesPerSecond.addChangeListener(this);

		framesPerSecond.setMajorTickSpacing(2);
		framesPerSecond.setMinorTickSpacing(1);
		framesPerSecond.setPaintTicks(true);
		framesPerSecond.setPaintLabels(true);
		framesPerSecond.setValue(8);

		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(new Integer(0), new JLabel("Fast"));
		labelTable.put(new Integer(8), new JLabel("Normal"));
		labelTable.put(new Integer(14), new JLabel("Slow"));
		framesPerSecond.setLabelTable(labelTable);

		panel.add(framesPerSecond);
		panel.add(synchButton);
		panel.add(setupButton);
		panel.add(startButton);
		// panel.add(saveButton);
		panel.add(tickLabel);

//		JScrollPane scroll = new JScrollPane(panel);
//		scroll.setMinimumSize(new Dimension(100, 100));
//		scroll.setPreferredSize(new Dimension(getWidth(), 50));
		
//		this.getContentPane().add(scroll, BorderLayout.NORTH);
		this.getContentPane().add(panel, BorderLayout.NORTH);
		this.pack();
		
		setupMenu();
		readConfigFile();

		renderDialog = null;
		randomSeedDialog = new RandomSeedDialog(this);
		
		this.addWindowFocusListener(new WindowAdapter() {
		    public void windowGainedFocus(WindowEvent e) {
		    	if (canvas != null) {
		    		canvas.requestFocusInWindow();
		    	}
		    }
		});
	}
	

	public void setupRender(Node mainFrameNode) {
		this.mainFrameNode = mainFrameNode;
		Dimension dim = null;

		if (render != null) {
			this.removeKeyListener(render);
		}
		
		if (canvas != null) {
			dim = canvas.getSize();
			this.getContentPane().remove(canvas);
			this.getContentPane().validate();
		}

		render = Coordinator.getInstance().createRender(mainFrameNode, renderType);
		canvas = render.getCanvas();
		
		render.setName("MainFrame");
		
		canvas.addKeyListener(render);

		renderDialog = new RenderProperties(this, render);
		renderDialog.setVisible(false);

		MouseListener popupListener = new PopupListener();
		canvas.addMouseListener(popupListener);
		this.getContentPane().add(canvas, BorderLayout.CENTER);
//		this.getContentPane().validate();
		canvas.setMinimumSize(new Dimension(100, 100));
		canvas.validate();
//		canvas.setPreferredSize(new Dimension(1000, 1000));
		this.getContentPane().validate();
		
		if (dim != null) {
			canvas.setSize(dim);
			pack();
		}
		
		System.out.println("Width = " + canvas.getWidth() + " Height = " + canvas.getHeight());
		
	}

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
		menuItem = new JMenuItem("Open", KeyEvent.VK_O);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Opens Model File");
		menuItem.addActionListener(this);
		menu.add(menuItem);

		// Save state
		menuItem = new JMenuItem("Save state", KeyEvent.VK_O);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Saves Model State");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		// Load state
		menuItem = new JMenuItem("Load state...", KeyEvent.VK_O);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Loads Model State");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		fileMenu = menu;
		recentProjectsStart = fileMenu.getMenuComponentCount();

		// Menu "Options"
		menu = new JMenu("Options");
		menu.setMnemonic(KeyEvent.VK_P);
		menu.getAccessibleContext().setAccessibleDescription("Options menu");
		menuBar.add(menu);

		// Observer selection
		menuItem = new JMenuItem("Observer", KeyEvent.VK_V);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
				ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Changes the observer");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		// Random seed selection
		menuItem = new JMenuItem("Random seed", KeyEvent.VK_S);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Sets the random seed");
		menuItem.addActionListener(this);
		menu.add(menuItem);

		// Renderer selection
		menuItem = new JMenuItem("Graphics", KeyEvent.VK_R);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Selects the renderer");
		menuItem.addActionListener(this);
		menu.add(menuItem);

		// Batch Run Parameters
		menuItem = new JMenuItem("Batch Run Parameters", KeyEvent.VK_R);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Sets up the parameters of batch run");
		menuItem.addActionListener(this);
		menu.add(menuItem);

		// Data layer options
		menuItem = new JMenuItem("Data Layer Parameters", KeyEvent.VK_D);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
				ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Sets up the parameters of data layers");
		menuItem.addActionListener(this);
		menu.add(menuItem);

		// Menu "Window"
		windowMenu = menu = new JMenu("Window");
		menu.setMnemonic(KeyEvent.VK_W);
		menu.getAccessibleContext().setAccessibleDescription("Window menu");

/*		menuItem = new JMenuItem("To Front", KeyEvent.VK_R);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Brings window to front");
		menuItem.addActionListener(this);
		menu.add(menuItem);
*/

		// New View
		menuItem = new JMenuItem("New view", KeyEvent.VK_W);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
				ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Creates a new view");
		menuItem.addActionListener(this);
		menu.add(menuItem);

		// Tile Windows
		menuItem = new JMenuItem("Tile Windows", KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
				ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Tiles windows");
		menuItem.addActionListener(this);
		menu.add(menuItem);

		
		menu.addSeparator();
		menuBar.add(menu);

		windowMenuStart = menu.getMenuComponentCount();
		
		/* Help menu */
		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		menu.getAccessibleContext().setAccessibleDescription("Help menu");
		menuBar.add(menu);

		// About Model
		menuItem = new JMenuItem("About Model", KeyEvent.VK_M);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
				ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"About Model");
		menuItem.addActionListener(this);
		menu.add(menuItem);


		// About SPARK
		menuItem = new JMenuItem("About SPARK", KeyEvent.VK_A);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"About SPARK Dialog");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		
		this.setJMenuBar(menuBar);

		// Create the popup menu for render properties
		popup = new JPopupMenu();

		menuItem = new JMenuItem("Properties");
		menuItem.addActionListener(this);
		popup.add(menuItem);

		menuItem = new JMenuItem("Snapshot");
		menuItem.addActionListener(this);
		popup.add(menuItem);
	}

	/**
	 * Reads a configuration file
	 */
	private void readConfigFile() {
		Document doc;

		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			doc = db.parse("spark-config.xml");
		} catch (Exception e) {
			// No configure file or other problems
			return;
		}
		
		NodeList list;
		
		list = doc.getElementsByTagName("render");
		if (list.getLength() > 0) {
			String srender = list.item(0).getTextContent();
			renderType = Integer.parseInt(srender);
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
		PrintStream out = new PrintStream(new File("spark-config.xml"));

		out.println("<config>");

		out.println("\t<render>" + renderType + "</render>");
		
		out.println("\t<recent-models>");
		for (int i = recentProjects.size() - 1; i >= 0; i--) {
			out.print("\t\t<file>");
			out.print(recentProjects.get(i).getPath());
			out.print("</file>");
			out.println();
		}
		out.println("\t</recent-models>");

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

	private int id = 0;
	private HashMap<Integer, JCheckBoxMenuItem> windowMenuItems = new HashMap<Integer, JCheckBoxMenuItem>();


	

	public void clearWindowMenu() {
//		windowMenuItems.clear();
		int n = windowMenu.getMenuComponentCount() - windowMenuStart;
		for (int i = 0; i < n; i++)
			windowMenu.remove(windowMenuStart);
	}
	

	

	

	public void synchronizeWindowMenu(int id, boolean visible) {
		JCheckBoxMenuItem item = windowMenuItems.get(id);
		if (item == null)
			return;

		item.setSelected(visible);
	}

	class PopupListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	private void openModelFile() throws Exception {
		final JFileChooser fc = new JFileChooser(Coordinator.getInstance()
				.getCurrentDir());
		
		fc.setFileFilter(new FileFilter() {

			// Accept all directories and all xml files
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}

				String extension = getExtension(f);
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

		int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			openModel(file);
		}

	}
	
	
	private File openFile(final String extension) throws Exception {
		final JFileChooser fc = new JFileChooser(Coordinator.getInstance()
				.getCurrentDir());

		fc.setFileFilter(new FileFilter() {

			// Accept all directories and all xml files
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}

				String extension = getExtension(f);
				if (extension != null) {
					if (extension.equals(extension))
						return true;
					else
						return false;
				}

				return false;
			}

			// The description of this filter
			public String getDescription() {
				return "*." + extension;
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
	 * Opens a model file
	 * 
	 * @param file
	 */
	private void openModel(File file) throws Exception {
		Coordinator.getInstance().loadModel(file);
		Coordinator.getInstance().startLoadedModel();
		addRecentProject(file);
	}
	
	
	
	
	

	/* Actions handler */
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		try {
			if (src instanceof JMenuItem) {
				JMenuItem item = (JMenuItem) src;
				String name = item.getText();
				String cmd = item.getActionCommand();

				if (cmd.startsWith("project")) {
					int i = Integer.parseInt(cmd.substring("project".length()));
					openModel(recentProjects.get(i));
					return;
				}
				
				if (name.equals("Open")) {
					openModelFile();
					return;
				}
				

				if (name.equals("Random seed")) {
					randomSeedDialog.init();
					randomSeedDialog.setVisible(true);
					return;
				}

				if (name.startsWith("Data Layer")) {
					DataLayersDialog dialog = new DataLayersDialog(this);
					dialog.init(Coordinator.getInstance()
							.getDataLayerStyles());
					dialog.setVisible(true);
					return;
				}
				
				

				if (name.equals("To Front")) {
					toFront();
					return;
				}

				if (name.equals("Properties")) {
					if (renderDialog != null) {
						renderDialog.init();
						renderDialog.setVisible(true);
					}

					return;
				}
				
				if (name.equals("Snapshot")) {
					if (render != null)
						render.takeSnapshot();
				}
			}


			if (src == setupButton) {
				Coordinator.getInstance().startLoadedModel();
			} else if (src == startButton) {
				Coordinator.getInstance().pauseResumeLoadedModel();
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.toString());
		}
	}

	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public void consume(DataRow row) {
		final long tick = row.getTime().getTick();
		final boolean paused = row.getState().isPaused();

		tickLabel.setText(String.valueOf(tick));
		startButton.setText(paused ? "Start" : "Pause");
	}


}

