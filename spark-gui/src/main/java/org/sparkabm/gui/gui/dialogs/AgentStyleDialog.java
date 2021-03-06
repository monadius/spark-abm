package org.sparkabm.gui.gui.dialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sparkabm.gui.renderer.AgentStyle;
import org.sparkabm.gui.renderer.Renderer;
import org.sparkabm.math.Vector4d;
import org.sparkabm.gui.Coordinator;
import org.sparkabm.gui.renderer.font.BitmapFont;
import org.sparkabm.gui.renderer.font.FontManager;
import org.sparkabm.utils.FileUtils;

/**
 * The dialog for configuring advanced properties
 * of an agent's style
 *
 * @author Monad
 */
public class AgentStyleDialog extends JDialog {

    /**
     * Default constructor
     */
    public AgentStyleDialog(Window owner, Renderer renderer, AgentStyle style) {
        super(owner, "Advanced properties");
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        init(renderer, style);
    }


    /**
     * Initializes the dialog elements
     */
    private void init(Renderer renderer, AgentStyle style) {
        // Main pane
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Label", new LabelOptions(renderer, style));
        tabs.addTab("Image", new ImageOptions(renderer, style));
        tabs.addTab("Blending", new AlphaBlendingOptions(renderer, style));

        this.add(tabs);
        this.pack();
    }
}


/**
 * Abstract base for option panels
 *
 * @author Alexey
 */
abstract class OptionPanel extends JPanel implements ActionListener {
    protected AgentStyle style;
    protected Renderer renderer;


    /**
     * Default constructor
     */
    public OptionPanel(Renderer renderer, AgentStyle style) {
        this.renderer = renderer;
        this.style = style;
        init();
    }

    protected abstract void init();
}


/**
 * Image options
 */
class ImageOptions extends OptionPanel implements ChangeListener {
    private JCheckBox drawShape;
    private JCheckBox colorBlending;
    private JCheckBox modulateSize;
    // Image manager selection button
    private JButton managerButton;
    private JSpinner scaleSpinner;

    private final static String CMD_MODULATE_SIZE = "modulate-size";
    private final static String CMD_MANAGER = "manager";
    private final static String CMD_DRAW_SHAPE = "draw-shape";
    private final static String CMD_COLOR_BLENDING = "color-blending";


    public ImageOptions(Renderer renderer, AgentStyle style) {
        super(renderer, style);
    }

    @Override
    protected void init() {
//		setLayout(new SpringLayout());
        JPanel panel = new JPanel(new GridLayout(5, 2));

        // Image manager selection button
        managerButton = new JButton();
        if (style.getTileManager() == null)
            managerButton.setText("null");
        else
            managerButton.setText(style.getTileManager().getName());

        managerButton.setActionCommand(CMD_MANAGER);
        managerButton.addActionListener(this);

        // Scale spinner
        scaleSpinner = new JSpinner(
                new SpinnerNumberModel(style.getScaleFactor(), -100, 100, 0.01));
        scaleSpinner.addChangeListener(this);


        // Draw shape check box
        drawShape = new JCheckBox();
        drawShape.setSelected(style.getDrawShapeWithImageFlag());
        drawShape.setActionCommand(CMD_DRAW_SHAPE);
        drawShape.addActionListener(this);

        // Color blending check box
        colorBlending = new JCheckBox();
        colorBlending.setSelected(style.getColorBlending());
        colorBlending.setActionCommand(CMD_COLOR_BLENDING);
        colorBlending.addActionListener(this);

        // Modulate size
        modulateSize = new JCheckBox();
        modulateSize.setSelected(style.getModulateSize());
        modulateSize.setActionCommand(CMD_MODULATE_SIZE);
        modulateSize.addActionListener(this);


        // Add all components to the main pane
        panel.add(new JLabel("Image Manager"));
        panel.add(managerButton);

        panel.add(new JLabel("Scale"));
        panel.add(scaleSpinner);

        panel.add(new JLabel("Modulate scale"));
        panel.add(modulateSize);

        panel.add(new JLabel("Draw shape"));
        panel.add(drawShape);

        panel.add(new JLabel("Blend colors"));
        panel.add(colorBlending);

        add(panel);
//		SpringUtilities.makeCompactGrid(this, 5, 2, 5, 5, 5, 5);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case CMD_DRAW_SHAPE: {
                boolean flag = drawShape.isSelected();
                style.setDrawShapeWithImageFlag(flag);
                renderer.update();
                break;
            }

