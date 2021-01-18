package org.sparkabm.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.sparkabm.math.Vector;
import org.sparkabm.math.Vector4d;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Utilities for working with xml documents
 *
 * @author Monad
 */
public class XmlDocUtils {
    // Log
    private static final Logger logger = Logger.getLogger(XmlDocUtils.class.getName());

    /**
     * Loads an xml file
     *
     * @param fname
     * @return
     */
    public static Document loadXmlFile(String fname) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(fname);
            return doc;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception", e);
        }

        return null;
    }

    /**
     * Loads an xml file
     */
    public static Document loadXmlFile(File file) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(file);

            return doc;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception", e);
        }

        return null;
    }

    /**
     * Creates a new document with the given root node.
     * If the version is positive then the version attributes is attached to the root node.
     */
    public static Document createNewDocument(String rootName, Version version) {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            Document doc = db.newDocument();
            Node root = doc.createElement(rootName);
            if (version != null) {
                addAttr(doc, root, "version", version);
            }

            doc.appendChild(root);
            return doc;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception", e);
        }

        return null;
    }


    /**
     * Parses the attribute version of the form major.minor
     */
    public static Version getNodeVersion(Node node) {
        String value = getValue(node, "version", "0");
        String[] els = value.split("\\.");
        if (els.length == 0)
            els = new String[]{value};

        int major = 0, minor = 0;

        if (els.length >= 1) {
            try {
                major = Integer.valueOf(els[0]);
                if (els.length >= 2)
                    minor = Integer.valueOf(els[1]);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "exception", e);
            }
        }

        return new Version(major, minor);
    }


    /**
     * Saves the document in the given file
     */
    public static void saveDocument(Document doc, File file) throws Exception {
        if (doc == null || file == null)
            return;

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    /**
     * Returns a list of children with the specified name
     *
     * @param name
     * @return
     */
    public static ArrayList<Node> getChildrenByTagName(Node node, String name) {
        ArrayList<Node> list = new ArrayList<Node>();
        if (node == null)
            return list;

        for (Node child = node.getFirstChild(); child != null; child = child
                .getNextSibling()) {
            if (child.getNodeName().equals(name))
                list.add(child);
        }

        return list;
    }


    /**
     * Returns a list of all child nodes
     *
     * @param node
     * @return
     */
    public static ArrayList<Node> getAllChildren(Node node) {
        ArrayList<Node> list = new ArrayList<Node>();
        if (node == null)
            return list;

        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            list.add(child);
        }

        return list;
    }


    /**
     * Returns the first child node with the given name
     *
     * @param node
     * @param name
     * @return
     */
    public static Node getChildByTagName(Node node, String name) {
        if (node == null)
            return null;

        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeName().equals(name))
                return child;
        }

        return null;
    }


    /**
     * Removes all children nodes with the specific name of the given node
     *
     * @param node
     */
    public static void removeChildren(Node node, String name) {
        if (node == null)
            return;

        for (Node item : getChildrenByTagName(node, name)) {
            node.removeChild(item);
        }
    }


    /**
     * Removes the given attribute from the node
     */
    public static void removeAttr(Node node, String attrName) {
        if (node.getAttributes().getNamedItem(attrName) != null)
            node.getAttributes().removeNamedItem(attrName);
    }


    /**
     * Adds the attribute to the given node
     *
     * @param doc
     * @param node
     * @param attrName
     * @param attrValue
     */
    public static void addAttr(Document doc, Node node, String attrName, Object attrValue) {
        Node attr = doc.createAttribute(attrName);
        attr.setNodeValue(attrValue.toString());
        node.getAttributes().setNamedItem(attr);
    }


    /**
     * Gets a string value of the given attribute
     *
     * @param node
     * @param attrName
     * @param defaultValue
     * @return
     */
    public static String getValue(Node node, String attrName, String defaultValue) {
        Node tmp;
        if (node == null)
            return defaultValue;

        NamedNodeMap attrs = node.getAttributes();
        if (attrs == null)
            return defaultValue;

        String value = (tmp = attrs.getNamedItem(attrName)) != null ?
                tmp.getNodeValue()
                : null;

        if (value == null)
            return defaultValue;

        return value;
    }


    /**
     * Gets a boolean value of the given attribute
     *
     * @param node
     * @param attrName
     * @param defaultValue
     * @return
     */
    public static boolean getBooleanValue(Node node, String attrName, boolean defaultValue) {
        String value = getValue(node, attrName, null);

        if (value == null)
            return defaultValue;

        return Boolean.valueOf(value);
    }


    /**
     * Gets an integer value of the given attribute
     *
     * @param node
     * @param attrName
     * @param defaultValue
     * @return
     */
    public static int getIntegerValue(Node node, String attrName, int defaultValue) {
        String value = getValue(node, attrName, null);

        if (value == null)
            return defaultValue;

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets a long value of the given attribute
     *
     * @param node
     * @param attrName
     * @param defaultValue
     * @return
     */
    public static long getLongValue(Node node, String attrName, long defaultValue) {
        String value = getValue(node, attrName, null);

        if (value == null)
            return defaultValue;

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }


    /**
     * Gets a float value of the given attribute
     *
     * @param node
     * @param attrName
     * @param defaultValue
     * @return
     */
    public static float getFloatValue(Node node, String attrName, float defaultValue) {
        String value = getValue(node, attrName, null);

        if (value == null)
            return defaultValue;

        try {
            return Float.valueOf(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }


    /**
     * Gets a double value of the given attribute
     *
     * @param node
     * @param attrName
     * @param defaultValue
     * @return
     */
    public static double getDoubleValue(Node node, String attrName, double defaultValue) {
        String value = getValue(node, attrName, null);

        if (value == null)
            return defaultValue;

        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }


    /**
     * Gets a 3d-vector value of the given attribute
     *
     * @param node
     * @param attrName
     * @param defaultValue
     * @return
     */
    public static Vector getVectorValue(Node node, String attrName, String delim, Vector defaultValue) {
        String value = getValue(node, attrName, null);

        Vector v = StringUtils.StringToVector(value, delim);
        if (v == null)
            return defaultValue;

        return v;
    }


    /**
     * Gets a 4d-vector value of the given attribute
     *
     * @param node
     * @param attrName
     * @param defaultValue
     * @return
     */
    public static Vector4d getVector4dValue(Node node, String attrName, String delim, Vector4d defaultValue) {
        String value = getValue(node, attrName, null);

        Vector4d v = StringUtils.StringToVector4d(value, delim);
        if (v == null)
            return defaultValue;

        return v;
    }

}
