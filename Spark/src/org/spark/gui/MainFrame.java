package org.spark.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
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

import org.spark.core.ExecutionMode;
import org.spark.core.ObserverFactory;
import org.spark.gui.dialogs.AboutDialog;
import org.spark.gui.dialogs.BatchRunDialog;
import org.spark.gui.dialogs.DataLayerProperties;
import org.spark.gui.dialogs.RandomSeedDialog;
import org.spark.gui.dialogs.RenderProperties;
import org.spark.gui.dialogs.RenderSelectionDialog;
import org.spark.gui.render.Render;
import org.spark.runtime.BatchRunController;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MainFrame extends JFrame implements IUpdatableFrame,
		ActionListener, ChangeListener {

	private static final long serialVersionUID = 2800524835363109821L;

	private JButton startButton, setupButton;
	private JButton saveButton;
	private JCheckBox synchButton;
	private JLabel tickLabel;
	private JPopupMenu popup;
	private JMenu windowMenu;

	private AboutDialog aboutDialog;
	private BatchRunDialog batchRunDialog;
	private RandomSeedDialog randomSeedDialog;
	
	private Canvas canvas;
	private Render render;
	private RenderProperties renderDialog;
	private final RenderSelectionDialog renderSelection;
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

	public MainFrame() {
		super("Main Frame");

		// this.canvas = new GLCanvas();
		this.render = null;

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// System.exit(0);

				try {
					GUIModelManager.getInstance().unloadModel();
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
		renderSelection = new RenderSelectionDialog(this, renderType);
		
		aboutDialog = new AboutDialog(this);
		batchRunDialog = new BatchRunDialog(this);
		randomSeedDialog = new RandomSeedDialog(this);
		
		this.addWindowFocusListener(new WindowAdapter() {
		    public void windowGainedFocus(WindowEvent e) {
		    	if (canvas != null) {
		    		canvas.requestFocusInWindow();
		    	}
		    }
		});
	}
	

	void setupRender(Node mainFrameNode) {
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

		render = GUIModelManager.getInstance().createRender(mainFrameNode, renderType);
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

	public void addUpdatableFrame(UpdatableFrame frame, boolean visible) {
		frame.setId(id);
		frame.setVisible(visible);

		JCheckBoxMenuItem item = new JCheckBoxMenuItem(frame.getTitle(),
				visible);
		item.setActionCommand("window" + id);
		item.addActionListener(this);
		windowMenu.add(item);
		windowMenuItems.put(id, item);

		id += 1;
	}
	
	
	/**
	 * Removes a frame from the 'window' menu
	 * @param frame
	 */
	public void removeUpdatableFrame(UpdatableFrame frame) {
		int id = frame.id;
		
		JMenuItem menuItem = windowMenuItems.get(id);
		if (menuItem == null)
			return;
		
		windowMenu.remove(menuItem);
		windowMenuItems.remove(id);
	}
	

	public void clearWindowMenu() {
//		windowMenuItems.clear();
		int n = windowMenu.getMenuComponentCount() - windowMenuStart;
		for (int i = 0; i < n; i++)
			windowMenu.remove(windowMenuStart);
	}
	
	
	public void initDialogs() {
		batchRunDialog.init();
	}
	
	
	public void updateWindowMenu(UpdatableFrame frame) {
		int id = frame.id;
		
		JMenuItem menuItem = windowMenuItems.get(id);
		if (menuItem == null)
			return;
		
		menuItem.setText(frame.getTitle());
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
		final JFileChooser fc = new JFileChooser(GUIModelManager.getInstance()
				.getCurrentDirectory());

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

			GUIModelManager.getInstance().setCurrentDirectory(
					fc.getCurrentDirectory());
		}

	}
	
	
	private File openFile(final String extension) throws Exception {
		final JFileChooser fc = new JFileChooser(GUIModelManager.getInstance()
				.getCurrentDirectory());

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
		GUIModelManager.getInstance().loadModel(file);
		GUIModelManager.getInstance().setCurrentDirectory(
				file.getParentFile());
		addRecentProject(file);
	}
	
	
	/**
	 * Tiles all visible frames
	 * @throws Exception
	 */
	private void tileFrames() throws Exception {
		ArrayList<Window> charts = new ArrayList<Window>();
		ArrayList<Window> views = new ArrayList<Window>();
		ArrayList<Window> others = new ArrayList<Window>();
		Window parameters = null;
		
		for (UpdatableFrame frame : GUIModelManager.getInstance().frames) {
			if (!frame.isVisible())
				continue;
			
			if (frame instanceof ParameterPanel)
				parameters = frame;
			else if (frame instanceof ChartFrame || frame instanceof HistogramFrame)
				charts.add(frame);
			else if (frame instanceof RenderFrame)
				views.add(frame);
			else
				others.add(frame);
		}
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		
		int mainLength = dim.width / 3 + 50;
		int mainHeight = mainLength + 100;
		int parametersLength = dim.width * 5 / 12;
		int parametersHeight = mainHeight / 2;
		int othersLength = dim.width - mainLength - parametersLength;
		
		// MainFrame
		this.setSize(mainLength, mainHeight);
		this.setLocation(0, 0);
		
		// Parameters frame
		if (parameters != null) {
			Dimension parDim = parameters.getPreferredSize();
			parametersHeight = parDim.height;
			if (parametersHeight > (mainHeight * 2) / 3)
				parametersHeight = (mainHeight * 2) / 3;
			
			parameters.setSize(parametersLength, parametersHeight);
			parameters.setLocation(mainLength, 0);
		}
		
		// Other frames
		FrameLocationManager.tileFrames(others, parametersHeight, 1, 
				dim.width - othersLength, 0, othersLength, parametersHeight);
		
		// Chart frames
		int x = mainLength;
		int y = parameters != null ? parametersHeight : 0;
		int w = parameters != null ? dim.width - x : parametersLength;
		int h = dim.height - y;
		
		FrameLocationManager.tileFrames(charts, 300, 1.5, x, y, w, h);
		
		// View frames
		x = 0;
		y = mainHeight;
		w = mainLength;
		h = dim.height - y;
		
		FrameLocationManager.tileFrames(views, mainLength, 1, x, y, w, h);
	}
	
	
	/**
	 * Shows the dialog with the information about the current model
	 */
	private void showAboutModelDialog() {
		JDialog dialog = new TextPanel(GUIModelManager.getInstance().getAboutFile());
		dialog.setVisible(true);
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
				
				if (name.equals("Observer")) {
					String selection = (String) JOptionPane.showInputDialog(this, 
							"Select observer", "Select observer", 
							JOptionPane.DEFAULT_OPTION, 
							null,
							ObserverFactory.getObserversList(), ObserverFactory.getObserversList()[0]);
					
					if (selection != null) {
						GUIModelManager instance = GUIModelManager.getInstance();

						instance.stopModel();
						instance.CreateObserver(selection, ExecutionMode.SERIAL_MODE);
						instance.setupModel();
					}
		
					return;
				}

				if (name.startsWith("Graphics")) {
					renderSelection.init(renderType);
					renderSelection.setVisible(true);
					return;
				}
				
				if (name.equals("Open")) {
					openModelFile();
					return;
				}
				
				// Save model state
				if (name.equals("Save state")) {
					GUIModelManager instance = GUIModelManager.getInstance();
					if (instance != null)
						instance.saveModelState("state.dat");
					
					return;
				}
				
				// Load model state
				if (name.startsWith("Load state")) {
					GUIModelManager instance = GUIModelManager.getInstance();
					if (instance != null) {
						File file = openFile("dat");
						if (file == null)
							return;
						
						instance.loadModelState(file);
					}

					return;
				}

				if (name.startsWith("Batch")) {
					batchRunDialog.setVisible(true);
					
					BatchRunController ctrl = batchRunDialog.getBatchRunController();
					if (ctrl != null) {
						GUIModelManager.getInstance().setBatchRunController(ctrl);
						GUIModelManager.getInstance().setupModel();
						GUIModelManager.getInstance().pauseResumeModel();
					}
					
					return;
				}
				
				if (name.equals("Random seed")) {
					randomSeedDialog.init();
					randomSeedDialog.setVisible(true);
					return;
				}

				if (name.startsWith("Data Layer")) {
					DataLayerProperties dialog = new DataLayerProperties(this);
					dialog.init(GUIModelManager.getInstance()
							.getDataLayerStyles());
					dialog.setVisible(true);
					return;
				}
				
				if (name.equals("About SPARK")) {
					aboutDialog.setVisible(true);
					return;
				}
				
				
				if (name.equals("About Model")) {
					showAboutModelDialog();
					return;
				}
				

				if (name.equals("New view")) {
					if (ModelManager.getModelClass() == null)
						return;
					
					// Create a new xml node for a new render frame
					Document doc = ModelManager.getInstance().getXmlDocument();
					Node node = null;
					if (doc != null) {
						node = doc.createElement("renderframe");
						doc.getFirstChild().appendChild(node);
					}
					
					RenderFrame frame = ModelManager.getInstance()
							.createRenderFrame(node);
					frame.setSize(300, 300);
					addUpdatableFrame(frame, true);
					return;
				}
				
				if (name.equals("Tile Windows")) {
					tileFrames();
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

			if (src instanceof JCheckBoxMenuItem) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem) src;
				String command = item.getActionCommand();

				if (command.startsWith("window")) {
					int id = Integer.parseInt(command.substring("window"
							.length()));
					GUIModelManager.getInstance().setFrameVisibility(id,
							item.isSelected());
				}

				return;
			}

			if (src == setupButton) {
				GUIModelManager.getInstance().setupModel();
			} else if (src == startButton) {
				boolean result = GUIModelManager.getInstance()
						.pauseResumeModel();
				if (result)
					startButton.setText("Start");
				else
					startButton.setText("Pause");
			} else if (src == synchButton) {
				synchronized (GUIModelManager.lock) {
					GUIModelManager.getInstance().synchFlag = synchButton
							.isSelected();
					GUIModelManager.lock.notify();
				}
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.toString());
		}
	}

	// @Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		int n = source.getValue();
		if (n < 0 || n >= delaySize.length)
			n = 8;

		int delay = delaySize[n];
		GUIModelManager.getInstance().changeSimulationSpeed(delay);
	}

	public void updateTick(long tick) {
		tickLabel.setText(Long.toString(tick));
	}

	public void reset() {
		boolean paused = GUIModelManager.getInstance().isModelPaused();
		if (paused)
			startButton.setText("Start");
		else
			startButton.setText("Pause");
	}


	/**
	 * Writes out interface information into an xml file
	 */
	public void writeXML(Document doc) {
		FrameLocationManager.saveLocationChanges(doc, this, mainFrameNode);
		
		if (render != null)
			render.writeXML(doc, mainFrameNode);
	}

	
	private boolean invoked = false;

	public void updateData(final long tick) {
		if (!invoked && canvas != null) {

			invoked = true;

			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					if (canvas != null) {
						render.setTick(tick);
						render.display();
					}

					tickLabel.setText(String.valueOf(tick));

					synchronized (GUIModelManager.lock) {
						invoked = false;
						GUIModelManager.lock.notify();
					}
				}
			});
		}
	}

	public void updateData() {
		if (!invoked && canvas != null) {

			invoked = true;

			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					if (canvas != null)
						render.display();

					invoked = false;
				}
			});
		}
	}

}
