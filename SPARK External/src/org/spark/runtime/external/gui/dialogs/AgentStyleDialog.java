package org.spark.runtime.external.gui.dialogs;

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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.spark.math.Vector;
import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.render.AgentStyle;
import org.spark.runtime.external.render.Render;
import org.spark.utils.FileUtils;
import org.spark.utils.SpringUtilities;

/**
 * The dialog for configuring advanced properties
 * of an agent's style
 * @author Monad
 */
@SuppressWarnings("serial")
public class AgentStyleDialog extends JDialog {
	// Main pane
	private JTabbedPane tabs;
	
	/**
	 * Default constructor
	 * @param owner
	 * @param style
	 */
	public AgentStyleDialog(Window owner, Render render, AgentStyle style) {
		super(owner, "Advanced properties");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		init(render, style);
	}
	
	
	/**
	 * Initializes the dialog elements
	 */
	private void init(Render render, AgentStyle style) {
		tabs = new JTabbedPane();
		
		tabs.addTab("Label", new LabelOptions(render, style));
		tabs.addTab("Image", new ImageOptions(render, style));
		tabs.addTab("Blending", new AlphaBlendingOptions(render, style));
		
		this.add(tabs);
		this.pack();
	}
}


/**
 * Abstract base for option panels
 * @author Alexey
 *
 */
@SuppressWarnings("serial")
abstract class OptionPanel extends JPanel implements ActionListener {
	protected AgentStyle style;
	protected Render render;
	
	
	/**
	 * Default constructor
	 * @param render
	 * @param style
	 */
	public OptionPanel(Render render, AgentStyle style) {
		this.render = render;
		this.style = style;
		init();
	}
	
	protected abstract void init();
}



/**
 * Image options
 */
@SuppressWarnings("serial")
class ImageOptions extends OptionPanel {
	private JCheckBox drawShape;
	// Image manager selection button
	private JButton managerButton;
	
	
	public ImageOptions(Render render, AgentStyle style) {
		super(render, style);
	}

	@Override
	protected void init() {
		setLayout(new SpringLayout());
	
		// Image manager selection button
		managerButton = new JButton();
		if (style.getTileManager() == null)
			managerButton.setText("null");
		else
			managerButton.setText(style.getTileManager().getName());
		
		managerButton.setActionCommand("manager");
		managerButton.addActionListener(this);
		
		
		// Draw shape check box
		drawShape = new JCheckBox();
		drawShape.setSelected(style.getDrawShapeWithImageFlag());
		drawShape.setActionCommand("draw-shape");
		drawShape.addActionListener(this);
		
		// Add all components to the main pane
		add(new JLabel("Image Manager"));
		add(managerButton);
		
		add(new JLabel("Draw shape"));
		add(drawShape);
		
		SpringUtilities.makeCompactGrid(this, 2, 2, 5, 5, 5, 5);
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand().intern();
		
		// Draw shape
		if (cmd == "draw-shape") {
			boolean flag = drawShape.isSelected();
			style.setDrawShapeWithImageFlag(flag);
			
			render.update();
		}
		
		// Select manager
		if (cmd == "manager") {
			File file = FileUtils.openFileDialog(Coordinator.getInstance().getCurrentDir(), "xml", null);
			if (file != null) {
				style.setTileManager(file);
				if (style.getTileManager() == null)
					managerButton.setText("null");
				else
					managerButton.setText(style.getTileManager().getName());
				
				render.update();
			}
		}
	}
}


/**
 * Label (font and position) options
 */
@SuppressWarnings("serial")
class LabelOptions extends OptionPanel implements ChangeListener {
	private JSpinner spinnerDx;
	private JSpinner spinnerDy;
	
	private JButton fontButton;
	private JButton colorButton;
	
