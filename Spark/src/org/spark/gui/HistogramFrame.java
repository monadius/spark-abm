package org.spark.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.data.statistics.HistogramDataset;
import org.w3c.dom.*;

public class HistogramFrame extends UpdatableFrame implements ActionListener, ChangeListener {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7551565408364521258L;
	
	private JButton clearButton;
	private JButton saveButton;
	
	private JCheckBox lockSizeCheckBox;
	private JCheckBox lockBinsCheckBox;
	
	private JSpinner maxSpinner, minSpinner;
	private JSpinner binSizeSpinner, binsSpinner;

	private String name;
	private PhetHistogramDataset dataset;	
	private long interval;
	//private Method method;
	
	private boolean restrictedScale = false;
	private boolean restrictedBins = false;
	private boolean restrictedBinSize = true;
	private double maximum = 0;
	private double minimum = 0;
	private int bins = 20;
	private double binSize = 1;

	
	private final ArrayList<HistogramDataSeries> rowDataSeries;
	
	
	/**
	 * Maximum value 
	 */
	@SuppressWarnings("serial")
	private class MaximumModel extends SpinnerNumberModel {
		private double internalValue = 0;

		public Object getNextValue() {
			internalValue += 1;
			maximum = internalValue;
			return internalValue;
		}

		public Object getPreviousValue() {
			internalValue -= 1;
			maximum = internalValue;
			return internalValue;
		}

		public Object getValue() {
			return internalValue;
		}

		public void setValue(Object arg0) {
			if (arg0 != null && arg0 instanceof Number) {
				internalValue = ((Number) arg0).doubleValue();
				maximum = internalValue;
				this.fireStateChanged();
			}
		}
		
	}

	
	/**
	 * Minimum value 
	 */
	@SuppressWarnings("serial")
	private class MinimumModel extends SpinnerNumberModel {
		private double internalValue = 0;

		public Object getNextValue() {
			internalValue += 1;
			minimum = internalValue;
			return internalValue;
		}

		public Object getPreviousValue() {
			internalValue -= 1;
			minimum = internalValue;
			return internalValue;
		}

		public Object getValue() {
			return internalValue;
		}

		public void setValue(Object arg0) {
			if (arg0 != null && arg0 instanceof Number) {
				internalValue = ((Number) arg0).doubleValue();
				minimum = internalValue;
				this.fireStateChanged();
			}
		}
		
	}
	
	
	/**
	 * Bin size value 
	 */
	@SuppressWarnings("serial")
	private class BinSizeModel extends SpinnerNumberModel {

		public Object getNextValue() {
			return binSize + 1;
		}

		public Object getPreviousValue() {
			if (binSize > 0)
				return binSize - 1;
			else
				return binSize;
		}

		public Object getValue() {
			return binSize;
		}

		public void setValue(Object arg0) {
			if (arg0 != null && arg0 instanceof Number) {
				binSize = ((Number) arg0).doubleValue();
				if (binSize <= 0)
					binSize = 1;
				this.fireStateChanged();
			}
		}
		
	}

	
	/**
	 * Number of bins 
	 */
	@SuppressWarnings("serial")
	private class BinsModel extends SpinnerNumberModel {

		public Object getNextValue() {
			return bins + 1;
		}

		public Object getPreviousValue() {
			if (bins > 1)
				return bins - 1;
			else
				return bins;
		}

		public Object getValue() {
			return bins;
		}

		public void setValue(Object arg0) {
			if (arg0 != null && arg0 instanceof Number) {
				bins = ((Number) arg0).intValue();
				if (bins < 1)
					bins = 1;
				this.fireStateChanged();
			}
		}
		
	}


	
	public HistogramFrame(Node node, JFrame owner) throws Exception {
		super(node, owner, "");
		
		rowDataSeries = new ArrayList<HistogramDataSeries>();
		
		//add case then this field absent in XML (default values)
		NamedNodeMap attributes = node.getAttributes();
		this.name = attributes.getNamedItem("name").getNodeValue();
		//String smethod = attributes.getNamedItem("method").getNodeValue();
		String sinterval = attributes.getNamedItem("interval").getNodeValue();
		//method = GUIModelManager.getModelClass().getMethod(smethod);
		interval = Long.parseLong(sinterval);
		Node tmp;
		
		// default for max = 0 (I need it value for default)
		String max = (tmp = attributes.getNamedItem("max")) != null ? tmp.getNodeValue() : "0";
		// default for min = 0 (I Use it value if 0)
		String min = (tmp = attributes.getNamedItem("min")) != null ? tmp.getNodeValue() : "0";
		// default for bins =0;
		String bin = (tmp = attributes.getNamedItem("bins")) != null ? tmp.getNodeValue() : "0";
		// default for binsize =0;
		String binsize = (tmp = attributes.getNamedItem("binSize")) != null ? tmp.getNodeValue() : "0";
		
		maximum = Double.parseDouble(max);
		minimum = Double.parseDouble(min);
		if ((maximum ==0)&&(minimum ==0)) restrictedScale = false;
		else restrictedScale=true;
		
		bins = Integer.parseInt(bin);
		if (bins>0) restrictedBins = true;

		binSize = Double.parseDouble(binsize);
		if (binSize>0) restrictedBinSize =true;
		
		
		//throw Error if.... (some thing should be restricted)
		if ((!restrictedBins)&&(!restrictedBinSize)) {
			throw new Exception("Something should be restricted");
		}
			
		
		readXMLHistogramDataNode(node);
		
		dataset = new PhetHistogramDataset();
		createEmptySeries(dataset);
		setTitle(name);
		setupChart();		
		
		FrameLocationManager.setLocation(this, node);
	}
	

