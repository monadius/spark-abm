package org.sparkabm.modelfile;

import static org.sparkabm.utils.XmlDocUtils.*;

import java.util.ArrayList;
import java.util.HashSet;

import org.sparkabm.utils.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Level 1 model converter
 *
 * @author Monad
 */
class ModelConverter_1 extends ModelConverter {
    private static final Version VERSION = new Version(1, 0);

    ModelConverter_1() {
        super(new ModelConverter_0());
    }

    @Override
    public CheckResult checkDocument(Document doc) {
        Node root = doc.getFirstChild();

        if (root == null)
            return CheckResult.OLDER_VERSION;

        if (!"spark".equals(root.getNodeName()))
            return CheckResult.OLDER_VERSION;

        Version version = getNodeVersion(root);
        int cmp = VERSION.compare(version);

        if (cmp < 0)
            return CheckResult.NEWER_VERSION;
        if (cmp > 0)
            return CheckResult.OLDER_VERSION;

        return CheckResult.GOOD_VERSION;
    }

    @Override
    protected Document convert0(Document doc) throws Exception {
        // Create a new document
        Document result = createNewDocument("spark", VERSION);
        Node newRoot = result.getFirstChild();

        // Create 'files' node
        Node files = result.createElement("files");
        newRoot.appendChild(files);

        // Create 'model' and 'interface' nodes
        Node model = result.createElement("model");
        Node interfaceNode = result.createElement("interface");

        newRoot.appendChild(model);
        newRoot.appendChild(interfaceNode);

        // Create 'windows' subnode
        Node windows = result.createElement("windows");
        removeChildren(interfaceNode, "windows");
        interfaceNode.appendChild(windows);

        /****** Begin a process ******/
        // Take the original root node
        Node root = doc.getFirstChild();

        // Expand 'charts' node
        Node charts = getChildByTagName(root, "charts");
        if (charts != null) {
            for (Node chart : getChildrenByTagName(charts, "chart")) {
                root.appendChild(chart);
            }
        }

        removeChildren(root, "charts");

        // Iterate over all nodes in 'doc'
        NodeList nodes = root.getChildNodes();
        HashSet<String> names = new HashSet<String>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String name = node.getNodeName().intern();

            node = result.importNode(node, true);

            if (name == "classpath") {
                String path = getValue(node, "path", ".");
                addAttr(result, files, "path", path);
                // Include all files in the given directory
                addAttr(result, files, "all", true);
                continue;
            }

            if (name == "setup" || name == "agents"
                    || name == "variables" || name == "methods") {
                model.appendChild(node);

                // Special treatment of methods
                if (name == "methods") {
                    Node methods = result.createElement("methods-panel");
                    int x = getIntegerValue(node, "x", 0);
                    int y = getIntegerValue(node, "y", 0);
                    int width = getIntegerValue(node, "width", 300);
                    int height = getIntegerValue(node, "height", 300);

                    addAttr(result, methods, "x", x);
                    addAttr(result, methods, "y", y);
                    addAttr(result, methods, "width", width);
                    addAttr(result, methods, "height", height);

                    interfaceNode.appendChild(methods);
                    processWindow(result, windows, methods, "methods", names);
                }
            } else {
                interfaceNode.appendChild(node);

                processWindow(result, windows, node, name, names);
            }
        }

        return result;
    }

    /**
     * Creates special nodes for interface windows
     *
     * @param windows
     * @param node
     * @param name
     */
    private void processWindow(Document doc, Node windows, Node node,
                               String name, HashSet<String> names) {
        String nodeName = null;
        name = name.intern();

        if (name == "mainframe")
            nodeName = "Main Window";
        else if (name == "renderframe")
            nodeName = getValue(node, "name", "View");
        else if (name == "dataset")
            nodeName = "Dataset";
        else if (name == "parameterframe")
            nodeName = "Parameters";
        else if (name == "methods")
            nodeName = "Methods";
        else if (name == "chart")
            nodeName = getValue(node, "name", "Chart");
        else if (name == "charts") {
            ArrayList<Node> charts = getChildrenByTagName(node, "chart");

            for (Node chart : charts) {
                processWindow(doc, windows, chart, "chart", names);
            }

            return;
        }

        if (nodeName == null)
            return;

        // We need unique names for all windows
        if (names.contains(nodeName)) {
            for (int i = 1; i < 1000; i++) {
                String newName = nodeName + " " + i;
                if (!names.contains(newName)) {
                    nodeName = newName;
                    break;
                }
            }
        }

        names.add(nodeName);

        addAttr(doc, node, "location", nodeName);

        // Read attributes
        int x = getIntegerValue(node, "x", 0);
        int y = getIntegerValue(node, "y", 0);
        int width = getIntegerValue(node, "width", 300);
        int height = getIntegerValue(node, "height", 300);

        // Create new node
        Node window = doc.createElement("window");

        addAttr(doc, window, "name", nodeName);
        addAttr(doc, window, "x", x);
        addAttr(doc, window, "y", y);
        addAttr(doc, window, "width", width);
        addAttr(doc, window, "height", height);
        if (name == "mainframe")
            addAttr(doc, window, "main", true);

        windows.appendChild(window);
    }

}
