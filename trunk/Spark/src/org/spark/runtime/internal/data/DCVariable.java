package org.spark.runtime.internal.data;

import org.spark.core.SparkModel;
import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.data.DataObject;
import org.spark.runtime.data.DataObject_Bool;
import org.spark.runtime.data.DataObject_Double;
import org.spark.runtime.data.DataObject_Integer;
import org.spark.runtime.data.DataObject_Long;
import org.spark.runtime.internal.ModelVariable;

/**
 * Collects values of the given variable
 * 
 * @author Monad
 * 
 */
public class DCVariable extends DataCollector {
	protected String varName;
	protected ModelVariable variable;

	/**
	 * Creates the variable data collector for the given variable
	 * 
	 * @param varName
	 */
	DCVariable(String varName) {
		this.varName = varName;
		this.variable = null;

		this.dataName = DataCollectorDescription
				.typeToString(DataCollectorDescription.VARIABLE)
				+ varName;
	}

	/**
	 * Creates the variable data collector for the given variable
	 * 
	 * @param var
	 */
	public DCVariable(ModelVariable var) {
		this.varName = var.getName();
		this.variable = var;

		this.dataName = "$variable:" + varName;
	}

	@Override
	public DataObject collect0(SparkModel model) throws Exception {
		if (variable == null) {
			if (varName == null)
				return null;

			variable = model.getVariable(varName);
			if (variable == null)
				throw new BadDataSourceException("Variable " + varName
						+ " is not defined");
		}

		Object value = variable.getValue();
		if (value == null)
			return null;

		if (value instanceof Double) {
			return new DataObject_Double((Double) value);
		}

		if (value instanceof Long) {
			return new DataObject_Long((Long) value);
		}

		if (value instanceof Integer) {
			return new DataObject_Integer((Integer) value);
		}

		if (value instanceof Boolean) {
			return new DataObject_Bool((Boolean) value);
		}

		throw new Exception("Unsupported data type: " + value.getClass());
	}

	@Override
	public void reset() {
		// TODO: it is not required
		variable = null;
	}

}
