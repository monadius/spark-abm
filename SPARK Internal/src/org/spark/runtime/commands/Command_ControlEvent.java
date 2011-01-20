package org.spark.runtime.commands;

import org.spark.core.Observer;
import org.spark.core.SparkModel;
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
	
	// The index of the space for which the event is valid
	private int spaceIndex;
	
	// Key-related parameters
	private int keyCode;
	private char keySymbol;
	
	// Mouse-related parameters
	private int mouseButtons;
	// Mouse position (in the space coordinates)
	private double mouseX, mouseY;
	private int mouseWheelRotation;
	
	
	/**
	 * Constructor for key events
	 */
	public Command_ControlEvent(boolean keyPressed, int code, char symbol) {
		this.eventType = keyPressed ? KEY_PRESSED : KEY_RELEASED;
		this.keyCode = code;
		this.keySymbol = symbol;
	}
	
	
	/**
	 * Constructor for mouse events
	 */
	public Command_ControlEvent(int eventType, int mouseButtons, double mouseX, double mouseY, int mouseWheel) {
		this.eventType = eventType;
		this.mouseButtons = mouseButtons;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.mouseWheelRotation = mouseWheel;
	}
	
	/**
	 * Executes the command on the given model
	 * @param model
	 * @throws Exception
	 */
	public void execute(SparkModel model, AbstractSimulationEngine engine) throws Exception {
		Observer observer = model.getObserver();
		
		switch (eventType) {
		case KEY_PRESSED:
			observer.addCommand(String.valueOf(keySymbol));
		}
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
		return "Command_ControlEvent: " + eventToName(eventType);
	}
}
