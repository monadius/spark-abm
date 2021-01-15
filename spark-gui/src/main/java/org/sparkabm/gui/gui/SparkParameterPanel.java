package org.sparkabm.gui.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;

import org.sparkabm.gui.Coordinator;
import org.sparkabm.gui.Parameter;
import org.sparkabm.gui.VariableSet;
import org.sparkabm.gui.VariableSetFactory;
import org.sparkabm.gui.gui.dialogs.ParameterEditDialog;
import org.sparkabm.utils.SpringUtilities;
import org.sparkabm.utils.XmlDocUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * Parameter panel in SPARK
 *
 * @author Monad
 */
@SuppressWarnings("serial")
public class SparkParameterPanel extends JPanel implements ActionListener, ISparkPanel {
    private JPanel parameterPanel;
    private JPanel setPanel;
    private final HashMap<String, Parameter> parameters = new HashMap<String, Parameter>();

    // The corresponding xml-node
    private Node xmlNode;

    // Commands
    private static final String CMD_NEW_SET = "new-set";
    private static final String CMD_SELECT_SET = "select-set";
    private static final String CMD_AUTO_SAVE = "auto-save";
    private static final String CMD_SAVE_SET = "save-set";
    private static final String CMD_EDIT = "edit";


    // Currently selected variable set
    private VariableSet selectedSet = null;
    // TODO: depends on a selected set
    private boolean autoSaveFlag = false;

    private JComboBox variableSetBox;

    // The dialog for editing parameter options
    private ParameterEditDialog editDialog;


    /**
     * Default constructor
     *
     * @param manager
     * @param node
     */
    public SparkParameterPanel(WindowManager manager, Node node) {
        init();
        this.xmlNode = node;

        // Load saved user options
        if (node != null)
            loadUserOptions(node);

        String location = XmlDocUtils.getValue(node, "location", null);
        manager.setLocation(this, location);
    }


