package org.spark.gui;

import java.awt.Dimension;
import java.io.File;
import java.io.FileReader;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

/**
 * This class displays the text information
 * 
 * @author Monad
 * 
 */
@SuppressWarnings("serial")
public class TextPanel extends JDialog {
	/**
	 * The default constructor
	 * @param file
	 */
	public TextPanel(File file) {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		String text = readFile(file);
		if (text == null) {
			text = "";
		}
		
		// Create a text pane
		JEditorPane textPane = new JEditorPane("text/html", text);
		textPane.setEditable(false);
//		textPane.setText(text);

		// Create a scroll pane
		JScrollPane scroll = new JScrollPane(textPane);
		scroll.setMinimumSize(new Dimension(100, 100));
		scroll.setPreferredSize(new Dimension(600, 600));
		add(scroll);
		pack();
	}
	

	/**
	 * Reads in the file into a string
	 * @param file
	 * @return
	 */
	private String readFile(File file) {
		if (file == null || !file.exists()) {
			return null;
		}

		try {
			// Read in the file
			FileReader in = new FileReader(file);
			char[] buffer = new char[4096];
			int len;
			StringBuilder text = new StringBuilder(10000);

			while ((len = in.read(buffer)) != -1) {
				text.append(buffer, 0, len);
			}
			
			// TODO: do we need this?
			return text.toString().replaceAll("\n\r", "<br>");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
