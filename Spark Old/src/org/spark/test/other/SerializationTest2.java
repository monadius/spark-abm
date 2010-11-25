package org.spark.test.other;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.spark.core.Observer;
import org.spark.data.DataLayer;
import org.spark.example.ModelSetup;
import org.spark.startup.ABMModel;

public class SerializationTest2 {
	
	public static void replicateModel(ABMModel model) throws Exception {
		// Create new model instance
		ABMModel newModel = model.getClass().newInstance();
		// Get fields of the original model
		Field[] fields = model.getClass().getFields();
		
		// Iterate through all fields and select only static ones
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers())) {
				System.out.println(field.toGenericString());
				
				// If the field has a primitive type then replicate it straightforwardly
				// Actually it is useless here since a static field is the same for
				// all instances inside the same process
				if (field.getType().isPrimitive()) {
					field.set(newModel, field.get(model));
				}
				
				// Replicate data layers
				Object val = field.get(model);
				if (val != null && val instanceof DataLayer) {
					System.out.println(getDataLayerName((DataLayer) val));
				}
			}
		}
	}
	
	
	private static String[] names = null;
	
	public static String getDataLayerName(DataLayer dataLayer) {
		if (names == null) {
			names = Observer.getDefaultSpace().getDataLayerNames();
			if (names == null)
				return null;
		}
		
		for (String name : names) {
			if (dataLayer == Observer.getDefaultSpace().getDataLayer(name))
				return name;
		}
		
		return null;
	}
	
	
	public static void main(String[] args) throws Exception {
		Observer.init("org.spark.core.Observer1");
		
		ModelSetup model = new ModelSetup();
		model.setup();
		
		replicateModel(model);
	}
}
