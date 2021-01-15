package org.sparkabm.gui.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.sparkabm.gui.data.DataFilter;
import org.sparkabm.runtime.data.DataCollectorDescription;
import org.sparkabm.runtime.data.DataObject;
import org.sparkabm.runtime.data.DataObject_Bool;
import org.sparkabm.runtime.data.DataObject_Double;
import org.sparkabm.runtime.data.DataRow;
import org.sparkabm.gui.data.IDataConsumer;

/**
 * Stores data collected in a test simulation
 *
 * @author Monad
 */
class TestEngineData implements IDataConsumer {
    /**
     * Abstract representation of a data column
     *
     * @author Monad
     */
    abstract static class DataColumn implements IDataConsumer {
        // Name of the column
        private final String name;
        // Collected data
        protected final ArrayList<Object> data;

        // The last tick for which data was saved
        private transient long lastTick;

        /**
         * Default constructor
         *
         * @param name
         */
        public DataColumn(String name, int simulationLength) {
            this.name = name;
            this.data = new ArrayList<Object>(simulationLength);
            this.lastTick = -1;
        }

        /**
         * Returns column's name
         *
         * @return
         */
        public final String getName() {
            return name;
        }


        /**
         * Clears the data
         */
        public final void clear() {
            data.clear();
            lastTick = -1;
        }


        /**
         * Returns collected data
         *
         * @return
         */
        public final ArrayList<Object> getData() {
            return data;
        }


