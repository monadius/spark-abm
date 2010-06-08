package Worms;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class Obstacle extends SpaceAgent
{
	
	
	public void _init()
	{
	}
	
	public  Obstacle()
	{
		_init();
		((CircleNode) this.getNode()).setRadius(1.0);
		this.setColor(SpaceAgent.RED);
	}
	
}
