package org.spark.gui.applet;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.spark.gui.render.Render;
import org.w3c.dom.Node;



public class MainPanel implements IUpdatablePanel, ActionListener, ChangeListener {

	private static final long serialVersionUID = 2800524835363109821L;

	private JPanel mainPanel;

	private JButton startButton, setupButton;
	private JCheckBox synchButton;
	private JLabel	tickLabel;
	private JPopupMenu popup;

	private Canvas canvas;
	private Render render;
	
	private RenderProperties renderDialog;
	
	
	public MainPanel(JPanel mainPanel) {
		render = null;
		this.mainPanel = mainPanel;
		
		JPanel panel = new JPanel();
		panel.setMinimumSize(new Dimension(100, 100));
		
		synchButton = new JCheckBox("Sychronized", null, true);
		startButton = new JButton("Start");
		setupButton = new JButton("Setup");
		tickLabel = new JLabel("");
		tickLabel.setMinimumSize(new Dimension(100, 80));
		
		synchButton.addActionListener(this);
		startButton.addActionListener(this);
		setupButton.addActionListener(this);
		
		JSlider framesPerSecond = new JSlider(JSlider.HORIZONTAL,
                0, 300, 0);
		framesPerSecond.addChangeListener(this);

		framesPerSecond.setMajorTickSpacing(100);
		framesPerSecond.setMinorTickSpacing(50);
		framesPerSecond.setPaintTicks(true);
		framesPerSecond.setPaintLabels(true);
		
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put( new Integer( 0 ), new JLabel("Fast") );
		labelTable.put( new Integer( 300 ), new JLabel("Slow") );
		framesPerSecond.setLabelTable( labelTable );
		
		panel.add(setupButton);
		panel.add(startButton);
//		panel.add(saveButton);
		panel.add(tickLabel);
		panel.add(synchButton);
		panel.add(framesPerSecond);

	
	    popup = new JPopupMenu();
	    JMenuItem menuItem = new JMenuItem("Properties");
	    menuItem.addActionListener(this);
	    popup.add(menuItem);
	    
		
		mainPanel.add(panel, BorderLayout.NORTH);
		mainPanel.validate();
		
		renderDialog = null;
	}
	
	
	void setupRender(Node node) {
		if (canvas != null) {
			mainPanel.remove(canvas);
		}
		
		render = AppletModelManager.getInstance().createRender(node, Render.JOGL_RENDER);
		canvas = render.getCanvas();
//		canvas.setPreferredSize(new Dimension(mainPanel.getWidth(), mainPanel.getWidth()));
//		canvas.setMinimumSize(new Dimension(mainPanel.getWidth(), mainPanel.getWidth()));

	    MouseListener popupListener = new PopupListener();
	    canvas.addMouseListener(popupListener);
	    mainPanel.add(canvas, BorderLayout.CENTER);
	    
//	    JPanel empty = new JPanel();
//	    empty.setMinimumSize(new Dimension(100, 100));
//	    empty.setPreferredSize(new Dimension(100, 100));
//	    mainPanel.add(empty, BorderLayout.SOUTH);
	    
		renderDialog = new RenderProperties(render);
		renderDialog.setVisible(false);
	}
	
	
		

	/* Actions handler */
	class PopupListener extends MouseAdapter {
	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	            popup.show(e.getComponent(),
	                       e.getX(), e.getY());
	        }
	    }
	}
	
	
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		
		try {
			if (src instanceof JMenuItem) {
				JMenuItem item = (JMenuItem) src;
				String name = item.getText();

				if (name.equals("Properties")) {
					if (renderDialog != null) {
						renderDialog.init();
						renderDialog.setVisible(true);
					}
					
					return;
				}
			}
			
			if (src == setupButton) {
				AppletModelManager.getInstance().setupModel();
			} else if (src == startButton) {
				boolean result = AppletModelManager.getInstance().pauseResumeModel();
				if (result)
					startButton.setText("Start");
				else
					startButton.setText("Pause");
			} else if (src == synchButton) {
				synchronized(AppletModelManager.lock) {
					AppletModelManager.getInstance().synchFlag = synchButton.isSelected();
					AppletModelManager.lock.notify();
				}
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex.toString());
		}
	}


//	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
//	    if (!source.getValueIsAdjusting()) {
			AppletModelManager.getInstance()
				.changeSimulationSpeed((int) source.getValue());
//	    }		
	}
	
	
	public void updateTick(long tick) {
		tickLabel.setText(Long.toString(tick));
	}


	public void reset() {
		boolean paused = AppletModelManager.getInstance().isModelPaused();
		if (paused)
			startButton.setText("Start");
		else
			startButton.setText("Pause");
	}


	public void saveData() {
	}

	
	private boolean invoked = false;

	public void updateData(final long tick) {
		if (!invoked && canvas != null) {
			
			invoked = true;
			
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					if (canvas != null)
						render.display();

					tickLabel.setText(String.valueOf(tick));
					
					synchronized(AppletModelManager.lock) {
						invoked = false;
						AppletModelManager.lock.notify();
					}
				}
			});
		}
	}

	
	public void updateData() {
		if (!invoked && canvas != null) {
			
			invoked = true;
			
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					if (canvas != null)
						render.display();

					invoked = false;
				}
			});
		}
	}
	
}
