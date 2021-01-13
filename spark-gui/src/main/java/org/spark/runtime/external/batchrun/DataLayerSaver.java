package org.spark.runtime.external.batchrun;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sparkabm.runtime.data.DataCollectorDescription;
import org.sparkabm.runtime.data.DataObject_Grid;
import org.sparkabm.runtime.data.DataRow;
import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.data.DataFilter;
import org.spark.runtime.external.data.IDataConsumer;

/**
 * Saves data layers
 * @author Monad
 */
class DataLayerSaver implements IDataConsumer {
	private final static Logger log = LogManager.getLogger();
	
	// Names of data layers
	private final ArrayList<String> dataLayers;
	
	// If true, then all data is saved in one file
	private boolean oneFileFlag;
	
	// Precision of saved numerical values
	private int savePrecision;
	
	// Prefix of output file names
	private String fileNamePrefix;
	
	/* Data filter */
	private final DataFilter dataFilter;
	
	private long lastTick;
	
	private final ArrayList<PrintWriter> fileWriters;
	private PrintWriter writer;
	
	/**
	 * Default internal constructor
	 */
	DataLayerSaver() {
		dataLayers = new ArrayList<String>();
		dataFilter = new DataFilter(this, "grid");
		lastTick = -1;
		fileWriters = new ArrayList<PrintWriter>();
		fileNamePrefix = "grid";
		savePrecision = 5;
	}
	
	/**
	 * Returns the data filter
	 * @return
	 */
	public DataFilter getFilter() {
		return dataFilter;
	}
	
	/**
	 * Sets the prefix of output files
	 */
	public void setFileNamePrefix(String prefix) {
		if (prefix == null) {
			prefix = "grid";
		}
		
		this.fileNamePrefix = prefix;
	}
	
	/**
	 * Sets the precision of saved numerical values
	 */
	public void setPrecision(int precision) {
		this.savePrecision = precision;
	}
	
	/**
	 * Sets the flag for saving all data in one file
	 */
	public void setOneFileFlag(boolean flag) {
		this.oneFileFlag = flag;
	}
	
	/**
	 * Adds a data layer
	 * @param name
	 */
	public synchronized void addDataLayer(String name) {
		dataLayers.add(name);
		dataFilter.addData(DataCollectorDescription.DATA_LAYER, name);
	}
	
	/**
	 * Removes all data layers
	 */
	public synchronized void removeAllDataLayers() {
		for (String name : dataLayers) {
			dataFilter.removeData(DataCollectorDescription.DATA_LAYER, name);
		}
		
		dataLayers.clear();
	}
	
	/**
	 * Closes all open files
	 */
	public synchronized void clear() {
		if (writer != null) {
			writer.close();
			writer = null;
		}
		
		for (PrintWriter w : fileWriters) {
			if (w != null) {
				w.close();
			}
		}
		
		fileWriters.clear();
		lastTick = -1;
	}
	
	/**
	 * Saves the data
	 */
	private void save(long tick, DataRow row) {
		Coordinator c = Coordinator.getInstance();
		String format;
		if (savePrecision > 0) {
			format = "%." + savePrecision + "g";
		}
		else {
			format = "%g";
		}
		
		// Create new files (if necessary)
		try {
			if (oneFileFlag) {
				if (writer == null) {
					File file = new File(c.getOutputDir(), fileNamePrefix + "grids.csv");
					writer = new PrintWriter(file);
				}
			}
			else {
				if (fileWriters.size() == 0) {
					for (String name : dataLayers) {
						File file = new File(c.getOutputDir(), 
								fileNamePrefix + name + "-grid.csv");
						fileWriters.add(new PrintWriter(file));
					}
				}
			}
		}
		catch (Exception e) {
			log.error(e.getMessage());
		}
		
		// Save the data
		int index = 0;
		for (String name : dataLayers) {
			StringBuilder str = new StringBuilder(100000);

			// tick
			str.append(tick);
			str.append(',');
			
			// name
			str.append(name);
			str.append(',');
			
			// data
			DataObject_Grid data = row.getGrid(name);
			if (data == null) {
				continue;
			}
			
			int xSize = data.getXSize();
			int ySize = data.getYSize();
			
			for (int y = 0; y < ySize; y++) {
				for (int x = 0; x < xSize; x++) {
					double v = data.getValue(x, y);
					str.append(String.format(format, v));
					str.append(',');
				}
			}
			
			PrintWriter w;
			if (oneFileFlag) {
				w = writer;
			}
			else {
				w = fileWriters.get(index);
			}
			
			w.println(str);
			index++;
		}
	}
	
	/**
	 * IDataConsumer implementation
	 */
	public synchronized void consume(DataRow data) {
		if (data.getState().isInitialState()) {
			clear();
		}
		
		long tick = data.getState().getTick();
		
		if (tick > lastTick) {
			lastTick = tick;
			if (dataLayers.size() > 0) {
				save(tick, data);
			}
		}
	}
}

