package org.spark.space;

import org.spark.core.Agent;
import org.spark.core.Link;
import org.spark.utils.Vector;
import org.spark.utils.Vector4d;

/**
 * Link for connecting space agents
 * @author Monad
 */
public class SpaceLink extends Link {
	/**
	 * Default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * The default constructor
	 */
	public SpaceLink() {
	}
	
	
	/**
	 * A constructor
	 * @param end1
	 * @param end2
	 */
	public SpaceLink(SpaceAgent end1, SpaceAgent end2) {
		connect(end1, end2);
	}
	
	
	@Override
	public void connect(Agent end1, Agent end2) {
		if (!(end1 instanceof SpaceAgent) || !(end2 instanceof SpaceAgent))
			return;
		
		SpaceAgent e1 = (SpaceAgent) end1;
		SpaceAgent e2 = (SpaceAgent) end2;
		
		// Cannot connect agents in different spaces */
		if (e1.getSpace() != e2.getSpace())
			return;
		
		super.connect(end1, end2);
	}
	
	
	@Override
	public SpaceAgent getEnd1() {
		return (SpaceAgent) end1;
	}
	
	
	@Override
	public SpaceAgent getEnd2() {
		return (SpaceAgent) end2;
	}
	
	
	/**
	 * Returns a vector-distance (componentwise distance)
	 * between connected agents
	 * @return null if one or both ends are not initialized
	 */
	public Vector getVector() {
		if (end1 == null || end2 == null)
			return null;
		
		Space space = ((SpaceAgent) end1).getSpace();
		return space.getVector((SpaceAgent) end1, (SpaceAgent) end2);
	}
	
	
	//*********************
	// Rendering interface
	//*********************
	
	protected Vector4d color = new Vector4d();
	protected double width = 0.1;
	
	public void setColor(Vector color) {
		this.color.set(color);
	}
	
	
	public void setColor(Vector4d color) {
		this.color.set(color);
	}
	
	
	public Vector4d getColor() {
		// TODO: new Vector(color) but then it is impossible to
		// modify individual color components
		return color;
	}
	
	
	public Vector getColor3() {
		return new Vector(color.x, color.y, color.z);
	}
	
	
	public double getAlpha() {
		return color.a;
	}
	
	
	public void setAlpha(double a) {
		color.a = a;
	}
	
	
	public Vector getRGBColor() {
		return new Vector(color.x, color.y, color.z);
	}
	
	
	public double getWidth() {
		return width;
	}
	
	public void setWidth(double width) {
		if (width < 0)
			return;
		
		this.width = width;
	}
}
