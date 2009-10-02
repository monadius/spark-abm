package org.spark.test.other;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLongArray;

import org.spark.utils.ListOfArrays;
import org.spark.utils.Vector;

/**
 * 500000 objects of type B
 * For all objects 100 times:
 * ((Base) objects[i].f()): 400
 * ((B) objects[i]).f() (.bf()): 203
 * list<B>.get(i).f(): 656
 * list<Base>.get(i).f(): 906
 * synchronized f(): 2400
 * 
 * @author Monad
 *
 */
class CodeEfficiencyTest {
	public static abstract class Base {
		public abstract void f();
	}

	public static class A extends Base {
		double num;
		
		public void f() {
		}
	}

	public static class B extends A {
		public void f() {
		}

		public void bf() {

		}
	}
	
	public static class C extends B {
		public void f() {
			
		}
	}

	final int n = 500000;
	Object[] objects = new Object[n];

	
	public void test1() {
		for (int i = 0; i < n; i++) {
			objects[i] = new B();
		}

		ArrayList<B> list = new ArrayList<B>(n);
		for (int i = 0; i < n; i++)
			list.add((B)objects[i]);

		final long start = System.currentTimeMillis();

		
		for (int k = 0; k < 100; k++) {
			for (int i = 0; i < n; i++) {
				((B) objects[i]).f();
//				list.get(i).f();
//				((Base) objects[i]).f();
//				((B) objects[i]).f();
			}
		}

		final long end = System.currentTimeMillis();

		System.out.println("Time = " + (end - start));
	}
	

	
	/**
	 * n = 1000000
	 * repeated 100 times
	 * a = new Vector(i, k, i + k) : 1687
	 * 
	 * a = new Vector() : 1406
	 * 
	 * a = createVector() : 2843
	 * 
	 * synchronized pool : 7000
	 */
	public void test2() {
		final long start = System.currentTimeMillis();

		final int n = 1000000;
		@SuppressWarnings("unused")
		Vector a, b;
		
		b = new Vector();
		
		for (int i = 0; i < 100; i++) {
			for (int k = 0; k < n; k++) {
//				a = new Vector();
				a = createVector();
			}
		}
		
		final long end = System.currentTimeMillis();

		System.out.println("Time = " + (end - start));
		
	}
	
	
	
	private static Vector[] vectorPool;
	{
		vectorPool = new Vector[1024];
		for (int i = 0; i < 1024; i++)
			vectorPool[i] = new Vector();
	}
	
	
	static int poolIndex = 0;
	
	
	static Vector createVector() {
		if (poolIndex >= 1024) {
			for (int i = 0; i < 1024; i++)
				vectorPool[i] = new Vector();
			
			poolIndex = 0;
		}
		
		return vectorPool[poolIndex++];
	}
	
	
	private HashMap<Class<? extends Agent>, ArrayList<Agent>> agents = 
		new HashMap<Class<? extends Agent>, ArrayList<Agent>>();
	
	ArrayList<Agent> agents2 = new ArrayList<Agent>();
	
	static class Agent {}
	static class SpaceAgent extends Agent {};
	
