package org.spark.runtime.external.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.data.DataRow;
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
	private JButton clearButton;
	private JButton saveButton;

	// Name of the chart
	private String name;
	// Main dataset
	private XYSeriesCollection dataset;
	
	// Information about plotted series
	private static class SeriesInfo {
		public String varName;
		public XYSeries series;
		
		public SeriesInfo(String varName, XYSeries series) {
			this.varName = varName;
			this.series = series;
		}
	}
	
	private final ArrayList<SeriesInfo> series;
	
	private DataFilter dataFilter;

	/**
	 * Creates a chart panel
	 * @param manager
	 * @param node
	 * @throws Exception
	 */
	public SparkChartPanel(WindowManager manager, Node node) {
		super(new BorderLayout());

		dataFilter = new DataFilter(this, "variable");
		series = new ArrayList<SeriesInfo>(2);
		
//		String varNames = XmlDocUtils.getValue(node, "variable", null);

		// TODO: distinct intervals for distinct variables
		int interval = XmlDocUtils.getIntegerValue(node, "interval", 1);
		if (interval < 1)
			interval = 1;
		
/*		if (varNames != null) {
			dataFilter.setInterval(interval);
			String[] names = varNames.split(",");
			
			for (String name : names) {
				name = name.trim();
				series.add(new SeriesInfo(name, null));
				dataFilter.addData(DataCollectorDescription.VARIABLE, name);
			}
		}
*/		
		setupChart();
		addChart(node);

		String location = XmlDocUtils.getValue(node, "location", null);
		manager.setLocation(this, location);
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
	public void addChart(Node node) {
		String varNames = XmlDocUtils.getValue(node, "variable", null);
		String label = XmlDocUtils.getValue(node, "label", "");

		if (varNames != null) {
			String[] names = varNames.split(",");
			
			for (String name : names) {
				name = name.trim();
				XYSeries s = new XYSeries(label, false);
				
				series.add(new SeriesInfo(name, s));
				dataset.addSeries(s);

				dataFilter.addData(DataCollectorDescription.VARIABLE, name);
			}
		}		
	}
	

	/**
	 * Creates a JFreeChart
	 */
	private void setupChart() {
		dataset = new XYSeriesCollection();
		
/*		for (SeriesInfo info : series) {
			info.series = new XYSeries("", false);
			dataset.addSeries(info.series);
		}
*/
		JFreeChart chart = ChartFactory.createXYLineChart(name, "x", "y",
				dataset, PlotOrientation.VERTICAL, true, false, false);
		
		ChartPanel panel = new ChartPanel(chart);
		panel.setMinimumSize(new Dimension(100, 100));
		panel.setPreferredSize(new Dimension(300, 200));

		clearButton = new JButton("Clear");
		saveButton = new JButton("Save");
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

		clearButton.addActionListener(this);
		saveButton.addActionListener(this);

		clearButton.setActionCommand("clear");
		saveButton.setActionCommand("save");

		buttonPanel.add(clearButton);
		buttonPanel.add(saveButton);

		this.add(panel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
	}

	
	protected void addValue(double x, double y, int index) {
		addValue(x, y, this.series.get(index));
	}
	
	
	protected void addValue(double x, double y, SeriesInfo info) {
		synchronized (info.series) {
			info.series.add(x, y);
		}
	}

	
	public void updateData(long tick) {
/*		if (tick % interval == 0) {
			try {
				Double y = (Double) method.invoke(null);
				if (y!= null) addValue(tick, y);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/
	}

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

		try {
			if (cmd.equals("clear")) {
				reset();
				return;
			}

			if (cmd.equals("save")) {
				saveDataFile();
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
		// Nothing to do here
	}

}
