package org.sparkabm.gui.gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sparkabm.runtime.data.DataObject_State;
import org.sparkabm.runtime.data.DataRow;
import org.sparkabm.gui.Coordinator;
import org.sparkabm.gui.data.IDataConsumer;
import org.sparkabm.gui.renderer.Renderer;
import org.sparkabm.utils.XmlDocUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Contains all SPARK control elements
 *
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
    private JSlider delaySlider;

    /* Delay sizes for the speed slider */
    private static final int[] delaySize = new int[]{-100, -50, -20, -10, -5,
            -4, -3, -2, 0, 10, 20, 50, 100, 200, 500};

    /* Frequency values */
    private static final int[] freqSize = new int[]{0, 10, 20, 30, 60};
    private JSlider freqSlider;


    public SparkControlPanel() {
        this.setMinimumSize(new Dimension(300, 100));
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        int delay = 8;
        int freq = 0;

//		synchButton = new JCheckBox("Sychronized", null, true);

        startButton = new JButton("Start");
        setupButton = new JButton("Setup");

        Dimension d = new Dimension(80, 30);
        startButton.setPreferredSize(d);
        startButton.setMinimumSize(d);
        startButton.setMaximumSize(d);

        setupButton.setPreferredSize(d);
        setupButton.setMinimumSize(d);
        setupButton.setMaximumSize(d);

        tickLabel = new JLabel("");
        tickLabel.setMinimumSize(new Dimension(60, 20));
        tickLabel.setPreferredSize(new Dimension(60, 20));
        tickLabel.setMaximumSize(new Dimension(60, 20));

//		synchButton.setActionCommand("synchronize");
        startButton.setActionCommand("start");
        setupButton.setActionCommand("setup");

//		synchButton.addActionListener(this);
        startButton.addActionListener(this);
        setupButton.addActionListener(this);

        // Sliders
        delaySlider = new JSlider(JSlider.HORIZONTAL, 0, delaySize.length - 1, 0);
        delaySlider.addChangeListener(this);

        freqSlider = new JSlider(JSlider.HORIZONTAL, 0, freqSize.length - 1, 0);
        freqSlider.addChangeListener(this);

        delaySlider.setMajorTickSpacing(2);
        delaySlider.setMinorTickSpacing(1);
        delaySlider.setPaintTicks(true);
        delaySlider.setPaintLabels(true);
        delaySlider.setValue(delay);

        freqSlider.setMajorTickSpacing(2);
        freqSlider.setMinorTickSpacing(1);
        freqSlider.setPaintTicks(true);
        freqSlider.setPaintLabels(true);
        freqSlider.setValue(freq);

        // Slider labels
        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
        labelTable.put(new Integer(0), new JLabel("Fast"));
        labelTable.put(new Integer(8), new JLabel("Normal"));
        labelTable.put(new Integer(14), new JLabel("Slow"));
        delaySlider.setLabelTable(labelTable);

        labelTable = new Hashtable<Integer, JLabel>();
        labelTable.put(0, new JLabel("0"));
        labelTable.put(1, new JLabel("10"));
        labelTable.put(2, new JLabel("20"));
        labelTable.put(3, new JLabel("30"));
        labelTable.put(4, new JLabel("60"));
        freqSlider.setLabelTable(labelTable);

        this.add(Box.createRigidArea(new Dimension(10, 0)));
        this.add(tickLabel);
        this.add(startButton);
        this.add(Box.createRigidArea(new Dimension(5, 0)));
        this.add(setupButton);
        this.add(Box.createRigidArea(new Dimension(5, 0)));
        this.add(delaySlider);
        this.add(Box.createRigidArea(new Dimension(5, 0)));
        this.add(freqSlider);
        this.add(Box.createRigidArea(new Dimension(10, 0)));
//		this.add(synchButton);
    }


    /**
     * Initializes the values of the control elements from the given xml node
     *
     * @param node
     */
    public void init(Node node) {
        int freq = XmlDocUtils.getIntegerValue(node, "freq", 0);
        int delay = XmlDocUtils.getIntegerValue(node, "delay", 8);

        delaySlider.setValue(delay);
        freqSlider.setValue(freq);
    }


    /**
     * Updates XML node
     */
    public void updateXML(SparkWindow location, Document xmlModelDoc, Node interfaceNode, File xmlModelFile) {
        XmlDocUtils.removeChildren(interfaceNode, "control-panel");

        Node node = xmlModelDoc.createElement("control-panel");
        XmlDocUtils.addAttr(xmlModelDoc, node, "delay", delaySlider.getValue());
        XmlDocUtils.addAttr(xmlModelDoc, node, "freq", freqSlider.getValue());

        interfaceNode.appendChild(node);
    }


    /**
     * Invoked when a button is pressed
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand().intern();
        Coordinator c = Coordinator.getInstance();

        if (cmd == "synchronize") {
            // TODO: implement
        } else if (cmd == "start") {
            c.pauseResumeLoadedModel();
            // Move focus to a renderer for which the user control is on
            for (Renderer r : c.getRenderers()) {
                if (r.getControlState() == Renderer.CONTROL_STATE_CONTROL) {
                    if (r.getCanvas() != null) {
                        r.getCanvas().requestFocus();
                        break;
                    }
                }
            }
        } else if (cmd == "setup") {
            c.startLoadedModel(Long.MAX_VALUE, true);
        }
    }


    /**
     * Invoked when a slider is moving
     */
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();

        // Delay
        if (source == delaySlider) {
            int n = source.getValue();
            if (n < 0 || n >= delaySize.length)
                n = 8;

            int delay = delaySize[n];
            Coordinator.getInstance().setSimulationDelay(delay);
        }
        // Frequency
        else if (source == freqSlider) {
            int n = source.getValue();
            if (n < 0 || n >= freqSize.length)
                n = 0;

            int freq = freqSize[n];
            Coordinator.getInstance().setSimulationFrequency(freq);
        }
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
     *
     * @author Alexey
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
