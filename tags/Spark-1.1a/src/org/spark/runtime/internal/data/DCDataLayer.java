package org.spark.runtime.internal.data;

import org.spark.core.Observer;
import org.spark.core.SparkModel;
import org.spark.data.DataLayer;
import org.spark.data.Diffuse;
import org.spark.data.Grid;
import org.spark.data.Grid3d;
import org.spark.data.QueueGrid;
import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.data.DataObject;
import org.spark.runtime.data.DataObject_Grid;

/**
 * Collects data of the given data layer
 * 
 * @author Monad
 * 
 */
public class DCDataLayer extends DataCollector {
	// protected String spaceName;
	protected String dataLayerName;

	protected DataLayer data;
	protected int spaceIndex;

	/**
	 * Creates the data collector for collecting data from the given data layer
	 * in the given space
	 * 
	 * @param spaceName
	 *            null means to use the default space
	 * @param dataLayerName
	 */
	DCDataLayer(String dataLayerName) {
		// this.spaceName = spaceName;
		this.dataLayerName = dataLayerName;

		// this.dataName = "$data-layer:" + spaceName + ":" + dataLayerName;
		this.dataName = DataCollectorDescription
				.typeToString(DataCollectorDescription.DATA_LAYER) +
				dataLayerName;
	}

	@Override
	public DataObject collect0(SparkModel model) throws Exception {
		if (data == null) {
			// Space space = null;
			// if (spaceName == null)
			// space = Observer.getDefaultSpace();
			// else
			// space = Observer.getSpace(spaceName);

			data = Observer.getInstance().findDataLayer(dataLayerName);

			// if (space == null)
			// throw new BadDataSourceException("Space " + spaceName +
			// " does not exist");

			// data = space.getDataLayer(dataLayerName);
			if (data == null)
				throw new BadDataSourceException("Data layer " + dataLayerName
						+ " does not exist");

			spaceIndex = data.getSpace().getIndex();
		}

		// TODO: maybe it is better to get data collectors
		// directly from data layers, i.e. define a new interface method
		// getDataCollector()
		if (data instanceof Grid || data instanceof QueueGrid) {
			Grid grid = (Grid) data;
			double[][] vals = grid.getData();
			return new DataObject_Grid(spaceIndex, vals, grid.getXStep(), grid
					.getYStep());
		}
		else if (data instanceof Grid3d) {
			Grid3d grid = (Grid3d) data;
			double[][][] vals = grid.getData();
			return new DataObject_Grid(spaceIndex, vals, 
					grid.getXStep(), grid.getYStep(), grid.getZStep());
		}
		else if (data instanceof Diffuse) {
			Diffuse diffuseGrid = (Diffuse) data;
			double[][] vals = diffuseGrid.getData().getDouble2d();
			return new DataObject_Grid(spaceIndex, vals, diffuseGrid.getXStep(), diffuseGrid.getYStep());
		} else {
			throw new BadDataSourceException("Unknown data layer type "
					+ dataLayerName.getClass());
		}
	}

	@Override
	public void reset() {
		data = null;
	}
}
