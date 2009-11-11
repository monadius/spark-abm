package org.spark.runtime.external.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

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