        /**
         * Adds a new value to the data
         *
         * @param val
         * @param row
         */
        protected final void update(Object val, DataRow row) {
            long tick = row.getState().getTick();

            if (tick > lastTick) {
                data.add(val);
            } else if (tick == lastTick) {
                data.set(data.size() - 1, val);
            } else {
                throw new Error("Old data cannot be modified: " + tick + "/" + lastTick);
            }

            lastTick = tick;
            if (row.getState().isInitialState()) {
                lastTick = -1;
            }
        }


        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof DataColumn))
                return false;

            DataColumn column2 = (DataColumn) obj;

            // Compare names
            if (!name.equals(column2.getName()))
                return false;

            // Compare data
            if (data.equals(column2.data))
                return true;

            return false;
        }

        /**
         * Sets up a data filter
         */
        public abstract void setupDataFilter(DataFilter filter);
    }

    /**
     * A data column with the specified data values
     *
     * @author Monad
     */
    static class SimpleColumn extends DataColumn {
        /**
         * Default constructor
         *
         * @param name
         * @param data
         */
        public SimpleColumn(String name, ArrayList<Object> data) {
            super(name, data.size());

            for (int i = 0; i < data.size(); i++) {
                this.data.add(data.get(i));
            }
        }

        @Override
        public void consume(DataRow row) {
            // Nothing to do here
        }

        @Override
        public void setupDataFilter(DataFilter filter) {
            // Nothing to do here
        }

    }

    /**
     * A data column for the number of ticks
     *
     * @author Monad
     */
    static class TickColumn extends DataColumn {
        /**
         * Default constructor
         *
         * @param simulationLength of the simulation
         */
        public TickColumn(int simulationLength) {
            super("$tick", simulationLength);
        }

        @Override
        public void consume(DataRow row) {
            long tick = row.getState().getTick();
            if (row.getState().isInitialState())
                tick = -1;

            update(tick, row);
        }

        @Override
        public void setupDataFilter(DataFilter filter) {
            // Empty
        }
    }

    /**
     * A data column for a model variable
     *
     * @author Monad
     */
    static class VariableColumn extends DataColumn {
        private final String varName;

        /**
         * Default constructor
         *
         * @param varName
         * @param length
         */
        public VariableColumn(String varName, int length) {
            super(varName, length);
            this.varName = varName;
        }

        @Override
        public void consume(DataRow row) {
            DataObject val = row.get(DataCollectorDescription.VARIABLE, varName);
            if (val == null)
                update(val, row);
            else if (val instanceof DataObject_Double)
                update(((DataObject_Double) val).getValue(), row);
            else if (val instanceof DataObject_Bool)
                update(((DataObject_Bool) val).getValue(), row);
            else
                throw new Error("Unknown data type");
        }

        @Override
        public void setupDataFilter(DataFilter filter) {
            filter.addData(DataCollectorDescription.VARIABLE, varName);
        }
    }

    /**
     * A data column for the number of agents
     *
     * @author Monad
     */
    static class AgentNumberColumn extends DataColumn {
        private final String agentType;

        /**
         * Default constructor
         *
         * @param agentType
         * @param length
         */
        public AgentNumberColumn(String agentType, String agentName, int length) {
            super("$number-" + agentName, length);
            this.agentType = agentType;
        }

        @Override
        public void consume(DataRow row) {
            update(row.getNumberOfAgents(agentType), row);
        }

        @Override
        public void setupDataFilter(DataFilter filter) {
            filter.addData(DataCollectorDescription.NUMBER_OF_AGENTS, agentType);
        }
    }


    /* Contains all data columns with their names */
    private HashMap<String, DataColumn> map = new HashMap<String, DataColumn>();

    /* Contains all data columns in a fixed order */
    private ArrayList<DataColumn> data = new ArrayList<DataColumn>();

    // The main data filter
    private DataFilter dataFilter;


    /**
     * Returns the data filter
     *
     * @return
     */
    public DataFilter getDataFilter() {
        return dataFilter;
    }


    /**
     * Adds a data column to the data set
     *
     * @param column
     */
    public void addDataColumn(DataColumn column) throws Exception {
        String name = column.getName();

        if (map.containsKey(name))
            throw new Exception("Data column '" + name + "' is already defined");

        map.put(name, column);
        data.add(column);

        column.setupDataFilter(dataFilter);
    }


    /**
     * Private empty constructor
     */
    private TestEngineData() {
    }

    /**
     * Default constructor
     */
    public TestEngineData(int simulationLength, boolean saveAllData, HashMap<String, String> agentTypesAndNames, String[] varNames) {
        dataFilter = new DataFilter(this, "data");
        dataFilter.setSynchronizedFlag(true);
        if (!saveAllData) {
            dataFilter.setInterval(simulationLength);
        }

        try {
            // Add the tick data column
            addDataColumn(new TickColumn(simulationLength));

            // Add variables' data columns
            for (String varName : varNames) {
                addDataColumn(new VariableColumn(varName, simulationLength));
            }

            // Add agents' columns
            if (agentTypesAndNames != null) {
                for (String agentType : agentTypesAndNames.keySet()) {
                    String agentName = agentTypesAndNames.get(agentType);
                    addDataColumn(new AgentNumberColumn(agentType, agentName, simulationLength));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void consume(DataRow row) {
        for (DataColumn c : data) {
            c.consume(row);
        }
    }


    /**
     * Clears the collected data
     */
    public void clear() {

    }


    /**
     * Saves the data into a text file
     *
     * @param file
     */
    public void saveAsText(File file) throws IOException {
        // No data
        if (data.size() == 0)
            return;

        int n = data.size();

        FileOutputStream fos = new FileOutputStream(file);
        PrintStream out = new PrintStream(fos);

        // Save headers
        for (int i = 0; i < n; i++) {
            DataColumn column = data.get(i);

            out.print(column.getName());
            if (i < n - 1)
                out.print(',');
        }

        out.println();
        int size = data.get(0).getData().size();
        for (int k = 0; k < size; k++) {
            // Save data values
            for (int i = 0; i < n; i++) {
                DataColumn column = data.get(i);

                out.print(column.getData().get(k));
                if (i < n - 1)
                    out.print(',');
            }

            out.println();
        }

        out.close();
    }

    /**
     * Saves the data into a binary file
     *
     * @param file
     */
    public void saveAsBinary(File file) throws Exception {
        // No data
        if (data.size() == 0)
            return;

        int n = data.size();

        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream out = new ObjectOutputStream(fos);

        // Save the number of columns
        out.writeInt(n);

        // Save data
        for (int i = 0; i < n; i++) {
            DataColumn column = data.get(i);

            // Write out column's name
            out.writeUTF(column.getName());

            // Write out column's data
            ArrayList<Object> vals = column.getData();

            int size = vals.size();
            out.writeInt(size);

            if (size == 0)
                continue;

            // Write out the type
            Object val = vals.get(0);

            // TODO: other types
            if (val instanceof Double) {
                out.writeUTF("Double");

                for (int k = 0; k < size; k++)
                    out.writeDouble((Double) vals.get(k));
            } else if (val instanceof Float) {
                out.writeUTF("Float");

                for (int k = 0; k < size; k++)
                    out.writeFloat((Float) vals.get(k));
            } else if (val instanceof Integer) {
                out.writeUTF("Integer");

                for (int k = 0; k < size; k++)
                    out.writeInt((Integer) vals.get(k));
            } else if (val instanceof Long) {
                out.writeUTF("Long");

                for (int k = 0; k < size; k++)
                    out.writeLong((Long) vals.get(k));
            } else if (val instanceof Boolean) {
                out.writeUTF("Boolean");

                for (int k = 0; k < size; k++)
                    out.writeBoolean((Boolean) vals.get(k));
            } else
                throw new Exception("Unsupported data type: " + val);
        }

        out.close();
    }

    /**
     * Reads the data set from the given binary file
     *
     * @param file
     */
    public static TestEngineData readData(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream in = new ObjectInputStream(fis);

        TestEngineData data = new TestEngineData();

        // Read the number of columns
        int n = in.readInt();

        for (int i = 0; i < n; i++) {
            String name = in.readUTF();
            int size = in.readInt();

            ArrayList<Object> vals = new ArrayList<Object>(size);

            if (size == 0) {
                data.addDataColumn(new SimpleColumn(name, vals));
                continue;
            }

            // Read the type
            String type = in.readUTF();
            if (type.equals("Double")) {
                for (int k = 0; k < size; k++)
                    vals.add(in.readDouble());
            } else if (type.equals("Float")) {
                for (int k = 0; k < size; k++)
                    vals.add(in.readFloat());
            } else if (type.equals("Integer")) {
                for (int k = 0; k < size; k++)
                    vals.add(in.readInt());
            } else if (type.equals("Long")) {
                for (int k = 0; k < size; k++)
                    vals.add(in.readLong());
            } else if (type.equals("Boolean")) {
                for (int k = 0; k < size; k++)
                    vals.add(in.readBoolean());
            } else
                throw new Exception("Unsupported type:" + type);

            data.addDataColumn(new SimpleColumn(name, vals));
        }

        in.close();

        return data;
    }


    /**
     * Result of comparison of two data sets
     *
     * @author Alexey
     */
    public static class CompareResult {
        // Columns which are present only in the first data set
        public final ArrayList<String> unique1;
        // Columns which are present only in the second data set
        public final ArrayList<String> unique2;

        // Equal columns
        public final ArrayList<String> equal;

        // Different columns
        public final ArrayList<String> different;

        /**
         * Default constructor
         */
        public CompareResult(ArrayList<String> unique1, ArrayList<String> unique2, ArrayList<String> equal, ArrayList<String> different) {
            this.unique1 = unique1;
            this.unique2 = unique2;
            this.equal = equal;
            this.different = different;
        }

        /**
         * Returns true if the data sets are equal
         *
         * @return
         */
        public boolean isEqual() {
            return unique1.size() == 0 && unique2.size() == 0 && different.size() == 0;
        }

        /**
         * Returns true if there is at least one pair of non-equal data columns
         *
         * @return
         */
        public boolean isConflict() {
            return different.size() > 0;
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder(100);

            if (different.size() > 0) {
                str.append("Diff: ");
                for (String s : different) {
                    str.append(s);
                    str.append(", ");
                }
            }

            if (unique1.size() > 0) {
                str.append("; Unique1: ");
                for (String s : unique1) {
                    str.append(s);
                    str.append(", ");
                }
            }

            if (unique2.size() > 0) {
                str.append("; Unique2: ");
                for (String s : unique2) {
                    str.append(s);
                    str.append(", ");
                }
            }

            str.append("; Eq: ");
            for (String s : equal) {
                str.append(s);
                str.append(", ");
            }

            return str.toString();
        }
    }


    /**
     * Compares two data sets
     *
     * @param data
     * @return
     */
    public static CompareResult compare(TestEngineData data1, TestEngineData data2) {
        ArrayList<String> unique1 = new ArrayList<String>();
        ArrayList<String> unique2 = new ArrayList<String>();
        ArrayList<String> equal = new ArrayList<String>();
        ArrayList<String> diff = new ArrayList<String>();

        // Get the columns which are present in both sets
        Set<String> keys1 = new HashSet<String>(data1.map.keySet());
        Set<String> keys2 = new HashSet<String>(data2.map.keySet());

        Set<String> keys = new HashSet<String>(keys1);
        keys.retainAll(data2.map.keySet());

        // Save information about unique columns
        keys1.removeAll(keys);
        keys2.removeAll(keys);

        for (String s : keys1) {
            unique1.add(s);
        }

        for (String s : keys2) {
            unique2.add(s);
        }

        // Compare columns with the same names
        for (String key : keys) {
            DataColumn c1 = data1.map.get(key);
            DataColumn c2 = data2.map.get(key);

            boolean flag = c1.equals(c2);
            if (flag)
                equal.add(key);
            else
                diff.add(key);
        }

        return new CompareResult(unique1, unique2, equal, diff);
    }
}
