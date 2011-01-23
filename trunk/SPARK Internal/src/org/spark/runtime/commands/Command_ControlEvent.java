package org.spark.runtime.commands;

import java.awt.event.KeyEvent;

import org.spark.core.ControlState;
import org.spark.core.Observer;
import org.spark.core.SparkModel;
import org.spark.math.Vector;
import org.spark.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Key is pressed
 * @author Monad
 *
 */
@SuppressWarnings("serial")
public class Command_ControlEvent extends ModelManagerCommand {
	// Possible events
	public static final int KEY_PRESSED = 0x1;
	public static final int KEY_RELEASED = 0x2;
	public static final int LBUTTON_DOWN = 0x4;
	public static final int LBUTTON_UP = 0x8;
	public static final int RBUTTON_DOWN = 0x10;
	public static final int RBUTTON_UP = 0x20;
	public static final int MBUTTON_DOWN = 0x40;
	public static final int MBUTTON_UP = 0x80;
	public static final int MOUSE_MOVE = 0x100;
	public static final int MOUSE_WHEEL = 0x200;
	
	// Type of the control event 
	private final int eventType;
	
	// The name of the space for which the event is valid
	private final String spaceName;
	
	// Key-related parameters
	private int keyCode;
	private char keySymbol;
	
	// Mouse-related parameters
	private int mouseButtons;
	// Mouse position (in the space coordinates)
	private Vector mousePosition;
	private int mouseWheel;
	
	
	/**
	 * Constructor for key events
	 */
	public Command_ControlEvent(String spaceName, boolean keyPressed, int code, char symbol) {
		this.spaceName = spaceName;
		this.eventType = keyPressed ? KEY_PRESSED : KEY_RELEASED;
		this.keyCode = code;
		this.keySymbol = symbol;
	}
	
	
	/**
	 * Constructor for mouse events
	 */
	public Command_ControlEvent(String spaceName, int eventType, 
				int mouseButtons, Vector mousePosition, int mouseWheel) {
		this.spaceName = spaceName;
		this.eventType = eventType;
		this.mouseButtons = mouseButtons;
		this.mousePosition = mousePosition;
		this.mouseWheel = mouseWheel;
	}
	
	/**
	 * Executes the command on the given model
	 * @param model
	 * @throws Exception
	 */
	public void execute(SparkModel model, AbstractSimulationEngine engine) throws Exception {
		Observer observer = model.getObserver();
		
		switch (eventType) {
		// Key released
		case KEY_RELEASED:
			observer.addKeyEvent(new ControlState.KeyEvent(false, decodeKey(keyCode, keySymbol)));
			break;
		// Key pressed
		case KEY_PRESSED:
			observer.addKeyEvent(new ControlState.KeyEvent(true, decodeKey(keyCode, keySymbol)));
			break;
			
		// Mouse events
		default:
			observer.addMouseEvent(new ControlState.MouseEvent(eventToName(eventType), 
					mousePosition, mouseButtons, mouseWheel));
			break;
		}
	}
	
	
	/**
	 * Returns a name for the given key
	 * @param code
	 * @param symbol
	 * @return
	 */
	public static String decodeKey(int code, char symbol) {
		switch (code) {
		case KeyEvent.VK_UP:
			return "up";
		case KeyEvent.VK_DOWN:
			return "down";
		case KeyEvent.VK_LEFT:
			return "left";
		case KeyEvent.VK_RIGHT:
			return "right";
		case KeyEvent.VK_ENTER:
			return "enter";
		case KeyEvent.VK_ESCAPE:
			return "esc";
		case KeyEvent.VK_INSERT:
			return "insert";
		case KeyEvent.VK_HOME:
			return "home";
		case KeyEvent.VK_DELETE:
			return "delete";
		case KeyEvent.VK_SPACE:
			return "space";
		case KeyEvent.VK_END:
			return "end";
		case KeyEvent.VK_PAGE_DOWN:
			return "page down";
		case KeyEvent.VK_PAGE_UP:
			return "page_up";
		}
		
		return String.valueOf(symbol);
	}
	
	
	/**
	 * Converts the event type to its name
	 * @param eventType
	 * @return
	 */
	public static String eventToName(int eventType) {
		switch (eventType) {
		case KEY_PRESSED:
			return "KEY_PRESSED";
		case KEY_RELEASED:
			return "KEY_RELEASED";
		case LBUTTON_DOWN:
			return "LBUTTON_DOWN";
		case LBUTTON_UP:
			return "LBUTTON_UP";
		case RBUTTON_DOWN:
			return "RBUTTON_DOWN";
		case RBUTTON_UP:
			return "RBUTTON_UP";
		case MBUTTON_DOWN:
			return "MBUTTON_DOWN";
		case MBUTTON_UP:
			return "MBUTTON_UP";
		case MOUSE_MOVE:
			return "MOUSE_MOVE";
		case MOUSE_WHEEL:
			return "MOUSE_WHEEL";
		}
		
		return "(unknown)";
	}
	
	
	@Override
	public String toString() {
		String str = "Command_ControlEvent: " + eventToName(eventType);
		str += " at " + mousePosition;
		return str;
	}
}
