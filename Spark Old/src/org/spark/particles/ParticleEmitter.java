package org.spark.particles;

import javax.media.opengl.GL;

import org.spark.core.Agent;
import org.spark.utils.Vector4d;


@SuppressWarnings("serial")
public abstract class ParticleEmitter extends Agent {
	protected Particle[] pool; 
	{
		pool = new Particle[1000];
		for (int i = 0; i < 1000; i++)
			pool[i] = new Particle();
	};
	
	protected Particle start;
	
	
	protected int numParticles;
	protected double lastCreationTime;
	protected double birthPeriod;
	protected int srcBlendingMode;
	protected int dstBlendingMode;
	protected double curTime;
	
	private int index = 0;
	
	protected Particle allocateParticle() {
		for (int j = 0; j < 1000; index++, j++) {
			if (index >= 1000)
				index = 0;
			
			if (!pool[index].used) {
				pool[index].used = true;
				return pool[index];
			}
		}
		
		return pool[0];
	}
	
	
	public ParticleEmitter(int particlesPerSecond) {
		start = null;
		numParticles = 0;
		birthPeriod = particlesPerSecond > 0 ? 1.0 / particlesPerSecond : 10000;
		lastCreationTime = 0;
		srcBlendingMode = GL.GL_SRC_ALPHA;
		dstBlendingMode = GL.GL_ONE;
		curTime = 0;
		
		for (int i = 0; i < 1000; i++)
			pool[i].used = false;
	}
	
	
	public int init() {
		return 1;
	}
	
	
	public void update() {
		for (Particle cur = start; cur != null; ) {
			Particle next = cur.next;
			
			if (!cur.isAlive(curTime))
				remove(cur);
			
			cur = next;
		}
		
		if (birthPeriod < 0)
			return;
		
		if (lastCreationTime + birthPeriod < curTime) {
			for (double time = lastCreationTime; time + birthPeriod < curTime; time += birthPeriod)
				createParticle(time);
			
			lastCreationTime = curTime;
		}
		
		curTime += 0.1;
	}
	
	
	
	public void doDraw(GL gl) {
		gl.glDepthMask(false);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(srcBlendingMode, dstBlendingMode);
		
		for (Particle cur = start; cur != null; cur = cur.next) {
			Vector4d color = cur.color;
			gl.glColor4d(color.x, color.y, color.z, color.a);
			// drawBillboard(cur.position, cur.texture, cur.size)
		}
		
		gl.glDisable(GL.GL_BLEND);
		gl.glDepthMask(true);
	}
	
	
	public void insert(Particle p) {
		p.next = start;
		p.prev = null;
		
		if (start != null)
			start.prev = p;
		
		start = p;
		numParticles++;
	}
	
	
	public void remove(Particle p) {
		if (p.prev != null)
			p.prev.next = p.next;
		else
			start = p.next;
		
		if (p.next != null)
			p.next.prev = p.prev;
		
		p.used = false;
		numParticles--;
	}
	
	
	public void setBlendingMode(int src, int dst) {
		srcBlendingMode = src;
		dstBlendingMode = dst;
	}
	
	
	public int getNumParticles() {
		return numParticles;
	}
	
	
	protected abstract void createParticle(double time);
}
