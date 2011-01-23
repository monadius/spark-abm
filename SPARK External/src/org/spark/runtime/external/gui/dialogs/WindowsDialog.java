package org.spark.runtime.external.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.gui.SparkWindow;
import org.spark.runtime.external.gui.WindowManager;


/**
 * Dialog for managing windows
 */
@SuppressWarnings("serial")
public class WindowsDialog extends JDialog implements ActionListener {
	private final WindowManager winManager;
	private final JPanel mainPanel;
	private JList winList;
	
	/**
	 * Default constructor
	 * @param owner
	 */
	public WindowsDialog(JFrame owner) {
		super(owner, "Windows", true);
		
		this.winManager = Coordinator.getInstance().getWindowManager();
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.LINE_AXIS));
		
		init();
		
		add(mainPanel);
		pack();
	}
	
	
	/**
	 * Initializes the dialog
	 */
	private void init() {
		mainPanel.removeAll();
		
		// Create control buttons
		JPanel buttons = new JPanel();
		JButton kill = new JButton("Kill");

		kill.setActionCommand("kill");
		kill.addActionListener(this);
		
		buttons.add(kill);
		
		
		// Create the list containing all windows
		winList = new JList();
		updateWindows();
		
		// Add controls to the main panel
		mainPanel.add(winList);
		mainPanel.add(buttons);
	}
	
	
	/**
	 * Updates the list of windows
	 */
	private void updateWindows() {
		if (winManager == null)
			return;
		
		winList.setListData(winManager.getWindows());
	}
	
	
	/**
	 * Removes the window
	 */
	private void remove(SparkWindow win) {
		String message = "Do you want to remove the window ";
		message += win.getName();
		message += "?";
		int result = JOptionPane.showConfirmDialog(this, message, "Kill", JOptionPane.YES_NO_OPTION);
		
		if (result == JOptionPane.YES_OPTION) {
			if (winManager.removeWindow(win))
				updateWindows();
		}
	}

	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == null)
			return;
		
		Object obj = winList.getSelectedValue();
		if (obj == null || !(obj instanceof SparkWindow))
			return;
		
		if (winManager == null)
			return;
		
		SparkWindow win = (SparkWindow) obj;
		cmd = cmd.intern();
		
		// Kill
		if (cmd == "kill") {
			remove(win);
		}
	}

}
