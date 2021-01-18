package org.sparkabm.runtime.internal.data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sparkabm.core.Observer;
import org.sparkabm.core.SparkModel;
import org.sparkabm.runtime.data.DataCollectorDescription;
import org.sparkabm.runtime.data.DataObject;
import org.sparkabm.runtime.data.DataObject_Inspection;
import org.sparkabm.space.Space;
import org.sparkabm.space.SpaceAgent;

/**
 * Collects data about specific type of space agents
 *
 * @author Monad
 */
public class DCInspectionData extends DataCollector {
    private static final Logger logger = Logger.getLogger(DCInspectionData.class.getName());

    // The name of this inspector
    protected String name;
    // The initial inspection position
    protected DataObject_Inspection.Parameters parameters;

    // The list of inspected agents
    protected ArrayList<SpaceAgent> agents;

    /**
     * Creates the data collector for the given type of space agents
     */
    DCInspectionData(String name, DataObject_Inspection.Parameters pars) {
        this.name = name;
        this.parameters = pars;

        this.dataName = DataCollectorDescription
                .typeToString(DataCollectorDescription.INSPECTION_DATA)
                + name;
    }

    /**
     * Returns all agents at the position specified by the parameters
     *
     * @return
     */
    private ArrayList<SpaceAgent> findAgents() {
        if (parameters == null || parameters.position == null)
            return null;

        String spaceName = parameters.spaceName;
        Space space = (spaceName != null) ? Observer.getSpace(parameters.spaceName) :
                Observer.getDefaultSpace();

        if (space == null)
            return null;

        ArrayList<SpaceAgent> agents = space.getAgents(parameters.position, 0);

        // TODO: for grid spaces select agents intersecting with the given point

        return agents;
    }


    /**
     * Returns information about the given object
     *
     * @param obj
     * @return
     */
    private DataObject_Inspection.ObjectInformation inspect(Object obj) {
        DataObject_Inspection.ObjectInformation info;

        if (obj == null)
            return null;

        // Get object's class
        Class<? extends Object> cls = obj.getClass();
        info = new DataObject_Inspection.ObjectInformation(cls.getSimpleName());

        inspect(obj, cls, info);


        return info;
    }


    /**
     * Recursively collects the data
     */
    private void inspect(Object obj, Class<? extends Object> cls, DataObject_Inspection.ObjectInformation info) {
        if (obj == null || cls == null)
            return;

        try {
            // Get all fields
            Field[] fields = cls.getDeclaredFields();
            if (fields != null) {
                for (Field field : fields) {
                    int modifiers = field.getModifiers();
                    // Ignore final static fields
                    if ((modifiers & Modifier.FINAL) != 0 &&
                            (modifiers & Modifier.STATIC) != 0)
                        continue;

                    // We need to be able to access private and protected fields
                    field.setAccessible(true);

                    // Get the field's value
                    Object val = field.get(obj);
                    String sVal = String.valueOf(val);

                    info.addVariable(field.getName(), sVal);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception", e);
        }

        inspect(obj, cls.getSuperclass(), info);
    }


    //	@SuppressWarnings("unchecked")
    @Override
    public DataObject collect0(SparkModel model) throws Exception {
        DataObject_Inspection data = new DataObject_Inspection();

        if (agents == null) {
            // It is the first time collection: find agents at the given position
            agents = findAgents();
            if (agents == null)
                return data;
        }

        for (int i = 0; i < agents.size(); i++) {
            data.addObject(inspect(agents.get(i)));
        }
		
/*		DataObject_Inspection.ObjectInformation info = new DataObject_Inspection.ObjectInformation("Agent1");
		info.addVariable("var1", "1424");
		info.addVariable("x", "name");
		
		data.addObject(info);
		data.addObject(info);
		
		info = new DataObject_Inspection.ObjectInformation("Agent2");
		info.addVariable("a", "b");
		data.addObject(info);*/

        return data;
    }

    @Override
    public void reset() {
        parameters = null;
        agents = null;
    }
}