	/**
	 * Default constructor
	 */
	public LabelOptions(Render render, AgentStyle style) {
		super(render, style);
	}
	
	
	/**
	 * Initializes the panel
	 */
	protected void init() {
		setLayout(new SpringLayout());
		
		// Font
		fontButton = new JButton();
		Font font = style.getFont(); 
		fontButton.setFont(font);
		fontButton.setText(font.getFamily() + ": " + font.getSize());
		
		fontButton.setActionCommand("font");
		fontButton.addActionListener(this);
		
		// Label offsets
		spinnerDx = new JSpinner(
						new SpinnerNumberModel(style.getLabelDx(), -1000, 1000, 1));
		spinnerDy = new JSpinner(
						new SpinnerNumberModel(style.getLabelDy(), -1000, 1000, 1));
		
		spinnerDx.addChangeListener(this);
		spinnerDy.addChangeListener(this);
		
		// Color
		colorButton = new JButton();
		colorButton.setBackground(style.getLabelColor().toAWTColor());
		colorButton.setActionCommand("color");
		colorButton.addActionListener(this);
		
		// Font selection
		add(new JLabel("Font"));
		add(fontButton);
		
		// Offsets
		add(new JLabel("dx"));
		add(spinnerDx);
		
		add(new JLabel("dy"));
		add(spinnerDy);
		
		add(new JLabel("Color"));
		add(colorButton);
		
		SpringUtilities.makeCompactGrid(this, 4, 2, 5, 5, 5, 5);
	}


	@Override
	public void stateChanged(ChangeEvent e) {
		float dx = ((Number) spinnerDx.getValue()).floatValue();
		float dy = ((Number) spinnerDy.getValue()).floatValue();
		
		style.setLabelOffset(dx, dy);
		render.update();
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand().intern();
		
		// Color
		if (cmd == "color") {
			Color c = JColorChooser.showDialog(this, "Choose Color", style.getLabelColor().toAWTColor());
			style.setLabelColor(Vector.fromAWTColor(c));
			colorButton.setBackground(c);
			
			render.update();
		}
		
		// Font
		if (cmd == "font") {
			FontSelectionDialog dialog = new FontSelectionDialog(null);
			dialog.setVisible(true);
			
			Font font = dialog.getSelectedFont();
			if (font != null) {
				style.setFont(font.getFamily(), font.getStyle(), font.getSize());
			}
			else {
				style.setFont(null, 0, 10);
			}
			
			font = style.getFont();
			fontButton.setFont(font);
			fontButton.setText(font.getFamily() + ": " + font.getSize());
			
			render.update();
		}
	}
}



/**
 * Alpha blending options
 */
class AlphaBlendingOptions extends OptionPanel implements ChangeListener {
	private static final long serialVersionUID = 1L;

	/* Button with selected texture name on it */
	private JButton textureButton;


	
	/**
	 * Default constructor
	 */
	public AlphaBlendingOptions(Render render, AgentStyle style) {
		super(render, style);
	}
	

