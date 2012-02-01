package org.spark.runtime.external.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.data.DataRow;
import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.data.DataFilter;
import org.spark.runtime.external.data.IDataConsumer;
import org.spark.utils.XmlDocUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A chart panel
 * @author Monad
 */
@SuppressWarnings("serial")
public class SparkChartPanel extends JPanel implements ActionListener, ISparkPanel, IDataConsumer {
	// The corresponding node in the document (could be null for user-created charts)
	private Node xmlNode;
	// The window where this chart panel is located
	private SparkWindow win;
	
	// Control commands
	private static final String CMD_CLEAR = "clear";
	private static final String CMD_SAVE = "save";
	private static final String CMD_REMOVE = "remove";
	
	// Control elements
	private JButton clearButton;
	private JButton saveButton;
	private JButton removeButton;

	// Name of the chart
//	private String name;
	// Main dataset
	private XYSeriesCollection dataset;
	
	// Information about plotted series
	private static class SeriesInfo {
		public final String varName;
		public final String label;
		public final XYSeries series;
		
		public SeriesInfo(String varName, String label, XYSeries series) {
			this.varName = varName;
			this.label = label;
			this.series = series;
		}
	}
	
	private final ArrayList<SeriesInfo> series = new ArrayList<SeriesInfo>(5);
	
	private DataFilter dataFilter;

	
	/**
	 * Creates a chart panel based on the given xml-node
	 */
	public SparkChartPanel(WindowManager manager, Node node) {
		this.xmlNode = node;
		
		int interval = XmlDocUtils.getIntegerValue(node, "interval", 1);
		String location = XmlDocUtils.getValue(node, "location", null);
		
		init(manager, interval, location);
		addSeries(node);
	}
	
	
	/**
	 * Constructor
	 */
	public SparkChartPanel(WindowManager manager, int interval, String location) {
		init(manager, interval, location);
	}
	

	/**
	 * Initializes the chart panel
	 */
	private void init(WindowManager manager, int interval, String location) {
		setLayout(new BorderLayout());
		
		this.dataFilter = new DataFilter(this, "variable");
		
		// TODO: distinct intervals for distinct variables
		if (interval < 1)
			interval = 1;
		
		setupChart();

		this.win = manager.setLocation(this, location);
	}
	

	
	
	/**
	 * Returns chart's data filter
	 * @return
	 */
	public DataFilter getDataFilter() {
		return dataFilter;
	}
	
	
	/**
	 * Adds a chart information to the existing chart panel
	 * @param node
	 */
	public void addSeries(Node node) {
		String varNames = XmlDocUtils.getValue(node, "variable", null);
		String label = XmlDocUtils.getValue(node, "label", "");

		if (varNames != null) {
			String[] names = varNames.split(",");
			String[] labels = label.split(",");
			int i = 0;
			
			for (String name : names) {
				label = labels[i];
				name = name.trim();

				addSeries(name, label);
				if (i < labels.length - 1)
					i += 1;
			}
		}		
	}
	
	
	/**
	 * Adds a series to the chart
	 */
	public void addSeries(String varName, String label) {
		XYSeries s = new XYSeries(label, false);
		
		series.add(new SeriesInfo(varName, label, s));
		dataset.addSeries(s);

		dataFilter.addData(DataCollectorDescription.VARIABLE, varName);
	}
	

	/**
	 * Creates a JFreeChart
	 */
	private void setupChart() {
		// Create a dataset
		dataset = new XYSeriesCollection();
		
		// Create a chart
		JFreeChart chart = ChartFactory.createXYLineChart(null, "x", "y",
				dataset, PlotOrientation.VERTICAL, true, false, false);
		
		// Create a chart panel
		ChartPanel panel = new ChartPanel(chart);
		panel.setMinimumSize(new Dimension(100, 100));
		panel.setPreferredSize(new Dimension(300, 200));

		// Create additional control elements
		JPanel controlPanel = new JPanel(new GridLayout(1, 3));

		clearButton = new JButton("Clear");
		saveButton = new JButton("Save");
		removeButton = new JButton("Remove");
		
		clearButton.setActionCommand(CMD_CLEAR);
		saveButton.setActionCommand(CMD_SAVE);
		removeButton.setActionCommand(CMD_REMOVE);

		clearButton.addActionListener(this);
		saveButton.addActionListener(this);
		removeButton.addActionListener(this);


		// Add controls to the control panel
		controlPanel.add(clearButton);
		controlPanel.add(saveButton);
		controlPanel.add(removeButton);

		this.add(panel, BorderLayout.CENTER);
		this.add(controlPanel, BorderLayout.SOUTH);
	}

	
	protected void addValue(double x, double y, int index) {
		addValue(x, y, this.series.get(index));
	}
	
	
	protected void addValue(double x, double y, SeriesInfo info) {
		synchronized (info.series) {
			info.series.add(x, y);
		}
	}

	
	/**
	 * Removes the panel and its window
	 */
	public void remove() {
		String message = "Do you want to remove the window ";
		message += win.getName();
		message += "?";
		int result = JOptionPane.showConfirmDialog(this, message, "Kill", JOptionPane.YES_NO_OPTION);
		
		if (result == JOptionPane.YES_OPTION) {
			if (Coordinator.getInstance().getWindowManager().removeWindow(win)) {
				dataFilter.removeAllData();
				Coordinator.getInstance().getDataReceiver().removeDataConsumer(dataFilter);
				
				if (xmlNode != null)
					xmlNode.getParentNode().removeChild(xmlNode);
			}
		}
	}

	

