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

public class OptionsDialog extends JDialog implements ActionListener {
    /* A reference to the main frame */
    private final MainFrame parent;

    /* SPARK/lib */
    private File sparkLib;

    /* spark-external.jar */
//    private File sparkExternal;

    private final JTextField sparkLibTextField;
//    private final JTextField sparkExternalTextField;

    /* rt.jar */
/*	private File rtLibrary;
	{
		String path = System.getProperty("sun.boot.class.path");
		if (path != null) {
			String[] paths = path.split(";");
			for (int i = 0; i < paths.length; i++) {
				if (paths[i].endsWith("rt.jar")) {
					rtLibrary = new File(paths[i]);
				}
			}
		}
	}*/

    /**
     * Creates a new options dialog
     */
    public OptionsDialog(MainFrame owner) {
        super(owner, "Options");
        parent = owner;

        setDefaultCloseOperation(HIDE_ON_CLOSE);

        /* Main panel */
        JPanel panel = new JPanel(new GridLayout(1, 3));

//		rtTextField = new JTextField();
//		rtTextField.setEditable(false);

//		if (rtLibrary != null)
//			rtTextField.setText(rtLibrary.getPath());

        sparkLibTextField = new JTextField();
        sparkLibTextField.setEditable(false);

//        sparkExternalTextField = new JTextField();
//        sparkExternalTextField.setEditable(false);

//		rtChangeButton = new JButton("...");
//		rtChangeButton.setActionCommand("rt-change");
//		rtChangeButton.addActionListener(this);

        JButton sparkLibChangeButton = new JButton("...");
        sparkLibChangeButton.setActionCommand("spark-lib");
        sparkLibChangeButton.addActionListener(this);

//        JButton sparkExternalChangeButton = new JButton("...");
//        sparkExternalChangeButton.setActionCommand("spark-external");
//        sparkExternalChangeButton.addActionListener(this);

        panel.add(new JLabel("Path to SPARK/lib:"));
        panel.add(sparkLibTextField);
        panel.add(sparkLibChangeButton);

//        panel.add(new JLabel("spark-external.jar path:"));
//        panel.add(sparkExternalTextField);
//        panel.add(sparkExternalChangeButton);

//		panel.add(new JLabel("rt.jar path:"));
//		panel.add(rtTextField);
//		panel.add(rtChangeButton);

        add(panel);
        pack();
    }


    /**
     * Sets the path to SPARK/lib
     */
    public void setSparkLibPath(File path) {
        if (path != null && path.exists() && path.isDirectory()) {
            sparkLib = path;
            sparkLibTextField.setText(sparkLib.getPath());
        }
    }

//    /**
//     * Sets the path to spark-external.jar
//     */
//    public void setSparkExternalPath(File path) {
//        if (path != null && path.exists()) {
//            sparkExternal = path;
//            sparkExternalTextField.setText(sparkExternal.getPath());
//        }
//    }


    /**
     * Returns the path to spark-core.jar
     *
     * @return null if no path specified
     */
    public File getSparkLibPath() {
        return sparkLib;
    }


//    /**
//     * Returns a path to spark-external.jar
//     *
//     * @return null if no path specified
//     */
//    public File getSparkExternalPath() {
//        return sparkExternal;
//    }


//    /**
//     * Sets a path to rt.jar
//     * @param path
//     */
//	public void setRtPath(File path) {
//		if (path != null && path.exists()) {
//			rtLibrary = path;
//			rtTextField.setText(rtLibrary.getPath());
//		}
//	}


//    /**
//     * Returns a path to rt.jar
//     *
//     * @return null if no path specified
//     */
//	public File getRtPath() {
//		return rtLibrary;
//	}

    public void actionPerformed(ActionEvent arg0) {
        String cmd = arg0.getActionCommand();

        try {
            /* Change spark-core path command */
            if (cmd.equals("spark-lib")) {
                File base = sparkLib != null ? sparkLib : parent.getCurrentDirectory();
                File file = FileUtils.selectDirDialog(parent, base);
                setSparkLibPath(file);
//                return;
            }

//            /* Change spark-external path command */
//            if (cmd.equals("spark-external")) {
//                File file = parent.openFileDialog("jar");
//                setSparkExternalPath(file);
//                return;
//            }


            /* Change rt path command */
/*			if (cmd.equals("rt-change")) {
				File file = parent.openFileDialog("jar");
				setRtPath(file);
				return;
			}
*/
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.toString());
        }
    }
}
