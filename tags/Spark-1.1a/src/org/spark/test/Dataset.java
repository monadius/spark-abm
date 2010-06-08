package org.spark.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.spark.core.Agent;
import org.spark.core.Observer;
import org.spark.data.DataLayer;
import org.spark.runtime.internal.ModelVariable;
import org.spark.space.Space;

/**
 * Stores data collected in a simulation
 * @author Monad
 */
class Dataset {
	/**
	 * Abstract representation of a data column
	 * @author Monad
	 *
	 */
	abstract static class DataColumn {
		// Name of the column
		private final String name;
		// Collected data
		protected final ArrayList<Object> data;
		
		/**
		 * Default constructor
		 * @param name
		 */
		public DataColumn(String name, int simulationLength) {
			this.name = name;
			this.data = new ArrayList<Object>(simulationLength);
		}
		
		/**
		 * Returns column's name
		 * @return
		 */
		public final String getName() {
			return name;
		}
		
		/**
		 * Returns collected data
		 * @return
		 */
		public final ArrayList<Object> getData() {
			return data;
		}

		/**
		 * Returns a new data value
		 * @param tick
		 * @return
		 */
		public abstract Object getData(long tick);
		
		/**
		 * Updates the data
		 * @param tick
		 */
		public final void updateData(long tick) {
			Object value = getData(tick);
			data.add(value);
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
	}
	
	
	/**
	 * A data column with the specified data values
	 * @author Monad
	 *
	 */
	static class SimpleColumn extends DataColumn {
		/**
		 * Default constructor
		 * @param name
		 * @param length
		 */
		public SimpleColumn(String name, ArrayList<Object> data) {
			super(name, data.size());
			
			for (int i = 0; i < data.size(); i++) {
				this.data.add(data.get(i));
			}
		}
		
		@Override
		public Object getData(long tick) {
			return null;
		}
	}
	
	
	/**
	 * A data column for the number of ticks
	 * @author Monad
	 *
	 */
	static class TickColumn extends DataColumn {
		/**
		 * Default constructor
		 * @param length of the simulation
		 */
		public TickColumn(int length) {
			super("$tick", length);
		}
		
		@Override
		public Object getData(long tick) {
			return tick;
		}
	}
	
	
	/**
	 * A data column for a model variable
	 * @author Monad
	 *
	 */
	static class VariableColumn extends DataColumn {
		private final ModelVariable var;

		/**
		 * Default constructor
		 * @param var
		 * @param length
		 */
		public VariableColumn(ModelVariable var, int length) {
			super(var.getName(), length);
			this.var = var;
		}
		
		@Override
		public Object getData(long tick) {
			return var.getValue();
		}
	}
	
	
	/**
	 * A data column for the number of agents
	 * @author Monad
	 *
	 */
	static class AgentNumberColumn extends DataColumn {
		private final Class<Agent> agentType;
		
		/**
		 * Default constructor
		 * @param type
		 * @param length
		 */
		public AgentNumberColumn(Class<Agent> type, int length) {
			super("$number-" + type.getSimpleName(), length);
			
			this.agentType = type;
		}
		
		@Override
		public Object getData(long tick) {
			int n = Observer.getInstance().getAgentsNumber(agentType);
			return n;
		}
	}
	
	
	/**
	 * A data column for the total value stored in a data layer
	 * @author Monad
	 *
	 */
	static class DataLayerSumColumn extends DataColumn {
		private final DataLayer dataLayer;
		
		/**
		 * Default constructor
		 * @param space
		 * @param name
		 * @param length
		 */
		public DataLayerSumColumn(String spaceName, String name, int length) {
			super("$datalayer-" + spaceName + '-' + name, length);
			
			Space space = Observer.getSpace(spaceName);
			this.dataLayer = space.getDataLayer(name);
		}
		
		@Override
		public Object getData(long tick) {
			double sum = dataLayer.getTotalNumber();
			return sum;
		}
	}
	
	
	/* Contains all data columns with their names */
	public HashMap<String, DataColumn> map = new HashMap<String, DataColumn>();
	
	/* Contains all data columns in a specific order */
	public ArrayList<DataColumn> data = new ArrayList<DataColumn>();
	
	
	/**
	 * Adds a data column to the data set
	 * @param column
	 */
	public void addDataColumn(DataColumn column) throws Exception {
		String name = column.getName();
		
		if (map.containsKey(name))
			throw new Exception("Data column '" + name + "' is already defined");
		
		map.put(name, column);
		data.add(column);
	}
	
	
	/**
	 * Default constructor
	 */
	public Dataset(int simulationLength, Class<Agent>[] agentTypes, ModelVariable[] vars) {
		try {
			// Add the tick data column 
			addDataColumn(new TickColumn(simulationLength));
			
			// Add variables' data columns
			for (ModelVariable var : vars) {
				addDataColumn(new VariableColumn(var, simulationLength));
			}
			
			// Add agents' columns
			if (agentTypes != null) {
				for (Class<Agent> type : agentTypes) {
					addDataColumn(new AgentNumberColumn(type, simulationLength));
				}
			}
			
			
			// Add data layers' columns
			String[] spaceNames = Observer.getInstance().getSpaceNames();
			for (String spaceName : spaceNames) {
				// Get space
				Space space = Observer.getSpace(spaceName);
				
				String[] dataLayerNames = space.getDataLayerNames();
				for (String name : dataLayerNames) {
					// Add data layer column
					addDataColumn(new DataLayerSumColumn(spaceName, name, simulationLength));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Private constructor
	 */
	private Dataset() {
	}
	
	
	/**
	 * Updates the data set
	 * @param tick
	 */
	public void update(long tick) {
		for (DataColumn column : data) {
			column.updateData(tick);
		}
	}
	
	
	/**
	 * Saves the data into a text file
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
	 * @param file
	 */
	public void saveAsBinary(File file) throws IOException {
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
			}
			else if (val instanceof Float) {
				out.writeUTF("Float");
				
				for (int k = 0; k < size; k++)
					out.writeFloat((Float) vals.get(k));
			}
			else if (val instanceof Integer) {
				out.writeUTF("Integer");
				
				for (int k = 0; k < size; k++)
					out.writeInt((Integer) vals.get(k));
			}
			else if (val instanceof Long) {
				out.writeUTF("Long");
				
				for (int k = 0; k < size; k++)
					out.writeLong((Long) vals.get(k));
			}
		}
		
		out.close();
	}
	
	
	/**
	 * Reads the data set from the given binary file
	 * @param file
	 */
	public static Dataset readData(File file) throws Exception {
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream in = new ObjectInputStream(fis);
		
		Dataset data = new Dataset();
		
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
			}
			else if (type.equals("Float")) {
				for (int k = 0; k < size; k++)
					vals.add(in.readFloat());
			}
			else if (type.equals("Integer")) {
				for (int k = 0; k < size; k++)
					vals.add(in.readInt());
			}
			else if (type.equals("Long")) {
				for (int k = 0; k < size; k++)
					vals.add(in.readLong());
			}

			data.addDataColumn(new SimpleColumn(name, vals));
		}
		
		in.close();
		
		return data;
	}
	
	
	@Override
	/**
	 * Compares two data sets and returns true if they
	 * contain the same data
	 */
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Dataset))
			return false;
		
		Dataset set = (Dataset) obj;
		
		// Compare the number of data columns
		if (set.data.size() != data.size())
			return false;
		
		// Compare data columns
		for (DataColumn column : data) {
			DataColumn column2 = set.map.get(column.getName());
			
			if (column2 == null)
				return false;
			
			if (!column.equals(column2))
				return false;
		}
		
		return true;
	}
}