            case CMD_COLOR_BLENDING: {
                boolean flag = colorBlending.isSelected();
                style.setColorBlending(flag);
                renderer.update();
                break;
            }

            case CMD_MODULATE_SIZE: {
                boolean flag = modulateSize.isSelected();
                style.setModulateSize(flag);
                renderer.update();
                break;
            }

            case CMD_MANAGER: {
                File file = FileUtils.openFileDialog(null, Coordinator.getInstance().getCurrentDir(), "xml");
                if (file != null) {
                    style.setTileManager(file);
                    if (style.getTileManager() == null)
                        managerButton.setText("null");
                    else
                        managerButton.setText(style.getTileManager().getName());

                    renderer.update();
                } else {
                    style.setTileManager(null);
                    managerButton.setText("null");
                    renderer.update();
                }
                break;
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        float scale = ((Number) scaleSpinner.getValue()).floatValue();
        style.setScaleFactor(scale);
        renderer.update();
    }
}


/**
 * Label (font and position) options
 */
class LabelOptions extends OptionPanel implements ChangeListener {
    // Label dx, dy
    private JSpinner spinnerDx;
    private JSpinner spinnerDy;

    // Label width, height
    private JSpinner spinnerWidth;
    private JSpinner spinnerHeight;

    // Font relative size
    private JSpinner spinnerSize;

    // Font (Java);
    private JButton fontButton;
    // Color
    private JButton colorButton;
    // All available bitmap fonts
    private JComboBox<String> bitmapFonts;
    // Text alignment options
    private JComboBox<BitmapFont.Align> textAlignments;

    // Modulate label color check box
    private JCheckBox modulateLabelColor;

    // Modulate label size check box
    private JCheckBox modulateLabelSize;

    // Constants
    private static final String CMD_COLOR = "color";
    private static final String CMD_FONT = "font";
    private static final String CMD_BITMAP_FONT = "bitmap-font";
    private static final String CMD_MODULATE_LABEL_COLOR = "modulate-label-color";
    private static final String CMD_MODULATE_LABEL_SIZE = "modulate-label-size";
    private static final String CMD_TEXT_ALIGNMENT = "text-alignment";


    /**
     * Default constructor
     */
    public LabelOptions(Renderer renderer, AgentStyle style) {
        super(renderer, style);
    }


