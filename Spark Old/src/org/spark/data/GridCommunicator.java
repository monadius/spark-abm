package org.spark.data;

import java.io.Serializable;
import java.util.ArrayList;

import org.spark.cluster.ClusterManager;
import org.spark.core.Observer;
import org.spark.space.BoundedSpace;
import org.spark.space.GlobalSpace;

import org.spark.cluster.*;

import com.spinn3r.log5j.Logger;

/**
 * Class for grid communication.
 * Used for initialization of grids on slaves and for intercommunication between
 * grids during the model execution process.
 * @author Monad
 */
public class GridCommunicator implements Serializable {
	private static final Logger logger = Logger.getLogger();
	// Default serial id
	private static final long serialVersionUID = 1L;

	/**
	 * This class contains all data necessary for creating
	 * the grid on a slave machine
	 * @author Monad
	 */
	public static class GridData implements Serializable {
		// Default serial id
		private static final long serialVersionUID = 1L;
		private String	name;
		private double[][]	data;
		private int	xSize, ySize;
		private int xBorder, yBorder;
		
		
		/**
		 * Copies all necessary data from the given Grid.
		 * Dimensions should be specified without border.
		 * @param grid
		 * @param name
		 * @param x0
		 * @param y0
		 * @param xSize
		 * @param ySize
		 * @param xBorder
		 * @param yBorder
		 */
		public GridData(Grid grid, String name, int x0, int y0, int xSize, int ySize, int xBorder, int yBorder) {
			this.name = name;
			this.xSize = xSize + xBorder * 2;
			this.ySize = ySize + yBorder * 2;
			this.data = new double[this.xSize][this.ySize];
			this.xBorder = xBorder;
			this.yBorder = yBorder;
			
			double[][] data0 = grid.getData();
			
			int n = data0.length;
			int m = data0[0].length;
			
			for (int i = x0 - xBorder, ii = 0; i < x0 + xSize + xBorder; i++, ii++)
				for (int j = y0 - yBorder, jj = 0; j < y0 + ySize + yBorder; j++, jj++) {
					int x = i;
					int y = j;
					
					if (x < 0) x += n;
					else if (x >= n) x -= n;
					
					if (y < 0) y += m;
					else if (y >= m) y -= m;
					
					data[ii][jj] = data0[x][y];
				}
		}
		
		
		public Grid createGrid() {
			// FIXME: guard condition should be removed later
			BoundedSpace space = (BoundedSpace) Observer.getDefaultSpace();
			if (xSize != (int)space.getXSize() ||
				ySize != (int)space.getYSize())
					throw new Error("Dimension problem");

			// Create new grid of the specified size
			Grid grid = new Grid(Observer.getDefaultSpace(), xSize, ySize);
			grid.setBorders(xBorder, yBorder);
			
			double[][] data0 = grid.getData();
			for (int i = 0; i < xSize; i++)
				for (int j = 0; j < ySize; j++)
					data0[i][j] = data[i][j];
			
			// GC data
			data = null;
			
			// Add new grid to the observer
			return Observer.getInstance().addDataLayer(name, grid);
		}
	}
	
	
	private GridData[] gridData;
	private Grid[]	grids;		
	
	
	/**
	 * Creates an instance of the grid communicator
	 */
	public GridCommunicator() {
		gridData = null;
		grids = null;
	}
	
	
	/**
	 * Creates grids on a slave machine
	 */
	public void createLocalGrids() {
		if (gridData == null)
			return;
		
		grids = new Grid[gridData.length];
		
		for (int i = 0; i < gridData.length; i++) {
			grids[i] = gridData[i].createGrid();
		}
	}
	