	/**
	 * Resets all data
	 */
	public synchronized void reset() {
//		dataset.removeAllSeries();
		for (SeriesInfo info : series) {
			synchronized (info.series) {
				// TODO: very often causes an exception
				// because the series can be in use in the redrawing process
				info.series.clear();
//				info.series = new XYSeries("", false, false);
//				dataset.addSeries(info.series);
			}
		}
	}

	/**
	 * Save data dialog
	 * 
	 * @throws Exception
	 */
	private void saveDataFile() throws Exception {
/*		final JFileChooser fc = new JFileChooser(GUIModelManager.getInstance()
				.getCurrentDirectory());
		fc.setFileFilter(new FileFilter() {

			// Accept all directories and all txt files
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}

				String extension = MainFrame.getExtension(f);
				if (extension != null) {
					if (extension.equals("txt"))
						return true;
					else
						return false;
				}
				return false;
			}

			// The description of this filter
			public String getDescription() {
				return "*.txt";
			}
		});

		int returnVal = fc.showSaveDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			PrintStream out = new PrintStream(file);
			saveData(out);

		}*/
	}

	/**
	 * Writes out data into the output stream
	 * 
	 * @param out
	 */
/*	public synchronized void saveData(PrintStream out) {
		out.println("tick\t" + name);

		int l = series.getItemCount();
		for (int k = 0; k < l; k++) {
			out.print(series.getX(k));
			out.print('\t');
			out.print(series.getY(k));
			out.println();
		}

		out.flush();
		out.close();
	}
*/
	
	/**
	 * Processes control elements actions
	 */
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();
		if (cmd == null)
			return;
		
		cmd = cmd.intern();

		try {
			// clear
			if (cmd == CMD_CLEAR) {
				reset();
				return;
			}

			// save
			if (cmd == CMD_SAVE) {
				saveDataFile();
				return;
			}
			
			// remove
			if (cmd == CMD_REMOVE) {
				remove();
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Consumes data
	 */
	public void consume(DataRow row) {
		if (row.getState().isInitialState())
			reset();
		
		long tick = row.getState().getTick();
		
		for (SeriesInfo info : series) {
			Double newValue = row.getVarDoubleValue(info.varName);
			if (newValue == null)
				continue;
		
			try {
				addValue(tick, newValue, info);
			}
			catch (Exception e) {
			}
		}
	}
	

	/**
	 * Updates XML node
	 */
	public void updateXML(SparkWindow location, Document xmlModelDoc, Node interfaceNode, File xmlModelFile) {
		// TODO: update an existing node (when an editing mode is implemented)
		// Existing problem: separate chart nodes from xml-document are combined in one
		// chart panel now. Saving the existing chart panel will result in an inconsistency.
		if (xmlNode != null)
			return;

		// Get variable names and labels
		StringBuilder varNames = new StringBuilder();
		StringBuilder labels = new StringBuilder();
		
		int n = series.size();
		for (int i = 0; i < n; i++) {
			SeriesInfo info = series.get(i);
			varNames.append(info.varName);
			labels.append(info.label);
			
			if (i < n - 1) {
				varNames.append(',');
				labels.append(',');
			}
		}
		
		xmlNode = xmlModelDoc.createElement("user-chart");
		// location
		XmlDocUtils.addAttr(xmlModelDoc, xmlNode, "location", location.getName());

		// name
		// TODO: name != location in general
		XmlDocUtils.addAttr(xmlModelDoc, xmlNode, "name", location.getName());
		// interval
		XmlDocUtils.addAttr(xmlModelDoc, xmlNode, "interval", 1);
		// variables
		XmlDocUtils.addAttr(xmlModelDoc, xmlNode, "variable", varNames.toString());
		// labels
		XmlDocUtils.addAttr(xmlModelDoc, xmlNode, "label", labels.toString());
		
		// Add this node to the document
		interfaceNode.appendChild(xmlNode);
	}

}
