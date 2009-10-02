package RSV_bronchiole;

import java.util.ArrayList;
import org.spark.core.*;
import org.spark.data.*;
import org.spark.space.*;
import org.spark.utils.*;
import org.spark.math.*;

public class Cleared extends SpaceAgent
{
	
	
	public void _init()
	{
	}
	
	public  Cleared()
	{
		super(0.5, SpaceAgent.SQUARE);
		_init();
		this.setColor(SpaceAgent.WHITE);
	}
	
}