	/**
	 * Prepares all grids for sending to the slave with rank = rank
	 * @param rank
	 */
	public void prepareGridsForTransferring(GlobalSpace globalSpace, int rank) {
		GlobalSpace.LocalSpaceDimensions dim = globalSpace.getSpaceDimensions(rank);
		
		Observer observer = Observer.getInstance();
		String[] names = observer.getDataLayers();
		
		gridData = new GridData[names.length];
		
		for (int i = 0; i < names.length; i++) {
			// TODO: other data layer types
			Grid grid = (Grid) observer.getDataLayer(names[i]);
			
			// FIXME: guard condition
			BoundedSpace space = (BoundedSpace) Observer.getDefaultSpace();
			if (grid.xSize != (int) space.getXSize() ||
				grid.ySize != (int) space.getYSize())
					throw new Error("Dimension problem");
			
			// TODO: do we need eps?
			double eps = 0;
			int x0 = grid.findX(dim.xMin + eps);
			int y0 = grid.findY(dim.yMin + eps);

			int xBorder = dim.wrapX ? 0 : 1;
			int yBorder = dim.wrapY ? 0 : 1;

			gridData[i] = new GridData(grid, names[i], x0, y0, 
					(int)dim.xSize, (int)dim.ySize, xBorder, yBorder);
		}
	}
	

	/**
	 * Class for storing border data of all grids
	 * @author Monad
	 */
	// TODO: do we need a default constructor for serialization?
	private static class GridBorderData implements Serializable {
		private static final long serialVersionUID = 1L;

		// first index = grid number
		// second index = x-axis data
		// third index = y-axis data
		private double[][][] borderData;
		// Which border is stored here.
		private int	dx, dy;
		

		/**
		 * Creates buffers with a border data for all grids in the array.
		 * dx and dy specify the border. They should be between -1 and 1.
		 * If dx = dy = 0, then everything except borders is stored in the buffer.
		 * @param grids
		 * @param dx 
		 * @param dy
		 */
		public GridBorderData(Grid[] grids, int dx, int dy) {
			if (grids == null)
				return;
			
			this.dx = dx;
			this.dy = dy;
			this.borderData = new double[grids.length][][];
			
			for (int i = 0; i < grids.length; i++) {
				double[][] data = grids[i].getData();
				int xBorder = grids[i].xBorder;
				int yBorder = grids[i].yBorder;
				int xSize = grids[i].xSize;
				int ySize = grids[i].ySize;
				
				int x0, x1, y0, y1;

				if (dx > 0) {
					x0 = xSize - 2*xBorder;
					x1 = xSize - xBorder;
				}
				else if (dx == 0) {
					x0 = xBorder;
					x1 = xSize - xBorder;
				}
				else {
					x0 = xBorder;
					x1 = 2*xBorder;
				}
				
				if (dy > 0) {
					y0 = ySize - 2*yBorder;
					y1 = ySize - yBorder;
				}
				else if (dy == 0) {
					y0 = yBorder;
					y1 = ySize - yBorder;
				}
				else {
					y0 = yBorder;
					y1 = 2*yBorder;
				}
				
				double[][] border = new double[x1 - x0][y1 - y0];
				for (int x = x0; x < x1; x++)
					for (int y = y0; y < y1; y++)
						border[x - x0][y - y0] = data[x][y];
				
				borderData[i] = border;
			}
		}
		
		
		public void loadBorderData(Grid[] grids) {
			// Switch signs
			dx = -dx;
			dy = -dy;
			
			if (grids == null || grids.length != borderData.length) {
				throw new Error("Illegal argument grids");
			}
			
			for (int i = 0; i < grids.length; i++) {
				double[][] data = grids[i].getData();
				int xBorder = grids[i].xBorder;
				int yBorder = grids[i].yBorder;
				int xSize = grids[i].xSize;
				int ySize = grids[i].ySize;
				
				int x0, x1, y0, y1;

				if (dx > 0) {
					x0 = xSize - xBorder;
					x1 = xSize;
				}
				else if (dx == 0) {
					x0 = xBorder;
					x1 = xSize - xBorder;
				}
				else {
					x0 = 0;
					x1 = xBorder;
				}
				
				if (dy > 0) {
					y0 = ySize - yBorder;
					y1 = ySize;
				}
				else if (dy == 0) {
					y0 = yBorder;
					y1 = ySize - yBorder;
				}
				else {
					y0 = 0;
					y1 = yBorder;
				}
				
				double[][] border = borderData[i];
				for (int x = x0; x < x1; x++)
					for (int y = y0; y < y1; y++)
						data[x][y] = border[x - x0][y - y0];
			}
			
		}
	}
	
