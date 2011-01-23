package org.spark.core;

import java.util.HashMap;

import org.spark.math.Vector;

/**
 * Describes a control event
 */
public class ControlState {
	/**
	 * Stores information about pressed keys
	 */
	public static class KeyState extends ControlState {
		private final HashMap<String, Boolean> keys;
		
		public KeyState() {
			this.keys = new HashMap<String, Boolean>();
		}
		
		/**
		 * Changes a key state based on a keyboard event
		 */
		public void changeState(KeyEvent e) {
			keys.put(e.name, e.keyPressed);
		}
		
		/**
		 * Returns the state of the given key
		 */
		public boolean getState(String keyName) {
			Boolean state = keys.get(keyName);
			if (state == null)
				return false;
			
			return state.booleanValue();
		}
		
		/**
		 * Resets all states
		 */
		public void reset() {
			keys.clear();
		}
	}
	
	
	/**
	 * Describes a keyboard control event
	 */
	public static class KeyEvent extends ControlState {
		public final boolean keyPressed;
		public final String name;
		
		public KeyEvent(boolean keyPressed, String name) {
			this.keyPressed = keyPressed;
			this.name = name;
		}
	}
	

	/**
	 * Describes a mouse control event
	 */
	public static class MouseEvent extends ControlState {
		public final String eventType;
		public final Vector position;
		public final int buttons;
		public final int mouseWheel;
		
		public MouseEvent(String eventType, Vector position, int buttons, int mouseWheel) {
			this.eventType = eventType;
			this.position = new Vector(position);
			this.buttons = buttons;
			this.mouseWheel = mouseWheel;
		}
	}
}