    /**
     * Initializes the panel
     */
    protected void init() {
//		setLayout(new SpringLayout());
//		setLayout(new GridLayout(4, 2));

        JPanel panel = new JPanel(new GridLayout(11, 2));

        // Font
        fontButton = new JButton();
        Font font = style.getFont();
        fontButton.setFont(font);
        fontButton.setText(font.getFamily() + ": " + font.getSize());

        fontButton.setActionCommand(CMD_FONT);
        fontButton.addActionListener(this);

        // Bitmap font
        FontManager fontManager = Coordinator.getInstance().getFontManager();
        bitmapFonts = new JComboBox<>(fontManager.getFontNames());
        String bitmapFontName = style.getBitmapFontName();
        if (style == null)
            bitmapFontName = fontManager.getDefaultFontName();

        bitmapFonts.setSelectedItem(bitmapFontName);
        bitmapFonts.setActionCommand(CMD_BITMAP_FONT);
        bitmapFonts.addActionListener(this);

        // Alignment
        BitmapFont.Align[] alignments = BitmapFont.Align.values();
        textAlignments = new JComboBox<>(alignments);
        textAlignments.setSelectedItem(style.getTextAlignment());
        textAlignments.setActionCommand(CMD_TEXT_ALIGNMENT);
        textAlignments.addActionListener(this);

        // Label offsets
        spinnerDx = new JSpinner(
                new SpinnerNumberModel(style.getLabelDx(), -1000, 1000, 0.1));
        spinnerDy = new JSpinner(
                new SpinnerNumberModel(style.getLabelDy(), -1000, 1000, 0.1));

        spinnerDx.addChangeListener(this);
        spinnerDy.addChangeListener(this);

        // Label dimensions
        spinnerWidth = new JSpinner(
                new SpinnerNumberModel(style.getLabelWidth(), -1000, 1000, 0.1));
        spinnerHeight = new JSpinner(
                new SpinnerNumberModel(style.getLabelHeight(), -1000, 1000, 0.1));

        spinnerWidth.addChangeListener(this);
        spinnerHeight.addChangeListener(this);

        // Relative size
        spinnerSize = new JSpinner(
                new SpinnerNumberModel(style.getBitmapFontSize(), 0, 100, 0.1));

        spinnerSize.addChangeListener(this);

        modulateLabelSize = new JCheckBox();
        modulateLabelSize.setSelected(style.getModulateLabelSize());
        modulateLabelSize.setActionCommand(CMD_MODULATE_LABEL_SIZE);
        modulateLabelSize.addActionListener(this);


        // Color
        colorButton = new JButton();
        colorButton.setOpaque(true);
        colorButton.setBackground(style.getLabelColor().toAWTColor());
        colorButton.setActionCommand(CMD_COLOR);
        colorButton.addActionListener(this);

        modulateLabelColor = new JCheckBox();
        modulateLabelColor.setSelected(style.getModulateLabelColor());
        modulateLabelColor.setActionCommand(CMD_MODULATE_LABEL_COLOR);
        modulateLabelColor.addActionListener(this);


        // Font general properties
        panel.add(new JLabel("Font"));
        panel.add(fontButton);

        panel.add(new JLabel("Bitmap Font"));
        panel.add(bitmapFonts);

        panel.add(new JLabel("Align"));
        panel.add(textAlignments);

        panel.add(new JLabel("Size"));
        panel.add(spinnerSize);

        // Offsets and dimensions
        panel.add(new JLabel("dx"));
        panel.add(spinnerDx);

        panel.add(new JLabel("dy"));
        panel.add(spinnerDy);

        panel.add(new JLabel("Width"));
        panel.add(spinnerWidth);

        panel.add(new JLabel("Height"));
        panel.add(spinnerHeight);

        // Color
        panel.add(new JLabel("Color"));
        panel.add(colorButton);

        panel.add(new JLabel("Modulate Label Color"));
        panel.add(modulateLabelColor);

        panel.add(new JLabel("Modulate Label Size"));
        panel.add(modulateLabelSize);

//		SpringUtilities.makeCompactGrid(this, 4, 2, 5, 5, 5, 5);
        add(panel);
    }


    @Override
    public void stateChanged(ChangeEvent e) {
        float dx = ((Number) spinnerDx.getValue()).floatValue();
        float dy = ((Number) spinnerDy.getValue()).floatValue();
        float w = ((Number) spinnerWidth.getValue()).floatValue();
        float h = ((Number) spinnerHeight.getValue()).floatValue();
        float size = ((Number) spinnerSize.getValue()).floatValue();

        style.setBitmapFontSize(size);
        style.setLabelOffset(dx, dy);
        style.setLabelDimension(w, h);
        renderer.update();
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            // Color
            case CMD_COLOR: {
                Color c = JColorChooser.showDialog(this, "Choose Color", style.getLabelColor().toAWTColor());
                if (c != null) {
                    style.setLabelColor(Vector4d.fromAWTColor(c));
                    colorButton.setBackground(c);
                    renderer.update();
                }
                break;
            }

            // Alignment
            case CMD_TEXT_ALIGNMENT: {
                if (textAlignments.getSelectedIndex() >= 0) {
                    BitmapFont.Align align = (BitmapFont.Align) textAlignments.getSelectedItem();
                    style.setTextAlignment(align);
                    renderer.update();
                }
                break;
            }

            // Bitmap font
            case CMD_BITMAP_FONT: {
                if (bitmapFonts.getSelectedIndex() >= 0) {
                    String name = (String) bitmapFonts.getSelectedItem();
                    style.setBitmapFontName(name);
                    renderer.update();
                }
                break;
            }

            // Modulate label color
            case CMD_MODULATE_LABEL_COLOR: {
                style.setModulateLabelColor(modulateLabelColor.isSelected());
                renderer.update();
                break;
            }

            // Modulate label size
            case CMD_MODULATE_LABEL_SIZE: {
                style.setModulateLabelSize(modulateLabelSize.isSelected());
                renderer.update();
                break;
            }

            // Font
            case CMD_FONT: {
                FontSelectionDialog dialog = new FontSelectionDialog(null);
                dialog.setVisible(true);

                Font font = dialog.getSelectedFont();
                if (font != null) {
                    style.setFont(font.getFamily(), font.getStyle(), font.getSize());
                } else {
                    style.setFont(null, 0, 10);
                }

                font = style.getFont();
                fontButton.setFont(font);
                fontButton.setText(font.getFamily() + ": " + font.getSize());

                renderer.update();
                break;
            }
        }
    }
}


