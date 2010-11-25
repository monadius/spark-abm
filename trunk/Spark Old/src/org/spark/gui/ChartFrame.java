package org.spark.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ChartFrame extends UpdatableFrame implements ActionListener {

	private static final long serialVersionUID = 8470674394331875790L;

	private JButton clearButton;
	private JButton saveButton;

	private String name;
	private DefaultTableXYDataset dataset;
	private XYSeries series;
	private long interval;
	private Method method;

	public ChartFrame(Node node, JFrame owner) throws SecurityException,
			NoSuchMethodException {
		super(node, owner, "");

		NamedNodeMap attributes = node.getAttributes();
		this.name = attributes.getNamedItem("name").getNodeValue();
		String smethod = attributes.getNamedItem("method").getNodeValue();
		String sinterval = attributes.getNamedItem("interval").getNodeValue();

		method = GUIModelManager.getModelClass().getMethod(smethod);
		interval = Long.parseLong(sinterval);
		
		if (interval < 1)
			interval = 1;

		setTitle(name);

		setupChart();
		FrameLocationManager.setLocation(this, node);
	}

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

		this.add(panel);
		this.add(buttonPanel, BorderLayout.SOUTH);

		this.pack();
	}

	public void addValue(double x, double y) {
		series.add(x, y);
	}

	public void updateData(long tick) {
		if (tick % interval == 0) {
			try {
				Double y = (Double) method.invoke(null);
				if (y!= null) addValue(tick, y);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void reset() {
		// TODO: not a good solution, we need to wait for end of painting
		// operation on AWT-0
		if (!java.awt.EventQueue.isDispatchThread())
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		dataset.removeAllSeries();
		series = new XYSeries("", false, false);
		dataset.addSeries(series);
	}

	/**
	 * Save data dialog
	 * 
	 * @throws Exception
	 */
	private void saveDataFile() throws Exception {
		final JFileChooser fc = new JFileChooser(GUIModelManager.getInstance()
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

		}
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

}
