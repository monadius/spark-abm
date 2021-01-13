package org.sparkabm.gui.render;

import static org.sparkabm.utils.XmlDocUtils.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.sparkabm.gui.data.DataFilter;
import org.sparkabm.math.Vector;
import org.sparkabm.runtime.data.DataCollectorDescription;
import org.sparkabm.runtime.data.DataObject_Grid;
import org.sparkabm.runtime.data.DataRow;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Contains visualization properties for data layers 
 */
public class DataLayerGraphics {
	// Information about a data layer for visualization
	static public class DataLayerInfo {
		// Information about a data layer itself
		public final DataLayerStyle dataLayerStyle;
		
		// The weight coefficient for the color information
		public final double colorWeight; 
		
		// The weight coefficient for the height information
		public final double heightWeight;

		// Default constructor
		public DataLayerInfo(DataLayerStyle style, double colorWeight, double heightWeight) {
			if (colorWeight < 0)
				colorWeight = 0;
			if (heightWeight < 0)
				heightWeight = 0;
			
			this.dataLayerStyle = style;
			this.colorWeight = colorWeight;
			this.heightWeight = heightWeight;
		}
	}
	

	// General grid information
	static class GridInfo {
		private boolean initialized = false;
		
		public int xSize;
		public int ySize;
		public double xStep;
		public double yStep;
		public int spaceIndex;
		
		public GridInfo() {
			xSize = -1;
		}
		
		public boolean Compare(DataObject_Grid grid) {
			int xSize = grid.getXSize();
			int ySize = grid.getYSize();
			double xStep = grid.getXStep();
			double yStep = grid.getYStep();
			int spaceIndex = grid.getSpaceIndex();
			
			if (!initialized) {
				this.xSize = xSize;
				this.ySize = ySize;
				this.xStep = xStep;
				this.yStep = yStep;
				this.spaceIndex = spaceIndex;
				initialized = true;
				
				return true;
			}
			
			if (this.xSize != xSize || this.ySize != ySize ||
					this.xStep != xStep || this.yStep != yStep ||
					this.spaceIndex != spaceIndex)
				return false;
			
			return true;
		}
	}
	
	// All layers for visualization
	private final ArrayList<DataLayerInfo> layers;

	// Geometry of data layers
	private Vector[][] gridGeometry;
	
	/**
	 * Default constructor
	 */
	public DataLayerGraphics() {
		layers = new ArrayList<DataLayerInfo>();
	}
	
	
	/**
	 * Creates description for one data layer
	 * @param style
	 */
	public DataLayerGraphics(DataLayerStyle style) {
		this();
		addDataLayer(style, 1, 0);
	}
	
	
	/**
	 * Adds (or changes) a data layer
	 * @param style
	 */
	public void addDataLayer(DataLayerStyle style, double colorWeight, double heightWeight) {
		if (style == null)
			return;
		
		// Find if we already have the given data layer
		for (int i = 0; i < layers.size(); i++) {
			if (layers.get(i).dataLayerStyle == style) {
				// Remove the existing descriptor
				layers.remove(i);
				break;
			}
		}
		
		layers.add(new DataLayerInfo(style, colorWeight, heightWeight));

		// Invalidate the geometry
		gridGeometry = null;
	}
	

