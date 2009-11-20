package org.spark.runtime.external.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;
import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.data.DataRow;
import org.spark.runtime.external.data.DataFilter;
import org.spark.runtime.external.data.IDataConsumer;
import org.spark.utils.XmlDocUtils;
import org.w3c.dom.Node;

/**
 * A chart panel
 * @author Monad
 */
@SuppressWarnings("serial")
public class SparkChartPanel extends JPanel implements ActionListener, ISparkPanel, IDataConsumer {
	private JButton clearButton;
	private JButton saveButton;

	private String name;
	private String varName;
	private DefaultTableXYDataset dataset;
	private XYSeries series;
	
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
		
		varName = XmlDocUtils.getValue(node, "name", null);
		int interval = XmlDocUtils.getIntegerValue(node, "interval", 1);
		if (interval < 1)
			interval = 1;
		
		if (varName != null) {
			dataFilter.setInterval(interval);
			dataFilter.addData(DataCollectorDescription.VARIABLE, varName);
		}
		
		setupChart();

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
	 * Creates a JFreeChart
	 */
	private void setupChart() {
		dataset = new DefaultTableXYDataset();
		series = new XYSeries("", false, false);
		dataset.addSeries(series);

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

	public void addValue(double x, double y) {
		synchronized (series) {
			series.add(x, y);
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
		synchronized (series) {
			dataset.removeAllSeries();
			series = new XYSeries("", false, false);
			dataset.addSeries(series);
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
	public synchronized void saveData(PrintStream out) {
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
		if (row.getState().isInitial())
			reset();
		
		double newValue = row.getVarDoubleValue(varName);
		long tick = row.getState().getTick();
		
		try {
			addValue(tick, newValue);
		}
		catch (Exception e) {
			
		}
	}

}
