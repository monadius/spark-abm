package org.spark.gui.cluster;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.*;

import org.spark.cluster.ClusterCommand;
import org.spark.cluster.ClusterManager;
import org.spark.core.Agent;
import org.spark.core.ExecutionMode;
import org.spark.core.Observer;
import org.spark.core.ObserverFactory;
import org.spark.gui.FrameLocationManager;
import org.spark.gui.GUIModelManager;
import org.spark.gui.RenderFrame;
import org.spark.gui.UpdatableFrame;
import org.spark.gui.render.AgentStyle;
import org.spark.gui.render.DataLayerStyle;
import org.spark.gui.render.Render;
import org.spark.modelfile.ModelFileLoader;
import org.spark.runtime.internal.SparkModelXMLFactory;
import org.spark.utils.Vector;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class MasterModelManager extends GUIModelManager {
	
	
	public static void init() {
		new MasterModelManager();
	}
	
	private ClassLoader classLoader = null;
	private File currentDir;
	
	private Node mainFrameNode;
	private Document xmlDoc = null;
	private File	 xmlDocFile = null;
	private MasterMainFrame mainFrame;
	
	
	private Class<? extends Agent>[] agentTypes;
	private HashMap<Class<? extends Agent>, String> agentNames = new HashMap<Class<? extends Agent>, String>();
	private HashMap<String, DataLayerStyle> dataLayerStyles = new HashMap<String, DataLayerStyle>();
	private HashMap<String, Node> dataLayerStyleNodes = new HashMap<String, Node>();

	
	public File getCurrentDirectory() {
		if (currentDir != null)
			return currentDir;
		else
			return new File(".");
	}
	
	
	public void setCurrentDirectory(File dir) {
		currentDir = dir;
	}
	
	
//	public Document getXmlDocument() {
//		return xmlDoc;
//	}
	
	
	public HashMap<String, DataLayerStyle> getDataLayerStyles() {
		return dataLayerStyles;
	}
	
	
	private MasterModelManager() {
		// TODO: remove everywhere
		ClusterManager.getAutoShutdown().start(50000);

		mainFrame = new MasterMainFrame();
		super.mainFrame = mainFrame;
		mainFrame.setSize(500, 600);
		mainFrame.setVisible(true);
		
		model = null;
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
	
	
	public Render createRender(Node node, int renderType) {
		// TODO: let user choose render
//		Render render = new org.spark.gui.render.JOGLRender();
		System.err.println("No JOGL is used");
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
		
		render.setSpace("space", false);
		render.setDataLayer(selectedDataLayer);

		return render;
	}
	
	
	public void showAllFrames() {
		for (UpdatableFrame frame : frames) {
			mainFrame.addUpdatableFrame(frame, true);
		}
	}
	
	
	public void setFrameVisibility(int id, boolean visible) {
		UpdatableFrame.setFrameVisibility(frames, id, visible);
	}
	
	
	public void hideFrame(int id) {
		mainFrame.synchronizeWindowMenu(id, false);
	}
	
	
	private void setupClassPath(Node node) {
		classLoader = null;
		
		NamedNodeMap attributes = node.getAttributes();
		Node tmp;

		String path = (tmp = attributes.getNamedItem("path")) != null ? tmp.getNodeValue() : null;
		
		try {
			if (path != null) {
				File dir = xmlDocFile.getParentFile();
				String path2 = dir.getAbsolutePath().concat(path);
				
				URI uri = new File(path2).toURI();
				classLoader = new URLClassLoader(new URL[] {uri.toURL()} );
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			classLoader = null;
		}
		
	}
	
	
	@SuppressWarnings({ "unchecked" })
	public void loadModel(final File modelFile) throws Exception {
		try {
			unloadModel();

			NodeList nodes;
			UpdatableFrame frame;

			xmlDoc = ModelFileLoader.loadModelFile(modelFile);
			xmlDocFile = modelFile;
			
			model = SparkModelXMLFactory.loadModel(xmlDoc, modelFile.getParentFile());

			/* Load model */
			
			nodes = xmlDoc.getElementsByTagName("classpath");
			if (nodes.getLength() >= 1) {
				setupClassPath(nodes.item(0));
			}

/*			nodes = xmlDoc.getElementsByTagName("setup");
			if (nodes.getLength() != 1)
				throw new Exception(
						"The setup class must be uniquely specified");

			String setupName = nodes.item(0).getTextContent();
			if (classLoader != null) {
				model = (SparkModel) classLoader.loadClass(setupName).newInstance();
			}
			else {
				model = (SparkModel) Class.forName(setupName).newInstance();
			}
*/
			/* Load agents */

			nodes = xmlDoc.getElementsByTagName("agent");
			agentTypes = new Class[nodes.getLength()];

			for (int i = 0; i < nodes.getLength(); i++) {
				NamedNodeMap attributes = nodes.item(i).getAttributes();
				Node tmp;
				
				String name = (tmp = attributes.getNamedItem("name")) != null ? tmp.getNodeValue() : null;
				
				String className = nodes.item(i).getTextContent();
				if (classLoader != null) {
					agentTypes[i] = (Class<? extends Agent>) classLoader.loadClass(className);
				} 
				else {
					agentTypes[i] = (Class<? extends Agent>) Class.forName(className);
				}

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
				frame = new RenderFrame(nodes.item(i), mainFrame, Render.JAVA_2D_RENDER);
				frames.add(frame);
			}


			/* Main Frame setup */
			nodes = xmlDoc.getElementsByTagName("mainframe");
			if (nodes.getLength() >= 1) {
				mainFrameNode = nodes.item(0);
				FrameLocationManager.setLocation(mainFrame, mainFrameNode);
			}
			else {
				mainFrameNode = null;
			}

			CreateObserver("Observer1", "serial");
			
			// Setup render
			mainFrame.setupRender(mainFrameNode);
			// Setup model
			setupModel();
			// Setup slaves
			initSlaves();

		} catch (Exception e) {
			model = null;
			xmlDoc = null;
			e.printStackTrace();
			throw e;
		}

		showAllFrames();
				
	}
	
	
	
	/**
	 * Sends initialization commands to slaves
	 */
	private void initSlaves() throws Exception {
		ClusterManager.getInstance().sendModelDescription(xmlDoc);

		if (ClusterManager.getInstance().getComm().size() > 1) {
			ClusterManager.getInstance().sendInitCommands(model);
		} else {
			throw new Exception("Impossible to run simulation on a single machine");
		}

	}
	

	/**
	 * Stops simulation and unloads a model
	 */
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

		mainFrame.clearWindowMenu();
		
		for (JDialog frame : frames) {
			frame.setVisible(false);
			frame.dispose();
		}
		
		frames.clear();

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
	
	ModelRun modelRun = new ModelRun();

	
	class ModelRun implements Runnable {
		private void updateData(long tick) {
			for (UpdatableFrame frame : frames) {
				frame.updateData(tick);
			}

			mainFrame.updateData(tick);
		}

		private void updateData() {
			for (UpdatableFrame frame : frames) {
				frame.updateData();
			}

			mainFrame.updateData();
		}

		public void run() {
			ClusterManager.getAutoShutdown().start(10000);
			
			try {
			for (int k = 0; k < 1; k++) {
				for (long tick = 0; tick < Long.MAX_VALUE; tick++) {
					// if (!modelIsInitialized) return;

					if (synchFlag) {
						synchronized (lock) {
							updateData(tick);
							if (modelThread == null) {
								ClusterCommand.broadcastAndExecute(new ClusterCommand(ClusterCommand.EXIT_IMMEDIATELY));
								return;
							}
							try {
								lock.wait();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else {
						updateData(tick);
						if (modelThread == null) {
							ClusterCommand.broadcastAndExecute(new ClusterCommand(ClusterCommand.EXIT_IMMEDIATELY));
							return;
						}
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
					
					/* Main process */
					ClusterCommand cmd = new ClusterCommand();
					if (tick % 10 == 0)
						cmd.addAction(ClusterCommand.GET_AGENTS_DATA);
					cmd.addAction(ClusterCommand.BARRIER);
					try {
						ClusterCommand.broadcastAndExecute(cmd);
					}
					catch (Exception e) {
						e.printStackTrace();
						break;
					}

					if (delayTime > 0) {
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

				mainFrame.reset();
			}
			
			modelThread = null;
			}
			catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		
	}


	@Override
	protected Runnable createModelRunClass() {
		return modelRun;
	}


	@Override
	public Document getXmlDocument() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void saveDataLayerStyles() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void saveGUIChanges() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
