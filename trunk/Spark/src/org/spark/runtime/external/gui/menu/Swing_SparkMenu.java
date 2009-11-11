package org.spark.runtime.external.gui.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.spinn3r.log5j.Logger;

/**
 * Swing implementation of the SPARK menu class
 * @author Monad
 *
 */
public class Swing_SparkMenu extends SparkMenu implements SwingMenu {
	private static final Logger logger = Logger.getLogger();
	
	/* Swing menu itself */
	private final JMenu menu;
	
	/**
	 * Protected constructor
	 * @param name
	 * @param group
	 */
	protected Swing_SparkMenu(String name, int group) {
		super(name, group);
		menu = new JMenu(name);
	}
	
	
	/**
	 * Adds the menu to the menu bar
	 * @return
	 */
	public void addToMenuBar(JMenuBar menuBar) {
		Component[] components = menu.getMenuComponents();
		
		for (int i = 0; i < components.length; i++) {
			Component item = components[i];
			if (item instanceof JMenu) {
				menuBar.add((JMenu) item);
			}
		}
	}
	
	
	/**
	 * Returns swing menu
	 * @return
	 */
	public JMenu getMenu() {
		return menu;
	}
	
	
	/**
	 * Implements SwingMenu interface
	 * @return
	 */
	public JMenuItem getSwingItem() {
		return menu;
	}
	

	@Override
	protected void insertAfter0(SparkMenuItem before, SparkMenuItem item, boolean separated) {
		if (item instanceof SwingMenu) {
			int index = findIndex(before);
			
			JMenuItem jItem = ((SwingMenu) item).getSwingItem();
			menu.insert(jItem, index + 1);
			
			if (separated) {
				if (index >= 0) {
					menu.insertSeparator(index + 1);
					index++;
				}
				
				if (index + 2 < menu.getMenuComponentCount())
					menu.insertSeparator(index + 2);
			}
			
		}
		else {
			logger.error("An attempt to insert a non-Swing menu item into a Swing menu");
		}
	}
	
	
	@Override
	protected void removeAt0(SparkMenuItem item) {
		int index = findIndex(item);
		if (index == -1)
			return;
		
		menu.remove(index);
		
		// Remove separators if necessary
		int n = menu.getMenuComponentCount();
		
		// TODO: implement
	}
	
	
	/**
	 * Returns the index of the given item in the swing menu
	 * @param item
	 * @return -1 if the item is not found
	 */
	private int findIndex(SparkMenuItem item) {
		if (item instanceof SwingMenu) {
			JMenuItem t = ((SwingMenu) item).getSwingItem();
			
			for (int i = 0; i < menu.getMenuComponentCount(); i++) {
				JMenuItem c = menu.getItem(i);
				
				if (c == t)
					return i;
			}
		}
		
		return -1;
	}

	
	@Override
	public void setActionListener(final ISparkMenuListener listener) {
		if (listener == null)
			return;
		
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				listener.onClick(Swing_SparkMenu.this);
			}
		});
	}

	@Override
	protected void stateChanged() {
		menu.setText(getName());
	}

}
