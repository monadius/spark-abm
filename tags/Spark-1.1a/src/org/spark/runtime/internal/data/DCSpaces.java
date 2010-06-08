package org.spark.runtime.internal.data;

import org.spark.core.Observer;
import org.spark.core.SparkModel;
import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.data.DataObject;
import org.spark.runtime.data.DataObject_Spaces;
import org.spark.space.BoundedSpace;
import org.spark.space.BoundedSpace3d;
import org.spark.space.Space;
import org.spark.utils.Vector;

/**
 * Collects basic information about all model spaces
 * 
 * @author Monad
 * 
 */
public class DCSpaces extends DataCollector {

	/**
	 * Default constructor
	 */
	// TODO: only one data collector of this type can be created
	DCSpaces() {
		this.dataName = DataCollectorDescription
				.typeToString(DataCollectorDescription.SPACES);
	}

	@Override
	public DataObject collect0(SparkModel model) throws Exception {
		String[] names = model.getObserver().getSpaceNames();

		DataObject_Spaces result = new DataObject_Spaces(names.length);

		// Iterate over all spaces
		for (String name : names) {
			Space space = Observer.getSpace(name);
			int index = space.getIndex();
			Vector min = new Vector();
			Vector max = new Vector();

			if (space instanceof BoundedSpace) {
				BoundedSpace boundedSpace = (BoundedSpace) space;
				min = new Vector(boundedSpace.getXMin(),
						boundedSpace.getYMin(), 0);
				max = new Vector(boundedSpace.getXMax(),
						boundedSpace.getYMax(), 0);
			} else if (space instanceof BoundedSpace3d) {
				BoundedSpace3d boundedSpace3d = (BoundedSpace3d) space;
				min = new Vector(boundedSpace3d.getXMin(), boundedSpace3d
						.getYMin(), boundedSpace3d.getZMin());
				max = new Vector(boundedSpace3d.getXMax(), boundedSpace3d
						.getYMax(), boundedSpace3d.getZMax());
			}

			result.addSpace(name, index, min, max);
		}

		return result;
	}

	@Override
	public void reset() {
	}

}
