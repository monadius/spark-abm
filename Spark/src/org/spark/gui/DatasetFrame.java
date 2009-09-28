package org.spark.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.spark.utils.RandomHelper;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DatasetFrame extends UpdatableFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private long interval;
	private String name;
	
	private JButton saveButton;
	private JPanel panel;
	
	private static class DataItem {
		public ArrayList<Number> data = new ArrayList<Number>(5000);
		public String name;
		public Method getMethod;
		
		public DataItem(String name, Method get) {
			this.name = name;
			this.getMethod = get;
		}
		
		public void clear() {
			data.clear();
		}
		
		public void add(Number number) {
			data.add(number);
		}
		
		public void getAndAdd() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			if (getMethod == null)
				return;
			data.add((Number)getMethod.invoke(null));
		}
	}
	
	private ArrayList<DataItem> dataItems = new ArrayList<DataItem>(10);
	
	public DatasetFrame(Node node, JFrame owner) {
		super(node, owner, "");
	
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		NamedNodeMap attributes = node.getAttributes();
		Node tmp;

		name = (tmp = attributes.getNamedItem("name")) != null ? tmp.getNodeValue() : "Data";
		String sinterval = (tmp = attributes.getNamedItem("interval")) != null ? tmp.getNodeValue() : "1";		
		interval = Long.parseLong(sinterval);

		dataItems.add(new DataItem("tick", null));
		
		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node itemNode = nodes.item(i);
			
			if (itemNode.getNodeName().equals("item")) {
				addItem(itemNode);
			}
		}
		
		
		saveButton = new JButton("Save");
		saveButton.addActionListener(this);

		panel.add(saveButton);
		this.add(panel);
		this.pack();
		
		this.setTitle(name);
		FrameLocationManager.setLocation(this, node);
	}


	private void addItem(Node item) {
		NamedNodeMap attributes = item.getAttributes();
		Node tmp;
		
		String name = (tmp = attributes.getNamedItem("name")) != null ? tmp.getNodeValue() : "???";
		String smethod = (tmp = attributes.getNamedItem("get")) != null ? tmp.getNodeValue() : "";
		
		Method method = null;
		
		try {
			method = GUIModelManager.getModelClass().getMethod(smethod);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		dataItems.add(new DataItem(name, method));
		
		JLabel label = new JLabel(name);
		panel.add(label);
	}
	
	
	@Override
	public synchronized void updateData(long tick) {
		if (tick % interval != 0)
			return;
		
		dataItems.get(0).add((Number)tick);
		
		for (int i = 1; i < dataItems.size(); i++) {
			try {
				dataItems.get(i).getAndAdd();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	@Override
	public synchronized void reset() {
		for (int i = 0; i < dataItems.size(); i++) {
			dataItems.get(i).data.clear();
		}
	}

	
	private void saveDataFile() throws Exception {
		final JFileChooser fc = new JFileChooser(GUIModelManager.getInstance().getCurrentDirectory());
		fc.setFileFilter(new FileFilter() {

			// Accept all directories and all csv files
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}

				String extension = MainFrame.getExtension(f);
				if (extension != null) {
					if (extension.equals("csv"))
						return true;
					else
						return false;
				}

				return false;
			}

			// The description of this filter
			public String getDescription() {
				return "*.csv";
			}
		});
		
		
		int returnVal = fc.showSaveDialog(this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fc.getSelectedFile();
            
            if (MainFrame.getExtension(file) == null) {
            	file = new File(file.getPath() + ".csv");
            }
//            java.io.FileOutputStream fo = new FileOutputStream(file, false);
            
            java.io.PrintStream out = new PrintStream(file);
            saveData(out);
            
		}
	}

	
	public void saveData(String fname) {
		try {
			File file = new File(GUIModelManager.getInstance().
					getXmlDocumentFile().getParentFile(), fname);
			PrintStream out = new PrintStream(file);
			saveData(out);
		} catch (Exception e) {
			// TODO: something
			e.printStackTrace();
		}
	}
	
	
	public synchronized void saveData(PrintStream out) {
		out.println("Random seed");
		out.println(RandomHelper.getSeed());
		
		ParameterPanel parFrame = ModelManager.getInstance().getParameterPanel();
		if (parFrame != null) {
			parFrame.saveParameters(out);
		}
		else {
			ParameterPanel parPanel = ModelManager.getInstance().getParameterPanel();
			if (parPanel != null) {
				parPanel.saveParameters(out);
			}
		}

		out.println("Experiment");
		
        try {
			int n = dataItems.size();

			for (int i = 0; i < n; i++) {
				out.print(dataItems.get(i).name);
				if (i != n - 1)
					out.print(',');
			}

			out.println();

			int l = dataItems.get(0).data.size();
			for (int k = 0; k < l; k++) {
				for (int i = 0; i < n; i++) {
					Object Entry = dataItems.get(i).data.get(k);
					if (Entry == null) out.print("n/a");
					else out.print((Number)Entry);
					
					if (i != n - 1)
						out.print(',');
				}

				out.println();
			}
        } finally {
        	out.flush();
        	out.close();
        }
	}
	

	public void actionPerformed(ActionEvent arg0) {
		Object src = arg0.getSource();
		
		if (src == saveButton) {
			try {
				saveDataFile();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
