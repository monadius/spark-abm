package org.spark.gui.cluster;

import java.util.ArrayList;
import java.util.HashMap;

import org.spark.cluster.ClusterCommand;
import org.spark.cluster.ClusterManager;
import org.spark.core.Agent;
import org.spark.core.ExecutionMode;
import org.spark.core.Observer;
import org.spark.core.ObserverFactory;
import org.spark.core.SparkModel;
import org.spark.data.GridCommunicator;
import org.spark.gui.IUpdatableFrame;
import org.spark.gui.UpdatableFrame;
import org.spark.gui.render.AgentStyle;
import org.spark.gui.render.DataLayerStyle;
import org.spark.gui.render.Render;
import org.spark.runtime.internal.SparkModelXMLFactory;
import org.spark.space.GlobalSpace;
import org.spark.utils.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.spinn3r.log5j.Logger;

public class SlaveModelManager {
	private static final Logger logger = Logger.getLogger();
	
	private static SlaveModelManager instance;
	
	public static SlaveModelManager getInstance() {
		return instance;
	}
	
	
	public static void init() {
		if (instance == null)
			instance = new SlaveModelManager();
	}
	
	
//	private SlaveMainFrame mainFrame;
	private ArrayList<UpdatableFrame> frames = new ArrayList<UpdatableFrame>();

	private Document xmlDoc = null;
	private SparkModel model = null;
	

	
	private Class<? extends Agent>[] agentTypes;
	private HashMap<Class<? extends Agent>, String> agentNames = new HashMap<Class<? extends Agent>, String>();
	private HashMap<String, DataLayerStyle> dataLayerStyles = new HashMap<String, DataLayerStyle>();
	private HashMap<String, Node> dataLayerStyleNodes = new HashMap<String, Node>();

	
	public Document getXmlDocument() {
		return xmlDoc;
	}
	

	HashMap<String, DataLayerStyle> getDataLayerStyles() {
		return dataLayerStyles;
	}

	
	
	
	private SlaveModelManager() {
//		mainFrame = new SlaveMainFrame();
//		mainFrame.setSize(450, 600);
//		mainFrame.setVisible(true);
		
        System.out.println("Receiving rank");
		int rank = ClusterManager.getInstance().getComm().rank();
		if (rank >= 1) rank--;
//		mainFrame.setLocation(460 * rank, 10);
		
		try {
			instance = this;
			loadModel();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Creates the observer for the currently loaded model
	 * @param name
	 * @param mode
	 */
	public void CreateObserver(String name, String mode) throws Exception {
		if (model == null)
			return;
		
		if (!name.startsWith("org.spark.core."))
			name = "org.spark.core." + name;
		
		int executionMode = ExecutionMode.parse(mode);
		
		ObserverFactory.create(model, name, executionMode);
	}
	
	
	public Render createRender(Node node) {
		// TODO: let user choose render
//		Render render = new org.spark.gui.render.JOGLRender();
		Render render = new org.spark.gui.render.JavaRender();
		
//		HashMap<String, Render.DataLayerStyle> dataStyles = new HashMap<String, Render.DataLayerStyle>();
		ArrayList<AgentStyle> agentStyles = new ArrayList<AgentStyle>();
		HashMap<String, AgentStyle> agentMap = new HashMap<String, AgentStyle>();

		for (int i = 0; i < agentTypes.length; i++) {
			AgentStyle agentStyle = new AgentStyle(agentTypes[i]);
			agentStyles.add(agentStyle);
			
			String name = agentNames.get(agentTypes[i]);
			if (name != null)
				agentMap.put(name, agentStyle);
		}
		
//		for (int i = 0; i < dataLayerStyles.size(); i++) {
//			Render.DataLayerStyle dataStyle = new Render.DataLayerStyle(dataLayerStyles.get(i));
//			dataStyles.put(dataStyle.name, dataStyle);
//		}
		
		String selectedDataLayer = null;
		
		if (node != null) {
			NodeList nodes = node.getChildNodes();
			NamedNodeMap attributes;
			Node tmp;
			
			for (int i = 0; i < nodes.getLength(); i++) {
				node = nodes.item(i);
				attributes = node.getAttributes();
				if (attributes == null) 
					continue;
				
				String name = (tmp = attributes.getNamedItem("name")) != null ? tmp.getNodeValue() : null;
				
				if (node.getNodeName().equals("datalayerstyle")) {
					String selected = (tmp = attributes.getNamedItem("selected")) != null ? tmp.getNodeValue() : "false";
					
					if (selected.equals("true"))
						selectedDataLayer = name;
				}
				else if (node.getNodeName().equals("agentstyle") && name != null) {
					AgentStyle agentStyle = agentMap.get(name);
					
					if (agentStyle == null) 
						continue;
					
					String visible = (tmp = attributes.getNamedItem("visible")) != null ? tmp.getNodeValue() : "true";
					
					if (visible.equals("false"))
						agentStyle.visible = false;
					
				}
			}
		}
		
		for (int j = 0; j < agentStyles.size(); j++) {
			render.addAgentStyle(agentStyles.get(j));
		}
	
		render.setGlobalDataLayerStyles(dataLayerStyles);
//		for (Render.DataLayerStyle dataStyle : dataStyles.values()) {
//			render.addDataLayerStyle(dataStyle);
//		}
		
		render.setSpace("local space", false);
		render.setDataLayer(selectedDataLayer);

		return render;
	}

	
	
	public void setFrameVisibility(int id, boolean visible) {
		UpdatableFrame.setFrameVisibility(frames, id, visible);
	}
	
	/**
	 * Loads user model and initializes it
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void loadModel() throws Exception {
		ClusterManager.getAutoShutdown().start(50000);
		System.out.println("Slave: " + ClusterManager.getInstance().getComm().rank() + " Loading model...");

		try {
			// First, get xml document file
			xmlDoc = ClusterManager.getInstance().receiveModelDescription();
			
			model = SparkModelXMLFactory.loadModel(xmlDoc, null);

			// Then, get all model related data
			ClusterManager.getInstance().waitInitCommands(model);

			
			NodeList nodes;

			/* Load agents */

			nodes = xmlDoc.getElementsByTagName("agent");
			agentTypes = new Class[nodes.getLength()];

			for (int i = 0; i < nodes.getLength(); i++) {
				NamedNodeMap attributes = nodes.item(i).getAttributes();
				Node tmp;
				
				String name = (tmp = attributes.getNamedItem("name")) != null ? tmp.getNodeValue() : null;
				
				String className = nodes.item(i).getTextContent();
//				if (classLoader != null) {
//					agentTypes[i] = (Class<? extends Agent>) classLoader.loadClass(className);
//				} 
//				else {
					agentTypes[i] = (Class<? extends Agent>) Class.forName(className);
//				}

				agentNames.put(agentTypes[i], name);
			}

			/* Load data layers */

			nodes = xmlDoc.getElementsByTagName("datalayer");

			for (int i = 0; i < nodes.getLength(); i++) {
				loadDataLayer(nodes.item(i));
			}

			/* Setup main frame render and start up model */
			nodes = xmlDoc.getElementsByTagName("mainframe");
			
			CreateObserver("Observer1", "serial");
			
//			mainFrame.setupRender(nodes.item(0));
			setupModel();

		} catch (Exception e) {
			xmlDoc = null;
			e.printStackTrace();
			throw e;
		}
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
	
//	private volatile boolean modelIsInitialized = false;
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
		logger.debug("SetupModel");
		
		paused = false;
		updateRequested = true;
//		modelIsInitialized = true;
//		delayed = true;
		delayTime = 30;
//		synchFlag = false;


//		mainFrame.reset();
		for (UpdatableFrame frame : frames) {
			frame.reset();
		}

		modelThread = new Thread(modelRun);
		modelThread.start();
//		System.err.println(modelThread.toString());
	}
	
	
	public void requestUpdate() {
		updateRequested = true;
	}

	
	class ModelRun implements Runnable {
		private void updateData(long tick) {
			for (UpdatableFrame frame : frames) {
				frame.updateData(tick);
			}

//			mainFrame.updateData(tick);
		}

