package org.spark.gui.applet;

import java.awt.Dimension;
import java.lang.reflect.Method;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


public class ChartPanel implements IUpdatablePanel {

	private static final long serialVersionUID = 8470674394331875790L;

	private String name;
	private DefaultTableXYDataset dataset;
	private XYSeries series;
	private long interval;
	private Method method;

	
	public ChartPanel(Node node) throws SecurityException, NoSuchMethodException {
		NamedNodeMap attributes = node.getAttributes();
		this.name = attributes.getNamedItem("name").getNodeValue();
		String smethod = attributes.getNamedItem("method").getNodeValue();
		String sinterval = attributes.getNamedItem("interval").getNodeValue();
		
		method = AppletModelManager.getModelClass().getClass().getMethod(smethod);
		interval = Long.parseLong(sinterval);
		
		if (interval < 1)
			interval = 1;
		
		setupChart();
	}

	
	
	private void setupChart() {

		dataset = new DefaultTableXYDataset();
		series = new XYSeries("", false, false);
		dataset.addSeries(series);
		
		JFreeChart chart = ChartFactory.createXYLineChart(
				name, 
				"x", 
				"y", 
				dataset, 
				PlotOrientation.VERTICAL,
				true, false, false);

		org.jfree.chart.ChartPanel chartPanel = new org.jfree.chart.ChartPanel(chart);
		chartPanel.setMinimumSize(new Dimension(100, 100));
		chartPanel.setPreferredSize(new Dimension(300, 200));
		
		SparkApplet.getChartPanel().add(chartPanel);
	}
	
	
	/**
	 * Add new value to chart
	 * @param x
	 * @param y
	 */
	public void addValue(double x, double y) {
		series.add(x, y);
	}
	
	public void updateData(long tick) {
		if (tick % interval == 0) {
			try {
				Double y = (Double) method.invoke(null);
				if (y != null)
					addValue(tick, y);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void reset() {
		// TODO: not a good solution, we need to wait for end of painting operation on AWT-0
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



	public void updateData() {
		// TODO Auto-generated method stub
		
	}

}
