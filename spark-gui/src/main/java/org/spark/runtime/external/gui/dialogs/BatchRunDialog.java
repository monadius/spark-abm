package org.spark.runtime.external.gui.dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;

import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.Parameter;
import org.spark.runtime.external.ProxyVariable;
import org.spark.runtime.external.ProxyVariableCollection;
import org.spark.runtime.external.batchrun.BatchRunController;
import org.spark.runtime.external.batchrun.BatchRunManager;
import org.spark.runtime.external.batchrun.DataAnalyzer;
import org.spark.runtime.external.batchrun.ParameterSweep;
import org.spark.runtime.external.render.DataLayerStyle;
import org.sparkabm.utils.FileUtils;
import org.sparkabm.utils.SpringUtilities;

/**
 * The dialog for setting up batch run properties 
 * @author Monad
 */
@SuppressWarnings("serial")
public class BatchRunDialog extends JDialog implements ActionListener {
	/* Main panel */
	private final JPanel panel;
	
	/* Tabs */
	private final JTabbedPane tabs;
	
	/* Parameters panel */
	private final JPanel parametersPanel;
	
	/* Data layers */
	private final JPanel dataLayerPanel;
	private JSpinner dataLayerInterval;
	private JSpinner dataLayerPrecision;
	private JCheckBox dataLayerOneFile;
	
	/* General controls */
	private JSpinner spinnerMaxTicks;
	private JSpinner spinnerRepetitionNumber;
	private JTextField textDataFileName;
	private JSpinner spinnerDataInterval;
	private JCheckBox checkSaveData;
	private JCheckBox checkSaveFinalSnapshots;
	
	private JCheckBox checkSaveSnapshots;
	private JSpinner spinnerSnapshotInterval;
	
	/* Data variable for analysis */
	private JComboBox<String> comboVariable;
	/* Experimental data file */
	private JTextField textExperimentalData;
	/* Data analysis method */
	private JComboBox<String> comboAnalysisMethod;
	

	/* Buttons */
	private JButton buttonStart, buttonCancel;

	/**
	 * Auxiliary class
	 */
	private static class ParameterWithOptions {
		public Parameter parameter;
		public JCheckBox use;
		public JSpinner minValue;
		public JSpinner maxValue;
		public JSpinner stepValue;
	}
	
	/* List of all parameters */
	private final ArrayList<ParameterWithOptions> parameters;
	
	/* List of all data layers */
	private final ArrayList<JCheckBox> dataLayers;
	
	/**
	 * Default constructor
	 * @param owner
	 */
	public BatchRunDialog(JFrame owner) {
		super(owner, true);
		
		parameters = new ArrayList<ParameterWithOptions>();
		dataLayers = new ArrayList<JCheckBox>();
		
		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		
		// Create the main panel
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		// Initialize general controls
		initGeneralControls();

		// Create tabs
		tabs = new JTabbedPane();
		panel.add(tabs);

		// Create the parameter tab
		parametersPanel = new JPanel();
		parametersPanel.setMinimumSize(new Dimension(300, 100));
		tabs.add("Parameters", parametersPanel);

		// Create the data layer tab
		dataLayerPanel = new JPanel();
		dataLayerPanel.setMinimumSize(new Dimension(200, 50));
		initDataLayerPanel();
		
		// Create the data analysis tab
		initDataAnalysisPanel();
		
		// Create control buttons
		JPanel buttons = new JPanel(new SpringLayout());
		buttonStart = new JButton("Start");
		buttonStart.setActionCommand("start");
		buttonStart.addActionListener(this);
		
		buttonCancel = new JButton("Cancel");
		buttonCancel.setActionCommand("cancel");
		buttonCancel.addActionListener(this);
		
		buttons.add(buttonStart);
		buttons.add(buttonCancel);
		
		SpringUtilities.makeCompactGrid(buttons, 1, 2, 10, 10, 20, 10);
		panel.add(buttons);
		
		JScrollPane scrollPane = new JScrollPane(panel);
		this.add(scrollPane);
//		this.add(panel);
		this.pack();
	}
	
	

