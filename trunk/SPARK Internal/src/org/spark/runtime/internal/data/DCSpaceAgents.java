package org.spark.runtime.internal.data;

import org.spark.core.Agent;
import org.spark.core.Observer;
import org.spark.core.SparkModel;
import org.spark.runtime.data.DataCollectorDescription;
import org.spark.runtime.data.DataObject;
import org.spark.runtime.data.DataObject_SpaceAgents;
import org.spark.runtime.data.DataObject_SpaceLinks;
import org.spark.space.PhysicalNode;
import org.spark.space.SpaceAgent;
import org.spark.space.SpaceLink;
import org.spark.space.SpaceNode;
import org.spark.math.Vector;
import org.spark.math.Vector4d;

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

			String label = agent.getLabel();
			Vector pos = node.getPosition();
			double r = node.getRelativeSize();
			// Make a copy
			Vector4d color = new Vector4d(node.getColor());
			// TODO: node.getShape() => torus node does not exist yet (as a
			// node)
			int shape = agent.getType();
			if (shape == SpaceAgent.SQUARE2)
				shape = SpaceAgent.SQUARE;
			if (shape > 3) {
				// TODO: better solution for special (static, dynamic) shapes
				shape = SpaceAgent.CIRCLE;
			}

			int spaceIndex = node.getSpace().getIndex();
			DataObject_SpaceAgents.ShapeInfo si = null;
			if (node instanceof PhysicalNode) {
				PhysicalNode pNode = (PhysicalNode) node;
				si = new DataObject_SpaceAgents.ShapeInfo(pNode.getShapeInfo());
			}

			result.addAgent(label, pos, r, color, node.getRotation(), si, shape, spaceIndex);
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
			
			if (end1Agent == null)
				continue;
			
			Vector end1 = link.getPoint1(); 
			Vector end2 = link.getPoint2();
			
			if (end1 == null || end2 == null)
				continue;

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