	/**
	 * Initializes all elements
	 */
	protected void init() {
		JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		JPanel texturePanel;
		JPanel blendPanel;
		JPanel alphaPanel;
		
		texturePanel = new JPanel(new GridLayout(2, 2));
		texturePanel.setMinimumSize(new Dimension(100, 100));
		texturePanel.setBorder(BorderFactory.createTitledBorder("Texture"));
		initTexturePanel(texturePanel);

		blendPanel = new JPanel(new GridLayout(2, 1));
		blendPanel.setMinimumSize(new Dimension(100, 100));
		blendPanel.setBorder(BorderFactory.createTitledBorder("Blend Function"));
		initBlendPanel(blendPanel);

		alphaPanel = new JPanel(new GridLayout(2, 1));
		alphaPanel.setMinimumSize(new Dimension(100, 100));
		alphaPanel.setBorder(BorderFactory.createTitledBorder("Alpha Function"));
		initAlphaPanel(alphaPanel);

        panel.add(texturePanel);
        panel.add(blendPanel);
        panel.add(alphaPanel);
        
		this.add(panel);
	}
	
	
	/**
	 * Creates the components of texturePanel
	 * @param panel
	 */
	private void initTexturePanel(JPanel panel) {
		JComboBox env = new JComboBox(AgentStyle.textureEnvs);
		env.setSelectedIndex(style.getTextureEnvIndex());
		env.addActionListener(this);
		env.setActionCommand("texture-env");
		
		File textureFile = style.getTextureFile();
		String textureName = (textureFile != null) ? textureFile.getName() : "No Texture";

		JButton texture = new JButton(textureName);
		texture.addActionListener(this);
		texture.setActionCommand("texture");
		
		textureButton = texture;
		
		JButton reset = new JButton("Reset");
		reset.addActionListener(this);
		reset.setActionCommand("reset-texture");
		
		panel.add(texture);
		panel.add(reset);
		panel.add(env);

	}
	
	
	/**
	 * Creates the components of blendPanel
	 * @param panel
	 */
	private void initBlendPanel(JPanel panel) {
		JComboBox src = new JComboBox(AgentStyle.srcBlends);
		JComboBox dst = new JComboBox(AgentStyle.dstBlends);
		
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
	 * @param panel
	 */
	private void initAlphaPanel(JPanel panel) {
		JComboBox alpha = new JComboBox(AgentStyle.alphaFuncs);
		alpha.setSelectedIndex(style.getAlphaFuncIndex());
		alpha.addActionListener(this);
		alpha.setActionCommand("alpha-function");
		
		JSpinner value = new JSpinner(new SpinnerNumberModel(style.alphaFuncValue, 0, 1, 0.1));
		value.addChangeListener(this);
		
		panel.add(alpha);
		panel.add(value);
	}
	
	
	/**
	 * Shows the open file dialog and sets a new texture for a class of agents
	 * @param style
	 * @return name of a new texture
	 */
	private String setTextureFile(AgentStyle style) {
		if (style == null)
			return null;
		
		File f = style.getTextureFile();
		if (f == null) {
//			f = GUIModelManager.getInstance().getXmlDocumentFile();
//			if (f == null)
//				f = GUIModelManager.getInstance().getCurrentDirectory();
			f = Coordinator.getInstance().getCurrentDir();
		}
		
		final JFileChooser fc = new JFileChooser(f);
		fc.setFileFilter(new FileFilter() {

			// Accept all directories and all supported images
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}

				String extension = FileUtils.getExtension(f);
				if (extension != null) {
					String str = extension.intern();
					if (str == "jpg" ||
						str == "png" ||
						str == "bmp") {
						return true;
					}
					else {
						return false;
					}
				}

				return false;
			}

			// The description of this filter
			public String getDescription() {
				return "*.jpg;*.png;*.bmp";
			}
		});

		int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile().getAbsoluteFile();
			style.setTexture(file.getAbsolutePath());
			return file.getName();
		}
		
//		style.setTexture(null);
		return null;
	}



	/**
	 * Action listener
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand().intern();
		int index = 0;
		
		// Get selected index
		if (e.getSource() instanceof JComboBox) {
			index = ((JComboBox) e.getSource()).getSelectedIndex();
		}
		
		// src-blend command
		if (cmd == "src-blend") {
			style.setSrcBlend(index);
			render.update();
//			GUIModelManager.getInstance().requestUpdate();
			return;
		}
		
		// dst-blend command
		if (cmd == "dst-blend") {
			style.setDstBlend(index);
//			GUIModelManager.getInstance().requestUpdate();
			return;
		}
		
		// alpha-function command
		if (cmd == "alpha-function") {
			style.setAlphaFunc(index);
//			GUIModelManager.getInstance().requestUpdate();
			return;
		}
		
		// texture-env command
		if (cmd == "texture-env") {
			style.setTextureEnv(index);
//			GUIModelManager.getInstance().requestUpdate();
			return;
		}

		// reset-texture command
		if (cmd == "reset-texture") {
			style.setTexture(null);
			textureButton.setText("No Texture");
			
//			GUIModelManager.getInstance().requestUpdate();
			return;
		}
		
		// texture command
		if (cmd == "texture") {
			String textureName = setTextureFile(style);
			if (textureName != null) {
				textureButton.setText(textureName);
			}
			
//			GUIModelManager.getInstance().requestUpdate();
			return;
		}


	}


	/**
	 * Alpha value listener
	 */
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JSpinner) {
			JSpinner value = (JSpinner) e.getSource();
			
			float val = ((Number) value.getValue()).floatValue();
			style.alphaFuncValue = val;
			
//			GUIModelManager.getInstance().requestUpdate();
		}
	}
}