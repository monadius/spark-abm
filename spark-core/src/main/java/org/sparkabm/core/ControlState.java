package org.sparkabm.core;

import java.util.HashMap;

import org.sparkabm.math.Vector;

/**
 * Describes a control event
 */
public class ControlState {
    // The name of the space where the event fired
    public final String spaceName;

    /**
     * Private constructor
     */
    private ControlState(String spaceName) {
        this.spaceName = spaceName;
    }

    /**
     * Stores information about pressed keys
     */
    public static class KeyState extends ControlState {
        private final HashMap<String, Boolean> keys;

        public KeyState(String spaceName) {
            super(spaceName);
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
     * Stores information about mouse position and pressed buttons
     */
    public static class MouseState extends ControlState {
        private Vector position;
        private String spaceName;
        private int buttons;
        private int wheel;

        public MouseState(String spaceName) {
            super(spaceName);
            this.position = new Vector();
            this.spaceName = spaceName;
        }

        /**
         * Changes a key state based on a keyboard event
         */
        public void changeState(MouseEvent e) {
            position = e.position;
            spaceName = e.spaceName;
            buttons = e.buttons;
            wheel = e.mouseWheel;
        }

        /**
         * Returns the space name
         *
         * @return
         */
        public String getSpaceName() {
            return spaceName;
        }

        /**
         * Returns the mouse position
         *
         * @return
         */
        public Vector getPosition() {
            return new Vector(position);
        }

        /**
         * Returns the state of mouse buttons
         *
         * @return
         */
        public int getButtons() {
            return buttons;
        }

        /**
         * Returns the state of the mouse wheel
         *
         * @return
         */
        public int getWheel() {
            return wheel;
        }
    }


    /**
     * Describes a keyboard control event
     */
    public static class KeyEvent extends ControlState {
        public final boolean keyPressed;
        public final String name;

        public KeyEvent(String spaceName, boolean keyPressed, String name) {
            super(spaceName);
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

        public MouseEvent(String spaceName, String eventType, Vector position, int buttons, int mouseWheel) {
            super(spaceName);
            this.eventType = eventType;
            this.position = new Vector(position);
            this.buttons = buttons;
            this.mouseWheel = mouseWheel;
        }
    }
}
