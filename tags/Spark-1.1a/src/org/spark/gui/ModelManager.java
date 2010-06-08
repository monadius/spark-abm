package org.spark.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.spark.core.Agent;
import org.spark.core.Observer;
import org.spark.gui.render.AgentStyle;
import org.spark.gui.render.DataLayerStyle;
import org.spark.gui.render.Render;
import org.spark.modelfile.ModelFileLoader;
import org.spark.runtime.ParameterFactory_Old;
import org.spark.runtime.VariableSetFactory;
import org.spark.runtime.internal.SparkModelXMLFactory;
import org.spark.utils.Vector;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.spinn3r.log5j.Logger;

public class ModelManager extends GUIModelManager {
	private static final Logger logger = Logger.getLogger();

	public static void init() {
		new ModelManager();
	}
	
	private File currentDir;

	private Node mainFrameNode;
	private Document xmlDoc = null;
	private File xmlDocFile = null;
	private MainFrame mainFrame;

	private Class<? extends Agent>[] agentTypes;
	private HashMap<Class<? extends Agent>, String> agentNames = new HashMap<Class<? extends Agent>, String>();
	private HashMap<String, DataLayerStyle> dataLayerStyles = new HashMap<String, DataLayerStyle>();
	private HashMap<String, Node> dataLayerStyleNodes = new HashMap<String, Node>();
	
//	private RationalNumber tickTime;
	

	public File getCurrentDirectory() {
		if (currentDir != null)
			return currentDir;
		else
			return new File(".");
	}

	public void setCurrentDirectory(File dir) {
		currentDir = dir;
	}

	public Document getXmlDocument() {
		return xmlDoc;
	}
	
	public File getXmlDocumentFile() {
		return xmlDocFile;
	}

	public HashMap<String, DataLayerStyle> getDataLayerStyles() {
		return dataLayerStyles;
	}

	private ModelManager() {
		mainFrame = new MainFrame();
		super.mainFrame = mainFrame;
		mainFrame.setSize(500, 600);
		mainFrame.setVisible(true);

		model = null;
	}
	
	@Override
	public String getRelativePath(File file) {
		if (xmlDocFile == null || file == null)
			return super.getRelativePath(file);
		
		File parent = xmlDocFile.getParentFile().getAbsoluteFile();
		if (!file.isAbsolute())
			file = new File(parent, file.getPath());
		
		String path = file.getName();
		
		for (File f = file.getParentFile(); f != null; f = f.getParentFile()) {
			if (f.equals(parent))
				return path;
			
			path = f.getName() + "/" + path;
		}
		
		return file.getAbsolutePath();
	}

