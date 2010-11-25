package org.spark.gui.cluster;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.spark.gui.GUIModelManager;
import org.spark.gui.IUpdatableFrame;
import org.spark.gui.UpdatableFrame;
import org.spark.gui.dialogs.DataLayerProperties;
import org.spark.gui.dialogs.RenderProperties;
import org.spark.gui.render.Render;
import org.w3c.dom.Document;
import org.w3c.dom.Node;



public class MasterMainFrame extends JFrame implements IUpdatableFrame, ActionListener, ChangeListener {

	private static final long serialVersionUID = 2800524835363109821L;

	private JButton startButton, setupButton;
	private JCheckBox synchButton;
	private JLabel	tickLabel;
	private JPopupMenu popup;
	private JMenu windowMenu;

	private Canvas canvas;
	private Render render;
	private RenderProperties renderDialog;
	


	
	static {
		javax.swing.ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	}

	
	public MasterMainFrame() {
		super("Cluster Main Frame");
		
//		this.canvas = new GLCanvas();
		this.render = null;

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
//				System.exit(0);
				
				try {
					MasterModelManager.getInstance().unloadModel();
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
		tickLabel = new JLabel("");
		tickLabel.setMinimumSize(new Dimension(100, 80));
		
		synchButton.addActionListener(this);
		startButton.addActionListener(this);
		setupButton.addActionListener(this);
		
		JSlider framesPerSecond = new JSlider(JSlider.HORIZONTAL,
                0, 300, 0);
		framesPerSecond.addChangeListener(this);

		framesPerSecond.setMajorTickSpacing(30);
		framesPerSecond.setMinorTickSpacing(10);
		framesPerSecond.setPaintTicks(true);
		framesPerSecond.setPaintLabels(true);
		
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put( new Integer( 0 ), new JLabel("Fast") );
		labelTable.put( new Integer( 300 ), new JLabel("Slow") );
		framesPerSecond.setLabelTable( labelTable );
		
		panel.add(framesPerSecond);
		panel.add(synchButton);
		panel.add(setupButton);
		panel.add(startButton);
		panel.add(tickLabel);
		
		this.getContentPane().add(panel, BorderLayout.NORTH);
		this.pack();

		setupMenu();
		
		renderDialog = null;
	}
	
	
	void setupRender(Node node) {
		if (canvas != null) {
			this.getContentPane().remove(canvas);
		}
		
		render = MasterModelManager.getInstance().createRender(node, Render.JAVA_2D_RENDER);
		canvas = render.getCanvas();

		renderDialog = new RenderProperties(this, render);
		renderDialog.setVisible(false);
		
	    MouseListener popupListener = new PopupListener();
	    canvas.addMouseListener(popupListener);
	    this.getContentPane().add(canvas, BorderLayout.CENTER);
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
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Opens Model File");
		menuItem.addActionListener(this);
		menu.add(menuItem);

		// Menu "Options"
		menu = new JMenu("Options");
		menu.setMnemonic(KeyEvent.VK_P);
		menu.getAccessibleContext().setAccessibleDescription("Options menu");
		menuBar.add(menu);


		// Data layer options 
		menuItem = new JMenuItem("Data Layer Parameters", KeyEvent.VK_D);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Sets up the parameters of data layers");
		menuItem.addActionListener(this);
		menu.add(menuItem);	

		
		// Menu "Window"
		windowMenu = menu = new JMenu("Window");
		menu.setMnemonic(KeyEvent.VK_W);
		menu.getAccessibleContext().setAccessibleDescription("Window menu");
		menuBar.add(menu);

		
		
		this.setJMenuBar(menuBar);
		
	    // Create the popup menu for render properties
	    popup = new JPopupMenu();
	    menuItem = new JMenuItem("Properties");
	    menuItem.addActionListener(this);
	    popup.add(menuItem);
	}
	
	
	private int id = 0;
	private HashMap<Integer, JCheckBoxMenuItem> windowMenuItems = new HashMap<Integer, JCheckBoxMenuItem>();
	
	public void addUpdatableFrame(UpdatableFrame frame, boolean visible) {
		frame.setId(id);
		frame.setVisible(visible);

		JCheckBoxMenuItem item = new JCheckBoxMenuItem(frame.getTitle(), visible);
		item.setActionCommand("window" + id);
		item.addActionListener(this);
		windowMenu.add(item);
		windowMenuItems.put(id, item);
		
		id += 1;
	}
	
	
	public void clearWindowMenu() {
		windowMenuItems.clear();
		windowMenu.removeAll();
	}
	
	
	public void synchronizeWindowMenu(int id, boolean visible) {
		JCheckBoxMenuItem item = windowMenuItems.get(id);
		if (item == null) return;

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
	            popup.show(e.getComponent(),
	                       e.getX(), e.getY());
	        }
	    }
	}

	
	


	
	public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
	}

	private void openModelFile() throws Exception {
		final JFileChooser fc = new JFileChooser(MasterModelManager.getInstance().getCurrentDirectory());
		
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
            java.io.File file = fc.getSelectedFile();
            MasterModelManager.getInstance().loadModel(file);
    		MasterModelManager.getInstance().setCurrentDirectory(fc.getCurrentDirectory());
		}
		
	}
	

	/* Actions handler */
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		
		try {
			if (src instanceof JMenuItem) {
				JMenuItem item = (JMenuItem) src;
				String name = item.getText();

				if (name.equals("Open")) {
					openModelFile();
					return;
				}
				
				if (name.startsWith("Data Layer")) {
					DataLayerProperties dialog = new DataLayerProperties(this);
					dialog.init(GUIModelManager.getInstance().getDataLayerStyles());
					dialog.setVisible(true);
					return;
				}

				if (name.equals("Properties")) {
					if (renderDialog != null) {
						renderDialog.init();
						renderDialog.setVisible(true);
					}
					
					return;
				}
			}
			
			if (src instanceof JCheckBoxMenuItem) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem) src;
				String command = item.getActionCommand();
				
				if (command.startsWith("window")) {
					int id = Integer.parseInt(command.substring("window".length()));
					MasterModelManager.getInstance().setFrameVisibility(id, item.isSelected());
				}
				
				return;
			}

			if (src == setupButton) {
				GUIModelManager.getInstance().setupModel();
			} else if (src == startButton) {
				boolean result = GUIModelManager.getInstance().pauseResumeModel();
				if (result)
					startButton.setText("Start");
				else
					startButton.setText("Pause");
			} else if (src == synchButton) {
				synchronized(GUIModelManager.lock) {
					MasterModelManager.getInstance().synchFlag = synchButton.isSelected();
					MasterModelManager.lock.notify();
				}
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.toString());
		}
	}


//	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
//	    if (!source.getValueIsAdjusting()) {
			MasterModelManager.getInstance()
				.changeSimulationSpeed((int) source.getValue());
//	    }		
	}
	
	
	public void updateTick(long tick) {
		tickLabel.setText(Long.toString(tick));
	}


	public void reset() {
		boolean paused = MasterModelManager.getInstance().isModelPaused();
		if (paused)
			startButton.setText("Start");
		else
			startButton.setText("Pause");
	}


	public void writeXML(Document doc) {
	}

	
	private boolean invoked = false;

	public void updateData(final long tick) {
		if (!invoked && canvas != null) {
			
			invoked = true;
			
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					if (canvas != null)
						render.display();

					tickLabel.setText(String.valueOf(tick));
					
					synchronized(GUIModelManager.lock) {
						invoked = false;
						MasterModelManager.lock.notify();
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

