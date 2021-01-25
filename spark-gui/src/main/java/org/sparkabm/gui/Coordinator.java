package org.sparkabm.gui;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.PropertyConfigurator;

import org.sparkabm.gui.data.DataFilter;
import org.sparkabm.gui.data.DataReceiver;
import org.sparkabm.gui.data.DataSetTmp;
import org.sparkabm.gui.gui.*;
import org.sparkabm.gui.gui.menu.StandardMenu;
import org.sparkabm.gui.renderer.DataLayerStyle;
import org.sparkabm.gui.renderer.Renderer;
import org.sparkabm.gui.renderer.font.FontManager;
import org.sparkabm.modelfile.ModelFileLoader;
import org.sparkabm.runtime.commands.*;
import org.sparkabm.runtime.data.DataCollectorDescription;
import org.sparkabm.runtime.data.DataObject;
import org.sparkabm.runtime.data.DataObject_State;
import org.sparkabm.gui.gui.menu.SparkMenu;
import org.sparkabm.runtime.internal.manager.IModelManager;
import org.sparkabm.runtime.internal.manager.ModelManager_Basic;
import org.sparkabm.utils.FileUtils;
import org.sparkabm.utils.XmlDocUtils;

import static org.sparkabm.utils.XmlDocUtils.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * Central external class which coordinates interactions between a model manager
 * and a data receiver
 *
 * @author Monad
 */
public class Coordinator {
    /* Logger */
    private static final Logger logger = Logger.getLogger(Coordinator.class.getName());

    /* A single instance of the class */
    private static Coordinator coordinator;

    /**************** Two main components ********************/
    /* Main model manager */
    private IModelManager modelManager;
    /* Main data receiver */
    private DataReceiver receiver;

    /**************** Files **********************************/
    /* Current directory */
    private File currentDir;

    /* Stack of output directories */
    private final Stack<File> outputDir;

    /* Loaded model xml file */
    private File modelXmlFile;
    /* Loaded model xml document */
    private Document modelXmlDoc;

    /*************** Collections *****************************/
    /* Collection of proxy variables */
    private ProxyVariableCollection variables;
    /* Collection of parameters in a loaded model */
    private ParameterCollection parameters;
    /* Collection of external methods */
    private MethodCollection methods;

    /* Styles of all data layers */
    private final HashMap<String, DataLayerStyle> dataLayerStyles;
    private final HashMap<String, Node> dataLayerStyleNodes;
    /* Type names and names of agents */
    private final HashMap<String, String> agentTypesAndNames;

    /********************* Model properties ********************/
    /* Random generator properties (for the next run) */
    private long randomSeed;
    private boolean useTimeSeed = true;

    /* Observer parameters (for the next run) */
    private String observerName = null;
    private String executionMode = "serial";

    /* Initial delay time */
    private int delayTime;

    /* Initial frequency */
    private int frequency;

    /* Data set */
    private DataSetTmp dataSet;


    /*************** Configuration *****************/
    private final Configuration configuration;


    /*************** GUI ******************/
    /* If true, then no GUI elements are created */
    private final boolean noGUI;

    /* Bitmap font manager */
    private final FontManager fontManager;

    /* Main window manager */
    private final WindowManager windowManager;

    /* Control panel */
    private SparkControlPanel controlPanel;

    /* List of all active renders */
    private final ArrayList<Renderer> renderers;

    /**
     * Private constructor
     *
     * @param manager
     * @param receiver
     */
    private Coordinator(IModelManager manager, DataReceiver receiver, boolean noGUI) {
        this.modelManager = manager;
        this.receiver = receiver;
        this.currentDir = new File(".");
        this.outputDir = new Stack<File>();

        this.dataLayerStyles = new HashMap<String, DataLayerStyle>();
        this.dataLayerStyleNodes = new HashMap<String, Node>();
        this.agentTypesAndNames = new HashMap<String, String>();

        this.noGUI = noGUI;
        this.renderers = new ArrayList<Renderer>();
        this.fontManager = new FontManager();

        if (noGUI) {
            this.windowManager = null;
            this.configuration = new Configuration(null);
        } else {
            this.windowManager = new Swing_WindowManager();
            SparkMenu mainMenu = StandardMenu.create(windowManager);
            windowManager.setMainMenu(mainMenu);

            this.configuration = new Configuration(mainMenu.getSubMenu("File"));
        }
    }


