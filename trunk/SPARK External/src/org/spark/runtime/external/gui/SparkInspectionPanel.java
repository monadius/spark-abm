package org.spark.runtime.external.gui;

import java.awt.GridLayout;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.data.DataObject_Inspection;
import org.spark.runtime.data.DataRow;
import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.data.DataFilter;
import org.spark.runtime.external.data.IDataConsumer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Displays inspection information  
 */
@SuppressWarnings("serial")
public class SparkInspectionPanel extends JPanel implements ISparkPanel, IDataConsumer {
	// Data filter
	private final DataFilter dataFilter;
	// Name of the inspector
	private final String name;
	
	private SparkWindow win;
	
	/**
	 * Default constructor
	 */
	public SparkInspectionPanel(WindowManager manager, String name) {
//		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setLayout(new GridLayout(0, 1));
		this.name = name;
		this.dataFilter = new DataFilter(this, "inspector");
		
		// Get the window name
		String location = manager.getGoodName("Inspection: " + name);
		if (location == null)
			return;
		
		win = manager.setLocation(this, location);
		win.doNotSave = true;
		
		Coordinator.getInstance().getDataReceiver().addDataConsumer(dataFilter);
	}
	
	
	/**
	 * Initializes the inspector
	 * @param parameters
	 */
	public void init(DataObject_Inspection.Parameters parameters) {
		dataFilter.removeAllData();
		dataFilter.addData(DataCollectorDescription.INSPECTION_DATA, name, parameters);
	}
	
	
	/**
	 * Updates the information
	 * @param data
	 */
	private void update(DataObject_Inspection data) {
		this.removeAll();
		
		int n = data.getObjects().size();
//		this.setLayout(new GridLayout(n, 1));
		
		for (int i = 0; i < n; i++) {
			String name = data.getObjects().get(i).objectName;
			JLabel label = new JLabel(name);
			this.add(label);
		}
		
		win.pack();
//		this.setPreferredSize(new Dimension(100, 200));
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
		
		update(data);
	}

}
