package org.spark.runtime.external.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import org.spark.runtime.external.Parameter;
import org.spark.runtime.external.ProxyVariable;


/**
 * A dialog for setting up visualization properties
 * @author Monad
 *
 */
public class ParameterEditDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = -4770465039114801520L;

	// Data for parameters
	private ParameterTableData parameterData;
	// Table for parameters
	private JTable parameterTable;
	
	// Data for parameters
	@SuppressWarnings("serial")
	private class ParameterTableData extends AbstractTableModel {
		// List of all parameters
		private ArrayList<Parameter> data; 

		// Default constructor
		public ParameterTableData() {
		}
		
		
		public Parameter get(int i) {
			if (data == null || i < 0 || i >= data.size())
				return null;
			return data.get(i);
		}
		
		
		// Updates the parameters
		public void update(Collection<Parameter> data) {
			this.data = new ArrayList<Parameter>(data.size());

			// Save only parameters of type Double
			for (Parameter p : data) {
				if (p.getVariable().getType() != ProxyVariable.DOUBLE_TYPE)
					continue;
				
				this.data.add(p);
			}

			fireTableDataChanged();
		}
		

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public int getRowCount() {
			if (data == null)
				return 0;
			return data.size(); 
		}
		
		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "Name";
			case 1:
				return "Overwrite default";
			case 2:
				return "Min";
			case 3:
				return "Max";
			case 4:
				return "Step";
			}
			
			return null;
		}
		
		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case 0:
				return String.class;
			case 1:
				return Boolean.class;
			case 2:
			case 3:
			case 4:
				return Double.class;
			}
			
			return null;
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return false;
			else
				return true;
		}
		

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (data == null || rowIndex >= data.size())
				return null;
			
			Parameter p = data.get(rowIndex);
			// Display user options only
			Parameter.Options opts = p.getUserOptions();
			
			switch (columnIndex) {
			// Name
			case 0:
				return p.getName();
			// User flag
			case 1:
				return p.isUsingUserOptions();
			// min
			case 2:
				return opts.min;
			// max
			case 3:
				return opts.max;
			// step
			case 4:
				return opts.step;
			}
			
			return null;
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			Parameter p = get(row);
			if (p == null)
				return;
			
			boolean modifyOptions = false;
			double min, max, step;
			
			Parameter.Options opts = p.getUserOptions();
			min = opts.min;
			max = opts.max;
			step = opts.step;
			
			switch (col) {
			case 0:
				// Name
				return;
			case 1:
				// User flag
				p.activateUserOptions((Boolean) value);
				return;
			case 2:
				// min
				min = (Double) value;
				modifyOptions = true;
				break;
			case 3:
				// max
				max = (Double) value;
				modifyOptions = true;
				break;
			case 4:
				step = (Double) value;
				modifyOptions = true;
				break;
				
			default:
				return;
			}
			
			if (modifyOptions) {
				p.setUserOptions(new Parameter.Options(min, max, step));
			}
		}

	}
	
	
	/**
	 * Default constructor
	 */
	public ParameterEditDialog() {
		this.setTitle("Parameter Options");
		setup();
	}
	

	/**
	 * Initializes the dialog during its creation process
	 */
	private void setup() {
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

		// Create a table for parameters
		parameterData = new ParameterTableData();
		parameterTable = new JTable(parameterData);
		parameterTable.setColumnSelectionAllowed(false);
		JScrollPane tablePane = new JScrollPane(parameterTable);
		this.add(tablePane);
		this.pack();
	}
	
	
	/**
	 * Initializes all controls
	 */
	public void init(Collection<Parameter> parameters) {
		initParameters(parameters);
		this.pack();
	}
	
	
	/**
	 * Initializes all parameters
	 */
	private void initParameters(Collection<Parameter> parameters) {
		parameterData.update(parameters);
	}
	
	
	/**
	 * Processes commands
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == null)
			return;
		
		cmd = cmd.intern();
		
	}


}