	// private List<string, string> Series;
	private class HistogramDataSeries {
		public String name;
		public Method getMethod;
		//ParallelDoubleArray data;
		public PhetHistogramSeries Series;
		//public double[] getData() {
		//	return data.getArray();
		//}
	}
	
	private void readXMLHistogramDataNode(Node node) throws SecurityException,
			NoSuchMethodException {
		// List of all <dataseries> nodes inside the given <histogram> node
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node dataSeriesNode = list.item(i);
			// Select only "dataseries" nodes
			if (!dataSeriesNode.getNodeName().equals("dataseries"))
				continue;

			// Take attributes and their values
			NamedNodeMap attr = dataSeriesNode.getAttributes();
			String name = attr.getNamedItem("name").getNodeValue();
			String methodName = attr.getNamedItem("get").getNodeValue();

			Method method = GUIModelManager.getModelClass().getMethod(
					methodName);

			HistogramDataSeries series = new HistogramDataSeries();
			series.getMethod = method;
			series.name = name;

			rowDataSeries.add(series);
		}
	}
	
	private void createEmptySeries(PhetHistogramDataset dataset) {
		//dataset = new PhetHistogramDataset();
		for (int i = 0; i < rowDataSeries.size(); i++) {
			PhetHistogramSeries newSeries;
			if (restrictedScale) 
			{
				if (restrictedBins)
					newSeries = new PhetHistogramSeries(rowDataSeries.get(i).name,
						minimum, maximum, bins);
				else 
					newSeries = new PhetHistogramSeries(rowDataSeries.get(i).name,
							minimum, maximum, binSize);
			} 
			else 
			{
				if (restrictedBinSize) newSeries = new PhetHistogramSeries(rowDataSeries.get(i).name, binSize);
				else newSeries = new PhetHistogramSeries(rowDataSeries.get(i).name, bins);
			}
			
			rowDataSeries.get(i).Series = newSeries;
			dataset.addSeries(newSeries);
		}
	}

	private void setupChart() {
		JFreeChart chart = ChartFactory.createHistogram(name, "x", "y",
				dataset, PlotOrientation.VERTICAL, true, true, false);
		
		ChartPanel panel = new ChartPanel(chart);
		panel.setMinimumSize(new Dimension(100, 100));
		panel.setPreferredSize(new Dimension(300, 200));		

		clearButton = new JButton("Clear");
		saveButton = new JButton("Save");
		JPanel buttonPanel = new JPanel(new GridLayout(3, 5));

		clearButton.addActionListener(this);
		saveButton.addActionListener(this);

		clearButton.setActionCommand("clear");
		saveButton.setActionCommand("save");

		
		
		// First row
		
		lockSizeCheckBox = new JCheckBox("Lock size");
		buttonPanel.add(lockSizeCheckBox);
		
		JLabel label = new JLabel("max ");
		label.setHorizontalAlignment(JLabel.RIGHT);
		buttonPanel.add(label);
		
		maxSpinner = new JSpinner(new MaximumModel());
		maxSpinner.setValue(maximum);
		buttonPanel.add(maxSpinner);
		
		label = new JLabel("min ");
		label.setHorizontalAlignment(JLabel.RIGHT);
		buttonPanel.add(label);
		
		minSpinner = new JSpinner(new MinimumModel());
		minSpinner.setValue(minimum);
		buttonPanel.add(minSpinner);
		
		// Second row
		
		lockBinsCheckBox = new JCheckBox("Lock bins");
		buttonPanel.add(lockBinsCheckBox);
		
		label = new JLabel("size ");
		label.setHorizontalAlignment(JLabel.RIGHT);
		buttonPanel.add(label);
		
		binSizeSpinner = new JSpinner(new BinSizeModel());
		buttonPanel.add(binSizeSpinner);
		
		label = new JLabel("bins ");
		label.setHorizontalAlignment(JLabel.RIGHT);
		buttonPanel.add(label);
		
		binsSpinner = new JSpinner(new BinsModel());
		buttonPanel.add(binsSpinner);
		
		// Third row
		
		label = new JLabel(" ");
		buttonPanel.add(label);
		buttonPanel.add(clearButton);
		buttonPanel.add(saveButton);
		
		buttonPanel.add(new JLabel(" "));
		buttonPanel.add(new JLabel(" "));

		this.add(panel);
		this.add(buttonPanel, BorderLayout.SOUTH);

		this.pack();

		lockSizeCheckBox.addActionListener(this);
		lockBinsCheckBox.addActionListener(this);
		
		
		
		maxSpinner.addChangeListener(this);
		minSpinner.addChangeListener(this);;
		binSizeSpinner.addChangeListener(this);
		binsSpinner.addChangeListener(this);;
		
		//actionPerformed(new ActionEvent(maxSpinner, 0, null));
		//actionPerformed(new ActionEvent(minSpinner, 0, null));
		//actionPerformed(new ActionEvent(binSizeSpinner, 0, null));
		//actionPerformed(new ActionEvent(binsSpinner, 0, null));
		
		lockSizeCheckBox.setSelected(restrictedScale);
		lockBinsCheckBox.setSelected(restrictedBins);

		actionPerformed(new ActionEvent(lockSizeCheckBox, 0, null));
		actionPerformed(new ActionEvent(lockBinsCheckBox, 0, null));
	}

	public void addValue(double tick, double value, HistogramDataSeries workSeries) {
		workSeries.Series.addObservation(value);			
	}

	public void updateData(long tick) {
		if (tick % interval == 0) {
			try {
				for (int i = 0; i < rowDataSeries.size(); i++) {					
					Double y = (Double) rowDataSeries.get(i).getMethod.invoke(null);
					if (y != null)
						addValue(tick, y, rowDataSeries.get(i));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void reset() {
		if (!java.awt.EventQueue.isDispatchThread())
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		dataset.removeAllSeries();
		createEmptySeries(dataset);		
	}

	/**
	 * Save data dialog
	 * 
	 * @throws Exception
	 */
	private void saveDataFile() throws Exception {
		//TODO need modify
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
		//TODO need modify
		/*out.println("tick\t" + name);

		int l = series.getItemCount();
		for (int k = 0; k < l; k++) {
			out.print(series.getX(k));
			out.print('\t');
			out.print(series.getY(k));
			out.println();
		}

		out.flush();
		out.close();*/
	}

	/**
	 * Processes control elements actions
	 */
	public void actionPerformed(ActionEvent arg0) {
		Object src = arg0.getSource();

		if (src == lockSizeCheckBox) {
			restrictedScale = lockSizeCheckBox.isSelected();
			maxSpinner.setEnabled(restrictedScale);
			minSpinner.setEnabled(restrictedScale);
			
			if (restrictedScale)
			{
				dataset.setSizeRestriction(((Number)minSpinner.getValue()).doubleValue() , 
						((Number)maxSpinner.getValue()).doubleValue());				
			}
			else {
				dataset.setSizeRestriction(false);
							}
			updateFromDataset();
			
			//actionPerformed(new ActionEvent(lockBinsCheckBox, 0, null));
			return;
		} else if (src == lockBinsCheckBox) 
		{
			restrictedBins = lockBinsCheckBox.isSelected();
			binsSpinner.setEnabled(restrictedBins);
			binSizeSpinner.setEnabled(!restrictedBins);

			if (restrictedBins) {
				if (binSize > 0)
					bins = (int) ((int) (maximum - minimum) / binSize);
				binsSpinner.setValue(bins);
			} else {
				if (bins > 0)
					binSize = (maximum - minimum) / bins;
				binSizeSpinner.setValue(binSize);
			}

			updateFromDataset();
			return;
		}

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

	private void updateFromDataset() {
		maximum = dataset.getMaximum();
		minimum = dataset.getMinimum();
		bins = dataset.getBins();
		binSize = dataset.getBinsWidth();
	}

	public void stateChanged(ChangeEvent arg0) {
		Object src = arg0.getSource();
		if ((restrictedBins) && (src == binsSpinner)) {
			if (bins > 0)
				binSize = (maximum - minimum) / bins;
			dataset.setBins(bins);
			binSizeSpinner.setValue(binSize);
		} else if ((!restrictedBins) && (src == binSizeSpinner)) {
			if (binSize > 0)
				bins = (int) ((int) (maximum - minimum) / binSize);
			dataset.setBinWidthLimit(binSize);
			binsSpinner.setValue(bins);
		} else if ((restrictedScale)
				&& ((src == maxSpinner) || (src == minSpinner))) {

			dataset.setSizeRestriction(((Number) minSpinner.getValue())
					.doubleValue(), ((Number) maxSpinner.getValue())
					.doubleValue());
			//actionPerformed(new ActionEvent(lockBinsCheckBox, 0, null));
		}		
		updateFromDataset();
	}

}
