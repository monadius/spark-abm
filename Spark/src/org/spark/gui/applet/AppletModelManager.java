package org.spark.gui.applet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.spark.core.Agent;
import org.spark.core.Observer;
import org.spark.core.ObserverFactory;
import org.spark.core.SparkModel;
import org.spark.gui.render.AgentStyle;
import org.spark.gui.render.DataLayerStyle;
import org.spark.gui.render.Render;
import org.spark.utils.Vector;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class AppletModelManager {
	private static AppletModelManager instance;
	
	public static AppletModelManager getInstance() {
		return instance;
	}
	
	public static SparkModel getModelClass() {
		return instance.model;
	}
	
	public static SparkModel getModel() {
		return instance.model;
	}
	
	public static void init() {
		instance = new AppletModelManager();
	}
	

	private MainPanel mainPanel;
	private ArrayList<IUpdatablePanel> panels = new ArrayList<IUpdatablePanel>();
	private Document xmlDoc = null;
	
	
	private SparkModel model;
	private Class<? extends Agent>[] agentTypes;
	private HashMap<Class<? extends Agent>, String> agentNames = new HashMap<Class<? extends Agent>, String>();
	private HashMap<String, DataLayerStyle> dataLayerStyles = new HashMap<String, DataLayerStyle>();
	private HashMap<String, Node> dataLayerStyleNodes = new HashMap<String, Node>();

	
	HashMap<String, DataLayerStyle> getDataLayerStyles() {
		return dataLayerStyles;
	}
	
	
	private AppletModelManager() {
		mainPanel = new MainPanel(SparkApplet.getMainPanel());
		model = null;
	}
	
	

	public Render createRender(Node node, int renderType) {
		// TODO: let user choose renderer
		Render render = null;
		if (renderType == Render.JOGL_RENDER) {
			try {
//				if (Observer.getDefaultSpace() instanceof BoundedSpace3d)
//					render = new org.spark.gui.render.JOGLRender3d();
//				else
					render = new org.spark.gui.render.JOGLRender();
			} catch (Exception e) {
				e.printStackTrace();
				render = new org.spark.gui.render.JavaRender();
			}
		} else {
			render = new org.spark.gui.render.JavaRender();
		}

		// HashMap<String, Render.DataLayerStyle> dataStyles = new
		// HashMap<String, Render.DataLayerStyle>();
		ArrayList<AgentStyle> agentStyles = new ArrayList<AgentStyle>();
		HashMap<String, AgentStyle> agentMap = new HashMap<String, AgentStyle>();

		for (int i = 0; i < agentTypes.length; i++) {
			AgentStyle agentStyle = new AgentStyle(agentTypes[i]);
			agentStyles.add(agentStyle);

			String name = agentNames.get(agentTypes[i]);
			if (name != null)
				agentMap.put(name, agentStyle);

			agentStyle.name = name;
		}

		// for (int i = 0; i < dataLayerStyles.size(); i++) {
		// Render.DataLayerStyle dataStyle = new
		// Render.DataLayerStyle(dataLayerStyles.get(i));
		// dataStyles.put(dataStyle.name, dataStyle);
		// }

		String selectedDataLayer = null;
		String selectedSpace = null;

		if (node != null) {
			NodeList nodes = node.getChildNodes();
			NamedNodeMap attributes;
			Node tmp;

			for (int i = 0; i < nodes.getLength(); i++) {
				node = nodes.item(i);
				attributes = node.getAttributes();
				if (attributes == null)
					continue;

				String name = (tmp = attributes.getNamedItem("name")) != null ? tmp
						.getNodeValue()
						: null;

				if (node.getNodeName().equals("spacestyle")) {
					String selected = (tmp = attributes
							.getNamedItem("selected")) != null ? tmp
							.getNodeValue() : "false";

					if (selected.equals("true"))
						selectedSpace = name;
				} else if (node.getNodeName().equals("datalayerstyle")) {
					String selected = (tmp = attributes
							.getNamedItem("selected")) != null ? tmp
							.getNodeValue() : "false";

					if (selected.equals("true"))
						selectedDataLayer = name;
				} else if (node.getNodeName().equals("agentstyle")
						&& name != null) {
					AgentStyle agentStyle = agentMap.get(name);

					if (agentStyle == null)
						continue;
					
					agentStyle.load(node, null);

				}
			}
		}

		Collections.sort(agentStyles);

		for (int j = 0; j < agentStyles.size(); j++) {
			render.addAgentStyle(agentStyles.get(j));
		}

		render.setGlobalDataLayerStyles(dataLayerStyles);

		if (selectedSpace == null) {
			selectedSpace = Observer.getInstance().getSpaceName(
					Observer.getDefaultSpace());
			
			// TODO: it is just a way around: find better solution
			if (selectedSpace == null)
				selectedSpace = "space";
		}

		render.setSpace(selectedSpace, false);
		render.setDataLayer(selectedDataLayer);

		return render;
	}
	
	
	
	@SuppressWarnings({ "unchecked" })
	public void loadModel(final java.io.InputStream is) throws Exception {
		try {
			unloadModel();

			NodeList nodes;
			IUpdatablePanel panel;

			javax.xml.parsers.DocumentBuilder db = javax.xml.parsers.DocumentBuilderFactory
					.newInstance().newDocumentBuilder();
			xmlDoc = db.parse(is);

			/* Load model */

			nodes = xmlDoc.getElementsByTagName("setup");
			if (nodes.getLength() != 1)
				throw new Exception(
						"The setup class must be uniquely specified");

			String setupName = nodes.item(0).getTextContent();
			model = (SparkModel) Class.forName(setupName).newInstance();

			/* Load agents */

			nodes = xmlDoc.getElementsByTagName("agent");
			agentTypes = new Class[nodes.getLength()];

			for (int i = 0; i < nodes.getLength(); i++) {
				NamedNodeMap attributes = nodes.item(i).getAttributes();
				Node tmp;
				
				String name = (tmp = attributes.getNamedItem("name")) != null ? tmp.getNodeValue() : null;
				
				String className = nodes.item(i).getTextContent();
				agentTypes[i] = (Class<? extends Agent>) Class.forName(className);

				agentNames.put(agentTypes[i], name);
			}

			/* Load data layers */

			nodes = xmlDoc.getElementsByTagName("datalayer");

			for (int i = 0; i < nodes.getLength(); i++) {
				loadDataLayer(nodes.item(i));
			}
			
			/* Load renders */
			nodes = xmlDoc.getElementsByTagName("renderframe");

			for (int i = 0; i < nodes.getLength(); i++) {
				panel = new ViewPanel(nodes.item(i), Render.JOGL_RENDER);
				panels.add(panel);
			}

			/* Load parameters */
			nodes = xmlDoc.getElementsByTagName("parameterframe");
			if (nodes.getLength() >= 1) {
				// TODO: only one parameter frame should be specified
				panel = new ParameterPanel(nodes.item(0));
				panels.add(panel);
			}

			/* Load methods */
			nodes = xmlDoc.getElementsByTagName("methods");
			if (nodes.getLength() >= 1) {
				panel = new MethodPanel(nodes.item(0));
				panels.add(panel);
			}

			/* Load charts */
			nodes = xmlDoc.getElementsByTagName("chart");
			for (int i = 0; i < nodes.getLength(); i++) {
				panel = new ChartPanel(nodes.item(i));
				panels.add(panel);
			}
			
			nodes = xmlDoc.getElementsByTagName("mainframe");
			mainPanel.setupRender(nodes.item(0));
			
			ObserverFactory.create(model, "org.spark.core.Observer1", 0);
			setupModel();

		} catch (Exception e) {
			model = null;
			xmlDoc = null;
			e.printStackTrace();
			throw e;
		}
	}
	
	
	public void unloadModel() throws Exception {
		if (model == null) return;
		
		try {
			stopModel();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		

		dataLayerStyles.clear();
		dataLayerStyleNodes.clear();
		agentTypes = null;
		agentNames.clear();

		panels.clear();

		model = null;
		xmlDoc = null;
	}
	
	
	
	private void loadDataLayer(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		Node tmp;
		
		String name = attributes.getNamedItem("name").getNodeValue();
		String sval1 = (tmp = attributes.getNamedItem("val1")) != null ? tmp.getNodeValue() : "0";
		String sval2 = (tmp = attributes.getNamedItem("val2")) != null ? tmp.getNodeValue() : "10";
		String scolor1 = (tmp = attributes.getNamedItem("color1")) != null ? tmp.getNodeValue() : "0;0;0";
		String scolor2 = (tmp = attributes.getNamedItem("color2")) != null ? tmp.getNodeValue() : "1;0;0";

		try {
			double val1 = Double.parseDouble(sval1);
			double val2 = Double.parseDouble(sval2);
			Vector color1 = Vector.parseVector(scolor1);
			Vector color2 = Vector.parseVector(scolor2);
			
			dataLayerStyles.put(name, new DataLayerStyle(name, val1, val2, color1, color2));
			dataLayerStyleNodes.put(name, node);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/* Model execution routines */
	
	private volatile boolean updateRequested = false;
	private volatile boolean paused = false;
	private volatile boolean delayed = false;
	private volatile int delayTime = 0;
	private volatile Thread modelThread = null;
	
	private volatile long maxTicks = 1000000000;
	private volatile int runNumber = 1;

	ModelRun modelRun = new ModelRun();
	static Object lock = new Object();
	volatile boolean synchFlag = true;

	
	protected void setupModel() {
		if (model == null) return;
		stopModel();
		
		// Setup is processed in serial mode always
		Observer.getInstance().beginSetup();
		model.setup();
		Observer.getInstance().finalizeSetup();

		paused = true;
		updateRequested = true;
//		modelIsInitialized = true;

		mainPanel.reset();
		for (IUpdatablePanel panel : panels) {
			panel.reset();
		}

		modelThread = new Thread(modelRun);
		modelThread.start();
//		System.err.println(modelThread.toString());
	}
	
	
	private void stopModel() {
		if (modelThread != null) {
			Thread t = modelThread;
//			modelIsInitialized = false;
			
			try {
				modelThread = null;

				synchronized(lock) {
					lock.notify();
				}
				
				t.join(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			finally {
				if (t.isAlive())
					throw new Error("Model thread has not been stopped");
				modelThread = null;
			}
		}
		
//		Observer.getInstance().clear();
	}

	
	protected boolean isModelPaused() {
		return paused;
	}
	
	
	protected boolean pauseResumeModel() {
		if (modelThread == null)
			return true;
		return paused = !paused;
	}
	
	

	protected void changeSimulationSpeed(int delayTime) {
		this.delayTime = delayTime;
		
        if (delayTime == 0) {
        	delayed = false;
        } else {
        	delayed = true;
        }
	}
	
	
	public long getMaxTicks() {
		return maxTicks;
	}
	
	
	public int getRunNumber() {
		return runNumber;
	}
	
	
	public void requestUpdate() {
		updateRequested = true;
	}

	
	class ModelRun implements Runnable {
		private void updateData(long tick) {
			for (IUpdatablePanel panel : panels) {
				panel.updateData(tick);
			}

			mainPanel.updateData(tick);
		}

		private void updateData() {
			for (IUpdatablePanel panel : panels) {
				panel.updateData();
			}

			mainPanel.updateData();
		}

		public void run() {
			for (int k = 0; k < runNumber; k++) {
				for (long tick = 0; tick < maxTicks; tick++) {
					// if (!modelIsInitialized) return;

					if (synchFlag) {
						synchronized (lock) {
							updateData(tick);
							if (modelThread == null)
								return;
							try {
								lock.wait();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else {
						updateData(tick);
						if (modelThread == null)
							return;
					}
					// if (modelThread == null) return;

					while (paused) {
						try {
							Thread.sleep(10);
							if (modelThread == null)
								return;
							if (updateRequested) {
								updateData();
								updateRequested = false;
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					/* Main process */
					if (model.begin(tick)) {
						pauseResumeModel();
						mainPanel.reset();
						continue;
					}
					Observer.getInstance().processAllAgents(tick);
					if (model.end(tick)) {
						pauseResumeModel();
						mainPanel.reset();
					}

					// TODO: remove
					// if (tick % 5 == 0) render.readFrameBuffer();

					if (delayed) {
						if (modelThread == null)
							return;
						try {
							Thread.sleep(delayTime);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}

				}
			
				// TODO: issues with this thread
				Observer.getInstance().reset();
				if (model == null)
					return;
					
				// Setup is processed in serial mode always
				Observer.getInstance().beginSetup();
				model.setup();
				Observer.getInstance().finalizeSetup();
				
				for (IUpdatablePanel panel : panels) {
					panel.reset();
				}
				
				mainPanel.reset();
			}
			
			modelThread = null;
		}
		
	}

}
