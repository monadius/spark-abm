package org.spark.runtime.external.data;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.data.DataRow;
import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.ParameterCollection;
import org.spark.utils.XmlDocUtils;
import org.w3c.dom.Node;

import com.spinn3r.log5j.Logger;

/**
 * Temporary data set class
 * @author Alexey
 *
 */
// TODO: implement real data sets
public class DataSetTmp implements IDataConsumer {
	private static final Logger logger = Logger.getLogger();
	
	/* Data filter */
	private final DataFilter dataFilter;
	
	/**
	 * A data item class
	 */
	private static class DataItem {
		public ArrayList<Double> data = new ArrayList<Double>(5000);
		public String name;
		public String variableName;
		
		public DataItem(String name, String variableName) {
			this.name = name;
			this.variableName = variableName;
		}
		
		
		public void clear() {
			data.clear();
		}
		
		
		public void add(DataRow row) {
			Double number = row.getVarDoubleValue(variableName);
			data.add(number);
		}
		
		
		public void replaceLast(DataRow row) {
			Double number = row.getVarDoubleValue(variableName);
			int n = data.size();
			data.remove(n - 1);
			data.add(number);
		}
	}

	/* List of collected data items */
	private final ArrayList<DataItem> dataItems = new ArrayList<DataItem>(10);
	
	/* Ticks */
	private final ArrayList<Long> ticks = new ArrayList<Long>(5000);
	private long lastTick;
	
	
	/**
	 * Creates a data set from the given xml node
	 * @param node
	 * @param owner
	 */
	public DataSetTmp(Node node) {
		// Create data filter and load data items
		dataFilter = new DataFilter(this, "variable");
//		name = XmlDocUtils.getValue(node, "name", "Data");
		int interval = XmlDocUtils.getIntegerValue(node, "interval", 1);		

		dataFilter.setInterval(interval);
		dataFilter.setSynchronizedFlag(true);
		
		lastTick = -1;
	
		ArrayList<Node> items = XmlDocUtils.getChildrenByTagName(node, "item");
		for (int i = 0; i < items.size(); i++) {
			Node itemNode = items.get(i);
			addItem(itemNode);
		}
	}
	
	
	/**
	 * Returns the data filter
	 * @return
	 */
	public DataFilter getDataFilter() {
		return dataFilter;
	}

	
	/**
	 * Adds an item for collection
	 * @param item
	 */
	private void addItem(Node item) {
		String name = XmlDocUtils.getValue(item, "name", "???");
		String varName = XmlDocUtils.getValue(item, "variable", null);
		
		if (varName == null) {
			logger.error("Undefined variable name for the item: " + name);
			return;
		}
		
		dataFilter.addData(DataCollectorDescription.VARIABLE, varName);
		dataItems.add(new DataItem(name, varName));
	}
	

	/**
	 * IDataConsumer implementation
	 */
	public synchronized void consume(DataRow data) {
		if (data.getState().isInitialState())
			reset();
		
		long tick = data.getState().getTick();
		
		if (tick == lastTick) {
			for (DataItem item : dataItems) {
				item.replaceLast(data);
			}
		}
		else {
			lastTick = tick;
			ticks.add(tick);
			
			for (DataItem item : dataItems) {
				item.add(data);
			}
		}
	}
	

	/**
	 * Resets the data set
	 */
	public synchronized void reset() {
		ticks.clear();
		lastTick = -1;
		
		for (int i = 0; i < dataItems.size(); i++) {
			dataItems.get(i).clear();
		}
	}

	
	/**
	 * Saves data in the given file
	 * @param fname
	 */
	public void saveData(String fname) {
		File file = new File(Coordinator.getInstance().getCurrentDir(), fname);
		saveData(file);
	}
	
	
	/**
	 * Saves data in the given file
	 * @param file
	 */
	public void saveData(File file) {
		PrintStream out = null;
		
		try {
			out = new PrintStream(file);
			saveData(out);
		}
		catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		finally {
			if (out != null)
				out.close();
		}
	}
	
	
	/**
	 * Saves all data into the given output stream
	 * @param out
	 */
	public synchronized void saveData(PrintStream out) {
		Coordinator c = Coordinator.getInstance();
		out.println("Random seed");
		out.println(c.getInitialState().getSeed());

		ParameterCollection parameters = c.getParameters();
		if (parameters != null) {
			parameters.saveParameters(out);
		}

		out.println("Experiment");
		
        try {
			int n = dataItems.size();

			out.print("Tick");
			if (n > 0)
				out.print(',');
			
			for (int i = 0; i < n; i++) {
				out.print(dataItems.get(i).name);
				if (i != n - 1)
					out.print(',');
			}

			out.println();

			int l = ticks.size();
			for (int k = 0; k < l; k++) {
				out.print(ticks.get(k));
				if (n > 0)
					out.print(',');

				for (int i = 0; i < n; i++) {
					Object Entry = dataItems.get(i).data.get(k);
					if (Entry == null) out.print("n/a");
					else out.print((Number)Entry);
					
					if (i != n - 1)
						out.print(',');
				}

				out.println();
			}
        } finally {
        	out.flush();
        }
	}
	

}
