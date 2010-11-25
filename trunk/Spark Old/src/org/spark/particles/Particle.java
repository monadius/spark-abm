package org.spark.particles;

import org.spark.utils.Vector;
import org.spark.utils.Vector4d;

/**
 * Simple particle class
 * @author Monad
 *
 */
class Particle {
	public boolean used;
	public Particle next, prev;
	
	public Vector position;
	public Vector prevPosition;
	public Vector velocity;
	
	public double mass;
	public double size;
	
	public Vector4d color;
	
	public double energy;
//	public Image texture;
	public double timeOfBirth;
	public double lifeTime;
	public double lastUpdateTime;
	
	
	public boolean isAlive(double curTime) {
		return (timeOfBirth + lifeTime >= curTime && energy > 0 && size > 0);
	}
	
	
	public void kill() {
		lifeTime = -1;
	}
}
