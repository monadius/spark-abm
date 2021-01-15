package org.sparkabm.gui;

import java.util.ArrayList;
import java.util.HashMap;

import org.sparkabm.gui.data.DataFilter;
import org.sparkabm.gui.data.DataReceiver;
import org.sparkabm.runtime.data.DataCollectorDescription;
import org.sparkabm.utils.XmlDocUtils;
import org.w3c.dom.Node;

/**
 * Collection of all proxy variables
 *
 * @author Monad
 */
public class ProxyVariableCollection {
    private final HashMap<String, ProxyVariable> variables;
    private final ArrayList<ProxyVariable> varList;


    /**
     * Creates a collection of proxy variables for all variables
     * inside the given node
     *
     * @param node
     */
    public ProxyVariableCollection(Node node) throws Exception {
        variables = new HashMap<String, ProxyVariable>();
        varList = new ArrayList<ProxyVariable>();
        ArrayList<Node> list = XmlDocUtils.getChildrenByTagName(node, "variable");

        for (Node varNode : list) {
            ProxyVariable var = ProxyVariable.loadVariable(varNode);
            variables.put(var.getName(), var);
            varList.add(var);
        }
    }


    /**
     * Returns a variable by its name
     *
     * @param name
     * @return
     */
    public ProxyVariable getVariable(String name) {
        return variables.get(name);
    }


    /**
     * Returns all variables in the collection
     *
     * @return
     */
    public ProxyVariable[] getVariables() {
        ProxyVariable[] result = new ProxyVariable[varList.size()];
        return varList.toArray(result);
    }


    /**
     * Register variables as data consumers
     *
     * @param receiver
     */
    public void registerVariables(DataReceiver receiver) {
        for (ProxyVariable var : variables.values()) {
            DataFilter df = new DataFilter(var, "variable");
            df.addData(DataCollectorDescription.VARIABLE, var.getName());
            receiver.addDataConsumer(df);
        }
    }
}
