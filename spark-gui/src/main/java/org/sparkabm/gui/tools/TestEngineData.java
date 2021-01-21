package org.sparkabm.gui.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.*;

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
        protected final List<Object> data;

        // The last tick for which data was saved
        private transient long lastTick;

        /**
         * Default constructor
         */
        public DataColumn(String name, int simulationLength) {
            this.name = name;
            this.data = new ArrayList<>(simulationLength);
            this.lastTick = -1;
        }

        /**
         * Returns column's name
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
         */
        public final List<Object> getData() {
            return data;
        }


        /**
         * Adds a new value to the data
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
            if (!(obj instanceof DataColumn))
                return false;

            DataColumn column2 = (DataColumn) obj;

            // Compare names
            if (!name.equals(column2.getName())) {
                return false;
            }

            // Compare data
            if (data.equals(column2.data)) {
                return true;
            }

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
         */
        public SimpleColumn(String name, ArrayList<Object> data) {
            super(name, data.size());
            this.data.addAll(data);
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
            if (row.getState().isInitialState()) {
                tick = -1;
            }

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
         */
        public VariableColumn(String varName, int length) {
            super(varName, length);
            this.varName = varName;
        }

        @Override
        public void consume(DataRow row) {
            DataObject val = row.get(DataCollectorDescription.VARIABLE, varName);
            if (val == null) {
                update(val, row);
            }
            else if (val instanceof DataObject_Double) {
                update(((DataObject_Double) val).getValue(), row);
            }
            else if (val instanceof DataObject_Bool) {
                update(((DataObject_Bool) val).getValue(), row);
            }
            else {
                throw new Error("Unknown data type");
            }
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
    private final Map<String, DataColumn> map = new HashMap<>();

    /* Contains all data columns in a fixed order */
    private final List<DataColumn> data = new ArrayList<>();

    // The main data filter
    private DataFilter dataFilter;


    /**
     * Returns the data filter
     */
    public DataFilter getDataFilter() {
        return dataFilter;
    }


    /**
     * Adds a data column to the data set
     */
    public void addDataColumn(DataColumn column) throws Exception {
        String name = column.getName();

        if (map.containsKey(name)) {
            throw new Exception("Data column '" + name + "' is already defined");
        }

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
     */
    public void saveAsText(File file) throws IOException {
        // No data
        if (data.size() == 0) return;

        int n = data.size();

        FileOutputStream fos = new FileOutputStream(file);
        PrintStream out = new PrintStream(fos);

        // Save headers
        for (int i = 0; i < n; i++) {
            DataColumn column = data.get(i);

            out.print(column.getName());
            if (i < n - 1) out.print(',');
        }

        out.println();
        int size = data.get(0).getData().size();
        for (int k = 0; k < size; k++) {
            // Save data values
            for (int i = 0; i < n; i++) {
                DataColumn column = data.get(i);

                out.print(column.getData().get(k));
                if (i < n - 1) out.print(',');
            }

            out.println();
        }

        out.close();
    }

    /**
     * Saves the data into a binary file
     */
    public void saveAsBinary(File file) throws Exception {
        // No data
        if (data.size() == 0) return;

        int n = data.size();

        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream out = new ObjectOutputStream(fos);

        // Save the number of columns
        out.writeInt(n);

        // Save data
        for (DataColumn column : data) {
            // Write out column's name
            out.writeUTF(column.getName());

            // Write out column's data
            List<Object> vals = column.getData();

            int size = vals.size();
            out.writeInt(size);

            if (size == 0)
                continue;

            // Write out the type
            Object val = vals.get(0);

            // TODO: other types
            if (val instanceof Double) {
                out.writeUTF("Double");
                for (Object o : vals) {
                    out.writeDouble((Double) o);
                }
            } else if (val instanceof Float) {
                out.writeUTF("Float");
                for (Object o : vals) {
                    out.writeFloat((Float) o);
                }
            } else if (val instanceof Integer) {
                out.writeUTF("Integer");
                for (Object o : vals) {
                    out.writeInt((Integer) o);
                }
            } else if (val instanceof Long) {
                out.writeUTF("Long");
                for (Object o : vals) {
                    out.writeLong((Long) o);
                }
            } else if (val instanceof Boolean) {
                out.writeUTF("Boolean");
                for (Object o : vals) {
                    out.writeBoolean((Boolean) o);
                }
            } else {
                throw new Exception("Unsupported data type: " + val);
            }
        }

        out.close();
    }

    /**
     * Reads the data set from the given binary file
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

            ArrayList<Object> vals = new ArrayList<>(size);

            if (size == 0) {
                data.addDataColumn(new SimpleColumn(name, vals));
                continue;
            }

            // Read the type
            String type = in.readUTF();
            switch (type) {
                case "Double":
                    for (int k = 0; k < size; k++) {
                        vals.add(in.readDouble());
                    }
                    break;
                case "Float":
                    for (int k = 0; k < size; k++) {
                        vals.add(in.readFloat());
                    }
                    break;
                case "Integer":
                    for (int k = 0; k < size; k++) {
                        vals.add(in.readInt());
                    }
                    break;
                case "Long":
                    for (int k = 0; k < size; k++) {
                        vals.add(in.readLong());
                    }
                    break;
                case "Boolean":
                    for (int k = 0; k < size; k++) {
                        vals.add(in.readBoolean());
                    }
                    break;
                default:
                    throw new Exception("Unsupported type:" + type);
            }

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
        public final List<String> unique1;
        // Columns which are present only in the second data set
        public final List<String> unique2;

        // Equal columns
        public final List<String> equal;

        // Different columns
        public final List<String> different;

        /**
         * Default constructor
         */
        public CompareResult(List<String> unique1, List<String> unique2, List<String> equal, List<String> different) {
            this.unique1 = unique1;
            this.unique2 = unique2;
            this.equal = equal;
            this.different = different;
        }

        /**
         * Returns true if the data sets are equal
         */
        public boolean isEqual() {
            return unique1.size() == 0 && unique2.size() == 0 && different.size() == 0;
        }

        /**
         * Returns true if there is at least one pair of non-equal data columns
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
     */
    public static CompareResult compare(TestEngineData data1, TestEngineData data2) {
        List<String> equal = new ArrayList<>();
        List<String> diff = new ArrayList<>();

        // Get the columns which are present in both sets
        Set<String> keys1 = new HashSet<>(data1.map.keySet());
        Set<String> keys2 = new HashSet<>(data2.map.keySet());

        Set<String> keys = new HashSet<>(keys1);
        keys.retainAll(data2.map.keySet());

        // Save information about unique columns
        keys1.removeAll(keys);
        keys2.removeAll(keys);

        List<String> unique1 = new ArrayList<>(keys1);
        List<String> unique2 = new ArrayList<>(keys2);

        // Compare columns with the same names
        for (String key : keys) {
            DataColumn c1 = data1.map.get(key);
            DataColumn c2 = data2.map.get(key);

            boolean flag = c1.equals(c2);
            if (flag) {
                equal.add(key);
            }
            else {
                diff.add(key);
            }
        }

        return new CompareResult(unique1, unique2, equal, diff);
    }
}