    /**
     * Initializes the parameter panel
     */
    private void init() {
        // Set layout
        setLayout(new BorderLayout());

        // Create panel for variable sets
        setPanel = new JPanel();
        setPanel.setLayout(new BoxLayout(setPanel, BoxLayout.LINE_AXIS));
        setPanel.setMinimumSize(new Dimension(200, 50));
        setPanel.setBorder(BorderFactory.createTitledBorder("Parameter set"));

        // Create panel for parameters		
        parameterPanel = new JPanel(new SpringLayout());
        JScrollPane scroll = new JScrollPane(parameterPanel);

        // Heading
        parameterPanel.add(new JLabel("Name"));
        // TODO: find better solution
        parameterPanel.add(new JLabel("Current Value"));
        parameterPanel.add(new JLabel("Value"));

        loadVariableSets();
        loadParameters();

        // Set the preferred size based on the number of parameters
        scroll.setPreferredSize(new Dimension(300, parameters.size() * 30));


        // Control panel
        JPanel ctrlPanel = new JPanel();
        ctrlPanel.setLayout(new BoxLayout(ctrlPanel, BoxLayout.LINE_AXIS));

        // Edit button
        JButton editButton = new JButton("Edit");
        editButton.setActionCommand(CMD_EDIT);
        editButton.addActionListener(this);

        ctrlPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        ctrlPanel.add(editButton);


        // Add panels to the main panel
        add(setPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(ctrlPanel, BorderLayout.SOUTH);

        SpringUtilities.makeCompactGrid(parameterPanel,
                parameters.values().size() + 1, 3,
                5, 5, 15, 5);

        // Create the dialog for editing parameter options
        editDialog = new ParameterEditDialog();
        editDialog.setVisible(false);
    }


    /**
     * Creates controls for variable sets
     */
    private void loadVariableSets() {
        String[] tmp = VariableSetFactory.getNames();
        String[] names = new String[tmp.length + 1];

        names[0] = "none ";
        for (int i = 0; i < tmp.length; i++)
            names[i + 1] = tmp[i];

        // Get the initially selected set
        VariableSet firstSet = VariableSetFactory.getFirstSet();
        if (firstSet != null) {
            try {
                firstSet.loadVariablesFromSet();
            } catch (Exception e) {
                e.printStackTrace();
                firstSet = null;
            }
        }

        // New
        JButton newButton = new JButton("New");
        newButton.setActionCommand(CMD_NEW_SET);
        newButton.addActionListener(this);

        // Selection
        variableSetBox = new JComboBox(names);
        variableSetBox.setActionCommand(CMD_SELECT_SET);
        variableSetBox.setMinimumSize(new Dimension(100, 30));
        variableSetBox.setPreferredSize(new Dimension(200, 30));

        if (firstSet != null) {
            variableSetBox.setSelectedItem(firstSet.getName());
            selectedSet = firstSet;
        } else {
            variableSetBox.setSelectedIndex(0);
            selectedSet = null;
        }

        variableSetBox.addActionListener(this);

        // Auto save
        JCheckBox autoBox = new JCheckBox("Auto save");
        autoBox.setSelected(autoSaveFlag);
        autoBox.setActionCommand(CMD_AUTO_SAVE);
        autoBox.addActionListener(this);

        // Save
        JButton saveButton = new JButton("Save");
        saveButton.setActionCommand(CMD_SAVE_SET);
        saveButton.addActionListener(this);

        // Add controls to the panel
        setPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        setPanel.add(newButton);
        setPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        setPanel.add(variableSetBox);
        setPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        setPanel.add(autoBox);
        setPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        setPanel.add(saveButton);
        setPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    }


    /**
     * Creates controls for all parameters
     */
    private void loadParameters() {
        Parameter[] pars = Coordinator.getInstance().getParameters().getParameters();

        for (int i = 0; i < pars.length; i++)
            addParameter(pars[i]);
    }


    /**
     * Creates controls for the given parameter
     */
    private void addParameter(Parameter p) {
        JLabel lname = new JLabel(p.getName());

        parameterPanel.add(lname);
        parameterPanel.add(p.getLabel());
        parameterPanel.add(p.getWidget());

        parameters.put(p.getName(), p);
    }


    /**
     * Updates the associated xml node
     */
    public void updateXML(SparkWindow location, Document xmlModelDoc, Node interfaceNode, File xmlModelFile) {
        if (autoSaveFlag && selectedSet != null)
            selectedSet.saveVariablesIntoSet();

        // Save user options
        if (xmlNode != null)
            saveUserOptions(xmlModelDoc, xmlNode);


        Node root = null;
        root = XmlDocUtils.getChildByTagName(interfaceNode, "variable-sets");

        if (root == null) {
            root = xmlModelDoc.createElement("variable-sets");
            interfaceNode.appendChild(root);
        }

        XmlDocUtils.removeChildren(root, "variable-set");
        XmlDocUtils.removeChildren(root, "#text");

        VariableSetFactory.saveXML(xmlModelDoc, root);
    }


    /**
     * Saves user options
     */
    private void saveUserOptions(Document doc, Node node) {
        XmlDocUtils.removeChildren(node, "user-parameter");
        XmlDocUtils.removeChildren(node, "#text");

        for (Parameter p : parameters.values()) {
            Node optNode = doc.createElement("user-parameter");

            // Add attributes
            XmlDocUtils.addAttr(doc, optNode, "name", p.getName());
            XmlDocUtils.addAttr(doc, optNode, "use", p.isUsingUserOptions());

            Parameter.Options userOptions = p.getUserOptions();
            userOptions.saveXml(doc, optNode);

            // Append the new node
            node.appendChild(optNode);
        }
    }


    /**
     * Loads user options
     */
    private void loadUserOptions(Node node) {
        ArrayList<Node> nodes = XmlDocUtils.getChildrenByTagName(node, "user-parameter");

        for (Node optNode : nodes) {
            String name = XmlDocUtils.getValue(optNode, "name", null);
            if (name == null)
                continue;

            Parameter p = parameters.get(name);
            // Silently ignore missing parameters
            if (p == null)
                continue;

            boolean useUserOptions = XmlDocUtils.getBooleanValue(optNode, "use", false);
            Parameter.Options userOptions = Parameter.Options.loadXml(optNode);

            p.setUserOptions(userOptions);
            p.activateUserOptions(useUserOptions);
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        if (cmd == null)
            return;

        cmd = cmd.intern();

        // 'edit' command
        if (cmd == CMD_EDIT) {
            editDialog.init(parameters.values());
            editDialog.setVisible(true);
        }

        // 'new-set' command
        if (cmd == CMD_NEW_SET) {
            String setName = JOptionPane.showInputDialog("Input set name");
            if (setName == null || setName.trim().equals(""))
                return;

            setName = setName.trim();

            // Do not change the existent sets
            String[] names = VariableSetFactory.getNames();
            for (int i = 0; i < names.length; i++) {
                if (setName.equals(names[i]))
                    return;
            }

            VariableSet newSet = VariableSetFactory.getVariableSet(setName);
            if (newSet == null)
                return;

            newSet.saveVariablesIntoSet();

            variableSetBox.addItem(setName);

            // Select the new set
            variableSetBox.setSelectedItem(setName);
            return;
        }

        // 'save-set' command
        if (cmd == CMD_SAVE_SET) {
            if (selectedSet == null)
                return;

            selectedSet.saveVariablesIntoSet();
            return;
        }

        // 'auto-set' command
        if (cmd == CMD_AUTO_SAVE) {
            JCheckBox auto = (JCheckBox) e.getSource();
            autoSaveFlag = auto.isSelected();
            return;
        }

        // 'set' command
        if (cmd == CMD_SELECT_SET) {
            JComboBox box = (JComboBox) e.getSource();

            String setName = (String) box.getSelectedItem();

            if (selectedSet != null) {
                if (autoSaveFlag)
                    selectedSet.saveVariablesIntoSet();
            }

            // TODO: what will happen if a user creates
            // a set with the name "Default"?
            if ("none ".equals(setName)) {
                // TODO: implement
                // ParameterFactory.loadDefaultValues();
                selectedSet = null;
                VariableSetFactory.setFirstSet(null);

                return;
            }

            selectedSet = VariableSetFactory.getVariableSet(setName);
            VariableSetFactory.setFirstSet(selectedSet);

            if (selectedSet == null)
                return;


            try {
                selectedSet.loadVariablesFromSet();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

//			GUIModelManager.getInstance().requestUpdate();
            return;
        }

    }

}