	/**
	 * Synchronizes grid borders
	 */
	public void synchronizeBorders() {
		logger.debug("begin border synchronization");
		GlobalSpace globalSpace = ClusterManager.getInstance().getGlobalSpace();
		
		Comm comm = ClusterManager.getInstance().getComm();
		ArrayList<Thread> threads1 = new ArrayList<Thread>(8);
		ArrayList<Thread> threads2 = new ArrayList<Thread>(8);
		
//		ArrayList<ClusterManager.DataCommunicator<GridBorderData>> borders = 
//			new ArrayList<ClusterManager.DataCommunicator<GridBorderData>>(8);
		ArrayList<GridReceiver> receivers = new ArrayList<GridReceiver>(8);

		int myRank = comm.rank();
		
		int i0 = -1, i1 = 1;
		int j0 = -1, j1 = 1;

		// TODO: is there a better solution?
		if (globalSpace.getXChunkNumber() == 1)
			i0 = i1 = 0;
		
		if (globalSpace.getYChunkNumber() == 1)
			j0 = j1 = 0;
		
		for (int i = i0; i <= i1; i++) {
			for (int j = j0; j <= j1; j++) {
				int neighbor = globalSpace.getNeighborRank(i, j);

				
				if (neighbor == myRank || neighbor == -1)
					continue;

//				System.out.println("neighbor at (" + i + ", " + j + ") has rank " + neighbor);
				
				GridBorderData borderData = new GridBorderData(grids, i, j);
//				borders.add(new ClusterManager.DataCommunicator<GridBorderData>(
//						comm, neighbor, borderData, (j + 1) * 3 + i + 1, (1 - j) * 3 - i + 1));
				
				// FIXME: do not be afraid of long data type names
//				Thread thread = new Thread(borders.get(borders.size() - 1));
//				threads.add(thread);
//				thread.start();
				
				GridSender sender = new GridSender(comm, neighbor, 
						borderData, ClusterManager.sendTag(i, j) | ClusterManager.GRID_DATA);
				GridReceiver receiver = new GridReceiver(comm, neighbor, 
						ClusterManager.recvTag(i, j) | ClusterManager.GRID_DATA);

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
				threads1.get(i).join();
				threads2.get(i).join();
				receivers.get(i).getReceivedData().loadBorderData(grids);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		logger.debug("end border synchronization");
	}
	
	
	
	
	static class GridSender implements Runnable {
		private int dstRank;
		private int tag;
		private Comm comm;
		private ObjectBuf<GridBorderData> buffer;
		
		
		public GridSender(Comm comm, int dstRank, GridBorderData data, int tag) {
			this.comm = comm;
			this.dstRank = dstRank;
			this.tag = tag;
			
			this.buffer = ObjectBuf.buffer(data);
		}
		
		public void run() {
			try {
//				System.out.println("Send/receive: myRank" + myRank + "; " + "dstRank: " + dstRank);
				comm.send(dstRank, tag, buffer);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}



	static class GridReceiver implements Runnable {
		private int dstRank;
		private int tag;
		private Comm comm;
		private ObjectBuf<GridBorderData> receiveBuffer;
		
		public GridBorderData getReceivedData() {
			return receiveBuffer.get(0);
		}
		
		public GridReceiver(Comm comm, int dstRank, int tag) {
			this.comm = comm;
			this.dstRank = dstRank;
			this.tag = tag;
			
			this.receiveBuffer = ObjectBuf.buffer();
		}
		
		public void run() {
			try {
//				System.out.println("Send/receive: myRank" + myRank + "; " + "dstRank: " + dstRank);
				comm.receive(dstRank, tag, receiveBuffer);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
