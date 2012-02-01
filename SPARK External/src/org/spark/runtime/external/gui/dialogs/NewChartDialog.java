package org.spark.runtime.external.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.ProxyVariable;
import org.spark.runtime.external.gui.SparkChartPanel;
import org.spark.runtime.external.gui.WindowManager;

public class NewChartDialog extends JDialog implements ActionListener, ListSelectionListener {
	private static final long serialVersionUID = 1L;
	
	// Commands
	private static final String CMD_SELECT = "select"; 
	private static final String CMD_REMOVE = "remove"; 
	private static final String CMD_RESET = "reset"; 
	private static final String CMD_CREATE = "create"; 

	// Components
	private JList varList;
	private JList selectedList;
	private JButton selectButton;
	private JButton removeButton;
	private JButton resetButton;
	private JButton createButton;
	
	private DefaultListModel selectedModel;
	
	/**
	 * Constructor
	 */
	public NewChartDialog(JFrame owner) {
		super(owner, false);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		create();
	}
	
	
	/**
	 * Creates the dialog elements
	 */
	private void create() {
		JPanel panel = new JPanel();
//		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setLayout(new BorderLayout());

		// Create the list of variables
		varList = new JList();
		varList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		varList.addListSelectionListener(this);
		
		// Create the list of selected variables
		selectedModel = new DefaultListModel();
		selectedList = new JList(selectedModel);
		selectedList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		selectedList.addListSelectionListener(this);

		// Create scroll panes for lists
		JScrollPane varScroll = new JScrollPane(varList);
		varScroll.setPreferredSize(new Dimension(200, 300));
		varScroll.setMinimumSize(new Dimension(100, 100));

		JScrollPane selectedScroll = new JScrollPane(selectedList);
		selectedScroll.setPreferredSize(new Dimension(200, 300));
		selectedScroll.setMinimumSize(new Dimension(100, 100));

		// Create a split pane
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(varScroll);
		splitPane.setRightComponent(selectedScroll);
		
		// Create buttons
		JPanel buttons = new JPanel();
		JButton button;
		
		// Select
		selectButton = button = new JButton("Select");
		button.setActionCommand(CMD_SELECT);
		button.addActionListener(this);
		buttons.add(button);

		// Remove
		removeButton = button = new JButton("Remove");
		button.setActionCommand(CMD_REMOVE);
		button.addActionListener(this);
		buttons.add(button);
		
		// Reset
		resetButton = button = new JButton("Reset");
		button.setActionCommand(CMD_RESET);
		button.addActionListener(this);
		buttons.add(button);

		// Create
		createButton = button = new JButton("Create");
		button.setActionCommand(CMD_CREATE);
		button.addActionListener(this);
		buttons.add(button);
		
		// Add all components
		panel.add(splitPane, BorderLayout.CENTER);
		panel.add(buttons, BorderLayout.SOUTH);
		
		
		this.add(panel);
		this.pack();
	}
	
	
	/**
	 * Initializes the dialog
	 */
	public void init() {
		Coordinator c = Coordinator.getInstance();
		if (c == null)
			return;
	
		ProxyVariable[] vars = c.getVariables().getVariables();
		ArrayList<String> varNames = new ArrayList<String>();
		
		// Get only variables of type double
		for (ProxyVariable var : vars) {
			if (var.getType() != ProxyVariable.DOUBLE_TYPE)
				continue;
			
			varNames.add(var.getName());
		}
		
		varList.setListData(varNames.toArray());
		selectedModel.removeAllElements();
	
		enableButtons();
	}
	
	
	/**
	 * Enables/disables buttons
	 */
	private void enableButtons() {
		// Select
    	if (varList.getSelectedIndex() == -1)
    		selectButton.setEnabled(false);
    	else
    		selectButton.setEnabled(true);

    	// Remove
    	if (selectedList.getSelectedIndex() == -1)
    		removeButton.setEnabled(false);
    	else
    		removeButton.setEnabled(true);

    	// Reset and Create
		if (selectedModel.size() > 0) {
			resetButton.setEnabled(true);
			createButton.setEnabled(true);
		}
		else {
			resetButton.setEnabled(false);
			createButton.setEnabled(false);
		}
	}
	
	/**
	 * Creates a chart based on the selected variables
	 */
	private void createChart() {
		Coordinator c = Coordinator.getInstance();
		WindowManager win = c.getWindowManager();
		String location = win.getGoodName("New chart");
		
		SparkChartPanel chartPanel = new SparkChartPanel(c.getWindowManager(), 1, location);
		
		// Add data series
		for (Object obj : selectedModel.toArray()) {
			String varName = (String) obj;
			chartPanel.addSeries(varName, varName);
		}
		
		// Register new chart panel as a data consumer
		c.getDataReceiver().addDataConsumer(chartPanel.getDataFilter());
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
	    if (e.getValueIsAdjusting() == false) {
	    	enableButtons();
	    }
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == null)
			return;
		
		cmd = cmd.intern();
		
		// Selection
		if (cmd == CMD_SELECT) {
			Object[] selected = varList.getSelectedValues();
			for (Object obj : selected) {
				if (!(obj instanceof String))
					continue;
				
				// Ignore duplicates
				if (selectedModel.contains(obj))
					continue;
				
				selectedModel.addElement(obj);
			}
			
			varList.clearSelection();
			enableButtons();
			return;
		}
		
		// Remove
		if (cmd == CMD_REMOVE) {
			Object[] selected = selectedList.getSelectedValues();
			for (Object obj : selected) {
				selectedModel.removeElement(obj);
			}

			enableButtons();
			return;
		}
		
		// Reset
		if (cmd == CMD_RESET) {
			selectedModel.removeAllElements();
			enableButtons();
			return;
		}
		
		// Create
		if (cmd == CMD_CREATE) {
			if (selectedModel.size() > 0)
				createChart();
			
			return;
		}
	}


}
