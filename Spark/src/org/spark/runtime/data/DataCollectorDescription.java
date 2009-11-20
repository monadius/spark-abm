package org.spark.runtime.data;

import java.io.Serializable;


/**
 * Contains information about a data collector
 * @author Monad
 *
 */
public class DataCollectorDescription implements Serializable {
	/* Serial version UID */
	private static final long serialVersionUID = 2788126458373243875L;

	
	/* Data collector types */
	public static final int VARIABLE = 1;
	public static final int DATA_LAYER = 2;
	public static final int SPACE_AGENTS = 3;
	public static final int SPACES = 4;
	
	public static final String STR_VARIABLE = "$variable:";
	public static final String STR_DATA_LAYER = "$data-layer:";
	public static final String STR_SPACE_AGENTS = "$space-agents:";
	public static final String STR_SPACES = "$spaces";
	
	
	/* Specifies the type of the data collector */
	private final int type;
	
	/* Describes the data to be collected */
	private final String dataName;
	
	/* Specifies the interval for a data collection process.
	 * interval == 0 means that the data should be collected only at
	 * the beginning and at the end of a simulation
	 */
	private final int interval;
	
	
	/**
	 * Default constructor
	 */
	public DataCollectorDescription(int type, String dataName, int interval) {
		if (interval < 0)
			interval = 0;
		
		if (type < VARIABLE)
			type = VARIABLE;
		else if (type > SPACES)
			type = SPACES;
		
		this.type = type;
		this.dataName = dataName;
		this.interval = interval;
	}

	
	/**
	 * Returns a string description of the given data collector type
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		switch (type) {
		case VARIABLE:
			return STR_VARIABLE;
		
		case DATA_LAYER:
			return STR_DATA_LAYER;
		
		case SPACE_AGENTS:
			return STR_SPACE_AGENTS;
			
		case SPACES:
			return STR_SPACES;
		}
		
		// Undefined data collector
		return "???";
	}
	
	/**
	 * Returns collection interval
	 * @return
	 */
	public int getInterval() {
		return interval;
	}
	
	
	/**
	 * Returns collector's type
	 * @return
	 */
	public int getType() {
		return type;
	}
	
	
	/**
	 * Returns a string description of the type
	 * @return
	 */
	public String getTypeString() {
		return typeToString(type);
	}
	
	
	/**
	 * Returns collected data name
	 * @return
	 */
	public String getDataName() {
		return dataName;
	}
	
	
	/**
	 * Compares two data collector descriptions
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		if (this == obj)
			return true;
		
		if (!(obj instanceof DataCollectorDescription))
			return false;
		
		DataCollectorDescription dcd = (DataCollectorDescription) obj;
		
		if (type == dcd.type && interval == dcd.interval) {
			if (dataName == null)
				return dcd.dataName == null;
			else
				return dataName.equals(dcd.dataName);
		}
		else
			return false;
	}
	
	
	@Override
	public int hashCode() {
		int code = type * 31;
		
		if (dataName != null)
			code ^= dataName.hashCode();
		
		if (interval == 0)
			return code;
		else
			return code * (interval + 1);
	}
	
	
	@Override
	public String toString() {
		String str = typeToString(type);
		
		if (dataName != null)
			str += dataName;
		
		str += ":" + interval;
		
		return str;
	}
}
