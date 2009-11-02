package org.spark.runtime.data;

import org.spark.core.Agent;
import org.spark.core.Observer;
import org.spark.core.SparkModel;
import org.spark.space.SpaceAgent;
import org.spark.space.SpaceNode;
import org.spark.utils.Vector;
import org.spark.utils.Vector4d;

/**
 * Collects data about specific type of space agents
 * @author Monad
 *
 */
public class DCSpaceAgents extends DataCollector {
	protected String typeName;
	protected Class<? extends Agent> type;
	
	
	/**
	 * Creates the data collector for the given type of space agents
	 * @param typeName
	 */
	public DCSpaceAgents(String typeName) {
		this.typeName = typeName;
		
		this.dataName = "$agents:" + typeName;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public DataObject collect0(SparkModel model) throws Exception {
		if (type == null) {
			type = (Class<? extends Agent>)model.getClass().getClassLoader().loadClass(typeName);

			if (type == null)
				throw new Exception("Type of agents is unresolved: " + typeName);
		}
		
		Agent[] tmpAgents = Observer.getInstance().getAgents(type);
		if (tmpAgents == null || tmpAgents.length == 0) {
			return new DataObject_SpaceAgents(0); 
		}
		
		if (!(tmpAgents[0] instanceof SpaceAgent))
			return new DataObject_SpaceAgents(0);
		
		Agent[] agents = tmpAgents;
		
		int n = agents.length;
		DataObject_SpaceAgents result = new DataObject_SpaceAgents(n);
		
		for (int i = 0; i < n; i++)
		{
			SpaceAgent agent = (SpaceAgent) agents[i];
			SpaceNode node = agent.getNode();
			
			Vector pos = node.getPosition();
			double r = node.getRelativeSize();
			// Make a copy
			Vector4d color = new Vector4d(node.getColor());
			// TODO: node.getShape() => torus node does not exist yet (as a node)
			int shape = agent.getType();
			
			int spaceIndex = node.getSpace().getIndex();
			
			result.AddAgent(pos, r, color, shape, spaceIndex);
		}
		
//		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(typeName + ".dat"));
//		oos.writeObject(result);
//		oos.close();
		
		return result;
	}
	
	
	@Override
	public void reset() {
		type = null;
	}

}