	/**
	 * Creates a panel for data analysis
	 */
	private void initDataAnalysisPanel() {
		// Create panel
		JPanel dataPanel = new JPanel(new SpringLayout());
		dataPanel.setMinimumSize(new Dimension(100, 100));
		
		// Create controls
		comboVariable = new JComboBox<String>();
		comboVariable.setMaximumSize(new Dimension(300, 50));
		
		textExperimentalData = new JTextField();
		textExperimentalData.setEditable(false);
		textExperimentalData.setMaximumSize(new Dimension(300, 50));
		
		JButton selectFile = new JButton("...");
		selectFile.setActionCommand("select-file");
		selectFile.addActionListener(this);
		
		comboAnalysisMethod = new JComboBox<String>(new String[] {
				"Least Squares", "Correlation"});
		comboAnalysisMethod.setMaximumSize(new Dimension(300, 50));
		
		// Add controls
		dataPanel.add(new JLabel("Data variable"));
		dataPanel.add(comboVariable);
		dataPanel.add(new JLabel(""));
		
		dataPanel.add(new JLabel("Experimental data"));
		dataPanel.add(textExperimentalData);
		dataPanel.add(selectFile);
		
		dataPanel.add(new JLabel("Method"));
		dataPanel.add(comboAnalysisMethod);
		dataPanel.add(new JLabel(""));
		
		SpringUtilities.makeCompactGrid(dataPanel, 3, 3, 5, 5, 5, 5);
	
		tabs.add("Data Analysis", dataPanel);
	}
	
	
	/**
	 * Creates the data layer tab
	 */
	private void initDataLayerPanel() {
		JPanel dataPanel = new JPanel(new SpringLayout());
		dataPanel.setMinimumSize(new Dimension(100, 100));
//		dataPanel.setMaximumSize(new Dimension(100, 200));

		// Create controls
		
		// Interval
		dataLayerInterval = new JSpinner(
				new SpinnerNumberModel(1, 1, 2000000000, 1));
		dataLayerInterval.setName("data-layer-interval");
		
		// Precision
		dataLayerPrecision = new JSpinner(
				new SpinnerNumberModel(5, 1, 2000000000, 1));
		dataLayerPrecision.setName("data-layer-precision");
		
		// One file
		dataLayerOneFile = new JCheckBox();
		dataLayerOneFile.setSelected(true);
		
		// Add controls
		dataPanel.add(new JLabel("Save interval"));
		dataPanel.add(dataLayerInterval);
		
		dataPanel.add(new JLabel("Numeric precision"));
		dataPanel.add(dataLayerPrecision);
		
		dataPanel.add(new JLabel("One output file"));
		dataPanel.add(dataLayerOneFile);

		SpringUtilities.makeCompactGrid(dataPanel, 3, 2, 5, 5, 5, 5);
		
		// Create a global panel
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(dataPanel);
		panel.add(dataLayerPanel);
		
//		JScrollPane scroll = new JScrollPane(panel);
		
		tabs.add("Data Layers", panel);
	}
	
	
	/**
	 * Creates general controls
	 */
	private void initGeneralControls() {
		// Create panel
		JPanel generalControls = new JPanel(new SpringLayout());
		generalControls.setBorder(BorderFactory.createTitledBorder("General parameters"));
		generalControls.setMinimumSize(new Dimension(100, 100));
		
		// Create controls
		spinnerMaxTicks = new JSpinner(
				new SpinnerNumberModel(100, 1, 2000000000, 1));
		spinnerMaxTicks.setName("max-ticks");
		
		spinnerRepetitionNumber = new JSpinner(
				new SpinnerNumberModel(1, 1, 10000, 1));
		spinnerRepetitionNumber.setName("repetition-number");
		
		textDataFileName = new JTextField("data");
		
		spinnerDataInterval = new JSpinner(
				new SpinnerNumberModel(1, 1, 100000, 1));
		
		checkSaveData = new JCheckBox();
		checkSaveData.setSelected(true);

		// Snapshot parameters
		checkSaveFinalSnapshots = new JCheckBox();
		checkSaveFinalSnapshots.setSelected(true);

		checkSaveSnapshots = new JCheckBox();
		checkSaveSnapshots.setSelected(false);
		
		spinnerSnapshotInterval = new JSpinner(
				new SpinnerNumberModel(100, 0, 1000000, 1));
		spinnerSnapshotInterval.setName("snapshot-interval");
		
		// Add controls
		generalControls.add(new JLabel("Number of ticks"));
		generalControls.add(spinnerMaxTicks);
		
		generalControls.add(new JLabel("Number of repetitions"));
		generalControls.add(spinnerRepetitionNumber);
		
		generalControls.add(new JLabel("Data file name"));
		generalControls.add(textDataFileName);

		generalControls.add(new JLabel("Data interval"));
		generalControls.add(spinnerDataInterval);
		
		generalControls.add(new JLabel("Save data"));
		generalControls.add(checkSaveData);
		
		generalControls.add(new JLabel("Save final snapshots"));
		generalControls.add(checkSaveFinalSnapshots);
		
		generalControls.add(new JLabel("Save snapshots"));
		generalControls.add(checkSaveSnapshots);
		
		generalControls.add(new JLabel("Snapshot interval"));
		generalControls.add(spinnerSnapshotInterval);

		SpringUtilities.makeCompactGrid(generalControls, 8, 2, 5, 5, 10, 3);
		
		panel.add(generalControls);
	}
	
	
	/**
	 * Initializes the dialog
	 */
	public void init() {
		initParameters();
		initVariables();
		initDataLayers();
		this.pack();
	}
	
	
	/**
	 * Initializes data layers
	 */
	private void initDataLayers() {
		Coordinator c = Coordinator.getInstance();
		
		dataLayerPanel.removeAll();
		dataLayers.clear();
		
		int index = 0;
		
		for (DataLayerStyle style : c.getDataLayerStyles().values()) {
			String name = style.getName();
			JCheckBox box = new JCheckBox(name);
			
			dataLayerPanel.add(box);
			dataLayers.add(box);
			
			index++;
		}
		
		dataLayerPanel.setLayout(new SpringLayout());
		SpringUtilities.makeCompactGrid(dataLayerPanel, index, 1, 0, 0, 5, 5);
	}
	
	
	/**
	 * Initializes data variables
	 */
	private void initVariables() {
		Coordinator c = Coordinator.getInstance();
		if (!c.isModelLoaded())
			return;
		
		ProxyVariable[] vars;
		ProxyVariableCollection varCollection = c.getVariables();
		if (varCollection == null)
			vars = new ProxyVariable[0];
		else
			vars = c.getVariables().getVariables();
		
		ArrayList<String> names = new ArrayList<String>();
		
		for (int i = 0; i < vars.length; i++) {
			ProxyVariable var = vars[i];
			// TODO: other types
			if (var.getType() == ProxyVariable.DOUBLE_TYPE) {
				names.add(var.getName());
			}
		}
		
		String[] tmp = new String[names.size()];
		comboVariable.setModel( 
				new DefaultComboBoxModel<String>(names.toArray(tmp)));
	}
	
	
	/**
	 * Initializes parameter controls
	 */
	private void initParameters() {
		// Remove all controls
		parametersPanel.removeAll();
		parameters.clear();
		
		int index = 0;
		Coordinator c = Coordinator.getInstance();
		if (!c.isModelLoaded())
			return;
		
		// Create controls for parameters
		Parameter[] parameters = c.getParameters().getParameters();
		for (int i = 0; i < parameters.length; i++) {
			Parameter p = parameters[i];
			// TODO: right now only Double parameters can be used
			if (p.getValue() instanceof Double &&
					p.getMin() > -1e100 &&
					p.getMax() < 1e100) {
				ParameterWithOptions pp = new ParameterWithOptions();
				pp.parameter = p;
				
				this.parameters.add(pp);
				
				JCheckBox useParameter = new JCheckBox();
				useParameter.setSelected(false);
				useParameter.setActionCommand("use-parameter" + index);
				useParameter.addActionListener(this);
				
				JLabel name = new JLabel(p.getName());

				JLabel startLabel = new JLabel("Start");
				JSpinner startValue = new JSpinner(
						new SpinnerNumberModel(p.getMin(), p.getMin(), p.getMax(), p.getStep()));
				startValue.setEnabled(false);
				
				JLabel endLabel = new JLabel("End");
				JSpinner endValue = new JSpinner(
						new SpinnerNumberModel(p.getMax(), p.getMin(), p.getMax(), p.getStep()));
				endValue.setEnabled(false);
				
				JLabel stepLabel = new JLabel("Step");
				JSpinner stepValue = new JSpinner(
						new SpinnerNumberModel(p.getStep(), 1e-10, p.getMax() - p.getMin(), p.getStep()) );
				stepValue.setEnabled(false);

				parametersPanel.add(useParameter);
				parametersPanel.add(name);
				parametersPanel.add(startLabel);
				parametersPanel.add(startValue);
				parametersPanel.add(endLabel);
				parametersPanel.add(endValue);
				parametersPanel.add(stepLabel);
				parametersPanel.add(stepValue);
				
				pp.use = useParameter;
				pp.minValue = startValue;
				pp.maxValue = endValue;
				pp.stepValue = stepValue;
				
				index++;
			}
		}
		
		parametersPanel.setLayout(new SpringLayout());
		SpringUtilities.makeCompactGrid(parametersPanel, index, 8, 0, 0, 5, 5);
		
//		this.pack();
	}
	
	
	/**
	 * Starts a batch run process
	 */
	private void startBatchProcess() {
		// Create sweep controller
		ParameterSweep sweep = new ParameterSweep();
		
		// Create data set
		String varName = (String) comboVariable.getSelectedItem();
		
		// Create data analyzer
		DataAnalyzer dataAnalyzer = null;
		String fname = textExperimentalData.getText();
		
		if (fname != null) {
			if (new File(fname).exists()) {
				String method = (String) comboAnalysisMethod.getSelectedItem();
				try {
					dataAnalyzer = new DataAnalyzer(fname, method);
				}
				catch (Exception e) {
					dataAnalyzer = null;
					e.printStackTrace();
				}
			}
		}
		
		// Fill in the sweep controller
		for (ParameterWithOptions pp : parameters) {
			if (pp.use.isSelected()) {
				double min = ((Number) pp.minValue.getValue()).doubleValue();
				double max = ((Number) pp.maxValue.getValue()).doubleValue();
				double step = ((Number) pp.stepValue.getValue()).doubleValue();
				
				sweep.addParameter(pp.parameter, min, max, step);
			}
		}
		
		// Create batch run controller
		long ticks = ((Number) spinnerMaxTicks.getValue()).longValue();
		int repetitions = ((Number) spinnerRepetitionNumber.getValue()).intValue();
		String dataFileName = textDataFileName.getText();
		int dataInterval = ((Number) spinnerDataInterval.getValue()).intValue();
		boolean saveSnapshots = checkSaveSnapshots.isSelected();
		int snapshotInterval = ((Number) spinnerSnapshotInterval.getValue()).intValue();
		
		
		
		BatchRunController batchRunController = new BatchRunController(
								repetitions, ticks, dataFileName);
		batchRunController.setSaveDataFlag(checkSaveData.isSelected());
		batchRunController.setSaveDataInterval(dataInterval);
		batchRunController.setSaveFinalSnapshotsFlag(checkSaveFinalSnapshots.isSelected());
		
		batchRunController.setParameterSweepController(sweep);
		batchRunController.setDataAnalyzer(dataAnalyzer, varName);
		
		batchRunController.initOutputFolder(Coordinator.getInstance().getCurrentDir());
		
		// Initialize data layers
		boolean oneFileFlag = dataLayerOneFile.isSelected();
		int precision = ((Number) dataLayerPrecision.getValue()).intValue();
		int interval = ((Number) dataLayerInterval.getValue()).intValue();
		
		ArrayList<String> names = new ArrayList<String>();
		for (JCheckBox box : dataLayers) {
			if (box.isSelected()) {
				names.add(box.getText());
			}
		}
		
		String[] tmp = new String[names.size()];
		tmp = names.toArray(tmp);
		
		batchRunController.setDataLayers(tmp, interval, precision, oneFileFlag);
		
		// Change parameters before setup method is called
		sweep.setInitialValuesAndAdvance();
		
		// Start a batch run process
		new BatchRunManager(batchRunController, varName).start(saveSnapshots, snapshotInterval);
	}

	/**
	 * Action listener routine
	 */
	public void actionPerformed(ActionEvent arg) {
		String cmd = arg.getActionCommand().intern();
		
		
		// Parameter check box
		if (cmd.startsWith("use-parameter")) {
			int index = Integer.parseInt( cmd.substring("use-parameter".length()) );
			ParameterWithOptions p = parameters.get(index);
			
			boolean enabled = ((JCheckBox) arg.getSource()).isSelected();
			p.maxValue.setEnabled(enabled);
			p.minValue.setEnabled(enabled);
			p.stepValue.setEnabled(enabled);
			
			return;
		}
		
		
		// Start batch run process
		if (cmd == "start") {
			startBatchProcess();
			
			// Hide the dialog
			setVisible(false);
			return;
		}
		
		// Cancel button
		if (cmd == "cancel") {
			setVisible(false);
			return;
		}
		
		
		// Select experimental data file
		if (cmd == "select-file") {
			try {
				File file = FileUtils.openFileDialog(Coordinator.getInstance().getCurrentDir(), null, null);
				if (file != null) {
					textExperimentalData.setText(file.getAbsolutePath());
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			return;
		}
		

	}
}