/**
 * Alpha blending options
 */
class AlphaBlendingOptions extends OptionPanel implements ChangeListener {
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public AlphaBlendingOptions(Renderer renderer, AgentStyle style) {
        super(renderer, style);
    }


    /**
     * Initializes all elements
     */
    protected void init() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JPanel trPanel;
        JPanel blendPanel;
        JPanel alphaPanel;
        JPanel stencilPanel;

        // Transparency
        trPanel = new JPanel(new GridLayout(1, 1));
        trPanel.setMinimumSize(new Dimension(100, 100));
        trPanel.setBorder(BorderFactory.createTitledBorder("Transparency"));
        initTransparencyPanel(trPanel);

        // Blending
        blendPanel = new JPanel(new GridLayout(2, 1));
        blendPanel.setMinimumSize(new Dimension(100, 100));
        blendPanel.setBorder(BorderFactory.createTitledBorder("Blend Function"));
        initBlendPanel(blendPanel);

        // Alpha
        alphaPanel = new JPanel(new GridLayout(2, 1));
        alphaPanel.setMinimumSize(new Dimension(100, 100));
        alphaPanel.setBorder(BorderFactory.createTitledBorder("Alpha Function"));
        initAlphaPanel(alphaPanel);

        // Stencil
        stencilPanel = new JPanel(new GridLayout(6, 1));
        stencilPanel.setMinimumSize(new Dimension(100, 100));
        stencilPanel.setBorder(BorderFactory.createTitledBorder("Stencil Function"));
        initStencilPanel(stencilPanel);

        panel.add(trPanel);
        panel.add(blendPanel);
        panel.add(alphaPanel);
        panel.add(stencilPanel);

