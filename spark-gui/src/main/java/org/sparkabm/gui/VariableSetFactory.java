package org.sparkabm.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sparkabm.utils.XmlDocUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Class for managing and creating sets of variables
 *
 * @author Monad
 */
public class VariableSetFactory {
    private static final Logger logger = Logger.getLogger(VariableSetFactory.class.getName());

    /* Table containing all variable sets */
    private static HashMap<String, VariableSet> variableSets =
            new HashMap<String, VariableSet>();

    // A set which is selected initially
    private static String firstSetName;


    /**
     * Returns names of all variable sets
     *
     * @return
     */
    public static String[] getNames() {
        String[] names = new String[variableSets.size()];

        int n = 0;
        for (VariableSet set : variableSets.values()) {
            names[n++] = set.getName();
        }

        return names;
    }

    /**
     * Returns a variable set with the given name or creates
     * a new variable set if it does not exist
     *
     * @param name
     * @return
     */
    public static VariableSet getVariableSet(String name) {
        VariableSet set = variableSets.get(name);

        if (set != null)
            return set;

        set = new VariableSet(name);
        set.synchronizeWithParameters(Coordinator.getInstance().getParameters());

        variableSets.put(name, set);

        return set;
    }


    /**
     * Returns an initially selected set
     */
    public static VariableSet getFirstSet() {
        if (firstSetName == null)
            return null;

        return variableSets.get(firstSetName);
    }


    /**
     * Sets the set which will be an initial set
     *
     * @param set
     */
    public static void setFirstSet(VariableSet set) {
        if (set == null)
            firstSetName = null;
        else
            firstSetName = set.getName();
    }


    /**
     * Loads all variable sets from a given xml-node
     *
     * @param parent
     */
    public static void loadVariableSets(Node parent) {
        clear();

        // Get the name of an initially selected set
        firstSetName = XmlDocUtils.getValue(parent, "first-set", null);

        // Get a list of all sets
        ArrayList<Node> list = XmlDocUtils.getChildrenByTagName(parent, "variable-set");

        for (Node node : list) {
            try {
                createVariableSet(node);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "exception", e);
            }
        }
    }


    /**
     * Creates an empty variable set
     *
     * @return
     */
    public static VariableSet createVariableSet(String name) {
        VariableSet set = new VariableSet(name);
        return set;
    }


    /**
     * Clears the table of variable sets
     */
    public static void clear() {
        variableSets.clear();
    }


    /**
     * Creates a new variable set based on a given xml-node.
     *
     * @param node could be null in which case a completely new
     *             variable set based on the parameters is created.
     * @return
     */
    public static VariableSet createVariableSet(Node node) throws Exception {
        if (node == null) {
            return null;
        }

        // Name of the set
        String name = XmlDocUtils.getValue(node, "name", "???");

        if (variableSets.containsKey(name)) {
            throw new Exception("Variable set " + name + " is already defined");
        }

        // Create a new set
        VariableSet set = new VariableSet(name);
        variableSets.put(name, set);

        // Load all variables
        ArrayList<Node> nodes = XmlDocUtils.getChildrenByTagName(node, "variable");
        for (Node varNode : nodes) {
            String varName = XmlDocUtils.getValue(varNode, "name", null);
            if (varName == null)
                continue;

            String svalue = XmlDocUtils.getValue(varNode, "value", "0");

            ProxyVariable var = Coordinator.getInstance().getVariable(varName);

            if (var == null) {
                logger.severe("Variable " + varName + " is not found");
                continue;
            }

            set.addVariable(var, svalue);
        }

        // Note: we need this synchronization to avoid
        // errors after removing/adding parameters from a SPARK-PL code
        set.synchronizeWithParameters(Coordinator.getInstance().getParameters());

        return set;
    }


    /**
     * Saves the current variable sets in an xml-document
     *
     * @param doc
     * @param root
     */
    public static void saveXML(Document doc, Node root) {
        XmlDocUtils.removeChildren(root, "variable-set");
        if (firstSetName != null)
            XmlDocUtils.addAttr(doc, root, "first-set", firstSetName);
        else
            XmlDocUtils.removeAttr(root, "first-set");

        for (VariableSet set : variableSets.values()) {
            Node node = set.createXML(doc);
            root.appendChild(node);
        }
    }


}

