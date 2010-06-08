package org.spark.gui.dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;

import org.spark.core.SparkModel;
import org.spark.gui.GUIModelManager;
import org.spark.gui.Utils;
import org.spark.runtime.BatchRunController;
import org.spark.runtime.DataAnalyzer;
import org.spark.runtime.DataSet;
import org.spark.runtime.ParameterFactory_Old;
import org.spark.runtime.ParameterSweep;
import org.spark.runtime.Parameter_Old;
import org.spark.runtime.internal.ModelVariable;
import org.spark.utils.SpringUtilities;

/**
 * The dialog for setting up batch run properties 
 * @author Monad
 */
public class BatchRunDialog extends JDialog implements ActionListener {
	/* Serial version UID */
	private static final long serialVersionUID = 5522509798084286407L;

	/* Main panel */
	private JPanel panel;
	
	/* Parameters panel */
	private JPanel parametersPanel;
	
	/* General controls */
	private JSpinner spinnerMaxTicks;
	private JSpinner spinnerRepetitionNumber;
	private JTextField textDataFileName;
	private JCheckBox checkSaveData;
	
	/* Data variable for analysis */
	private JComboBox comboVariable;
	/* Experimental data file */
	private JTextField textExperimentalData;
	/* Data analysis method */
	private JComboBox comboAnalysisMethod;
	

	/* Buttons */
	private JButton buttonStart, buttonCancel;

	/**
	 * Auxiliary class
	 */
	private class ParameterWithOptions {
		public Parameter_Old parameter;
		public JCheckBox use;
		public JSpinner minValue;
		public JSpinner maxValue;
		public JSpinner stepValue;
	}
	
	/* List of all parameters */
	private final ArrayList<ParameterWithOptions> parameters;
	
	/* Batch run controller */
	private BatchRunController batchRunController;
	
	/**
	 * Default constructor
	 * @param owner
	 */
	public BatchRunDialog(JFrame owner) {
		super(owner, true);
		
		parameters = new ArrayList<ParameterWithOptions>();
		
		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		initGeneralControls();

		parametersPanel = new JPanel();
		parametersPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));
		parametersPanel.setMinimumSize(new Dimension(300, 100));
		
		panel.add(parametersPanel);
		
		initDataPanel();
		
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
		
		this.add(panel);
		this.pack();
	}
	
	
	/**
	 * Returns the batch run controller
	 * @return
	 */
	public BatchRunController getBatchRunController() {
		return batchRunController;
	}
	
	
	/**
	 * Creates a panel for data analysis
	 */
	private void initDataPanel() {
		// Create panel
		JPanel dataPanel = new JPanel(new SpringLayout());
		dataPanel.setBorder(BorderFactory.createTitledBorder("Data Analysis"));
		dataPanel.setMinimumSize(new Dimension(100, 100));
		
		// Create controls
		comboVariable = new JComboBox();
		
		textExperimentalData = new JTextField();
		textExperimentalData.setEditable(false);
		
		JButton selectFile = new JButton("...");
		selectFile.setActionCommand("select-file");
		selectFile.addActionListener(this);
		
		comboAnalysisMethod = new JComboBox(new String[] {
				"Least Squares", "Correlation"});
		
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
		
		panel.add(dataPanel);
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
		
		checkSaveData = new JCheckBox();
		checkSaveData.setSelected(true);
		
		// Add controls
		generalControls.add(new JLabel("Number of ticks"));
		generalControls.add(spinnerMaxTicks);
		
		generalControls.add(new JLabel("Number of repetitions"));
		generalControls.add(spinnerRepetitionNumber);
		
		generalControls.add(new JLabel("Data file name"));
		generalControls.add(textDataFileName);
		
		generalControls.add(new JLabel("Save data"));
		generalControls.add(checkSaveData);

		SpringUtilities.makeCompactGrid(generalControls, 4, 2, 5, 5, 10, 10);
		
		panel.add(generalControls);
	}
	
	
	
	/**
	 * Initializes the dialog
	 */
	public void init() {
		initParameters();
		initVariables();
	}
	
	
	/**
	 * Initializes data variables
	 */
	private void initVariables() {
		SparkModel model = GUIModelManager.getInstance().getModel();
		ModelVariable[] vars = model.getVariables();
		ArrayList<String> names = new ArrayList<String>();
		
		for (int i = 0; i < vars.length; i++) {
			ModelVariable var = vars[i];
			// TODO: other types
			if (var.getType() == Double.class ||
					var.getType() == Integer.class) {
				names.add(var.getName());
			}
		}
		
		comboVariable.setModel( 
				new DefaultComboBoxModel(names.toArray()) );
	}
	
	
	/**
	 * Initializes parameter controls
	 */
	private void initParameters() {
		// Remove all controls
		parametersPanel.removeAll();
		parameters.clear();
		
		int index = 0;
		
		// Create controls for parameters
		Parameter_Old[] parameters = ParameterFactory_Old.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			Parameter_Old p = parameters[i];
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
		
		this.pack();
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
			// Create sweep controller
			ParameterSweep sweep = new ParameterSweep();
			
			// Create data set
			DataSet dataSet = new DataSet();
			String varName = (String) comboVariable.getSelectedItem();
			if (varName != null) {
				SparkModel model = GUIModelManager.getInstance().getModel();
				dataSet.addVariable(model, varName);
			}
			
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
			
			batchRunController = new BatchRunController(repetitions, ticks, dataFileName);
			batchRunController.setSaveDataFlag(checkSaveData.isSelected());
			
			batchRunController.setParameterSweepController(sweep);
			batchRunController.setDataSet(dataSet);
			batchRunController.setDataAnalyzer(dataAnalyzer, varName);
			
			File logFile = new File(GUIModelManager.getInstance().getXmlDocumentFile().getParent(),
					dataFileName + "_log.csv");
			try {
				batchRunController.setLogFile(logFile);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			// Change parameters before setup method is called
			sweep.setInitialValuesAndAdvance();
			
			// Hide the dialog
			setVisible(false);
			return;
		}
		
		// Cancel button
		if (cmd == "cancel") {
			batchRunController = null;
			setVisible(false);
			return;
		}
		
		
		// Select experimental data file
		if (cmd == "select-file") {
			try {
				File file = Utils.openFile(this, null);
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
