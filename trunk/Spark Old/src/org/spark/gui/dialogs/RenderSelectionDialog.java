package org.spark.gui.dialogs;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.spark.gui.GUIModelManager;
import org.spark.gui.render.Render;


public class RenderSelectionDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;

	private JPanel panel;
	
	public RenderSelectionDialog(JFrame owner, int renderType) {
		super(owner, "", true);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		panel = new JPanel(new GridLayout(0, 1));
		panel.setMinimumSize(new Dimension(300, 100));
		panel.setPreferredSize(new Dimension(400, 100));
		panel.setBorder(BorderFactory.createTitledBorder("Renderers (Reload open model after selecting a new renderer)"));

		init(renderType);
	}

	public RenderSelectionDialog(JDialog owner, int renderType) {
		super(owner, "", true);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		panel = new JPanel(new GridLayout(0, 1));
		panel.setMinimumSize(new Dimension(300, 100));
		panel.setPreferredSize(new Dimension(400, 100));
		panel.setBorder(BorderFactory.createTitledBorder("Renderers (Reload open model after selecting a new renderer)"));

		init(renderType);
	}


	public void init(int renderType) {
		panel.removeAll();
		
		ButtonGroup group = new ButtonGroup();
		
		/* Create Java2D button */
		JRadioButton button = new JRadioButton("Java2D");
		if (renderType == Render.JAVA_2D_RENDER)
			button.setSelected(true);
		button.setActionCommand("Java2D");
		button.addActionListener(this);
		group.add(button);
		panel.add(button);

		
		/* Create JOGL button */
		button = new JRadioButton("JOGL (OpenGL)");
		if (renderType != Render.JAVA_2D_RENDER)
			button.setSelected(true);
		button.setActionCommand("JOGL");
		button.addActionListener(this);
		group.add(button);
		panel.add(button);

		
		this.add(panel);
		this.pack();
	}
	
	

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == null)
			return;
		
		if (cmd.startsWith("Java2D")) {
			GUIModelManager.getInstance().setRenderType(Render.JAVA_2D_RENDER);
			return;
		}
		
		if (cmd.startsWith("JOGL")) {
			GUIModelManager.getInstance().setRenderType(Render.JOGL_RENDER);
			return;
		}
	}
	
}
