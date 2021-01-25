package org.sparkabm.gui.gui.dialogs;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;

import org.sparkabm.gui.Configuration;
import org.sparkabm.gui.Coordinator;
import org.sparkabm.gui.renderer.Renderer;
import org.sparkabm.gui.renderer.font.FontManager;
import org.sparkabm.utils.FileUtils;
import org.sparkabm.utils.SpringUtilities;


/**
 * SPARK preferences dialog
 *
 * @author Alexey
 */
public class SparkPreferencesDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;

    /* Coordinator configuration */
    private final Configuration config;

    /* Main panel */
    private final JPanel mainPanel;

    /* Main tabbed panel */
    private final JTabbedPane tabs;

    /* Specifies the maximum number of recent projects */
    private JComboBox recentProjectsBox;

    /* Font controls */
    private JTextField fontDir;
    private JTextField defaultFont;
    private JComboBox defaultFonts;

    /* Graphics buttons */
    private JRadioButton buttonJava2d;
    private JRadioButton buttonJOGL;

    // Constants
    private static final String CMD_SAVE = "save-configuration";
    private static final String CMD_FONT_DIR = "font-dir";
    private static final String CMD_DEFAULT_FONT = "default-font";


    /**
     * Default constructor
     *
     * @param owner
     */
    public SparkPreferencesDialog(JFrame owner, Configuration config) {
        super(owner, "Preferences", true);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        this.config = config;
        this.mainPanel = new JPanel();
        this.tabs = new JTabbedPane();

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Create the graphics panel
        JPanel graphics = createGraphicsPanel();

        // Create the general panel
        JPanel general = createGeneralPanel();

        // Create the font panel
        JPanel font = createFontPanel();


        // Add tabs to the main panel
        tabs.addTab("Graphics", graphics);
        tabs.addTab("General", general);
        tabs.addTab("Bitmap Fonts", font);

        mainPanel.add(tabs);

        // Create save button
        JButton save = new JButton("Save");
        save.setActionCommand(CMD_SAVE);
        save.addActionListener(this);
        mainPanel.add(save);

        this.getContentPane().add(mainPanel);
        this.pack();
    }


    /**
     * Initializes dialog's elements
     */
    public void init() {
        int rendererType = config.getRendererType();

        // Graphics
        if (rendererType == Renderer.JOGL_RENDERER)
            buttonJOGL.setSelected(true);
        else
            buttonJava2d.setSelected(true);

        // Recent projects
        int maxRecentProjects = config.getMaxRecentProjects();
        recentProjectsBox.setSelectedItem(maxRecentProjects);

        // Font
        FontManager fontManager = Coordinator.getInstance().getFontManager();

        // Directory
        File[] dirs = fontManager.getFontDirectories();
        if (dirs.length > 0) {
            fontDir.setText(dirs[0].getPath());
        } else {
            fontDir.setText("");
        }

        // Names
        String defaultName = fontManager.getDefaultFontName();
        if (defaultName != null)
            defaultFont.setText(defaultName);
        else
            defaultFont.setText("");

        String[] names = fontManager.getFontNames();
        defaultFonts.removeAllItems();

        for (String name : names) {
            defaultFonts.addItem(name);
        }

        defaultFonts.setSelectedItem(defaultName);
    }


    /**
     * Initializes the panel with graphics options
     */
    private JPanel createGraphicsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 1));
        panel.setMinimumSize(new Dimension(300, 100));
        panel.setPreferredSize(new Dimension(400, 100));
        panel.setBorder(BorderFactory.createTitledBorder("Reload the open model after selecting a new renderer"));

        ButtonGroup group = new ButtonGroup();

        /* Create Java2D button */
        buttonJava2d = new JRadioButton("Java2D");
        buttonJava2d.setActionCommand("Java2D");
        buttonJava2d.addActionListener(this);
        group.add(buttonJava2d);
        panel.add(buttonJava2d);

        /* Create JOGL button */
        buttonJOGL = new JRadioButton("JOGL (OpenGL)");
        buttonJOGL.setActionCommand("JOGL");
        buttonJOGL.addActionListener(this);
        group.add(buttonJOGL);
        panel.add(buttonJOGL);

        return panel;
    }


    /**
     * Initializes the panel with general options
     */
    private JPanel createGeneralPanel() {
        JPanel general = new JPanel();
//		general.setLayout(new BoxLayout(general, BoxLayout.LINE_AXIS));
        general.setLayout(new SpringLayout());

        // Create controls
        recentProjectsBox = new JComboBox(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 30, 40, 50});
        recentProjectsBox.setActionCommand("RecentProjects");
        recentProjectsBox.addActionListener(this);

        // Add controls
        general.add(new JLabel("Number of recent projects"));
        general.add(recentProjectsBox);

        SpringUtilities.makeCompactGrid(general, 1, 2, 0, 0, 5, 5);

        return general;
    }


    /**
     * Initializes the panel with font options
     */
    private JPanel createFontPanel() {
//		JPanel font = new JPanel(new GridLayout(2, 3));
        JPanel font = new JPanel();
        font.setLayout(new SpringLayout());

        // Directory
        fontDir = new JTextField();
        fontDir.setMaximumSize(new Dimension(600, 70));
        fontDir.setEditable(false);

        JButton dirButton = new JButton("...");
        dirButton.setActionCommand(CMD_FONT_DIR);
        dirButton.addActionListener(this);

        // Default font
        defaultFont = new JTextField();
        defaultFont.setMaximumSize(new Dimension(600, 70));
        defaultFont.setEditable(false);

        defaultFonts = new JComboBox();
        defaultFonts.setActionCommand(CMD_DEFAULT_FONT);
        defaultFonts.addActionListener(this);

        // Add controls
        font.add(new JLabel("Font directory: "));
        font.add(fontDir);
        font.add(dirButton);

        font.add(new JLabel("Default font: "));
        font.add(defaultFont);
        font.add(defaultFonts);

        SpringUtilities.makeCompactGrid(font, 2, 3, 0, 0, 5, 5);

        return font;
    }


    /**
     * Updates the configuration
     */
    private void updateConfiguration() {
        // Graphics
        if (buttonJOGL.isSelected())
            config.setRendererType(Renderer.JOGL_RENDERER);
        else
            config.setRendererType(Renderer.JAVA_2D_RENDERER);

        // Recent projects
        int max = (Integer) recentProjectsBox.getSelectedItem();
        config.setMaxRecentProjects(max);

        // Font
        FontManager fontManager = Coordinator.getInstance().getFontManager();

        fontManager.clear();
        fontManager.load(new File(fontDir.getText()));
        fontManager.setDefaultFontName(defaultFont.getText());

        config.saveFontManager(fontManager);
    }


    /**
     * Action listener
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == null)
            return;

        cmd = cmd.trim();

        // Font directory
        if (cmd == CMD_FONT_DIR) {
            File dir = FileUtils.selectDirDialog(null, new File("."));
            if (dir == null || !dir.exists() || !dir.isDirectory())
                return;

            fontDir.setText(dir.getPath());
            return;
        }

        // Default font
        if (cmd == CMD_DEFAULT_FONT) {
            Object item = defaultFonts.getSelectedItem();
            if (!(item instanceof String))
                return;

            String name = (String) item;
            defaultFont.setText(name);
            return;
        }

        // Save
        if (cmd == CMD_SAVE) {
            updateConfiguration();
            setVisible(false);
            return;
        }
    }

}

