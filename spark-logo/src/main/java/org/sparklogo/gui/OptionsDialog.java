package org.sparklogo.gui;

import org.sparkabm.utils.FileUtils;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

class OptionsDialog extends JDialog implements ActionListener {
    /* A reference to the main frame */
    private final MainFrame parent;

    private final JTextField sparkLibTextField;

    /**
     * Creates a new options dialog
     */
    public OptionsDialog(MainFrame owner) {
        super(owner, "Options");
        parent = owner;

        setDefaultCloseOperation(HIDE_ON_CLOSE);

        /* Main panel */
        JPanel panel = new JPanel(new GridLayout(1, 3));

        sparkLibTextField = new JTextField();
        sparkLibTextField.setEditable(false);

        JButton sparkLibChangeButton = new JButton("...");
        sparkLibChangeButton.setActionCommand("spark-lib");
        sparkLibChangeButton.addActionListener(this);

        panel.add(new JLabel("Path to SPARK/lib:"));
        panel.add(sparkLibTextField);
        panel.add(sparkLibChangeButton);

        add(panel);
        pack();
    }

    /**
     * Sets the path to SPARK/lib
     */
    void setSparkLibPath(File path) {
        if (path != null) {
            sparkLibTextField.setText(path.getPath());
        }
    }

    public void actionPerformed(ActionEvent arg0) {
        String cmd = arg0.getActionCommand();

        try {
            /* Change spark-core path command */
            if (cmd.equals("spark-lib")) {
                File base = parent.getConfiguration().getLibPath();
                if (base == null || !base.isDirectory()) {
                    base = parent.getCurrentDirectory();
                }
                File file = FileUtils.selectDirDialog(parent, base);
                parent.getConfiguration().setLibPath(file);
                setSparkLibPath(file);
//                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.toString());
        }
    }
}
