package org.spark.runtime.external.gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.spark.runtime.data.DataObject_State;
import org.spark.runtime.data.DataRow;
import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.data.IDataConsumer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Contains all SPARK control elements
 * @author Monad
 */
@SuppressWarnings("serial")
public class SparkControlPanel extends JPanel implements ISparkPanel, IDataConsumer, ActionListener, ChangeListener {
	/* Control buttons */
	private JButton startButton;
	private JButton setupButton;
	
	/* Synchronization check box */
//	private JCheckBox synchButton;
	
	/* Tick label */
	private JLabel tickLabel;
	
	/* Delay sizes for the speed slider */
	private static final int[] delaySize = new int[] { -100, -50, -20, -10, -5,
			-4, -3, -2, 0, 10, 20, 50, 100, 200, 500 };


	public SparkControlPanel() {
		this.setMinimumSize(new Dimension(300, 100));

//		synchButton = new JCheckBox("Sychronized", null, true);
		startButton = new JButton("Start");
		setupButton = new JButton("Setup");
		tickLabel = new JLabel("");
		tickLabel.setMinimumSize(new Dimension(100, 80));

//		synchButton.setActionCommand("synchronize");
		startButton.setActionCommand("start");
		setupButton.setActionCommand("setup");
		
//		synchButton.addActionListener(this);
		startButton.addActionListener(this);
		setupButton.addActionListener(this);

		JSlider framesPerSecond = new JSlider(JSlider.HORIZONTAL, 0, 14, 0);
		framesPerSecond.addChangeListener(this);

		framesPerSecond.setMajorTickSpacing(2);
		framesPerSecond.setMinorTickSpacing(1);
		framesPerSecond.setPaintTicks(true);
		framesPerSecond.setPaintLabels(true);
		framesPerSecond.setValue(8);

		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(new Integer(0), new JLabel("Fast"));
		labelTable.put(new Integer(8), new JLabel("Normal"));
		labelTable.put(new Integer(14), new JLabel("Slow"));
		framesPerSecond.setLabelTable(labelTable);

		this.add(framesPerSecond);
//		this.add(synchButton);
		this.add(setupButton);
		this.add(startButton);
		this.add(tickLabel);
	}
	
	
	/**
	 * Updates XML node
	 */
	public void updateXML(SparkWindow location, Document xmlModelDoc, Node interfaceNode, File xmlModelFile) {
		// Nothing to do here
	}




	/**
	 * Invoked when a button is pressed
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand().intern();
		Coordinator c = Coordinator.getInstance();
		
		if (cmd == "synchronize") {
		 	// TODO: implement
		}
		else if (cmd == "start") {
			c.pauseResumeLoadedModel();
		}
		else if (cmd == "setup") {
			c.startLoadedModel(Long.MAX_VALUE, true);
		}
	}
	
	
	/**
	 * Invoked when a slider is moving
	 */
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		int n = source.getValue();
		if (n < 0 || n >= delaySize.length)
			n = 8;

		int delay = delaySize[n];
		Coordinator.getInstance().setSimulationDelayTime(delay);
	}
	
	
	/* Latest state of a simulation */
	private DataObject_State latestSimulationState;

	private boolean updateInvoked = false;
	private Object lock = new Object();
	
	private Updater updater = new Updater();
	
	
	
	
	/**
	 * IDataConsumer implementation
	 */
	public void consume(DataRow row) {
		synchronized (lock) {
			latestSimulationState = row.getState();
			
			if (!updateInvoked) {
				updateInvoked = true;
				EventQueue.invokeLater(updater);
			}
		}
	}
	
	
	/**
	 * Updates simulation state information
	 * @author Alexey
	 *
	 */
	private class Updater implements Runnable {
		public void run() {
			DataObject_State state;
			
			synchronized (lock) {
				state = latestSimulationState;
				updateInvoked = false;
			}
			
			tickLabel.setText(String.valueOf(state.getTick()));
			if (state.isPaused())
				startButton.setText("Start");
			else
				startButton.setText("Pause");
		}
	}
}
