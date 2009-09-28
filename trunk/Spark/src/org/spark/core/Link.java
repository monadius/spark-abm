package org.spark.core;

/**
 * A generic link agent.
 * Any link agent has knowledge about two other agents
 * which are connected by the link
 * @author Monad
 *
 */
public class Link extends Agent {
	/**
	 * SerialVersionUID
	 */
	private static final long serialVersionUID = 4842070711491006805L;

	/* Link's ends */
	protected Agent end1, end2;

	/**
	 * The default constructor 
	 **/
	public Link() {
	}
	
	
	/**
	 * A constructor
	 * @param end1
	 * @param end2
	 */
	public Link(Agent end1, Agent end2) {
		this.end1 = end1;
		this.end2 = end2;
		
		if (end1 != null)
			end1.addLink(this);
		
		if (end2 != null)
			end2.addLink(this);
	}
	
	
	/**
	 * Removes one end of a link
	 * @param end
	 */
	void removeEnd(Agent end) {
		if (end == null)
			return;
		
		if (end1 == end)
			end1 = null;
		
		if (end2 == end)
			end2 = null;
	}
	
	
	/**
	 * Returns the first link's end
	 * @return
	 */
	public Agent getEnd1() {
		return end1;
	}
	
	
	/**
	 * Returns the second link's end
	 * @return
	 */
	public Agent getEnd2() {
		return end2;
	}
	
	
	/**
	 * Connects specified agents by the link
	 * @param end1
	 * @param end2
	 */
	public void connect(Agent end1, Agent end2) {
		if (this.end1 != null)
			this.end1.removeLink(this);
		
		if (this.end2 != null)
			this.end2.removeLink(this);
		
		this.end1 = end1;
		this.end2 = end2;
		
		if (end1 != null)
			end1.addLink(this);
		
		if (end2 != null)
			end2.addLink(this);
	}
	
	
	/**
	 * Returns true if the link is connected to the given agent
	 * @param end
	 * @return
	 */
	public boolean isConnectedTo(Agent end) {
		if (end == null)
			return false;
		
		return end1 == end || end2 == end;
	}
	
	
	@Override
	public void die() {
		if (end1 != null)
			end1.removeLink(this);
		
		if (end2 != null)
			end2.removeLink(this);
		
		super.die();
	}
}
