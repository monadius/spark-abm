package org.spark.space;

import java.io.Serializable;
import java.util.ArrayList;

import org.spark.cluster.ClusterManager;
import org.spark.cluster.ObjectBuf;
import org.spark.core.AgentBuffer;
import org.spark.core.Observer;
import org.spark.utils.Vector;

import org.spark.cluster.*;

import com.spinn3r.log5j.Logger;


public class GlobalSpace implements Serializable {
	private static final Logger logger = Logger.getLogger();
	/**
	 * Default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	private final double xMin0, xMax0, yMin0, yMax0;
	private final double xSize0, ySize0;
	private final boolean wrapX0, wrapY0;
	
	private final double xChunk, yChunk;
	private final int xChunkNumber, yChunkNumber;

	// contains information in a cyclic way
	private transient int[][] neighbors;
	private transient int myRank;
	
	private transient ArrayList<SpaceAgent>[][] buffers;

	/**
	 * Class containing information about dimensions of a local space
	 * @author Monad
	 *
	 */
	public static class LocalSpaceDimensions {
		public double xMin, xMax, yMin, yMax;
		public double xSize, ySize;
		public boolean wrapX, wrapY;
	}
	
	
	public GlobalSpace(int nodesNumber, BoundedSpace space) {
		if (nodesNumber <= 0) {
			throw new Error("Global space cannot be initialized on a single computer");
		}
		
		logger.info("Creating global space");
		
		this.xMin0 = space.getXMin();
		this.xMax0 = space.getXMax();
		this.yMin0 = space.getYMin();
		this.yMax0 = space.getYMax();
		
		this.wrapX0 = space.wrapX;
		this.wrapY0 = space.wrapY;
		
		this.xSize0 = xMax0 - xMin0;
		this.ySize0 = yMax0 - yMin0;
		
		// Slice space into chunks
		// TODO: better algorithm
		xChunkNumber = nodesNumber;
		yChunkNumber = 1;

		xChunk = xSize0 / nodesNumber;
		yChunk = ySize0;
		
		myRank = 0;
		
	}
	
	
	/**
	 * Returns number of local spaces along x-axis
	 * @return
	 */
	public int getXChunkNumber() {
		return xChunkNumber;
	}
	
	
	/**
	 * Returns number of local spaces along y-axis
	 * @return
	 */
	public int getYChunkNumber() {
		return yChunkNumber;
	}
	
	
	/**
	 * Returns the dimensions of the space specified by the rank
	 * @param rank
	 * @return
	 */
	public LocalSpaceDimensions getSpaceDimensions(int rank) {
		// TODO: change this function after changing local space parameters
		LocalSpaceDimensions dim = new LocalSpaceDimensions();
		
		int y = (rank - 1) / xChunkNumber;
		int x = rank - 1 - y * xChunkNumber;
		
		dim.xMin = xMin0 + x * xChunk;
		dim.xMax = dim.xMin + xChunk;
		dim.yMin = yMin0 + y * yChunk;
		dim.yMax = dim.yMin + yChunk;
		dim.xSize = xChunk;
		dim.ySize = yChunk;

		if (wrapX0 && xChunkNumber == 1)
			dim.wrapX = true;
		else
			dim.wrapX = false;

		if (wrapY0 && yChunkNumber == 1)
			dim.wrapY = true;
		else
			dim.wrapY = false;

		return dim;
	}
	
	
	/**
	 * Returns the rank of a neighbor at the relative position (dx, dy)
	 * @param dx should be -1, 0, or 1
	 * @param dy should be -1, 0, or 1
	 * @return -1 if no neighbor in that direction
	 */
	public int getNeighborRank(int dx, int dy) {
		if (dx < -1 || dx > 1 || dy < -1 || dy > 1)
			return -1;
		
		return neighbors[dx + 1][dy + 1];
	}
	
	
	@SuppressWarnings("unchecked")
	public void createLocalSpace() {
		logger.info("Creating local space");
		Comm comm = ClusterManager.getInstance().getComm();
		
		myRank = comm.rank();
		int y = (myRank - 1) / xChunkNumber;
		int x = myRank - 1 - y * xChunkNumber;
		
		int xBorder = 1, yBorder = 1;
		boolean wrapX = false, wrapY = false;
		
		if (xChunkNumber == 1) {
			if (wrapX0) {
				wrapX = true;
				xBorder = 0;
			}
		}
		
		if (yChunkNumber == 1) {
			if (wrapY0) {
				wrapY = true;
				yBorder = 0;
			}
		}
		
		// TODO: asymmetric borders
//		if (!wrapX0) {
//			if (x == 0)
				// leftBorder = 0;
//			if (x == xChunkNumber - 1)
				// rightBorder = 0;
//		}

		// FIXME: work around to prevent complication with non-integer space dimensions
		if (Math.floor(xMin0) != xMin0 ||
			Math.floor(yMin0) != yMin0 ||
			Math.floor(xChunk) != xChunk ||
			Math.floor(yChunk) != yChunk)
				throw new Error("Non-integer space dimensions");
		
		// TODO: other space types
		Observer.getInstance().setSpace(
				new ClusterNetLogoSpace(xMin0 + x*xChunk, xMin0 + x*xChunk + xChunk,
						yMin0 + y*yChunk, yMin0 + y*yChunk + yChunk, wrapX, wrapY, xBorder, yBorder));
		
		neighbors = new int[3][3];
		buffers = new ArrayList[3][3];
		
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				neighbors[i + 1][j + 1] = getRank(x + i, y + j);
				buffers[i + 1][j + 1] = new ArrayList<SpaceAgent>(100);
			}
		}
	}
	
	/**
	 * Call this function before changing the node position
	 * If the returned result is true then nothing else have to be done
	 * otherwise change the position manually
	 * @param node
	 * @return
	 */
	public boolean moveNode(SpaceNode node) {
		// FIXME: other types of spaces
		ClusterNetLogoSpace space = (ClusterNetLogoSpace) Observer.getDefaultSpace();
		
		int xBorder = space.getXBorder();
		int yBorder = space.getYBorder();
		
		double xMin = space.getXMin() + xBorder;
		double xMax = space.getXMax() - xBorder;
		double yMin = space.getYMin() + yBorder;
		double yMax = space.getYMax() - yBorder;
		
		
		double x = node.newPosition.x;
		double y = node.newPosition.y;
		int i = 0, j = 0;

		if (x < xMin) i = -1;
		else if (x >= xMax) i = 1;
		
		if (y < yMin) j = -1;
		else if (y >= yMax) j = 1;
		
		i += 1;
		j += 1;
		
		int rank = neighbors[i][j];
		
		x = restrictX(x);
		y = restrictY(y);
		
		node.newPosition.set(x, y);
//		System.out.println("Rank: " + rank + "; myRank: " + myRank);
		if (rank != -1 && rank != myRank) {
			// TODO: do we need to do it every time here?
			node.agent.setDeepSerialization(true);
//			System.out.println("i: " + i + "; j: " + j);
			buffers[i][j].add(node.agent);
//			System.out.println("size: " + buffers[i][j].size());

			// Correct node's position.
			// It is required because after transferring the node should be
			// at the correct position.
			node.position.set(node.newPosition);

			// The true value means that nothing else should be done with this node
			return true;
		}
		
		// The false value means that the original moveNode0 function should be called
		// for this node
		return false;
	}
	
	
	public int getRank(Vector p) {
		double x = restrictX(p.x);
		double y = restrictY(p.y);
		
		int xx = (int) Math.floor((x - xMin0) / xChunk);
		int yy = (int) Math.floor((y - yMin0) / yChunk);
		
		return yy * xChunkNumber + xx + 1;
	}
	
	
	public int getRank(int x, int y) {
		if (x >= xChunkNumber) {
			// FIXME: correct this
			if (wrapX0)
				x -= xChunkNumber;
			else
				return -1;
		}
		else if (x < 0) {
			if (wrapX0)
				x += xChunkNumber;
			else
				return -1;
		}

		if (y >= yChunkNumber) {
			// FIXME: correct this
			if (wrapY0)
				y -= yChunkNumber;
			else
				return -1;
		}
		else if (y < 0) {
			if (wrapY0)
				y += yChunkNumber;
			else
				return -1;
		}
		
		return y * xChunkNumber + x + 1;
	}
	
	
	public double restrictX(double x) {
		if (wrapX0) {
			if (x < xMin0) {
				x += xSize0;
				if (x < xMin0)
					x += (Math.floor((xMin0 - x) / xSize0) + 1) * xSize0;
			}
			else if (x >= xMax0) {
				x -= xSize0;
				if (x >= xMax0)
					x -= (Math.floor((x - xMax0) / xSize0) + 1) * xSize0;
			}
		}
		else {
			if (x < xMin0)
				x = xMin0;
			else if (x > xMax0)
				x = xMax0;
		}
		
		return x;
	}


	public double restrictY(double y) {
		if (wrapY0) {
			if (y < yMin0) {
				y += ySize0;
				if (y < yMin0)
					y += (Math.floor((yMin0 - y) / ySize0) + 1) * ySize0;
			}
			else if (y >= yMax0) {
				y -= ySize0;
				if (y >= yMax0)
					y -= (Math.floor((y - yMax0) / ySize0) + 1) * ySize0;
			}
		}
		else {
			if (y < yMin0)
				y = yMin0;
			else if (y > yMax0)
				y = yMax0;
		}
		
		return y;
	}

	
	public void sendReceiveAgents() {
		Comm comm = ClusterManager.getInstance().getComm();
		ArrayList<Thread> threads1 = new ArrayList<Thread>(8);
		ArrayList<Thread> threads2 = new ArrayList<Thread>(8);
		ArrayList<AgentReceiver> receivers = new ArrayList<AgentReceiver>(8);
		
		int myRank = comm.rank();
		
		// FIXME: if xChunkNumber = 1, then all 3 buffers are sent to the same neighbor
		//			x 0 y
		//			x 0 y
		//			x 0 y
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				if (i != 1 || j != 1) {
//					if (buffers[i][j].size() > 0)
//						System.err.println("Sending for: " + "i = " + i + "; j = " + j);
					if (neighbors[i][j] == -1) continue;
					if (neighbors[i][j] == myRank) continue;
					
					AgentSender sender = new AgentSender(
							comm, neighbors[i][j], buffers[i][j]);
					
					AgentReceiver receiver = new AgentReceiver(comm, neighbors[i][j]);
					
					Thread thread1 = new Thread(sender);
					Thread thread2 = new Thread(receiver);
					
					receivers.add(receiver);
					threads1.add(thread1);
					threads2.add(thread2);
					
					thread1.start();
					thread2.start();
				}
			}
		
		for (int i = 0; i < threads1.size(); i++) {
			try {
				threads1.get(i).join(200);
				threads2.get(i).join(200);
				AgentReceiver receiver = receivers.get(i);
				// TODO: do we need this?
				receiver.getReceivedAgents();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				buffers[i][j].clear();
	}
	
	
	static class AgentSender implements Runnable {
		private int dstRank;
		private Comm comm;
		private ObjectBuf<SpaceAgent[]> buffer;
		
		
		public AgentSender(Comm comm, int dstRank, ArrayList<SpaceAgent> agents) {
			this.comm = comm;
			this.dstRank = dstRank;
			
			SpaceAgent[] tmp = new SpaceAgent[agents.size()];
			tmp = agents.toArray(tmp);
			this.buffer = ObjectBuf.objectBuffer(tmp);
		}
		
		public void run() {
			try {
//				System.out.println("Send/receive: myRank" + myRank + "; " + "dstRank: " + dstRank);
				comm.send(dstRank, ClusterManager.AGENT_DATA, buffer);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}



	static class AgentReceiver implements Runnable {
		private int dstRank;
		private Comm comm;
		private ObjectBuf<SpaceAgent[]> receiveBuffer;
		
		public SpaceAgent[] getReceivedAgents() {
			return receiveBuffer.get(0);
		}
		
		public AgentReceiver(Comm comm, int dstRank) {
			this.comm = comm;
			this.dstRank = dstRank;
			
			this.receiveBuffer = ObjectBuf.buffer();
		}
		
		public void run() {
			try {
//				System.out.println("Send/receive: myRank" + myRank + "; " + "dstRank: " + dstRank);
				comm.receive(dstRank, ClusterManager.AGENT_DATA, receiveBuffer);
//				comm.sendReceive(dstRank, buffer, myRank, receiveBuffer);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Synchronizes agents at the borders
	 */
	public void synchronizeBorders() {
		// FIXME: other types of spaces
		ClusterNetLogoSpace space = (ClusterNetLogoSpace) Observer.getDefaultSpace();
		
		// First, remove all agents at the border
		space.removeAgentsAtTheBorder();

		int xb = space.getXBorder();
		int yb = space.getYBorder();
		
		int[][] xOffset = new int[][] {
				{xb, xb, xb},
				{0, 0, 0},
				{-xb, -xb, -xb}
		};

		int[][] yOffset = new int[][] {
				{yb, 0, -yb},
				{yb, 0, -yb},
				{yb, 0, -yb}
		};


		Comm comm = ClusterManager.getInstance().getComm();
		ArrayList<Thread> threads1 = new ArrayList<Thread>(8);
		ArrayList<Thread> threads2 = new ArrayList<Thread>(8);

		int myRank = comm.rank();
		
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				// TODO: negative ranks?
				if (neighbors[i][j] != myRank && neighbors[i][j] != -1) {
					ArrayList<SpaceAgent> agents = 
						space.getBorderAgents(xOffset[i][j], yOffset[i][j]);
					
					AgentSender sender = new AgentSender(
							comm, neighbors[i][j], agents);
					
					AgentReceiver receiver = new AgentReceiver(comm, neighbors[i][j]);
					
					Thread thread1 = new Thread(sender);
					Thread thread2 = new Thread(receiver);
					
					threads1.add(thread1);
					threads2.add(thread2);
					
					thread1.start();
					thread2.start();
				}
			}
		}


		for (int i = 0; i < threads1.size(); i++) {
			try {
				threads1.get(i).join(200);
				threads2.get(i).join(200);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	

	
	/**
	 * Sends all agents to a master.
	 * Could be called only after or before all synchronization,
	 * otherwise agents' deepSerialization will be a problem
	 */
	public void sendAllAgentsToMaster() throws Exception {
		// TODO: very inefficient implementation
		// let it work in background
		AgentBuffer agents = AgentBuffer.allAgents();
		
		// By default agents should have deepSerialization = false
/*		for (int i = 0; i < agents.length; i++) {
			agents[i].setDeepSerialization(false);
		}*/

		ObjectBuf<AgentBuffer> agentsBuf = ObjectBuf.buffer(agents);
		
		ClusterManager.getInstance().getComm().gather(
				0, 
				ClusterManager.AGENT_DATA | ClusterManager.SEND_MASTER, 
				agentsBuf, null);
//		ClusterManager.getInstance().getComm().send(0, ClusterManager.AGENT_DATA, agentsBuf);
		
	}
	
	
	
	@SuppressWarnings("unchecked")
	public void receiveAgentsOnMaster() throws Exception {
		// First, remove all existing agents
		Observer.getInstance().clearAgents();

		// We need to send something
		AgentBuffer tmp = AgentBuffer.empty();
		ObjectBuf<AgentBuffer> tmpBuf = ObjectBuf.buffer(tmp);
		
		int size = ClusterManager.getInstance().getComm().size();
		
		// Prepare receiving buffer
		ObjectBuf<AgentBuffer>[] recvBuf = new ObjectBuf[size];
		for (int i = 0; i < size; i++) {
			recvBuf[i] = ObjectBuf.buffer();
		}
		
		// Gather agents' data
//		System.out.println("Gather");

		ClusterManager.getInstance().getComm().gather(
				0, 
				ClusterManager.AGENT_DATA | ClusterManager.RECV_MASTER, 
				tmpBuf, recvBuf);

/*		Object[] dst = new Object[size];
		for (int i = 1; i < size; i++) {
			// TODO: use asynchronous command Ireceive
			ClusterManager.getInstance().getComm().receive(i, ClusterManager.AGENT_DATA, recvBuf[i]);
		}
*/		
		// Add agents to the Observer
		for (int i = 1; i < size; i++) {
//			System.out.println("Processing buffer: " + i);

			tmp = recvBuf[i].get(0);
			if (tmp == null)
				throw new Exception("Null buffer! (" + i + ")");
			
			tmp.addAgentsToTheObserver();
		}
	}
}
