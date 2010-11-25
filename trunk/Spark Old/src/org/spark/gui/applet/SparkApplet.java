package org.spark.gui.applet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.net.URI;
import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;


public class SparkApplet extends JApplet {
	private static final long serialVersionUID = 1L;
	
	
	private static SparkApplet instance;
	
	private JPanel mainPanel;
	private JPanel parameterPanel;
	private JPanel methodPanel;
	private JPanel chartPanel;
	private JPanel viewPanel;
	
	
	static SparkApplet getInstance() {
		return instance;
	}
	
	static JPanel getMainPanel() {
		if (instance != null)
			return instance.mainPanel;
		else
			return null;
	}

	
	static JPanel getParameterPanel() {
		if (instance != null)
			return instance.parameterPanel;
		else
			return null;
	}

	
	
	static JPanel getMethodPanel() {
		if (instance != null)
			return instance.methodPanel;
		else
			return null;
	}

	
	static JPanel getChartPanel() {
		if (instance != null)
			return instance.chartPanel;
		else
			return null;
	}
	
	
	static JPanel getViewPanel() {
		if (instance != null)
			return instance.viewPanel;
		else
			return null;
	}

	
	private void initPanels() {
		javax.swing.ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		mainPanel = new JPanel(new BorderLayout());
		parameterPanel = new JPanel();
		methodPanel = new JPanel();
		chartPanel = new JPanel(new GridLayout(0, 2));
		viewPanel = new JPanel(new GridLayout(0, 2));
		
		mainPanel.setMinimumSize(new Dimension(420, 420));
		mainPanel.setPreferredSize(new Dimension(420, 420));

		parameterPanel.setMinimumSize(new Dimension(350, 100));
		parameterPanel.setPreferredSize(new Dimension(400, 100));

		methodPanel.setMinimumSize(new Dimension(50, 200));
		methodPanel.setPreferredSize(new Dimension(60, 300));

		chartPanel.setMinimumSize(new Dimension(300, 300));
		chartPanel.setPreferredSize(new Dimension(300, 400));
		
		viewPanel.setMinimumSize(new Dimension(100, 100));
		viewPanel.setPreferredSize(new Dimension(500, 300));

		
		JScrollPane parameterScroll = new JScrollPane(parameterPanel);
		parameterScroll.setMinimumSize(new Dimension(350, 100));

		JSplitPane splitParsMethods = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, parameterScroll, methodPanel);
		JSplitPane splitChartPars = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitParsMethods, chartPanel);
		
		JSplitPane splitter2a = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainPanel, viewPanel);
        JSplitPane splitter1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitter2a, splitChartPars);

        this.add(splitter1, BorderLayout.CENTER);
        this.validate();

	}
	
	
	/**
	 * Applet initialization. Called before any other applet's method.
	 * This method initializes user interface and core SPARK objects.
	 */
	public void init() {
		try {
			System.out.println("init");

			// If the applet was already initialized
			// then we only need to create the user interface again
			// since a new top level panel was created for this applet
			if (instance != null) {
				instance = this;
				initPanels();
				AppletModelManager.init();
				return;
			}
		
			instance = this;
			initPanels();
			AppletModelManager.init();
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * This method is called after initialization
	 */
	public void start() {
		System.out.println("start");

		try {
			String model = getParameter("model");
			URI uri = getCodeBase().toURI();
			uri = new URI(uri.toString().concat(model));
			System.out.println(uri.toString());
			AppletModelManager.getInstance().loadModel(uri.toURL().openStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Stops the applet but does not unload it
	 */
	public void stop() {
		System.out.println("stop");
		try {
			AppletModelManager.getInstance().unloadModel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Unloads applet
	 */
	public void destroy() {
		try {
			AppletModelManager.getInstance().unloadModel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	

}
