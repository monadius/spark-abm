package org.sparkabm.runtime.internal.data;

import org.sparkabm.core.Agent;
import org.sparkabm.core.Observer;
import org.sparkabm.core.SparkModel;
import org.sparkabm.runtime.data.DataCollectorDescription;
import org.sparkabm.runtime.data.DataObject;
import org.sparkabm.runtime.data.DataObject_AgentData;

/**
 * Collects special data of agents of specific type 
 * @author Monad
 * 
 */
public class DCAgentData extends DataCollector {
	protected String typeName;
	protected Class<? extends Agent> type;

	/**
	 * Creates the data collector for the given type of space agents
	 * 
	 * @param typeName
	 */
	DCAgentData(String typeName) {
		this.typeName = typeName;

		this.dataName = DataCollectorDescription
				.typeToString(DataCollectorDescription.AGENT_DATA)
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
		
		Agent[] tmpAgents = Observer.getInstance().getAgents(type);
		if (tmpAgents == null || tmpAgents.length == 0) {
			return new DataObject_AgentData(0);
		}

		int n = tmpAgents.length;
		DataObject_AgentData result = new DataObject_AgentData(tmpAgents.length);

		for (int i = 0; i < n; i++) {
			result.addAgent(tmpAgents[i]);
		}
		
		return result;
	}

	@Override
	public void reset() {
		type = null;
	}
}

