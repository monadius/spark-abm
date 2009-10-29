/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package org.spark.space;


public class SquareNode extends CircleNode {
	/**
	 * Default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	protected SquareNode(Space space, double radius) {
		super(space, radius);
	}
	
	@Override
	public int getShape() {
		return SpaceAgent.SQUARE;
	}


}
