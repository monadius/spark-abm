package org.sparkabm.gui.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.logging.Logger;

import org.sparkabm.gui.Coordinator;
import org.sparkabm.gui.gui.dialogs.RendererProperties;
import org.sparkabm.gui.renderer.Renderer;
import org.sparkabm.utils.XmlDocUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.swing.*;

/**
 * View panel
 *
 * @author Monad
 */
@SuppressWarnings("serial")
public class SparkViewPanel extends JPanel implements ISparkPanel,
        ActionListener {
    private static final Logger logger = Logger.getLogger(SparkViewPanel.class.getName());

    /* Renderer for this view */
    private Renderer renderer;
    /* Node for the render */
    private Node xmlNode;

    /* Window for this panel */
    private final SparkWindow win;

    /* Tool bar */
    private JToolBar toolBar;

    /* Pop-up menu */
    private JPopupMenu popup;

    // TODO: what happens when the render window is destroyed
    private RendererProperties rendererDialog;

    /**
     * Default constructor
     *
     * @param node
     * @param rendererType
     */
    public SparkViewPanel(WindowManager manager, Node node, int rendererType) {
        init(node, rendererType);

        // Set panel's location
        String location = XmlDocUtils.getValue(node, "location", null);
        win = manager.setLocation(this, location);

        if (renderer != null) {
            renderer.setName(win.getName());
            toolBar.setName(win.getName());
        }
    }


    /**
     * Creates a view panel in the given window
     *
     * @param win
     * @param rendererType
     */
    public SparkViewPanel(SparkWindow win, int rendererType) {
        init(null, rendererType);
        win.addPanel(this);

        this.win = win;
        if (renderer != null && win != null) {
            renderer.setName(win.getName());
            toolBar.setName(win.getName());
        }
    }


    /**
     * Creates a tool bar
     */
    private void createToolBar(Renderer renderer) {
        // Create a tool bar
        toolBar = new JToolBar("View");
        toolBar.setPreferredSize(new Dimension(100, 40));

        // Create buttons

        // Control state buttons
        JRadioButton select = new JRadioButton("select");
        JRadioButton move = new JRadioButton("move");
        JRadioButton control = new JRadioButton("control");

        select.setToolTipText("Selection mode");
        move.setToolTipText("Camera control mode");
        control.setToolTipText("Model control mode");

        int controlState = renderer.getControlState();
        switch (controlState) {
            case Renderer.CONTROL_STATE_SELECT:
                select.setSelected(true);
                break;
            case Renderer.CONTROL_STATE_MOVE:
                move.setSelected(true);
                break;
            case Renderer.CONTROL_STATE_CONTROL:
                control.setSelected(true);
                break;
        }

        select.setActionCommand("control-state:" + Renderer.CONTROL_STATE_SELECT);
        move.setActionCommand("control-state:" + Renderer.CONTROL_STATE_MOVE);
        control.setActionCommand("control-state:" + Renderer.CONTROL_STATE_CONTROL);

        select.addActionListener(this);
        move.addActionListener(this);
        control.addActionListener(this);

        // Group control state buttons
        ButtonGroup group = new ButtonGroup();
        group.add(select);
        group.add(move);
        group.add(control);


        // Commands
        JButton properties = new JButton("props");
        properties.setActionCommand("properties");
        properties.addActionListener(this);

        JButton reset = new JButton("reset");
        reset.setActionCommand("reset");
        reset.addActionListener(this);

        JButton snapshot = new JButton("snapshot");
        snapshot.setActionCommand("snapshot");
        snapshot.addActionListener(this);

        JButton rename = new JButton("rename");
        rename.setActionCommand("rename");
        rename.addActionListener(this);

        JButton remove = new JButton("remove");
        remove.setActionCommand("remove");
        remove.addActionListener(this);


        // Add buttons
        toolBar.add(select);
        toolBar.add(move);
        toolBar.add(control);

        toolBar.addSeparator();

        toolBar.add(reset);
        toolBar.add(snapshot);
        toolBar.add(rename);
        toolBar.add(properties);

        toolBar.addSeparator();

        toolBar.add(remove);


        // Add the tool bar
        add(toolBar, BorderLayout.PAGE_START);

    }


    /**
     * Initializes the panel
     */
    private void init(Node node, int rendererType) {
        setLayout(new BorderLayout());

        this.xmlNode = node;

        // Create render
        renderer = Coordinator.getInstance().createRenderer(node, rendererType);
        if (renderer == null) {
            logger.severe("Cannot create a renderer");
            return;
        }

        // Create a tool bar
        createToolBar(renderer);

        // Get the canvas and set up its event listeners
        Canvas canvas = renderer.getCanvas();
        canvas.addKeyListener(renderer);
        canvas.addMouseListener(renderer);
        canvas.addMouseMotionListener(renderer);
        canvas.addMouseWheelListener(renderer);

        add(canvas, BorderLayout.CENTER);

        // Create render properties dialog
        rendererDialog = new RendererProperties(renderer);
        rendererDialog.setVisible(false);

        // Create pop-up menu
        popup = new JPopupMenu();

        JMenuItem menuItem = new JMenuItem("Properties");
        menuItem.setActionCommand("properties");
        menuItem.addActionListener(this);
        popup.add(menuItem);

        menuItem = new JMenuItem("Snapshot");
        menuItem.setActionCommand("snapshot");
        menuItem.addActionListener(this);
        popup.add(menuItem);

        popup.addSeparator();

        menuItem = new JMenuItem("Rename");
        menuItem.setActionCommand("rename");
        menuItem.addActionListener(this);
        popup.add(menuItem);

        popup.addSeparator();

        menuItem = new JMenuItem("Remove View");
        menuItem.setActionCommand("remove");
        menuItem.addActionListener(this);
        popup.add(menuItem);

        // Add listener to components that can bring up popup menus.
        MouseListener popupListener = new PopupListener();
        canvas.addMouseListener(popupListener);
    }


    /**
     * Removes the panel and its window
     */
    public void remove() {
        String message = "Do you want to remove the window ";
        message += win.getName();
        message += "?";
        int result = JOptionPane.showConfirmDialog(this, message, "Kill", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            if (Coordinator.getInstance().getWindowManager().removeWindow(win)) {
                if (xmlNode != null)
                    xmlNode.getParentNode().removeChild(xmlNode);

                // TODO: clear data filter, etc.

                renderer = null;
                rendererDialog.dispose();
            }
        }
    }


    /**
     * Shows the pop-up menu
     *
     * @author Monad
     */
    class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (renderer.getControlState() == Renderer.CONTROL_STATE_SELECT)
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
        }
    }

    /**
     * Process actions of the pop-up menu
     */
    public void actionPerformed(ActionEvent arg0) {
        String cmd = arg0.getActionCommand().intern();

        try {
            // Control states
            if (cmd.startsWith("control-state:")) {
                int state = Integer.parseInt(cmd.substring("control-state:".length()));
                if (renderer != null)
                    renderer.setControlState(state);

                return;
            }

            // Reset
            if (cmd == "reset") {
                if (renderer != null)
                    renderer.resetCamera(true);
                return;
            }

            // Properties
            if (cmd == "properties") {
                rendererDialog.init();
                rendererDialog.setVisible(true);
                return;
            }

            // Snapshot
            if (cmd == "snapshot") {
                if (renderer != null)
                    renderer.takeSnapshot("");

                return;
            }

            // Remove
            if (cmd == "remove") {
                remove();
                return;
            }

            // Rename
            if (cmd == "rename") {
                String newName = JOptionPane.showInputDialog("Input new name", win.getName());
                if (newName != null) {
                    win.setName(newName);
                    renderer.setName(win.getName());
                }
                return;
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.toString());
        }

    }


    /**
     * Updates XML node
     */
    public void updateXML(SparkWindow location, Document xmlModelDoc, Node interfaceNode, File xmlModelFile) {
        if (xmlNode == null) {
            xmlNode = xmlModelDoc.createElement("renderframe");
            interfaceNode.appendChild(xmlNode);
        }

        XmlDocUtils.addAttr(xmlModelDoc, xmlNode, "location", location.getName());
        renderer.writeXML(xmlModelDoc, xmlNode, xmlModelFile.getParentFile());
    }


}
