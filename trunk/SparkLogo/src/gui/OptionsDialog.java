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
	
	/* spark.jar */
	private File sparkLibrary;
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
	
//	private JTextField rtTextField;
	private JTextField sparkTextField;
	
//	private JButton rtChangeButton;
	private JButton sparkChangeButton;
	
	/**
	 * Creates a new options dialog
	 */
	public OptionsDialog(MainFrame owner) {
		super(owner, "Options");
		parent = owner;
		
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		
		panel = new JPanel(new GridLayout(1, 3));
		
//		rtTextField = new JTextField();
//		rtTextField.setEditable(false);
		
//		if (rtLibrary != null)
//			rtTextField.setText(rtLibrary.getPath());
		
		sparkTextField = new JTextField();
		sparkTextField.setEditable(false);
		
//		rtChangeButton = new JButton("...");
//		rtChangeButton.setActionCommand("rt-change");
//		rtChangeButton.addActionListener(this);
		
		sparkChangeButton = new JButton("...");
		sparkChangeButton.setActionCommand("spark-change");
		sparkChangeButton.addActionListener(this);
		
		panel.add(new JLabel("spark.jar path:"));
		panel.add(sparkTextField);
		panel.add(sparkChangeButton);
		
//		panel.add(new JLabel("rt.jar path:"));
//		panel.add(rtTextField);
//		panel.add(rtChangeButton);
		
		add(panel);
		pack();
	}

	
	/**
	 * Sets a path to spark.jar
	 * @param path
	 */
	public void setSparkPath(File path) {
		if (path != null && path.exists()) {
			sparkLibrary = path;
			sparkTextField.setText(sparkLibrary.getPath());
		}
	}
	
	
	/**
	 * Returns a path to spark.jar
	 * @return null if no path specified
	 */
	public File getSparkPath() {
		return sparkLibrary;
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
			/* Change spark path command */
			if (cmd.equals("spark-change")) {
				File file = parent.openFileDialog("jar");
				setSparkPath(file);
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
