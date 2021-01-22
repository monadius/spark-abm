package org.sparkabm.gui.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.sparkabm.gui.Coordinator;
import org.sparkabm.gui.data.DataSetTmp;
import org.sparkabm.utils.FileUtils;
import org.sparkabm.utils.XmlDocUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A data set panel
 *
 * @author Alexey
 */
@SuppressWarnings("serial")
public class SparkDatasetPanel extends JPanel implements ISparkPanel, ActionListener {
    private static final Logger logger = Logger.getLogger(SparkDatasetPanel.class.getName());

    /* Dataset name */
//	private String name;

    /* Data set */
    private final DataSetTmp data;

    /* Control button */
    private JButton saveButton;


    /**
     * Creates a data set panel from the data set
     *
     * @param manager
     * @param node
     */
    public SparkDatasetPanel(WindowManager manager, Node node, DataSetTmp data) {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        this.data = data;

        ArrayList<Node> items = XmlDocUtils.getChildrenByTagName(node, "item");
        for (int i = 0; i < items.size(); i++) {
            Node itemNode = items.get(i);
            addItem(itemNode);
        }


        // Create the Save button
        saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        this.add(saveButton);

        // Set panel's location
        String location = XmlDocUtils.getValue(node, "location", null);
        manager.setLocation(this, location);
    }


    /**
     * Updates XML node
     */
    public void updateXML(SparkWindow location, Document xmlModelDoc, Node interfaceNode, File xmlModelFile) {
        // Nothing to do here
    }


    /**
     * Adds an item for collection
     *
     * @param item
     */
    private void addItem(Node item) {
        String name = XmlDocUtils.getValue(item, "name", "???");
        JLabel label = new JLabel(name);
        this.add(label);
    }


    /**
     * Opens a file selection dialog and saves the data into a selected file
     *
     * @throws Exception
     */
    private void saveDataFile() {
        if (data == null)
            return;

        File file = FileUtils.saveFileDialog(null, Coordinator.getInstance().getCurrentDir(), "csv");
        PrintStream out = null;

        if (file != null) {
            try {
                if (FileUtils.getExtension(file).equals("")) {
                    file = new File(file.getPath() + ".csv");
                }

                out = new PrintStream(file);
                data.saveData(out, 1);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "exception", e);
            } finally {
                if (out != null)
                    out.close();
            }
        }
    }


    /**
     * Saves data in the given file
     *
     * @param fname
     */
    public void saveData(String fname) {
        if (data != null)
            data.saveData(fname, 1);
    }


    /**
     * Saves data in the given file
     *
     * @param file
     */
    public void saveData(File file) {
        if (data != null)
            data.saveData(file, 1);
    }


    /**
     * Action listener implementation
     */
    public void actionPerformed(ActionEvent arg0) {
        Object src = arg0.getSource();

        if (src == saveButton) {
            try {
                saveDataFile();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}

