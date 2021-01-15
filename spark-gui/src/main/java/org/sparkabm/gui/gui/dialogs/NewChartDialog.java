package org.sparkabm.gui.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sparkabm.math.parser.UserFunction;
import org.sparkabm.gui.Coordinator;
import org.sparkabm.gui.ProxyVariable;
import org.sparkabm.gui.gui.SparkChartPanel;
import org.sparkabm.gui.gui.WindowManager;
import org.sparkabm.gui.gui.SparkChartPanel.ChartType;

public class NewChartDialog extends JDialog implements ActionListener,
        ListSelectionListener {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger();

    // Commands
    private static final String CMD_SELECT = "select";
    private static final String CMD_REMOVE = "remove";
    private static final String CMD_RESET = "reset";
    private static final String CMD_CREATE = "create";
    private static final String CMD_EXPRESSION = "expression";

    // Components
    private JList varList;
    private JList selectedList;
    private JButton selectButton;
    private JButton removeButton;
    private JButton resetButton;
    private JButton createButton;
    private JTextField chartName;
    private JTextField expressionText;
    private JCheckBox pieChart;

    private DefaultListModel selectedModel;
    private final ArrayList<String> allVarNames;

    /**
     * Constructor
     */
    public NewChartDialog(JFrame owner) {
        super(owner, false);
        allVarNames = new ArrayList<String>();
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        create();
    }

    /**
     * Creates the dialog elements
     */
    private void create() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Create the list of variables
        varList = new JList();
        varList
                .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        varList.addListSelectionListener(this);

        // Create the list of selected variables
        selectedModel = new DefaultListModel();
        selectedList = new JList(selectedModel);
        selectedList
                .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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

        // Expressions
        // JPanel exprPanel = new JPanel(new GridLayout(1, 2));
        JPanel exprPanel = new JPanel();
        exprPanel.setLayout(new BoxLayout(exprPanel, BoxLayout.LINE_AXIS));
        JLabel label = new JLabel("Expression: ");
        label.setHorizontalAlignment(SwingConstants.RIGHT);

        expressionText = new JTextField();
        expressionText.setPreferredSize(new Dimension(500, 30));
        expressionText.setMinimumSize(new Dimension(300, 30));
        expressionText.setActionCommand(CMD_EXPRESSION);
        expressionText.addActionListener(this);

        exprPanel.add(label);
        exprPanel.add(expressionText);

        // Create additional controls
        JPanel ctrlPanel = new JPanel();
        ctrlPanel.setLayout(new GridLayout(0, 2));

        // Chart name
        chartName = new JTextField("New chart");
        label = new JLabel("Chart name: ");
        label.setHorizontalAlignment(SwingConstants.RIGHT);

        ctrlPanel.add(label);
        ctrlPanel.add(chartName);

        // Pie chart check box
        pieChart = new JCheckBox();
        label = new JLabel("Pie chart: ");
        label.setHorizontalAlignment(SwingConstants.RIGHT);

        ctrlPanel.add(label);
        ctrlPanel.add(pieChart);

        // Add all components
        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(ctrlPanel, BorderLayout.CENTER);
        panel2.add(buttons, BorderLayout.SOUTH);

        panel.add(exprPanel, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(panel2, BorderLayout.SOUTH);

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
        allVarNames.clear();

        // Get only variables of type double
        for (ProxyVariable var : vars) {
            if (var.getType() != ProxyVariable.DOUBLE_TYPE)
                continue;

            allVarNames.add(var.getName());
        }

        varList.setListData(allVarNames.toArray());
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
        } else {
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

        String name = chartName.getText();
        String location = win.getGoodName(name);

        ChartType chartType = ChartType.LINE_CHART;
        if (pieChart.isSelected())
            chartType = ChartType.PIE_CHART;

        SparkChartPanel chartPanel = new SparkChartPanel(c.getWindowManager(),
                1, location, chartType);

        // Add data series
        for (Object obj : selectedModel.toArray()) {
            if (obj instanceof String) {
                String varName = (String) obj;
                chartPanel.addSeries(varName, varName);
            } else if (obj instanceof UserFunction) {
                UserFunction f = (UserFunction) obj;
                chartPanel.addSeries(f, f.toString());
            }
        }

        // Register new chart panel as a data consumer
        c.getDataReceiver().addDataConsumer(chartPanel.getDataFilter());
    }


    /**
     * Parses an expression
     */
    private UserFunction parseExpression(String text) throws Exception {
        UserFunction f = UserFunction.create(text);

        // Check that all variable names are present in the list of all variables
        String[] varNames = f.getVarNames();

        for (String name : varNames) {
            if (!allVarNames.contains(name))
                throw new Exception("Variable " + name + " is not defined");
        }

        return f;
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

        try {
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

            // Expression
            if (cmd == CMD_EXPRESSION) {
                UserFunction f = parseExpression(expressionText.getText());
                selectedModel.addElement(f);
                enableButtons();
                return;
            }
        } catch (Exception ex) {
            logger.error(ex);
            JOptionPane.showMessageDialog(this, ex.toString());
        }
    }

}