		private void updateData() {
			for (UpdatableFrame frame : frames) {
				frame.updateData();
			}

//			mainFrame.updateData();
		}

		public void run() {
			logger.debug("run()");
			
			ClusterManager.getAutoShutdown().reset();
			ClusterManager.getAutoShutdown().start(50000);
	
			synchFlag = false;
			
			for (int k = 0; k < runNumber; k++) {
				for (long tick = 0; tick < maxTicks; tick++) {
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

			
					ClusterManager.getAutoShutdown().reset();
					if (tick == 1) {
						ClusterManager.getAutoShutdown().start(10000);
					}
					
					// TODO: receive commands from master here
					// FIXME: pause should be processed correctly or it will
					// result in a deadlock

					int rank = ClusterManager.getInstance().getComm().rank();
/*
					try {
						if (rank > 0) {
							ObjectBuf<Boolean> boolBuffer = ObjectBuf.buffer();
							ClusterManager.getInstance().getComm().broadcast(0, ClusterManager.CMD_DATA, boolBuffer);
							// TRUE means end the work
							if (boolBuffer.get(0).booleanValue()) {
								break;
							}

							ClusterManager.getInstance().getComm().barrier();
						}
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
*/
					if (rank > 0) {
						try {
							ClusterCommand.broadcastAndExecute(null);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					
					if (tick % 100 == 0) {
						System.out.println("Tick: " + tick);
//						logger.info("call GC");
//						System.gc();
					}
					
					/* Main process */
					logger.debug("model.begin");
					model.begin(tick);
					
					logger.debug("processAllAgents");
					Observer.getInstance().processAllAgents(tick);
					logger.debug("processAllDataLayers");
					Observer.getInstance().processAllDataLayers(tick);
					
					logger.debug("model.end");
					model.end(tick);

					// TODO: put it in the right place
					GridCommunicator gridCommunicator = ClusterManager.getInstance().getGridCommunicator();
					if (gridCommunicator != null) {
						logger.debug("Tick: %d; Rank: %d; calling gridCommunicator.synchronizeBorders", tick, rank);
						gridCommunicator.synchronizeBorders();
					}
					
					GlobalSpace globalSpace = ClusterManager.getInstance().getGlobalSpace();
					if (globalSpace != null) {
						logger.debug("Tick: %d; Rank: %d; calling globalSpace.sendReceiveAgents()", tick, rank);
						globalSpace.sendReceiveAgents();

						logger.debug("Tick: %d; Rank: %d; calling globalSpace.synchronizeBorders()", tick, rank);
						globalSpace.synchronizeBorders();
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
				
				for (IUpdatableFrame frame : frames) {
					frame.reset();
				}
				
				System.out.println("Finalizing MPI...");
				ClusterManager.getInstance().getComm().finalizeMPI();
				System.out.println("Exit");
				System.exit(0);
				
//				mainFrame.reset();
			}
			
			modelThread = null;
		}
		
	}

}
