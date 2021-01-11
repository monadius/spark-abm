package gui;

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

@SuppressWarnings("serial")
public class OptionsDialog extends JDialog implements ActionListener {
	/* A reference to the main frame */
	private MainFrame parent;
	
	/* spark-core.jar */
	private File sparkCore;
	
	/* spark-external.jar */
	private File sparkExternal;
	
	private JTextField sparkCoreTextField;
	private JTextField sparkExternalTextField;
	
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

	/* Main panel */
	private JPanel panel;
	
	/**
	 * Creates a new options dialog
	 */
	public OptionsDialog(MainFrame owner) {
		super(owner, "Options");
		parent = owner;
		
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		
		panel = new JPanel(new GridLayout(2, 3));
		
//		rtTextField = new JTextField();
//		rtTextField.setEditable(false);
		
//		if (rtLibrary != null)
//			rtTextField.setText(rtLibrary.getPath());
		
		sparkCoreTextField = new JTextField();
		sparkCoreTextField.setEditable(false);
		
	    sparkExternalTextField = new JTextField();
		sparkExternalTextField.setEditable(false);

//		rtChangeButton = new JButton("...");
//		rtChangeButton.setActionCommand("rt-change");
//		rtChangeButton.addActionListener(this);
		
		JButton sparkCoreChangeButton = new JButton("...");
		sparkCoreChangeButton.setActionCommand("spark-core");
		sparkCoreChangeButton.addActionListener(this);

		JButton sparkExternalChangeButton = new JButton("...");
		sparkExternalChangeButton.setActionCommand("spark-external");
		sparkExternalChangeButton.addActionListener(this);

		panel.add(new JLabel("spark-core.jar path:"));
		panel.add(sparkCoreTextField);
		panel.add(sparkCoreChangeButton);

		panel.add(new JLabel("spark-external.jar path:"));
		panel.add(sparkExternalTextField);
		panel.add(sparkExternalChangeButton);
		
//		panel.add(new JLabel("rt.jar path:"));
//		panel.add(rtTextField);
//		panel.add(rtChangeButton);
		
		add(panel);
		pack();
	}

	
	/**
	 * Sets the path to spark-core.jar
	 * @param path
	 */
	public void setSparkCorePath(File path) {
		if (path != null && path.exists()) {
			sparkCore = path;
			sparkCoreTextField.setText(sparkCore.getPath());
		}
	}

	/**
	 * Sets the path to spark-external.jar
	 * @param path
	 */
	public void setSparkExternalPath(File path) {
		if (path != null && path.exists()) {
			sparkExternal = path;
			sparkExternalTextField.setText(sparkExternal.getPath());
		}
	}

	
	
	/**
	 * Returns the path to spark-core.jar
	 * @return null if no path specified
	 */
	public File getSparkCorePath() {
		return sparkCore;
	}
	
	
	/**
	 * Returns a path to spark-external.jar
	 * @return null if no path specified
	 */
	public File getSparkExternalPath() {
		return sparkExternal;
	}

	
	
	/**
	 * Sets a path to rt.jar
	 * @param path
	 */
/*	public void setRtPath(File path) {
		if (path != null && path.exists()) {
			rtLibrary = path;
			rtTextField.setText(rtLibrary.getPath());
		}
	}
*/
	
	/**
	 * Returns a path to rt.jar
	 * @return null if no path specified
	 */
/*	public File getRtPath() {
		return rtLibrary;
	}
*/	
	
	

	
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();
		
		try {
			/* Change spark-core path command */
			if (cmd.equals("spark-core")) {
				File file = parent.openFileDialog("jar");
				setSparkCorePath(file);
				return;
			}

			/* Change spark-external path command */
			if (cmd.equals("spark-external")) {
				File file = parent.openFileDialog("jar");
				setSparkExternalPath(file);
				return;
			}

			
			/* Change rt path command */
/*			if (cmd.equals("rt-change")) {
				File file = parent.openFileDialog("jar");
				setRtPath(file);
				return;
			}
*/
		}
		catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.toString());
		}
	}
}