    /**
     * Initialization
     */
    private void init() {
        if (!noGUI) {
            // Initilize GUI
            SparkWindow mainWindow = windowManager.getMainWindow();
            mainWindow.setName("SPARK");

            controlPanel = new SparkControlPanel();
            mainWindow.addPanel(controlPanel, BorderLayout.NORTH);
        }

        // Load config file
        configuration.readConfigFile();
        configuration.loadFontManager(fontManager);
    }

    /**
     * Creates a coordinator
     *
     * @param manager
     * @param receiver
     * @param noGUI
     */
    public static void init(IModelManager manager, DataReceiver receiver, boolean noGUI) {
        if (coordinator != null) {
            logger.severe("Coordinator is already created");
            throw new Error("Illegal operation");
        }

        coordinator = new Coordinator(manager, receiver, noGUI);
        coordinator.init();
    }


    /**
     * Creates a coordinator with GUI
     *
     * @param manager
     * @param receiver
     */
    public static void init(IModelManager manager, DataReceiver receiver) {
        init(manager, receiver, false);
    }


    /**
     * Disposes the coordinator: saves configuration changes, etc.
     */
    public static void dispose() {
        try {
            if (coordinator != null) {
                // TODO: wait until the simulation is completely stopped
                coordinator.unloadModel();
                coordinator.configuration.saveFontManager(coordinator.fontManager);
                coordinator.configuration.saveConfigFile();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception", e);
        }
    }


    /**
     * Returns the instance of the class
     *
     * @return
     */
    public static Coordinator getInstance() {
        return coordinator;
    }


    /**
     * Returns the configuration
     *
     * @return
     */
    public Configuration getConfiguration() {
        return configuration;
    }


    /**
     * Returns the font manager
     */
    public FontManager getFontManager() {
        return fontManager;
    }


    /**
     * Returns the window manager
     *
     * @return
     */
    public WindowManager getWindowManager() {
        return windowManager;
    }


    /**
     * Returns the data receiver
     *
     * @return
     */
    public DataReceiver getDataReceiver() {
        return receiver;
    }


    /**
     * Sends a command to the model manager
     *
     * @param cmd
     */
    public void sendCommand(ModelManagerCommand cmd) {
        modelManager.sendCommand(cmd);
    }


    /**
     * Returns true if a model is loaded
     *
     * @return
     */
    public synchronized boolean isModelLoaded() {
        return modelXmlFile != null;
    }


    /**
     * Returns the most recently received data object of the given type
     *
     * @param type
     * @param name
     * @return
     */
    public DataObject getMostRecentData(int type, String name) {
        return receiver.getMostRecentData(type, name);
    }


    /**
     * Returns the initial state of the current simulation
     *
     * @return
     */
    public DataObject_State getInitialState() {
        return receiver.getInitialState();
    }


    /**
     * Returns the current data set
     *
     * @return
     */
    public DataSetTmp getDataSet() {
        return dataSet;
    }


    /**
     * Initial properties of random generator for the next simulation
     *
     * @return
     */
    public synchronized long getRandomSeed() {
        if (receiver.getInitialState() != null)
            return receiver.getInitialState().getSeed();

        return randomSeed;
    }

    public synchronized boolean getTimeSeedFlag() {
        return useTimeSeed;
    }

    public synchronized void setRandomSeed(long randomSeed, boolean useTimeSeed) {
        this.randomSeed = randomSeed;
        this.useTimeSeed = useTimeSeed;
    }


    /**
     * Returns visual styles for data layers
     *
     * @return
     */
    public HashMap<String, DataLayerStyle> getDataLayerStyles() {
        return dataLayerStyles;
    }


    /**
     * Returns types and names of agents in the loaded model
     *
     * @return
     */
    public HashMap<String, String> getAgentTypesAndNames() {
        return agentTypesAndNames;
    }


    /**
     * Sets parameters of the observer (for the next run)
     *
     * @param observerName
     * @param executionMode
     */
    public synchronized void setObserver(String observerName, String executionMode) {
        this.observerName = observerName;
        this.executionMode = executionMode;
    }

    public synchronized String getObserverName() {
        return observerName;
    }

    public synchronized String getExecutionMode() {
        return executionMode;
    }

    /**
     * Sends a command for changing value of the given variable
     *
     * @param varName
     * @param newValue
     */
    public void changeVariable(String varName, Object newValue) {
        modelManager
                .sendCommand(new Command_SetVariableValue(varName, newValue));
    }


    /**
     * Invokes the given external model method
     *
     * @param methodName
     */
    public void invokeMethod(String methodName) {
        modelManager.sendCommand(new Command_InvokeMethod(methodName));
    }


    /**
     * Adds a data collector
     *
     * @param dcd
     */
    public void addDataCollector(DataCollectorDescription dcd) {
        modelManager.sendCommand(new Command_AddDataCollector(dcd));
    }

    /**
     * Removes a data collector
     *
     * @param dcd
     */
    public void removeDataCollector(DataCollectorDescription dcd) {
        modelManager.sendCommand(new Command_RemoveDataCollector(dcd));
    }


    /**
     * Sets a delay time for a simulation
     *
     * @param delay
     */
    public synchronized void setSimulationDelay(int delay) {
        this.delayTime = delay;

        if (modelXmlFile != null && delay >= 0) {
            modelManager.sendCommand(new Command_SetDelay(delay));
            receiver.setCollectionInterval("render", 1);
        }

        if (delay < 0) {
            receiver.setCollectionInterval("render", -delay);
            if (modelXmlFile != null)
                modelManager.sendCommand(new Command_SetDelay(0));
        }
    }


    /**
     * Sets a frequency for a simulation
     *
     * @param freq
     */
    public synchronized void setSimulationFrequency(int freq) {
        this.frequency = freq;

        if (modelXmlFile != null) {
            modelManager.sendCommand(new Command_SetFrequency(freq));
//			receiver.setCollectionInterval("render", 1);
        }
    }


    /**
     * Returns a variable by its name
     *
     * @param varName
     * @return
     */
    public ProxyVariable getVariable(String varName) {
        if (variables == null)
            return null;

        return variables.getVariable(varName);
    }


    /**
     * Returns all available variables
     *
     * @return
     */
    public ProxyVariableCollection getVariables() {
        return variables;
    }


    /**
     * Returns a collection of all parameters
     *
     * @return
     */
    public ParameterCollection getParameters() {
        return parameters;
    }

    /**
     * Returns the current directory (the base directory of a loaded model)
     *
     * @return
     */
    public synchronized File getCurrentDir() {
        return currentDir;
    }


    /**
     * Returns the output directory
     *
     * @return
     */
    public synchronized File getOutputDir() {
        if (outputDir.empty())
            return getCurrentDir();
        else
            return outputDir.peek();
    }


    /**
     * Adds an output directory to the stack
     *
     * @param file
     * @return
     */
    public synchronized void pushOutputDir(File file) {
        outputDir.push(file);
    }


    /**
     * Removes the last added output directory
     */
    public synchronized void popOutputDir() {
        if (!outputDir.empty())
            outputDir.pop();
    }


    /**
     * Loads the given model file
     *
     * @param modelFile
     */
    public synchronized void loadModel(File modelFile) {
        try {
            if (modelXmlFile != null)
                unloadModel();

            Document xmlDoc = ModelFileLoader.loadModelFile(modelFile);
            currentDir = modelFile.getParentFile();
            FileUtils.setBaseDir(currentDir);

//			ModelFileLoader.saveModelFile(xmlDoc, new File("test.xml"));

            modelManager.sendCommand(new Command_LoadModel(modelFile,
                    modelManager));
            modelManager.sendCommand(new Command_AddDataReceiver(receiver));

            // Root node
            Node root = xmlDoc.getFirstChild();

            Node modelNode = XmlDocUtils.getChildByTagName(root, "model");
            Node interfaceNode = XmlDocUtils.getChildByTagName(root, "interface");

            ArrayList<Node> list;

            /* Load variables (for parameters) */
            list = XmlDocUtils.getChildrenByTagName(modelNode, "variables");
            if (list.size() >= 1) {
                variables = new ProxyVariableCollection(list.get(0));
                variables.registerVariables(receiver);
            }

            /* Load parameters and variable sets */
            parameters = new ParameterCollection();
            list = XmlDocUtils.getChildrenByTagName(modelNode, "parameters");
            if (list.size() >= 1) {
                parameters.loadParameters(list.get(0));
            }

            list = XmlDocUtils.getChildrenByTagName(interfaceNode, "variable-sets");
            if (list.size() >= 1) {
                VariableSetFactory.loadVariableSets(list.get(0));
            }

            /* Load methods */
            methods = new MethodCollection();
            list = XmlDocUtils.getChildrenByTagName(modelNode, "methods");
            if (list.size() >= 1) {
                methods.loadMethods(list.get(0));
            }


            /* Collect agents */
            agentTypesAndNames.clear();

            Node agents = XmlDocUtils.getChildByTagName(modelNode, "agents");
            if (agents != null) {
                list = XmlDocUtils.getChildrenByTagName(agents, "agent");
                for (int i = 0; i < list.size(); i++) {
                    Node node = list.get(i);
                    String typeName = node.getTextContent().trim();
                    String name = getValue(node, "name", null);

                    agentTypesAndNames.put(typeName, name);
                }
            }


            /* Load data layer styles */
            dataLayerStyles.clear();
            dataLayerStyleNodes.clear();

            Node dataLayersNode = XmlDocUtils.getChildByTagName(interfaceNode, "data-layers");
            list = XmlDocUtils.getChildrenByTagName(dataLayersNode, "datalayer");
            for (int i = 0; i < list.size(); i++) {
                Node node = list.get(i);
                loadDataLayer(node);
            }

            this.modelXmlFile = modelFile;
            this.modelXmlDoc = xmlDoc;


            if (interfaceNode != null) {
                loadInterface(xmlDoc, interfaceNode);
            }

            configuration.addRecentProject(modelFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Loads interface from the given interface node
     *
     * @param interfaceNode
     */
    private void loadInterface(Document doc, Node interfaceNode) {
        // TODO: load data set properly
        Node datasetNode = XmlDocUtils.getChildByTagName(interfaceNode, "dataset");
        if (datasetNode != null) {
            dataSet = new DataSetTmp(datasetNode);
            receiver.addDataConsumer(dataSet.getDataFilter());
        }

        // Load control parameters
        Node controlNode = XmlDocUtils.getChildByTagName(interfaceNode, "control-panel");
        if (controlNode != null && controlPanel != null) {
            controlPanel.init(controlNode);
        }


        // If windoManager == null (noGUI == true) then load renderers only
        if (windowManager == null) {
            ArrayList<Node> renders = XmlDocUtils.getChildrenByTagName(interfaceNode, "renderframe");
            Node mainRender = XmlDocUtils.getChildByTagName(interfaceNode, "mainframe");
            if (mainRender != null)
                renders.add(mainRender);

            for (Node render : renders) {
                createRenderer(render, configuration.getRendererType());
            }

            return;
        }

        XML_WindowsLoader.loadWindows(windowManager, interfaceNode);

        /* Load view panels */
        ArrayList<Node> list = XmlDocUtils.getChildrenByTagName(interfaceNode, "renderframe");
        for (Node render : list) {
            new SparkViewPanel(windowManager, render, configuration.getRendererType());
        }

        Node mainWindowRender = XmlDocUtils.getChildByTagName(interfaceNode, "mainframe");
        if (mainWindowRender != null) {
            new SparkViewPanel(windowManager, mainWindowRender, configuration.getRendererType());
        }

        /* Load the parameter panel */
        Node parameterNode = XmlDocUtils.getChildByTagName(interfaceNode, "parameterframe");
        if (parameterNode == null && getParameters().size() > 0) {
            // Create a parameter frame if there are some parameters in the model
            parameterNode = doc.createElement("parameterframe");
            XmlDocUtils.addAttr(doc, parameterNode, "location", "Parameters");
            interfaceNode.appendChild(parameterNode);
        }

        if (parameterNode != null) {
            new SparkParameterPanel(windowManager, parameterNode);
        }


        /* Load charts */
        list = XmlDocUtils.getChildrenByTagName(interfaceNode, "chart");
        list.addAll(XmlDocUtils.getChildrenByTagName(interfaceNode, "user-chart"));

        // TODO: ad hoc implementation of multiple plots in one window
        HashMap<String, SparkChartPanel> chartPanels = new HashMap<String, SparkChartPanel>();

        for (Node chart : list) {
            String name = XmlDocUtils.getValue(chart, "name", null);
            if (name != null) {
                if (chartPanels.containsKey(name)) {
                    chartPanels.get(name).addSeries(chart);
                    continue;
                }
            }

            SparkChartPanel chartPanel = new SparkChartPanel(windowManager, chart);
            receiver.addDataConsumer(chartPanel.getDataFilter());

            if (name != null)
                chartPanels.put(name, chartPanel);
        }

        /* Load methods */
        Node methodsNode = XmlDocUtils.getChildByTagName(interfaceNode, "methods-panel");
        if (methodsNode != null) {
            new SparkMethodPanel(windowManager, methodsNode, methods.getNames());
        }

        /* Load a data set panel */
        datasetNode = XmlDocUtils.getChildByTagName(interfaceNode, "dataset");
        if (datasetNode != null) {
            new SparkDatasetPanel(windowManager, datasetNode, dataSet);
        }


        /* Set up control panel */
        receiver.addDataConsumer(new DataFilter(controlPanel, "state"));
    }


    /**
     * Starts the loaded model
     *
     * @param simulationTime
     * @param paused
     */
    public synchronized void startLoadedModel(long simulationTime, boolean paused) {
        if (modelXmlDoc == null)
            return;

        setSimulationDelay(delayTime);
        setSimulationFrequency(frequency);
        modelManager.sendCommand(new Command_SetSeed(randomSeed, useTimeSeed));
        modelManager.sendCommand(new Command_Start(simulationTime, paused,
                observerName, executionMode));
    }


    /**
     * Pauses/resumes the simulation
     */
    public synchronized void pauseResumeLoadedModel() {
        if (modelXmlDoc == null)
            return;

        modelManager.sendCommand(new Command_PauseResume());
    }


    /**
     * Unloads the current model
     */
    public void unloadModel() {
        synchronized (this) {
            if (modelXmlDoc == null)
                return;

            // Stop a simulation
            stopSimulation();

            // Save GUI changes
            saveGUIChanges();

            // 	Clear variables
            dataSet = null;
            variables = null;
            modelXmlDoc = null;
            modelXmlFile = null;

            renderers.clear();
        }
        receiver.removeAllConsumers();

        if (windowManager != null)
            windowManager.disposeAll();
    }


    /**
     * Stops a simulation
     */
    public synchronized void stopSimulation() {
        modelManager.sendCommand(new Command_Stop());
    }


    /**
     * Loads data layer styles
     *
     * @param node
     */
    private void loadDataLayer(Node node) {
        DataLayerStyle style = DataLayerStyle.LoadXml(node);
        String name = style.getName();

        dataLayerStyles.put(name, style);
        dataLayerStyleNodes.put(name, node);
    }


    /**
     * Called whenever a model is unloading
     *
     * @throws Exception
     */
    public void saveGUIChanges() {
        if (noGUI)
            return;

        if (modelXmlDoc == null || modelXmlFile == null)
            return;

        try {
            XmlDocUtils.removeTextNodes(modelXmlDoc);
            // Save data layers
            saveDataLayerStyles(modelXmlDoc);

            Node interfaceNode = XmlDocUtils.getChildByTagName(modelXmlDoc.getFirstChild(), "interface");
            controlPanel.updateXML(windowManager.getMainWindow(), modelXmlDoc, interfaceNode, modelXmlFile);
            XML_WindowsLoader.saveWindows(windowManager, modelXmlDoc, interfaceNode, modelXmlFile);


            ModelFileLoader.saveModelFile(modelXmlDoc, modelXmlFile);
//			ModelFileLoader.saveModelFile(modelXmlDoc, new File("test2.xml"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception", e);
        }
    }

    /**
     * Saves changes of data layers
     */
    private void saveDataLayerStyles(Document doc) {
        for (String name : dataLayerStyles.keySet()) {
            DataLayerStyle style = dataLayerStyles.get(name);
            Node node = dataLayerStyleNodes.get(name);

            style.SaveXml(doc, node);
        }
    }


    /**
     * Creates a renderer of the given type from the given xml-node with
     * parameters
     *
     * @param node
     * @param rendererType
     * @return
     */
    public synchronized Renderer createRenderer(Node node, int rendererType) {
//		if (noGUI)
//			return null;

        int interval = (delayTime < 0) ? -delayTime : 1;

        Renderer renderer = Renderer.createRenderer(node, rendererType, interval, dataLayerStyles,
                agentTypesAndNames, currentDir, noGUI);

        renderer.updateDataFilter();
        renderer.register(receiver);

        renderers.add(renderer);

        return renderer;
    }


    /**
     * Invokes the update method for all active renderers
     */
    public synchronized void updateAllRenderers() {
//		if (noGUI)
//			return;

        for (Renderer renderer : renderers) {
            renderer.update();
        }
    }


    /**
     * Returns an array of all renders
     */
    public synchronized Renderer[] getRenderers() {
        Renderer[] result = new Renderer[renderers.size()];
        return renderers.toArray(result);
    }


    /**
     * Test main method
     *
     * @param args
     */
    public static void main(final String[] args) throws Exception {
        // The first thing to do is to set up the logger
        // TODO: logger
//		try {
//			if (new File("spark.log4j.properties").exists()) {
//				PropertyConfigurator.configure("spark.log4j.properties");
//			} else {
//				BasicConfigurator.configure();
//				logger
//						.error("File spark.log4j.properties is not found: using default output streams for log information");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			BasicConfigurator.configure();
//		}

        final ModelManager_Basic manager = new ModelManager_Basic();
        final DataReceiver receiver = new DataReceiver();

        new Thread(manager).start();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Coordinator.init(manager, receiver, false);
                if (args.length == 1) {
                    final String modelPath = args[0];
                    try {
                        Coordinator.getInstance().loadModel(new File(modelPath));
                        Coordinator.getInstance().startLoadedModel(Long.MAX_VALUE, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        /*
         * Coordinator c = Coordinator.getInstance(); c.loadModel(newFile(
         * "c:/help/alexey/my new projects/eclipse projects/spark/tests/models/rsv/RSVModel.xml"
         * )); c.startLoadedModel(); Thread.sleep(100); // c.loadModel(newFile(
         * "c:/help/alexey/my new projects/eclipse projects/spark/tests/models/rsv/RSVModel.xml"
         * )); c.loadModel(newFile(
         * "c:/help/alexey/my new projects/eclipse projects/spark/tests/models/basic/CreateDieA.xml"
         * )); c.startLoadedModel();
         */
        // c.setRandomSeed(0, false);
        // c.setObserver("Observer2", ExecutionMode.CONCURRENT_MODE);
        // c.startLoadedModel();
        // manager.sendCommand(new Command_SetSeed(0, false));
        // manager.sendCommand(new Command_Start(1000, "Observer1",
        // ExecutionMode.SERIAL_MODE));
        // manager.runOnce();
        // Thread.sleep(1);
        // manager.sendCommand(new Command_Start(1000, "Observer2",
        // ExecutionMode.CONCURRENT_MODE));
        // c.startLoadedModel();
    }
}
