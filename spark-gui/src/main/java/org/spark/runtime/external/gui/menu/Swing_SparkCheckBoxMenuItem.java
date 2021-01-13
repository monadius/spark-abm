package org.spark.runtime.external.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

/**
 * Swing implementation of the SparkCheckBoxMenuItem class
 * @author Monad
 *
 */
public class Swing_SparkCheckBoxMenuItem extends SparkCheckBoxMenuItem implements SwingMenu {
	/* Swing menu item */
	private final JCheckBoxMenuItem item;
	
	/**
	 * Protected constructor
	 * @param name
	 * @param group
	 */
	protected Swing_SparkCheckBoxMenuItem(String name, int group) {
		super(name, group);
		item = new JCheckBoxMenuItem(name);		
	}
	
	
	@Override
	public void setActionListener(final ISparkMenuListener listener) {
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				listener.onClick(Swing_SparkCheckBoxMenuItem.this);
			}
		});
	}
	
	
	@Override
	public void setSelected(boolean value) {
		item.setSelected(value);
	}
	
	
	@Override
	public boolean isSelected() {
		return item.isSelected();
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
