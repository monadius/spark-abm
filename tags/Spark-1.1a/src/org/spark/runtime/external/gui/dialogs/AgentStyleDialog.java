package org.spark.runtime.external.gui.dialogs;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.spark.runtime.external.Coordinator;
import org.spark.runtime.external.render.AgentStyle;
import org.spark.utils.FileUtils;

/**
 * The dialog for configuring advanced properties
 * of an agent's style
 * @author Monad
 */
@SuppressWarnings("serial")
public class AgentStyleDialog extends JDialog implements ActionListener, ChangeListener {
	/* Style with which this dialog is associated */
	private AgentStyle style;
	
	/* Button with selected texture name on it */
	private JButton textureButton;
	
	/**
	 * Default constructor
	 * @param owner
	 * @param style
	 */
	public AgentStyleDialog(Window owner, AgentStyle style) {
		super(owner, "Advanced properties");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		this.style = style;
		
		init();
	}
	
	
	private void init() {
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
		this.pack();
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