	static class Agent1 extends Agent {}
	static class Agent2 extends Agent1 {}
	static class Agent3 extends SpaceAgent {}

	
	private void addAgent(Agent agent) {
		Class<? extends Agent> cl = agent.getClass();
		ArrayList<Agent> list = agents.get(cl);
		
		if (list != null) {
			list.add(agent);
		}
		else {
			list = new ArrayList<Agent>();
			list.add(agent);
			agents.put(cl, list);
		}
	}
	
	
	protected void addAgent2(Agent agent) {
		agents2.add(agent);
	}
	
	
	/**
	 * n = 1000000
	 * addAgent: 710 (344 with preallocated list)
	 * addAgent2: 641 (390 with preallocated list)
	 * 
	 * Preallocated memory
	 * addAgent: 250
	 * addAgent2: 312 (220 with preallocation)
	 */
	protected void test3() {
		final int n = 1000000;

		final long start = System.currentTimeMillis();

		for (int i = 0; i < n; i++) {
			Agent agent = new Agent1();
			addAgent(agent);
			
			agent = new Agent2();
			addAgent(agent);
			
			agent = new Agent3();
			addAgent(agent);
		}
		
		final long end = System.currentTimeMillis();

		System.out.println("Time = " + (end - start));
	}
	
	
	/**
	 * n = 10000000
	 * initial capacity = 100
	 * 
	 * With double values:
	 * 
	 * Only add:
	 * ListOfArrays: 922
	 * ArrayList: 1125
	 * 
	 * With get:
	 * ListOfArrays: 1094
	 * ArrayList: 1250
	 * 
	 * Preallocated array: 47
	 */
	protected void test4() {
		int n = 10000000;
		
//		ArrayList<Integer> arrayList = new ArrayList<Integer>(100);
		ListOfArrays<Integer> listOfArrays = new ListOfArrays<Integer>(100);
//		int[] array = new int[n];

		final long start = System.currentTimeMillis();
		
		for (int i = 0; i < n; i++) {
//			arrayList.add(i);
			listOfArrays.add(i);
//			array[i] = i;
		}
		
		long s = 0;
		for (int i = 0; i < n; i++) {
//			int a = arrayList.get(i);
			int a = listOfArrays.get(i);
//			int a = array[i];
			s += a;
		}

		final long end = System.currentTimeMillis();

		System.out.println("Time = " + (end - start));
		System.out.println("Sum = " + s);
	}
	
	
	public void test5() {
		final long start = System.currentTimeMillis();
		int x = 1;
		int y = 0;

		for (long i = 0; i < 1000000000l; i++) {
			x |= y;
			y <<= 1;
			if (y < 2) {
				x += y;
			}
		}

		final long end = System.currentTimeMillis();

		System.out.println("Time = " + (end - start));
	}
	
	
	/**
	 * AtomicInteger: 20000
	 * int: 860
	 */
	public void test6() {
//		AtomicInteger integer = new AtomicInteger(0);
		int integer = 0;
//		int a = 1;

		final long start = System.currentTimeMillis();
		
		for (int i = 0; i < 1000000000; i++) {
//			a = integer.addAndGet(4);
			integer += 4;
//			a = integer;
		}
		
		final long end = System.currentTimeMillis();

		System.out.println("Time = " + (end - start));
	}
	

	
	volatile double[] array7 = new double[1000];
	volatile AtomicLongArray array72 = new AtomicLongArray(1000);
	
	
	/**
	 * n = 100000
	 * t = 2
	 * Naive implementation (incorrect): 1359
	 * Synchronization on array7: 42031
	 * Synchronization before loop: 2484
	 * AtomicLongArray: 7625
	 * 
	 * n = 10000
	 * t = 2
	 * Synchronization on array7: 4125
	 * Synchronization on array7 before loop: 200
	 * AtomicLongArray: 734
	 */
	public void test7() {
		final int t = 2;
		final int n = 100000;
		Thread[] threads = new Thread[t];
		
		for (int i = 0; i < t; i++) {
			threads[i] = new Thread(new Runnable() {
				public void run() {
//					synchronized (array7) {
						int start = (int)(Math.random() * 1000);
						int end = start + 1000 * n;
						for (int i = start; i < end; i++) {
//							synchronized (array7) {
//								array7[i % 1000] += 2;
								array72.addAndGet(i % 1000, 2);
//							}
						}
//					}
				}
			});
		}

		// Start gathering statistics
		final long start = System.currentTimeMillis();
		
		// Main job
		for (int i = 0; i < t; i++) {
			threads[i].start();
		}
		
		try {
			for (int i = 0; i < t; i++) {
				threads[i].join();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}

		// Print statistics
		final long end = System.currentTimeMillis();
		System.out.println("Time = " + (end - start));
		
		for (int i = 0; i < 1000; i++)
			array7[i] = array72.get(i);
		
		// Verify correctness
		for (int i = 0; i < 1000; i++) {
			if (array7[i] != 2 * n * t)
				System.out.println("i = " + i + " array7[i] = " + array7[i]);
		}
	}
	
	
	private static class AskTest {
		private int n;
		
		public AskTest(int n) {
			this.n = n;
		}
		
		
		public void ask(Runnable run) {
			for (int i = 0; i < n; i++)
				run.run();
		}
	}
	
	interface IAsk extends Runnable {
		public void set(double val);
		public double get();
	}
	
	/**
	 * n = 10000000
	 * job1: 625
	 * job2: 641
	 * 
	 */
	private void test8()
	{
		int n = 10000000;
		// Start gathering statistics
		long start = System.currentTimeMillis();
		double S = 0;
		
		// Job1
		S = 0;
		
		for (int i = 0; i < n; i++) {
			Vector v = new Vector(1, 2, 0);
			v.add(new Vector(0, 3, 1.2));
			
			S += v.length();
		}

		// Print statistics
		long end = System.currentTimeMillis();
		System.out.println("Time = " + (end - start));
		
		start = System.currentTimeMillis();
		
		// Job2
		S = 0;
		
		AskTest ask = new AskTest(n);
		IAsk cmd = new IAsk() {
			private double S;
			
			public void set(double val) {
				S = val;
			}
			
			public double get() {
				return S;
			}
			
			public void run() {
				Vector v = new Vector(1, 2, 0);
				v.add(new Vector(0, 3, 1.2));
				S += v.length();
			}
		};
		
		cmd.set(S);
		ask.ask(cmd);		
		cmd.get();
		
		// Print statistics
		end = System.currentTimeMillis();
		System.out.println("Time = " + (end - start));
	}
	
	
	public static void main(String[] args) {
//		new CodeEfficiencyTest().test1();
//		new CodeEfficiencyTest().test2();
//		new CodeEfficiencyTest().test3();
//		new CodeEfficiencyTest().test4();
//		new CodeEfficiencyTest().test5();
//		new CodeEfficiencyTest().test6();
//		new CodeEfficiencyTest().test7();
		new CodeEfficiencyTest().test8();
	}
	
}
