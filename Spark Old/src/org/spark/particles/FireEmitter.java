package org.spark.particles;

import org.spark.utils.RandomHelper;
import org.spark.utils.Vector;
import org.spark.utils.Vector4d;

@SuppressWarnings("serial")
public class FireEmitter extends ParticleEmitter {
//	private Image texture;
	private double startRadius;
	private double speed;
	
	private Vector4d startColor;
	private Vector4d endColor;
	
	private double lastUpdateTime;
	private double lifeTime;
	private double dispersion;
	private double minSize;
	private double maxSize;
	
	
	public FireEmitter(int particlesPerSecond, double theLifeTime,
			double radius, double theSpeed,
//			Image theTexture,
			Vector4d color1, Vector4d color2) {
		super(particlesPerSecond);
//		texture = theTexture;
		startRadius = radius;
		speed = theSpeed;
		startColor.set(color1);
		endColor.set(color2);
		lastUpdateTime = -1;
		lifeTime = theLifeTime;
		dispersion = 0.01;
		minSize = 0.8;
		maxSize = 1.0;
	}
	
	
	@Override
	public void update() {
		if (lastUpdateTime < 0)
			lastUpdateTime = curTime;
		
		super.update();
		
		double delta = curTime - lastUpdateTime;
		for (Particle cur = start; cur != null; ) {
			cur.prevPosition.set(cur.position);
			cur.position.add(cur.velocity, delta);
			cur.velocity.x *= 0.8;
			cur.velocity.y *= 0.8;
			cur.lastUpdateTime = curTime;
			
			setParticleColor(cur, curTime);
			
			cur = cur.next;
		}
		
		lastUpdateTime = curTime;
	}
			
	@Override
	protected void createParticle(double time) {
		Particle p = allocateParticle();
		
		p.color.set(startColor);
		p.lifeTime = lifeTime;
		p.mass = 1;
		p.energy = 1;
		p.position = Vector.randomVector(startRadius);
		p.prevPosition.set(p.position);
		p.size = RandomHelper.nextDoubleFromTo(minSize, maxSize);
//		p.texture = texture;
		p.timeOfBirth = curTime + lifeTime * (1 + Math.random()) * 0.2;
		p.lastUpdateTime = curTime;
		
		if (p.position.y < 0)
			p.position.y = 0;
		
		p.velocity.set(p.position);
		p.velocity.mul(dispersion);
		p.velocity.add(new Vector(0, 0, 1));
		p.velocity.mul(speed);
		
		insert(p);
	}
	

	protected void setParticleColor(Particle cur, double curTime) {
		double t = (curTime - cur.timeOfBirth) / lifeTime;
		cur.color = Vector4d.interpolate(startColor, endColor, t);
	}
	
	
}
