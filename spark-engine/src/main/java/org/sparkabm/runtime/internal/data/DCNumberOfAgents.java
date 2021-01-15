package org.sparkabm.runtime.internal.data;

import org.sparkabm.core.Agent;
import org.sparkabm.core.Observer;
import org.sparkabm.core.SparkModel;
import org.sparkabm.runtime.data.DataCollectorDescription;
import org.sparkabm.runtime.data.DataObject;
import org.sparkabm.runtime.data.DataObject_Integer;

/**
 * Collects data about specific type of space agents
 *
 * @author Monad
 */
public class DCNumberOfAgents extends DataCollector {
    protected String typeName;
    protected Class<? extends Agent> type;

    /**
     * Creates the data collector for the given type of space agents
     *
     * @param typeName
     */
    DCNumberOfAgents(String typeName) {
        this.typeName = typeName;

        this.dataName = DataCollectorDescription
                .typeToString(DataCollectorDescription.NUMBER_OF_AGENTS)
                + typeName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataObject collect0(SparkModel model) throws Exception {
        if (type == null) {
            type = (Class<? extends Agent>) model.getClass().getClassLoader()
                    .loadClass(typeName);

            if (type == null)
                throw new Exception("Type of agents is unresolved: " + typeName);
        }

        int n = Observer.getInstance().getAgentsNumber(type);
        return new DataObject_Integer(n);
    }

    @Override
    public void reset() {
        type = null;
    }
}