        this.add(panel);
    }


    /**
     * Creates the components of transparencyPanel
     */
    private void initTransparencyPanel(JPanel panel) {
        JSpinner value = new JSpinner(new SpinnerNumberModel(style.getTransparencyCoefficient(), 0, 1, 0.1));
        value.setName("transparency");
        value.addChangeListener(this);

        panel.add(value);
    }

    /**
     * Creates the components of blendPanel
     */
    private void initBlendPanel(JPanel panel) {
        JComboBox<AgentStyle.RendererProperty> src = new JComboBox<>(AgentStyle.srcBlends);
        JComboBox<AgentStyle.RendererProperty> dst = new JComboBox<>(AgentStyle.dstBlends);

        src.setSelectedIndex(style.getSrcBlendIndex());
        dst.setSelectedIndex(style.getDstBlendIndex());

        src.addActionListener(this);
        dst.addActionListener(this);

        src.setActionCommand("src-blend");
        dst.setActionCommand("dst-blend");

        panel.add(src);
        panel.add(dst);
    }


    /**
     * Creates components of the alphaPanel
     */
    private void initAlphaPanel(JPanel panel) {
        JComboBox<AgentStyle.RendererProperty> alpha = new JComboBox<>(AgentStyle.alphaFuncs);
        alpha.setSelectedIndex(style.getAlphaFuncIndex());
        alpha.addActionListener(this);
        alpha.setActionCommand("alpha-function");

        JSpinner value = new JSpinner(new SpinnerNumberModel(style.getAlphaFuncValue(), 0, 1, 0.1));
        value.setName("alpha");
        value.addChangeListener(this);

        panel.add(alpha);
        panel.add(value);
    }


    /**
     * Creates components of the stencilPanel
     */
    private void initStencilPanel(JPanel panel) {
        // func
        JComboBox<AgentStyle.RendererProperty> func = new JComboBox<>(AgentStyle.stencilFuncs);
        func.setSelectedIndex(style.getStencilFuncIndex());
        func.addActionListener(this);
        func.setActionCommand("stencil-function");

        // ref
        JSpinner ref = new JSpinner(new SpinnerNumberModel(style.getStencilRef(), 0, 0xFFFF, 1));
        ref.setName("ref");
        ref.addChangeListener(this);

        // mask
        JSpinner mask = new JSpinner(new SpinnerNumberModel(style.getStencilMask(), 0, 0xFFFF, 1));
        mask.setName("mask");
        mask.addChangeListener(this);

        // fail
        JComboBox<AgentStyle.RendererProperty> fail = new JComboBox<>(AgentStyle.stencilOps);
        fail.setSelectedIndex(style.getStencilFailIndex());
        fail.addActionListener(this);
        fail.setActionCommand("stencil-fail");

        // zfail
        JComboBox<AgentStyle.RendererProperty> zfail = new JComboBox<>(AgentStyle.stencilOps);
        zfail.setSelectedIndex(style.getStencilZFailIndex());
        zfail.addActionListener(this);
        zfail.setActionCommand("stencil-zfail");

        // zpass
        JComboBox<AgentStyle.RendererProperty> zpass = new JComboBox<>(AgentStyle.stencilOps);
        zpass.setSelectedIndex(style.getStencilZPassIndex());
        zpass.addActionListener(this);
        zpass.setActionCommand("stencil-zpass");

        // Add all components to the panel
        panel.add(func);
        panel.add(ref);
        panel.add(mask);
        panel.add(fail);
        panel.add(zfail);
        panel.add(zpass);
    }


    /**
     * Action listener
     */
    public void actionPerformed(ActionEvent e) {
        int index = 0;

        // Get selected index
        if (e.getSource() instanceof JComboBox) {
            index = ((JComboBox) e.getSource()).getSelectedIndex();
        }

        switch (e.getActionCommand()) {
            case "src-blend": {
                style.setSrcBlend(index);
                renderer.update();
                break;
            }

            case "dst-blend": {
                style.setDstBlend(index);
                renderer.update();
                break;
            }

            case "alpha-function": {
                style.setAlphaFunc(index);
                renderer.update();
                break;
            }

            case "texture-env": {
                style.setTextureEnv(index);
                renderer.update();
                break;
            }

            case "stencil-function": {
                style.setStencilFunc(index);
                renderer.update();
                break;
            }

            case "stencil-fail": {
                style.setStencilFail(index);
                renderer.update();
                break;
            }

            case "stencil-zfail": {
                style.setStencilZFail(index);
                renderer.update();
                break;
            }

            case "stencil-zpass": {
                style.setStencilZPass(index);
                renderer.update();
                break;
            }
        }
    }


    /**
     * Alpha/stencil values listener
     */
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() instanceof JSpinner) {
            JSpinner spinner = (JSpinner) e.getSource();

            switch (spinner.getName()) {
                case "transparency": {
                    float val = ((Number) spinner.getValue()).floatValue();
                    style.setTransparencyCoefficient(val);
                    renderer.update();
                    break;
                }

                case "alpha": {
                    float val = ((Number) spinner.getValue()).floatValue();
                    style.setAlphaFuncValue(val);
                    renderer.update();
                    break;
                }

                // Stencil ref
                case "ref": {
                    int val = ((Number) spinner.getValue()).intValue();
                    style.setStencilRef(val);
                    renderer.update();
                    break;
                }

                // Stencil mask
                case "mask": {
                    int val = ((Number) spinner.getValue()).intValue();
                    style.setStencilMask(val);
                    renderer.update();
                    break;
                }
            }
        }
    }
}