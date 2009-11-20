package org.spark.runtime.internal.data;

import org.spark.core.Agent;
import org.spark.core.Observer;
import org.spark.core.SparkModel;
import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.data.DataObject;
import org.spark.runtime.data.DataObject_SpaceAgents;
import org.spark.runtime.data.DataObject_SpaceLinks;
import org.spark.space.SpaceAgent;
import org.spark.space.SpaceLink;
import org.spark.space.SpaceNode;
import org.spark.utils.Vector;
import org.spark.utils.Vector4d;

/**
 * Collects data about specific type of space agents
 * 
 * @author Monad
 * 
 */
public class DCSpaceAgents extends DataCollector {
	protected String typeName;
	protected Class<? extends Agent> type;

	/**
	 * Creates the data collector for the given type of space agents
	 * 
	 * @param typeName
	 */
	DCSpaceAgents(String typeName) {
		this.typeName = typeName;

		this.dataName = DataCollectorDescription
				.typeToString(DataCollectorDescription.SPACE_AGENTS)
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
			return new DataObject_SpaceAgents(0);
		}

		Agent tmp = tmpAgents[0];
		if (tmp instanceof SpaceAgent)
			return collectSpaceAgents(tmpAgents);
		else if (tmp instanceof SpaceLink)
			return collectSpaceLinks(tmpAgents);

		return new DataObject_SpaceAgents(0);
	}
	

	/**
	 * Collects space agents
	 * @param agents
	 * @return
	 */
	private DataObject collectSpaceAgents(Agent[] agents) {
		int n = agents.length;
		DataObject_SpaceAgents result = new DataObject_SpaceAgents(n);

		for (int i = 0; i < n; i++) {
			SpaceAgent agent = (SpaceAgent) agents[i];
			SpaceNode node = agent.getNode();

			Vector pos = node.getPosition();
			double r = node.getRelativeSize();
			// Make a copy
			Vector4d color = new Vector4d(node.getColor());
			// TODO: node.getShape() => torus node does not exist yet (as a
			// node)
			int shape = agent.getType();

			int spaceIndex = node.getSpace().getIndex();

			result.addAgent(pos, r, color, shape, spaceIndex);
		}
		
		return result;
	}
	

	/**
	 * Collects space links
	 * @param agents
	 * @return
	 */
	private DataObject collectSpaceLinks(Agent[] agents) {
		int n = agents.length;
		DataObject_SpaceLinks result = new DataObject_SpaceLinks(n);

		for (int i = 0; i < n; i++) {
			SpaceLink link = (SpaceLink) agents[i];

			SpaceAgent end1Agent = link.getEnd1();
			SpaceAgent end2Agent = link.getEnd2();
			if (end1Agent == null || end2Agent == null)
				continue;
			
			Vector end1 = end1Agent.getPosition();
			Vector end2 = end2Agent.getPosition();

			double width = link.getWidth();

			// Make a copy
			Vector4d color = new Vector4d(link.getColor());

			int spaceIndex = end1Agent.getSpace().getIndex();

			result.addLink(end1, end2, width, color, spaceIndex);
		}
		
		return result;
	}
	

	@Override
	public void reset() {
		type = null;
	}

}