	@Override
	public void removeUpdatableFrame(UpdatableFrame frame) {
		mainFrame.removeUpdatableFrame(frame);
		super.removeUpdatableFrame(frame);
	}
	
	
	@Override
	public void updateUpdatableFrame(UpdatableFrame frame) {
		mainFrame.updateWindowMenu(frame);
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
			// TODO: verify what is happening for agents without name 
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
		boolean swapXYflag = false;

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

					String swapXY = (tmp = attributes
							.getNamedItem("swapXY")) != null ? tmp
							.getNodeValue() : "false";
							
					if (selected.equals("true"))
						selectedSpace = name;
					
					if (swapXY.equals("true"))
						swapXYflag = true;
					
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

					agentStyle.load(node, xmlDocFile.getParentFile());
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

		render.setSpace(selectedSpace, swapXYflag);
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
	
	
	/**
	 * Returns the path derived from the 'path' attribute of the node
	 * and the path to the model xml file
	 * @param node
	 * @return
	 */
	private File getPath(Node node) {
		if (xmlDocFile == null || node == null)
			return null;
		
		NamedNodeMap attributes = node.getAttributes();
		Node tmp;

		String path = (tmp = attributes.getNamedItem("path")) != null ? tmp
				.getNodeValue() : null;

		if (path != null) {
			File dir = xmlDocFile.getParentFile();
			File path2 = new File(dir, path);
			return path2;
		}
		
		return null;
	}
	

	
	/**
	 * Returns a list of children with the specified name
	 * @param name
	 * @return
	 */
	private ArrayList<Node> getChildrenByTagName(Node node, String name) {
		ArrayList<Node> list = new ArrayList<Node>();

		for (Node child = node.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			if (child.getNodeName().equals(name))
				list.add(child);
		}

		return list;
	}
	
	
	/**
	 * Loads definition of all agents
	 * @param node
	 */
	@SuppressWarnings("unchecked")
	private void loadAgents(Node node) throws Exception {
		ClassLoader classLoader = model.getClass().getClassLoader();
		
		NodeList list = xmlDoc.getElementsByTagName("agent");
		agentTypes = new Class[list.getLength()];

		for (int i = 0; i < list.getLength(); i++) {
			NamedNodeMap attributes = list.item(i).getAttributes();
			Node tmp;

			// Agent's name
			String name = (tmp = attributes.getNamedItem("name")) != null ? tmp
					.getNodeValue()
					: null;

			// Agent's class
			String className = list.item(i).getTextContent().trim();
			if (classLoader != null) {
				agentTypes[i] = (Class<? extends Agent>) classLoader
						.loadClass(className);
			} else {
				agentTypes[i] = (Class<? extends Agent>) Class
						.forName(className);
			}

			// Add agent's type to the table
			agentNames.put(agentTypes[i], name);
		}
		
	}
	
	
	/**
	 * Loads a model
	 */
	public void loadModel(final File modelFile) throws Exception {
		try {
			unloadModel();

			ArrayList<Node> nodes;
			NodeList list;
			UpdatableFrame frame;

			xmlDoc = ModelFileLoader.loadModelFile(modelFile);
			xmlDocFile = modelFile;
			
			model = SparkModelXMLFactory.loadModel(xmlDoc, modelFile.getParentFile());
			
			xmlDoc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(modelFile);
			
			final Node root = xmlDoc.getFirstChild();
			/* Load tick time */
/*			NamedNodeMap attributes = root.getAttributes();
			Node tmp = attributes.getNamedItem("tick");
			if (tmp != null) {
				tickTime = RationalNumber.parse(tmp.getNodeValue());
			}
			else {
				tickTime = RationalNumber.ONE;
			}
	*/		
/*			nodes = getChildrenByTagName(root, "setup");
			if (nodes.size() != 1)
				throw new Exception(
						"The setup class must be uniquely specified");

			String setupName = nodes.get(0).getTextContent().trim();
			if (classLoader != null) {
				model = (SparkModel) classLoader.loadClass(setupName)
						.newInstance();
			} else {
				model = (SparkModel) Class.forName(setupName).newInstance();
			}
			
			tmp = nodes.get(0);
			attributes = tmp.getAttributes();
			
			String observerName = ((tmp = attributes.getNamedItem("observer")) != null) ?
					tmp.getNodeValue() : "Observer1";
			String executionModeName = ((tmp = attributes.getNamedItem("mode")) != null) ?
					tmp.getNodeValue() : "serial";
*/			
			/* Load model description */

			nodes = getChildrenByTagName(root, "about");
			if (nodes.size() > 0) {
				aboutFile = getPath(nodes.get(0)); 
			}
			else {
				aboutFile = null;
			}

			/* Load agents (for rendering) */

			loadAgents(null);
			
			/* Load variables */
/*			nodes = getChildrenByTagName(root, "variables");
			if (nodes.size() > 0)
				nodes = getChildrenByTagName(nodes.get(0), "variable");

			if (nodes.size() == 0) {
				// Possibly we have an old SPARK xml model file
				convertOldModelFile(xmlDoc);
				nodes = getChildrenByTagName(root, "variables");
				if (nodes.size() > 0)
					nodes = getChildrenByTagName(nodes.get(0), "variable");
			}
			
			for (int i = 0; i < nodes.size(); i++) {
				ModelVariable.createVariable(model, nodes.get(i));
			}
*/
			/* Load data layers */

			list = xmlDoc.getElementsByTagName("datalayer");

			for (int i = 0; i < list.getLength(); i++) {
				loadDataLayer(list.item(i));
			}

			/* Load renders */
			list = xmlDoc.getElementsByTagName("renderframe");

			for (int i = 0; i < list.getLength(); i++) {
				createRenderFrame(list.item(i));
			}

			/* Load parameters and variable sets */
			ParameterFactory_Old.clear();
			
			list = xmlDoc.getElementsByTagName("parameterframe");
			if (list.getLength() >= 1) {
				ParameterFactory_Old.loadParameters(model, list.item(0));
			}

			
			VariableSetFactory.clear();
			
			list = xmlDoc.getElementsByTagName("variable-sets");
			if (list.getLength() >= 1) {
				VariableSetFactory.loadVariableSets(model, list.item(0));
			}

			
			/* Load parameter panel */
			list = xmlDoc.getElementsByTagName("parameterframe");
			if (list.getLength() >= 1) {
				// TODO: only one parameter frame should be specified
//				frame = new ParameterFrame(nodes.item(0), mainFrame);
//				frames.add(frame);
				
				frame = new ParameterPanel(list.item(0), mainFrame);
				frames.add(frame);
			}
			
			
			

			/* Load methods */
//			nodes = xmlDoc.getElementsByTagName("invokeframe");
			list = xmlDoc.getElementsByTagName("methods");
			if (list.getLength() >= 1) {
				frame = new InvokeFrame(model, list.item(0), mainFrame);
				frames.add(frame);
			}

			/* Load charts */
			list = xmlDoc.getElementsByTagName("chart");
			for (int i = 0; i < list.getLength(); i++) {
				frame = new ChartFrame(list.item(i), mainFrame);
				frames.add(frame);
			}

			/* Load histogram */
			list = xmlDoc.getElementsByTagName("histogram");
			for (int i = 0; i < list.getLength(); i++) {
				frame = new HistogramFrame(list.item(i), mainFrame);
				frames.add(frame);
			}

			/* Load datasets */
			list = xmlDoc.getElementsByTagName("dataset");
			for (int i = 0; i < list.getLength(); i++) {
				frame = new DatasetFrame(list.item(i), mainFrame);
				frames.add(frame);
			}

			/* Main Frame setup */
			list = xmlDoc.getElementsByTagName("mainframe");
			if (list.getLength() >= 1) {
				mainFrameNode = list.item(0);
				FrameLocationManager.setLocation(mainFrame, mainFrameNode);
			} else {
				mainFrameNode = null;
			}

			// TODO: setup cluster here
			// Think about arguments
			// org.spark.core.ClusterManager.init(args);

			// Create the model observer
			CreateObserver(model.getDefaultObserverName(), model.getDefaultExecutionMode());
			
			mainFrame.setupRender(mainFrameNode);
			setupModel();
			mainFrame.initDialogs();

//			if (Observer.getDefaultSpace() instanceof BoundedSpace3d) {
				// FIXME: very dangerous, need to be synchronized
				// with updates (otherwise canvas could be nullified
				// before update)
	//			mainFrame.setupRender(mainFrameNode);
		//	}

		} catch (Exception e) {
			model = null;
			xmlDoc = null;
			e.printStackTrace();
			throw e;
		}

		showAllFrames();

	}

	public void unloadModel() throws Exception {
		if (model == null)
			return;

		try {
			stopModel();
//			Observer.getInstance().clear();

			
			
			
						saveGUIChanges();
		
		
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		tickTime = null;

//		model.clearVariables();
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
		String sval1 = (tmp = attributes.getNamedItem("val1")) != null ? tmp
				.getNodeValue() : "0";
		String sval2 = (tmp = attributes.getNamedItem("val2")) != null ? tmp
				.getNodeValue() : "10";
		String scolor1 = (tmp = attributes.getNamedItem("color1")) != null ? tmp
				.getNodeValue()
				: "0;0;0";
		String scolor2 = (tmp = attributes.getNamedItem("color2")) != null ? tmp
				.getNodeValue()
				: "1;0;0";

		try {
			double val1 = Double.parseDouble(sval1);
			double val2 = Double.parseDouble(sval2);
			Vector color1 = Vector.parseVector(scolor1);
			Vector color2 = Vector.parseVector(scolor2);

			dataLayerStyles.put(name, new DataLayerStyle(name, val1, val2,
					color1, color2));
			dataLayerStyleNodes.put(name, node);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveGUIChanges() throws Exception {
		if (xmlDoc == null || xmlDocFile == null)
			return;

		saveDataLayerStyles();

		for (UpdatableFrame frame : frames) {
			frame.writeXML(xmlDoc);
		}

		// FrameLocationManager.saveLocationChanges(mainFrame, mainFrameNode);
		mainFrame.writeXML(xmlDoc);

		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();

		DOMSource source = new DOMSource(xmlDoc);
		StreamResult result = new StreamResult(xmlDocFile);
		transformer.transform(source, result);
	}

	public void saveDataLayerStyles() {
		for (String name : dataLayerStyles.keySet()) {
			DataLayerStyle style = dataLayerStyles.get(name);
			Node node = dataLayerStyleNodes.get(name);

			NamedNodeMap attributes = node.getAttributes();
			Document doc = getXmlDocument();

			Node tmp = doc.createAttribute("val1");
			tmp.setNodeValue(Double.toString(style.val1));
			attributes.setNamedItem(tmp);

			tmp = doc.createAttribute("color1");
			tmp.setNodeValue(style.color1.toString());
			attributes.setNamedItem(tmp);

			tmp = doc.createAttribute("val2");
			tmp.setNodeValue(Double.toString(style.val2));
			attributes.setNamedItem(tmp);

			tmp = doc.createAttribute("color2");
			tmp.setNodeValue(style.color2.toString());
			attributes.setNamedItem(tmp);
		}
	}

	/* Model execution routines */

	class ModelRun implements Runnable {
		private static final int EXTERNAL_STOP = 1;
		private static final int INTERNAL_STOP = 2;
		
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

		private void updateDataExceptViews(long tick) {
			for (UpdatableFrame frame : frames) {
				if (frame instanceof RenderFrame)
					continue;
				frame.updateData(tick);
			}
		}
		
		private void serializeState(long tick) {
			if (stateFile == null)
				return;
			
			// TODO: find better solution
			//File tempStateFile = new File(stateFile.getAbsolutePath() + (tick-1) + ".dat");
	
			saveStateFlag = false;
			try {
				FileOutputStream out = new FileOutputStream(stateFile);
				ObjectOutputStream oos = new ObjectOutputStream(out);
				oos.writeLong(tick);
				oos.flush();

				Observer.getInstance().serializeState(model, out);
				out.close();
			}
			catch (Exception e) {
				logger.error("Serialization error");
				logger.error(e);
			}
		}
		
		
		/**
		 * Main cycle function
		 * @return true if the simulation is stopped
		 */
		private int main(long tick) {
			while (paused) {
				try {
					Thread.sleep(10);
					if (modelThread == null)
						return EXTERNAL_STOP;
					
					if (updateRequested) {
						updateRequested = false;
						updateData();
					}
					
					if (saveStateFlag) {
						serializeState(tick);
					}
					
					if (model.synchronizeMethods())
						updateData();
					// TODO: is it a good idea to put synchronization here?
					// The problem can be with variables for which
					// values are computed (the number of agents).
					model.synchronizeVariables();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			/* Synchronize variables and methods
			   before each simulation step */
			model.synchronizeMethods();
			model.synchronizeVariables();
			
			// Save state
			if (saveStateFlag) {
				serializeState(tick);
			}
			
			
			/* Main process */
			long t = Observer.getInstance().getSimulationTick();
			
			if (model.begin(t)) {
				Observer.getInstance().advanceSimulationTick();
//				pauseResumeModel();
//				mainFrame.reset();
				return INTERNAL_STOP;
			}
//			Observer.getInstance().processAllAgents(tick);
			Observer.getInstance().processAllAgents(model.getTickTime());
			Observer.getInstance().processAllDataLayers(t);
			
			if (model.end(t)) {
				Observer.getInstance().advanceSimulationTick();
				batchRunController.updateBatchController(tick);
//				pauseResumeModel();
//				mainFrame.reset();
				return INTERNAL_STOP;
			}
			
			// Main step ends here
			Observer.getInstance().advanceSimulationTick();

			
			// TODO: is it the correct place for this function?
			batchRunController.updateBatchController(tick);
			

			if (delayTime > 0) {
				if (modelThread == null)
					return EXTERNAL_STOP;
				try {
					Thread.sleep(delayTime);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}

			// Update all frames
			if (delayTime >= 0
					|| (delayTime < 0 && tick % delayTime == 0)) {
				if (synchFlag) {
					synchronized (lock) {
						updateData(tick);
						if (modelThread == null) {
							return EXTERNAL_STOP;
						}
						try {
							lock.wait();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					updateData(tick);
					if (modelThread == null)
						return EXTERNAL_STOP;
				}
			} else {
				updateDataExceptViews(tick);
				if (modelThread == null)
					return EXTERNAL_STOP;
			}
			
			return 0;
		}
		
		
		/**
		 * Main simulation controller
		 */
		public void run() {
			logger.debug("ModelThread = " + modelThread);
//			logger.info("runNumber = " + runNumber);
//			logger.info("maxTicks = " + maxTicks);

			// synchFlag = false;

			long maxTicks = batchRunController.initialize(); 

			while (maxTicks > 0) {
				long tick = initTick;
				initTick = 0;
				
				ticks:
				for (; tick < maxTicks; tick++) {
					// if (modelThread == null) return;
					int state = main(tick);
					
					switch (state) {
					case EXTERNAL_STOP:
						return;
						
					case INTERNAL_STOP:
						break ticks;
					}
				}
				

				maxTicks = batchRunController.nextStep();
				if (maxTicks == 0)
					break;


				for (IUpdatableFrame frame : frames) {
					frame.reset();
				}

				mainFrame.reset();
			}

			modelThread = null;
		}

	}

	private ModelRun modelRun = new ModelRun();

	@Override
	protected Runnable createModelRunClass() {
		return modelRun;
	}

}
