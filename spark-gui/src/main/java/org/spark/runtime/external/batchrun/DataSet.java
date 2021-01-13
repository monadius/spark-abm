package org.spark.runtime.external.batchrun;

import java.util.ArrayList;

import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.data.DataRow;
import org.spark.runtime.external.data.DataFilter;
import org.spark.runtime.external.data.IDataConsumer;

// TODO: create a data set factory
// with static methods for creating data sets,
// removing data sets, and for updating data sets (as variables)
/**
 * Class for collecting data
 * @author Monad
 */
class DataSet implements IDataConsumer {
	/**
	 * A data item class
	 */
	private static class DataItem {
		public ArrayList<Double> data = new ArrayList<Double>(5000);
		public String variableName;
		
		public DataItem(String variableName) {
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
	
	
	/* Data for all variables */
	private final ArrayList<DataItem> items;
	
	/* Ticks */
	private final ArrayList<Long> ticks = new ArrayList<Long>(5000);
	private long lastTick;

	/* Data filter */
	private final DataFilter dataFilter;
	
	/**
	 * Default internal constructor
	 */
	DataSet() {
		items = new ArrayList<DataItem>();
		dataFilter = new DataFilter(this, "variable");
		lastTick = -1;
	}
	
	
	/**
	 * Returns the data filter
	 * @return
	 */
	public DataFilter getFilter() {
		return dataFilter;
	}
	
	
	/**
	 * Adds a variable to the data set
	 * @param name
	 */
	public synchronized void addVariable(String varName) {
		items.add(new DataItem(varName));
		dataFilter.addData(DataCollectorDescription.VARIABLE, varName);
	}
	
	
	/**
	 * Clears the data set
	 */
	public synchronized void clear() {
		for (DataItem item : items) {
			item.clear();
		}
		
		ticks.clear();
		lastTick = -1;
	}
	
	
	/**
	 * IDataConsumer implementation
	 */
	public synchronized void consume(DataRow data) {
		if (data.getState().isInitialState())
			clear();
		
		long tick = data.getState().getTick();
		
		if (tick == lastTick) {
			for (DataItem item : items) {
				item.replaceLast(data);
			}
		}
		else {
			lastTick = tick;
			ticks.add(tick);
			
			for (DataItem item : items) {
				item.add(data);
			}
		}
	}
	
	
	/**
	 * Returns data for the specific variable at the given
	 * tick points
	 * @param varName
	 * @param ticks could be null, in which case all data is returned
	 * @return null if no such variable in the data set
	 */
	public ArrayList<Double> getDataAtGivenTicks(String varName, ArrayList<Long> ticks) {
		for (DataItem var : items) {
			if (var.variableName.equals(varName)) {
				if (ticks == null)
					return var.data;
				
				ArrayList<Double> data = new ArrayList<Double>(ticks.size());
				
				int currentTickIndex = 0;
				long currentTick = ticks.get(0);
				
				for (int i = 0; i < var.data.size(); i++) {
					long tick = this.ticks.get(i);
					
					if (tick == currentTick) {
						data.add(var.data.get(i));
						
						currentTickIndex++;
						if (currentTickIndex < ticks.size())
							currentTick = ticks.get(currentTickIndex);
						else
							break;
					}
				}
				
				return data;
			}
		}
		
		return null;
	}
	
	
}