	/**
	 * Updates the given data filter
	 * @param filter
	 */
	public void updateDataFilter(DataFilter filter) {
		for (DataLayerInfo info : layers) {
			filter.addData(DataCollectorDescription.DATA_LAYER, info.dataLayerStyle.getName());
		}
	}
	
	
	/**
	 * Creates an xml description of the class
	 */
	public Node createNode(Document doc, File modelPath) {
		Node root = doc.createElement("datalayerstyle");

		for (DataLayerInfo info : layers) {
			Node node = doc.createElement("info");
			
			addAttr(doc, node, "style-name", info.dataLayerStyle.getName());
			addAttr(doc, node, "color-weight", info.colorWeight);
			addAttr(doc, node, "height-weight", info.heightWeight);
			
			root.appendChild(node);
		}
		
		return root;
	}
	
	
	/**
	 * Loads the class from the given xml-node
	 */
	public static DataLayerGraphics loadXML(Node dls, HashMap<String, DataLayerStyle> styles) {
		DataLayerGraphics info = new DataLayerGraphics();
		
		// Preserve the compatibility with the old format
		String name = getValue(dls, "name", null);
		DataLayerStyle style;
		
		if (name != null) {
			style = styles.get(name);
			if (style != null)
				info.addDataLayer(style, 1, 0);
		}
		
		// Load all descriptors
		ArrayList<Node> nodes = getChildrenByTagName(dls, "info");
		for (Node node : nodes) {
			name = getValue(node, "style-name", null);
			double colorWeight = getDoubleValue(node, "color-weight", 0);
			double heightWeight = getDoubleValue(node, "height-weight", 0);
			
			if (name == null)
				continue;
			
			style = styles.get(name);
			if (style == null)
				continue;
			
			info.addDataLayer(style, colorWeight, heightWeight);
		}
		
		return info;
	}
	
	
	/**
	 * Returns the geometry of data layers
	 * @return
	 */
	public Vector[][] getGeometry(DataRow dataRow, double xMin, double yMin) {
		if (layers.size() == 0)
			return null;

		if (gridGeometry != null)
			return gridGeometry;

		// Get the geometry for the first data layer (if the data is available)
		DataLayerStyle style = layers.get(0).dataLayerStyle;
		DataObject_Grid data = dataRow.getGrid(style.getName());
		
		if (data == null)
			return null;
		
		gridGeometry = GridGraphics.getGeometry(data, xMin, yMin);
		return gridGeometry;
	}
	
	
	/**
	 * Returns true if a 3d rendering is required
	 * @return
	 */
	public boolean is3d() {
		double total = 0;
		
		for (DataLayerInfo info : layers) {
			total += info.heightWeight;
		}
		
		if (total <= 1e-10)
			return false;
		
		return true;
	}
	
	
	/**
	 * Returns true if several data layers are used or
	 * if the height map is defined
	 * @return
	 */
	public boolean isSpecial() {
		if (layers.size() > 1)
			return true;

		return is3d();
	}
	
	
	/**
	 * Returns all styles of contained data layers
	 * @return
	 */
	public ArrayList<DataLayerInfo> getDescriptors() {
		ArrayList<DataLayerInfo> result = new ArrayList<DataLayerInfo>(layers);
		return result;
	}
	
	
	/**
	 * Returns the general grid information.
	 * This information should be the same for all data layers
	 * in the collection, otherwise null is returned
	 */
	public GridInfo getGridInfo(DataRow dataRow) {
		GridInfo gridInfo = new GridInfo();
		
		for (DataLayerInfo info : layers) {
			DataObject_Grid grid = dataRow.getGrid(info.dataLayerStyle.getName());
			if (grid == null)
				return null;
			
			if (!gridInfo.Compare(grid))
				return null;
		}
		
		return gridInfo;
	}

	
	/**
	 * Returns the color information
	 * @param dataRow
	 * @return
	 */
	public Vector[][] getColors(DataRow dataRow) {
		int n = layers.size();
		
		if (n == 0)
			return null;
		
		
		// Compute the total weight
		double total = 0;
		for (DataLayerInfo info : layers) {
			total += info.colorWeight;
		}

		if (total <= 1e-10)
			return null;
		
		// Get the data and compute weights
		DataObject_Grid[] data = new DataObject_Grid[n];
		double[] weights = new double[n];
		
		int xSize = -1, ySize = -1;
		
		for (int i = 0; i < n; i++) {
			DataLayerInfo info = layers.get(i);
			
			data[i] = dataRow.getGrid(info.dataLayerStyle.getName());
			if (data[i] == null)
				return null;
		
			if (xSize < 0)
				xSize = data[i].getXSize();
			if (ySize < 0)
				ySize = data[i].getYSize();

			if (data[i].getXSize() != xSize || data[i].getYSize() != ySize)
				return null;
		}

		// Compute weights
		for (int i = 0; i < n; i++) {
			weights[i] = layers.get(i).colorWeight / total;
		}

		// Compute the color map
		Vector[][] colorMap = null;
		
		for (int i = 0; i < n; i++) {
			double weight = weights[i];
			if (weight <= 0.0)
				continue;
			
			Vector[][] colors = GridGraphics.getColors(data[i], layers.get(i).dataLayerStyle);
			
			if (colorMap == null) {
				colorMap = colors;
				if (weight == 1.0)
					continue;
				
				for (int x = 0; x < xSize; x++) {
					for (int y = 0; y < ySize; y++) {
						colors[x][y].mul(weight);
					}
				}
				
				continue;
			}
			
			for (int x = 0; x < xSize; x++) {
				Vector[] colorMapColumn = colorMap[x];
				Vector[] colorsColumn = colors[x];
				
				for (int y = 0; y < ySize; y++) {
					colorMapColumn[y].add(colorsColumn[y].mul(weight));
				}
			}
		}
		
		return colorMap;
	}
	
	
	/**
	 * Returns the height information
	 * @param dataRow
	 * @return null if data for any data layer is not available, 
	 * or if data layer sizes are not compatible
	 */
	public double[][] getHeightMap(DataRow dataRow) {
		int n = layers.size();
		
		if (n == 0)
			return null;
		
		
		// Compute the total weight
		double total = 0;
		for (DataLayerInfo info : layers) {
			total += info.heightWeight;
		}

		if (total <= 1e-10)
			return null;
		
		// Get the data and compute weights
		DataObject_Grid[] data = new DataObject_Grid[n];
		double[] minValue = new double[n];
		double[] maxValue = new double[n];
		double[] weights = new double[n];
		
		int xSize = -1, ySize = -1;
		
		for (int i = 0; i < n; i++) {
			DataLayerInfo info = layers.get(i);
			data[i] = dataRow.getGrid(info.dataLayerStyle.getName());
			if (data[i] == null)
				return null;
		
			if (xSize < 0)
				xSize = data[i].getXSize();
			if (ySize < 0)
				ySize = data[i].getYSize();

			if (data[i].getXSize() != xSize || data[i].getYSize() != ySize)
				return null;
			
			minValue[i] = info.dataLayerStyle.getVal1();
			maxValue[i] = info.dataLayerStyle.getVal2();
		}

		// Compute weights
		for (int i = 0; i < n; i++) {
			weights[i] = layers.get(i).heightWeight;
		}

		// Compute the height map
		double[][] heightMap = new double[xSize][];
		for (int i = 0; i < xSize; i++)
			heightMap[i] = new double[ySize];
		
		for (int i = 0; i < n; i++) {
			DataObject_Grid grid = data[i];
			double min = minValue[i];
			double max = maxValue[i];
			double weight = weights[i];
			
			if (weight <= 0.0)
				continue;
			
			for (int x = 0; x < xSize; x++) {
				double[] column = heightMap[x];
				for (int y = 0; y < ySize; y++) {
					double val = grid.getValue(x, y);
					if (val < min) val = min; else if (val > max) val = max;
					
					val *= weight;
					column[y] += val;
				}
			}
		}
		
		return heightMap;
	}
}
