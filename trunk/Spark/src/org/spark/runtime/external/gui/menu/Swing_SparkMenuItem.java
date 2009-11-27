package org.spark.runtime.external.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * Swing implementation of the SparkMenuItem class
 * @author Monad
 *
 */
public class Swing_SparkMenuItem extends SparkMenuItem implements SwingMenu {
	/* Swing menu item */
	private final JMenuItem item;
	
	/**
	 * Protected constructor
	 * @param name
	 * @param group
	 */
	protected Swing_SparkMenuItem(String name, int group) {
		super(name, group);
		item = new JMenuItem(name);		
	}
	
	
	/**
	 * Sets the shortcut for the menu item
	 * @param key
	 * @param action
	 */
	public void setShortcut(int key, int action) {
		item.setAccelerator(KeyStroke.getKeyStroke(key, action));
	}

	
	
	@Override
	public void setActionListener(final ISparkMenuListener listener) {
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				listener.onClick(Swing_SparkMenuItem.this);
			}
		});
	}

	
	@Override
	protected void stateChanged() {
		item.setText(getName());
	}

	
	/**
	 * SwingMenu interface implementation
	 */
	public JMenuItem getSwingItem() {
		return item;
	}

}
