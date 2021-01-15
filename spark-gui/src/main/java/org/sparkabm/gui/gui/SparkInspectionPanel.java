package org.sparkabm.gui.gui;

import java.awt.Dimension;
import java.io.File;

import javax.swing.AbstractListModel;
import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.sparkabm.runtime.data.DataCollectorDescription;
import org.sparkabm.runtime.data.DataObject_Inspection;
import org.sparkabm.runtime.data.DataRow;
import org.sparkabm.gui.Coordinator;
import org.sparkabm.gui.data.DataFilter;
import org.sparkabm.gui.data.IDataConsumer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Displays inspection information
 */
@SuppressWarnings("serial")
public class SparkInspectionPanel extends JPanel implements ISparkPanel, IDataConsumer, ListSelectionListener {
    // Data filter
    private final DataFilter dataFilter;
    // Name of the inspector
    private final String name;

    private JTable table;
    private JList list;

    private TableModel tableData;
    private ListModel listData;

    /**
     * Implementation of the abstract table model
     */
    private static class TableModel extends AbstractTableModel {
        // The object information
        private DataObject_Inspection.ObjectInformation info;

        // Default constructor
        public TableModel() {
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            if (info == null)
                return 0;

            return info.varNames.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (info == null)
                return null;

            if (rowIndex >= info.varNames.size())
                return null;

            switch (columnIndex) {
                case 0:
                    return info.varNames.get(rowIndex);
                case 1:
                    return info.varValues.get(rowIndex);
            }

            return null;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case 0:
                    return "Variable";
                case 1:
                    return "Value";
            }

            return "???";
        }


        /**
         * Updates the data
         */
        public void update(DataObject_Inspection.ObjectInformation obj) {
            this.info = obj;
            fireTableDataChanged();
        }
    }


    /**
     * Implementation of the abstract list model
     */
    private static class ListModel extends AbstractListModel {
        private DataObject_Inspection info;

        // Default constructor
        public ListModel() {
        }

        @Override
        public Object getElementAt(int index) {
            if (index >= info.getObjects().size())
                return null;

            return info.getObjects().get(index);
        }

        @Override
        public int getSize() {
            if (info == null)
                return 0;

            return info.getObjects().size();
        }


        /**
         * Updates the data
         *
         * @param data
         * @return true if the previous data was null
         */
        public boolean update(DataObject_Inspection data) {
            boolean flag = info == null;
            this.info = data;
            this.fireContentsChanged(this, 0, 1000);

            return flag;
        }
    }


    /**
     * Default constructor
     */
    public SparkInspectionPanel(WindowManager manager, String name) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
//		setLayout(new GridLayout(0, 1));
        this.name = name;
        this.dataFilter = new DataFilter(this, "inspector");

        // Create the interface
        initInterface();

        // Get the window name
        String location = manager.getGoodName("Inspection: " + name);
        if (location == null)
            return;

        // Associate this component with the corresponding window
        SparkWindow win = manager.setLocation(this, location, 300, 300);
        win.doNotSave = true;
        win.pack();

        // Register the data filter
        Coordinator.getInstance().getDataReceiver().addDataConsumer(dataFilter);
    }


    /**
     * Initializes the inspector
     *
     * @param parameters
     */
    public void init(DataObject_Inspection.Parameters parameters) {
        dataFilter.removeAllData();
        dataFilter.addData(DataCollectorDescription.INSPECTION_DATA, name, parameters);
    }


    /**
     * Initializes the interface
     */
    private void initInterface() {
        // Create the table
        tableData = new TableModel();
        table = new JTable(tableData);

        // Create the list
        listData = new ListModel();
        list = new JList(listData);
        list.setMinimumSize(new Dimension(70, 200));
        list.addListSelectionListener(this);

        JScrollPane tablePane = new JScrollPane(table);
        JScrollPane listPane = new JScrollPane(list);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(listPane);
        split.setRightComponent(tablePane);

        split.setDividerLocation(100);

        // Initialize the main panel
//		mainPanel = new JScrollPane(table);
//		mainPanel.setMinimumSize(new Dimension(100, 100));
//		mainPanel.setPreferredSize(new Dimension(200, 200));
//		add(mainPanel);
        add(split);
    }


    @Override
    public void updateXML(SparkWindow location, Document xmlModelDoc,
                          Node interfaceNode, File xmlModelFile) {
        // Nothing to do here
    }

    @Override
    public void consume(DataRow row) {
        DataObject_Inspection data = row.getInspectionData(name);
        if (data == null)
            return;

        int n = data.getObjects().size();

        if (listData.update(data) && n > 0)
            list.setSelectedIndex(0);

        Object obj = list.getSelectedValue();
        if (obj == null || !(obj instanceof DataObject_Inspection.ObjectInformation))
            return;

        tableData.update((DataObject_Inspection.ObjectInformation) obj);
    }


    @Override
    public void valueChanged(ListSelectionEvent e) {
        Object obj = list.getSelectedValue();

        if (obj == null || !(obj instanceof DataObject_Inspection.ObjectInformation)) {
            tableData.update(null);
        }

        tableData.update((DataObject_Inspection.ObjectInformation) obj);
    }

}
