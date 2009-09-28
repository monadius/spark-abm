/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package org.spark.data;

import java.io.IOException;

import org.spark.cluster.ClusterManager;
import org.spark.core.Observer;
import org.spark.space.BoundedSpace;
import org.spark.utils.Parallel2dDoubleArray;

import org.spark.cluster.*;

import jsr166y.forkjoin.Ops;
import jsr166y.forkjoin.ParallelArray;


/**
 * @author max
 *
 */
public class ClusterGrid extends ParallelGrid
{	
	//for MPI Communication
	protected final Comm comm;
	protected int Slaves;
	protected int myRank;
	protected int[] NeighborRanks;
	/**
	 * array of messages for communication between slaves
	 */
	protected InsideGridCommunicator[] stepPackage;
	/**
	 * All slaves will have true.
	 * if it is not cluster version that isSlave=true
	 */
	protected final boolean isSlave;
	protected boolean isSyncronized;
	protected boolean useCluster;
	protected int ReportFrequency;
	protected int TicksFromLastCollection;	
	protected static int IDCounter = 0;
	
	/**
	 * Used for Tag creating in MPI 
	 */
	protected int DataLayerID;
	
	/**
	 * here defined information which data should be analized on this Slave, 
	 * For border area contain information on Rank slave which should receive this information
	 */
	protected Parallel2dDoubleArray	Filter;
	
	//for Grid
	protected int BeginX, BeginY, SlaveSizeX, SlaveSizeY;
	/**
	 * Each element of array contain information which is necessary 
	 * for creating Grid on one Slave
	 */
	protected GridHelper[] SlavesGrid;
	protected int[][] SlavesMap;
		
	/**
	 * How much frame for transferring between slaves
	 */
	protected int frame=0;
	protected int XFrame=0;
	protected int YFrame=0;	
	
	/**
	 * Creates the xSize by ySize grid
	 * @param xSize
	 * @param ySize
	 */
	public ClusterGrid(int xSize, int ySize) {
		this((BoundedSpace) Observer.getSpace(), xSize, ySize, null, 10, 0);
	}

	public ClusterGrid(BoundedSpace space,	int xSize, int ySize) {
		this(space, xSize, ySize, null, 10, 0);
	}

	
	public ClusterGrid(int xSize, int ySize, DataLayerStep step) {
		this((BoundedSpace) Observer.getSpace(), xSize, ySize, step, 10, 0);
	}
	
	
	public ClusterGrid(BoundedSpace space, 
			int xSize, int ySize, 
			DataLayerStep step, int ColFreq, int GridFrame) 
	{
		super((BoundedSpace) Observer.getSpace(), xSize, ySize, step);
		isSyncronized =false;
		comm = ClusterManager.getInstance().getComm();
		isSlave = ClusterManager.getInstance().isSlave();
		DataLayerID=IDCounter;
		IDCounter++;	
		
		if (comm!= null)
		{			
			//TODO this method should be executed in background;
			gridConstructorForCluster(xSize, ySize, ColFreq, GridFrame);
		}
		else 
		{			
			useCluster = false;
			isSyncronized =true;
		}		
	}

	/**
	 * Create on Cluster. Should be executed in background.
	 * @param xSize
	 * @param ySize
	 * @param ColFreq - frequency of collection full data on master.
	 * @param GridFrame - frame minimal data for sending between Slaves
	 */	
	private void gridConstructorForCluster(int xSize, int ySize, int ColFreq,
			int GridFrame) 
	{
		Slaves = comm.size()-1;
		myRank = comm.rank();
		useCluster = true;
		
		frame = GridFrame;
		TicksFromLastCollection = 0;
		ReportFrequency = ColFreq;
		if (!isSlave)
		{
			createGridOnMaster(xSize, ySize, GridFrame);
		}
		else
		{
			createGridOnSlaves();
		}
		//isSyncronized =true;
	}

	private void createGridOnSlaves() {
		//write method for receiving Grid info
		ObjectBuf<GridHelper>[] Empty = new ObjectBuf[Slaves+1];
		int tag = 1000000+ DataLayerID;
		ObjectBuf<GridHelper> temp=null;
		try {
			comm.scatter(0, tag, Empty, temp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		GridHelper Received = (GridHelper)temp.get(0);
		//Create Grid based on info from master
		
		this.BeginX = Received.SlaveBeginX;
		this.BeginY = Received.SlaveBeginY;
		this.SlaveSizeX = Received.SlaveSizeX;
		this.SlaveSizeY = Received.SlaveSizeY;
		this.frame = Received.frame;
		this.NeighborRanks = Received.NeighborRanks;
		this.ReportFrequency = Received.ReportFrequency;
		
		XFrame = Received.XFrame;
		YFrame = Received.YFrame;
		//if (NeighborRanks[3]==NeighborRanks[4]) XFrame=0;
		//if (NeighborRanks[1]==NeighborRanks[7]) YFrame=0;
					
		data = Parallel2dDoubleArray.CreateFromDouble(Received.data, 
				SlaveSizeX+2*XFrame , BeginY+2*YFrame);
					
		//Create Filter array for calculation data
		final int SXFrame=XFrame;
		final int SYFrame=YFrame;
		final int SSizeX= SlaveSizeX;
		final int SSizeY= SlaveSizeY;
		Filter = Parallel2dDoubleArray.createEmpty(SlaveSizeX+2*XFrame , BeginY+2*YFrame);
		Filter.replaceWithMappedIndex(new Ops.IntToDouble()
		{
			public double op(int index) 
			{
				int x = Filter.X(index);
				int y = Filter.Y(index);
				if (x<SXFrame) return -1;
				if (y<SYFrame) return -1;
				if (x>SSizeX+SXFrame) return -1;
				if (y>SSizeY+SYFrame) return -1;
				
				if ((x<2*SXFrame)&&(y<2*SYFrame)) return 0;
				if ((x<2*SXFrame)&&(y>SSizeY)) return 6;
				if ((x>SSizeX)&&(y<2*SYFrame)) return 2;
				if ((x>SSizeX)&&(y>SSizeY)) return 8;				
				
				if (x<2*SXFrame) return 3;
				if (y<2*SYFrame) return 1;
				if (x>SSizeX) return 5;
				if (y>SSizeY) return 7;
				
				return 4;
			}});
		
		stepPackage = new InsideGridCommunicator[9];				
		for (int j=0; j<10; j++)
		{
			if ((j==0)||(j==2)||(j==6)||(j==8))
				stepPackage[j]=new InsideGridCommunicator(XFrame, YFrame, myRank, j, NeighborRanks[j]);					
			if ((j==1)||(j==7))
				stepPackage[j]=new InsideGridCommunicator(SlaveSizeX, YFrame, myRank, j, NeighborRanks[j]);
			if ((j==3)||(j==5))
				stepPackage[j]=new InsideGridCommunicator(XFrame, SlaveSizeY, myRank, j, NeighborRanks[j]);
		}			
		isSyncronized =true;
	}

	private void createGridOnMaster(int xSize, int ySize, int GridFrame) {
		SlavesGrid = new GridHelper[Slaves+1];
		
		//TODO optimize Divide slaves on 2d
		int xSlaves = (int)java.lang.Math.sqrt(Slaves);
		int ySlaves = (int)Slaves/xSlaves;
		while ((Slaves-xSlaves*ySlaves)>0)
		{
			ySlaves--;
			xSlaves = (int)Slaves/ySlaves;
		}
		int xSlStep = xSize/xSlaves;
		int ySlStep = ySize/ySlaves;
		
		SlavesMap = new int[xSlaves][ySlaves];
		//Create information for creating grid on slaves
		for (int i = 1; i<Slaves+1; i++)
		{
			int x = i- ((int)i/xSlaves)*xSlaves;
			int y = i/xSlaves;
			SlavesMap[x][y]=i;
			SlavesGrid[i].SlaveBeginX = x*xSlStep;
			SlavesGrid[i].SlaveBeginY = y*ySlStep;
			SlavesGrid[i].SlaveSizeX = xSlStep;
			SlavesGrid[i].SlaveSizeY = ySlStep;
			SlavesGrid[i].ReportFrequency = ReportFrequency;
			//SlavesGrid[i].DatalayerID = DataLayerID;
			
			if (x==xSlaves-1) SlavesGrid[i].SlaveSizeX = xSize-SlavesGrid[i].SlaveBeginX;
			if (y==ySlaves-1) SlavesGrid[i].SlaveSizeY = ySize-SlavesGrid[i].SlaveBeginY;				
		}
		
		XFrame=GridFrame;
		YFrame=GridFrame;
		
		//Create information about neighbors for slaves.
		for (int i = 1; i<Slaves+1; i++)
		{				
			int x = i- ((int)i/xSlaves)*xSlaves;
			int y = i/xSlaves;
			int index = 0;
			for (int j = -1; j<=1; j++)
				for(int k= -1; k<=1; k++)
				{
					int xx = x +j;
					if (xx>=xSlaves) xx-=xSlaves;
					if (xx< 0) xx+=xSlaves;
					
					int yy = y+k;
					if (yy>=ySlaves) yy-=ySlaves;
					if (yy< 0) yy+=ySlaves;
					
					SlavesGrid[i].NeighborRanks[index]= SlavesMap[xx][yy];
					index++;
				}		
			
			
			if (xSlaves==0)XFrame = 0;
			if(ySlaves==0) YFrame=0;
			SlavesGrid[i].XFrame = XFrame;
			SlavesGrid[i].YFrame= YFrame;
			
			SlavesGrid[i].data = new double[(SlavesGrid[i].SlaveSizeX+2*XFrame)
			                                *(SlavesGrid[i].SlaveSizeY+2*YFrame)];
			SlavesGrid[i].frame = frame;
		}
		
		prepareOriginalDataToSendtoSlaves(xSize, ySize, xSlaves, ySlaves);
		
		int tag =1000000+ DataLayerID;
		SlavesGrid[0] = new GridHelper();
		
		ObjectBuf<GridHelper>[] ForSend = new ObjectBuf[Slaves+1];
		
		for (int j = 0; j<Slaves+1; j++)
			ForSend[j]=ObjectBuf.buffer(SlavesGrid[j]);
		ObjectBuf<GridHelper> temp = null;
		// TODO: asynchronous call
		initSlaves(tag, ForSend, temp);
	}

	private void initSlaves(int tag, ObjectBuf<GridHelper>[] ForSend,
			ObjectBuf<GridHelper> temp) {
		try {
			comm.scatter(0, tag,  ForSend, temp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		isSyncronized =true;
	}

	
	/** Divide initial data into chunks for sending to slaves
	 * @param xSize
	 * @param ySize
	 * @param xSlaves
	 * @param ySlaves
	 */
	private void prepareOriginalDataToSendtoSlaves(int xSize, int ySize,
			int xSlaves, int ySlaves) {
		//fill SlavesGrid[i].data with specific for this slave data from total datalayer
		final int FframeX=XFrame;
		final int FframeY=YFrame;
		final int FxSlaves= xSlaves;
		final int FySlaves= ySlaves;
		final int FxSize = xSize;
		final int FySize= ySize;
		data.withIndexedMapping(new Ops.IntAndDoubleToDouble()
		{
			public double op(int index, double value) 
			{
				int X = data.X(index);
				int Y = data.Y(index);
				
				//find Slaves which working with this space
				int xslave = 0;
				while ((SlavesGrid[SlavesMap[xslave][0]].SlaveBeginX
						+SlavesGrid[SlavesMap[xslave][0]].SlaveSizeX+FframeX)<X) xslave++;
				
				int yslave = 0;
				while ((SlavesGrid[SlavesMap[xslave][yslave]].SlaveBeginY
						+SlavesGrid[SlavesMap[xslave][yslave]].SlaveSizeY+FframeY)<Y) yslave++;
				
				int newX = X - SlavesGrid[SlavesMap[xslave][yslave]].SlaveBeginX+FframeX;
				int newY = Y - SlavesGrid[SlavesMap[xslave][yslave]].SlaveBeginY+FframeY;
				
				int newIndex = 
					newY*(SlavesGrid[SlavesMap[xslave][yslave]].SlaveSizeX+2*FframeX)+newX;
				SlavesGrid[SlavesMap[xslave][yslave]].data[newIndex]= value;			
				
				int NextXSlave = xslave+1;
				if (NextXSlave>FxSlaves) NextXSlave= 0;
				
				int NextYSlave = yslave+1;
				if (NextYSlave>FySlaves) NextYSlave= 0;
				
				int NextX2=0;
				int SlaveBeginX2 = SlavesGrid[SlavesMap[NextXSlave][yslave]].SlaveBeginX-FframeX;
				if (SlavesGrid[SlavesMap[NextXSlave][yslave]].SlaveBeginX==0)
					SlaveBeginX2 = FxSize-FframeX;
				
				if (X>SlaveBeginX2)
				{
					NextX2 = X - SlaveBeginX2;
					int ind = newY*(SlavesGrid[SlavesMap[NextXSlave][yslave]].SlaveSizeX+2*FframeX)+NextX2;
					SlavesGrid[SlavesMap[NextXSlave][yslave]].data[ind]= value;
				}
				
				int NextY2=0;
				int SlaveBeginY2=SlavesGrid[SlavesMap[xslave][NextYSlave]].SlaveBeginY-FframeY;
				if (SlavesGrid[SlavesMap[xslave][NextYSlave]].SlaveBeginY==0)
					SlaveBeginY2=FySize-FframeY;
				
				if (Y>SlaveBeginY2)
				{
					NextY2 = Y - SlaveBeginY2;
					int ind = NextY2*(SlavesGrid[SlavesMap[xslave][NextYSlave]].SlaveSizeX+2*FframeX)+newX;
					SlavesGrid[SlavesMap[xslave][NextYSlave]].data[ind]= value;
				}
				
				if ((Y>SlaveBeginY2)&&(X>SlaveBeginX2))
				{
					int ind = NextY2*(SlavesGrid[SlavesMap[NextXSlave][NextYSlave]].SlaveSizeX+2*FframeX)+NextX2;
					SlavesGrid[SlavesMap[NextXSlave][NextYSlave]].data[ind]= value;	
				}
				return value;
			}					
		});
	}

	//************************************
	// AdvancedDataLayer interface implementation
	//************************************
		
	/* executed each tick (non-Javadoc)
	 * @see org.spark.data.ParallelGrid#process(long)
	 */
	public void process(final long tick) 
	{
		// TODO synchronization
		//while (!isSynchronized) sleep(10);
		isSyncronized =false;
		if (isSlave) process_Slave(tick); //if it is not cluster version that isSlave=true
		else process_Master(tick);
	}

	
	/**
	 * this Method will be executed only on Master (in cluster mode) each tick
	 * @param tick
	 */
	private void process_Master(long tick) 
	{
		TicksFromLastCollection++;
		if (TicksFromLastCollection >= ReportFrequency)
		{
			final int tag = tickDatalayerTag(tick);
			//receive reports from slaves
			GridScreenShot[] ReceivedGrid = new GridScreenShot[Slaves];			
			ParallelArray.createFromCopy(ReceivedGrid, ParallelArray.defaultExecutor())
			.withIndexedMapping(new Ops.IntAndObjectToObject<GridScreenShot, GridScreenShot>()
					{
				public GridScreenShot op(int index, GridScreenShot Report) 
				{
				
					ObjectItemBuf<GridScreenShot> ReceivBuffer = ObjectBuf
											.buffer(Report);
									try {
										comm.receive(null, tag, ReceivBuffer);
									} catch (IOException e) {
										e.printStackTrace();
									}
									Report = ReceivBuffer.get(0);
									
									enterReceivedData(Report.data, Report.BeginX, Report.BeginY,
											Report.SizeX, Report.SizeY);						
					return null;
				}				
				/**
				 * enter received data to global Data (in datalayer).
				 * @param ReceivedData double[] which was received and should be added to datalayer
				 * @param BeginX - Starting X coordinate in datalayer
				 * @param BeginY - Starting Y coordinate in datalayer
				 * @param SizeX - Size received data array (ReceivedData)
				 * @param SizeY - Size received data array (ReceivedData)
				 */
				private void enterReceivedData(double[] ReceivedData,
						final int BeginX, final int BeginY, final int SizeX,
						final int SizeY) 
				{
					final Parallel2dDoubleArray P2dDA =
						Parallel2dDoubleArray.createFromCopy(ReceivedData, SizeX, SizeY);
					
					P2dDA.withIndexedMapping(new Ops.IntAndDoubleToDouble() 
									{
										public double op(int pos, double value) 
										{
											int x = P2dDA.X(pos);
											int y = P2dDA.Y(pos);
											data.SetDoubleAt(BeginX+x, BeginY+y, value);
											return value;
										}
									}).all();
				}
				}).all();
		}		
		isSyncronized =true;
	}

	/**
	 * This method will be executed on Slave each tick. 
	 * @param tick
	 */
	private void process_Slave(final long tick) 
	{
		if (!useCluster) 
		{
			if (stepOperation != null) 
			{
				data = new Parallel2dDoubleArray(data, data.withIndexedMapping(
						new Ops.IntAndDoubleToDouble() {
							public double op(final int i, final double element_i) {
								int x = data.X(i);
								int y = data.Y(i);
								return OurStep(tick, x, y, element_i);
							}
						}).all());
			}
			isSyncronized =true;
		} 
		else			
		{
			if (stepOperation != null) 
			{				
				data = new Parallel2dDoubleArray(data, data.withIndexedMapping(
						new Ops.IntAndDoubleToDouble() {
							public double op(final int i, final double element_i) {
								double mask = Filter.get(i);
								if (mask < 0)
									return element_i;

								int x = data.X(i)  - XFrame;
								int y = data.Y(i)  - YFrame;

								// Change Coordinate calculation for slaves to
								// global coordinates
								double result = OurStep(tick, x + BeginX, y + BeginY, element_i);

								if (mask == 0)//0, 3, 1
								{
									stepPackage[0].data[stepPackage[0].Pos(x, y)] = result;
									stepPackage[3].data[stepPackage[3].Pos(x, y)] = result;
									stepPackage[1].data[stepPackage[1].Pos(x, y)] = result;
								}

								if (mask == 3)
								{
									stepPackage[3].data[stepPackage[3].Pos(x, y)] = result;									
								}
								
								if (mask == 1)
								{
									stepPackage[1].data[stepPackage[1].Pos(x, y)] = result;									
								}
								
								if (mask == 7)
								{
									stepPackage[7].data[stepPackage[7].Pos(x, y-SlaveSizeY)] = result;									
								}
								
								if (mask == 5)
								{
									stepPackage[5].data[stepPackage[5].Pos(x-SlaveSizeX, y)] = result;									
								}
								
								if (mask == 2)//2, 1, 5
								{
									stepPackage[2].data[stepPackage[2].Pos(x-SlaveSizeX, y)] = result;	
									stepPackage[5].data[stepPackage[5].Pos(x-SlaveSizeX, y)] = result;
									stepPackage[1].data[stepPackage[1].Pos(x, y)] = result;
								}
								
								if (mask == 8)//5,7,8
								{
									stepPackage[5].data[stepPackage[5].Pos(x-SlaveSizeX, y)] = result;
									stepPackage[7].data[stepPackage[7].Pos(x, y-SlaveSizeY)] = result;
									stepPackage[8].data[stepPackage[8].Pos(x-SlaveSizeX, y-SlaveSizeY)] = result;
								}
								
								if (mask == 6)//3, 6, 7
								{
									stepPackage[7].data[stepPackage[7].Pos(x, y-SlaveSizeY)] = result;
									stepPackage[3].data[stepPackage[3].Pos(x, y)] = result;
									stepPackage[6].data[stepPackage[6].Pos(x, y-SlaveSizeY)] = result;
								}								
								return result;
							}
						}).all());
				
				//TODO Make it in background
				afterStepCommunication(tick);
			}			
		}		
	}

	/**
	 * this method executed if model work in cluster mode. 
	 * Perform all communication between neighbors and send report to master 
	 * @param tick
	 */
	private void afterStepCommunication(final long tick) {
		// Communication between Slaves in parallel mode
		CommRequest[] SenderHandles = new CommRequest[9];
		CommRequest[] ReceiversHandles = new CommRequest[9];
		// TODO optimize conversion from (long)tick to int
		final int tag = tickDatalayerTag(tick);

		ObjectBuf<InsideGridCommunicator>[] RecBuf = new ObjectBuf[9];

		sendGridReportToMaster(tick, SenderHandles, tag);
		final CommRequest MasterSender = SenderHandles[4];

		ParallelArray
				.createFromCopy(RecBuf, ParallelArray.defaultExecutor())
				.withIndexedMapping(
						new Ops.IntAndObjectToObject<ObjectBuf<InsideGridCommunicator>, ObjectBuf<InsideGridCommunicator>>() 
						{
							public ObjectBuf<InsideGridCommunicator> op(
									int i, ObjectBuf<InsideGridCommunicator> ReceivBuffer) 
									{
								if (i != 4) {
									
									//Check on absent neighbor. Avoid send to self.
									if (stepPackage[i].ReceiverRank==myRank)return null;
									
									ReceivBuffer = ObjectBuf
											.buffer(stepPackage[i]);
									CommRequest Receiver = null;

									CommRequest Sender = null;
									ObjectItemBuf<InsideGridCommunicator> SendBuf = ObjectBuf
											.buffer(stepPackage[i]);

									try {
										// start receiving new data in background
										Receiver = comm.receive(
												stepPackage[i].ReceiverRank,
												tag, ReceivBuffer, Receiver);

										// start sending new data in background
										Sender = comm.send(
												stepPackage[i].ReceiverRank,
												tag, SendBuf, Sender);

										// wait untill will be received new data
										CommStatus NeighMess = Receiver
												.waitForFinish();
										if ((NeighMess==null)||(NeighMess.tag!= tag))
											{
											//TODO print error
											}
										else
										{										
											// add received data to Grid Frame
											final InsideGridCommunicator info = ReceivBuffer.get(0);
											if (info.ReceiverRank!=myRank)
											{
												//TODO Print error													
											}
											else
											{
												int StartX=0;
												int StartY=0;
												//correct starting coordinates depend from SendedMask
												if (info.SendedMask == 0) //0 for sender mean 8 for receiver
												{
													StartX =SlaveSizeX+XFrame;
													StartY =SlaveSizeY+YFrame;														
												}
												if (info.SendedMask == 8) //8 for sender mean 0 for receiver
												{
													StartX =0;
													StartY =0;														
												}
												
												if (info.SendedMask == 1) //1 for sender mean 7 for receiver
												{
													StartX =XFrame;
													StartY = SlaveSizeY+YFrame;														
												}
												
												if (info.SendedMask == 7) //7 for sender mean 1 for receiver
												{
													StartX =XFrame;
													StartY = 0;														
												}
												
												if (info.SendedMask == 6) //6 for sender mean 2 for receiver
												{
													StartX =SlaveSizeX+XFrame;
													StartY =0;														
												}
												
												if (info.SendedMask == 2) //2 for sender mean 6 for receiver
												{
													StartX =0;
													StartY =SlaveSizeY+YFrame;														
												}
												
												if (info.SendedMask == 3) //3 for sender mean 5 for receiver
												{
													StartX =SlaveSizeX+XFrame;
													StartY =YFrame;														
												}
												
												if (info.SendedMask == 5) //5 for sender mean 3 for receiver
												{
													StartX =XFrame;
													StartY =SlaveSizeY+YFrame;														
												}
												
												enterReceivedData(info.data, StartX, StartY,
														info.xSize, info.ySize);
											}											
										}
										// wait until sender will finish
										Sender.waitForFinish();

									} catch (IOException e) {
										e.printStackTrace();
									}
								} else {
									try {
										MasterSender.waitForFinish();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
								return null;
							}
							
							/**
							 * enter received data to global Data (in datalayer).
							 * @param ReceivedData double[] which was received and should be added to datalayer
							 * @param BeginX - Starting X coordinate in datalayer
							 * @param BeginY - Starting Y coordinate in datalayer
							 * @param SizeX - Size received data array (ReceivedData)
							 * @param SizeY - Size received data array (ReceivedData)
							 */
							private void enterReceivedData(double[] ReceivedData,
									final int BeginX, final int BeginY, final int SizeX,
									final int SizeY) 
							{
								final Parallel2dDoubleArray P2dDA =
									Parallel2dDoubleArray.createFromCopy(ReceivedData, SizeX, SizeY);
								
								P2dDA.withIndexedMapping(new Ops.IntAndDoubleToDouble() 
												{
													public double op(int pos, double value) 
													{
														int x = P2dDA.X(pos);
														int y = P2dDA.Y(pos);
														data.SetDoubleAt(BeginX+x, BeginY+y, value);
														return value;
													}
												}).all();
							}
						}).all();
		isSyncronized = true;
	}	

	/**
	 * Send information about grid on current step to master
	 * @param tick
	 * @param SenderHandles
	 * @param taq
	 */
	private void sendGridReportToMaster(final long tick,
			CommRequest[] SenderHandles, int taq) {
		TicksFromLastCollection++;
		if (TicksFromLastCollection >= ReportFrequency) 
		{
			//Send data to Master
			GridScreenShot Report = new GridScreenShot();
			Report.Tick = tick;
			Report.SenderRank = myRank;
			Report.BeginX = BeginX;
			Report.BeginY =BeginY;
			Report.SizeX=SlaveSizeX;
			Report.SizeY = SlaveSizeY;
			Report.data = data.withFilter(new Ops.BinaryDoublePredicate()
			{
				public boolean op(double value, double filter) 
				{
					if (filter<0) return false;
					return true;
				}}, Filter).all().getArray();
		
			ObjectItemBuf<GridScreenShot> buf = ObjectBuf.buffer(Report);
			try 
			{
				SenderHandles[4]= comm.send(0, taq, buf, SenderHandles[4]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			TicksFromLastCollection = 0;
		}
		
		else SenderHandles[4] = null;
	}

	/**
	 * Create Tag specific for datalayer and tick
	 * @param tick
	 * @return
	 */
	private int tickDatalayerTag(final long tick) {
		int taq=(int)(tick%10000)*1000+DataLayerID;
		return taq;
	}
	
	/**
	 * Class for communication between slaves
	 * @author max
	 */
	public class InsideGridCommunicator
	{
		
		/**
		 * Class for communication between slaves
		 * @param SizeX  - Size of space by X
		 * @param SizeY  - Size of space by Y
		 * @param Rank	- Rank of computer which send this data
		 * @param mask	- Using this parameter possible to determine place this neighbor or where should be put data
		 * @param NeighborRank - Rank of computer which should receive this data
		 */
		public InsideGridCommunicator(int SizeX, int  SizeY, int Rank, int mask, int NeighborRank)
		{
			xSize = SizeX;
			ySize= SizeY;
			data = new double[xSize*ySize];
			
			SenderRank=Rank;
			SendedMask = mask;
			ReceiverRank = NeighborRank;
		}
		public int SenderRank = -1;
		public int SendedMask = -10000;
		public double[] data;
		public int ReceiverRank=-1;
		public int xSize;
		public int ySize;
		public int Pos(int x, int y)
		{
			return y*xSize+x;
		}
	}
	
	/**
	 * Contain information which is necessary for creating Grid on Slave
	 * @author max	 
	 */
	public class GridHelper
	{
		public int SlaveBeginX=-1, SlaveBeginY=-1, SlaveSizeX=-1, SlaveSizeY=-1;		
		public int frame=0, XFrame = 0, YFrame=0;
		/**
		 * Contain information about neighbors. Pattern of neighbors:
		 * 0		1		2;
		 * 3		same	5;
		 * 6		7		8;
		 * will be in next row:
		 * 0	1	2	3	Same	5	6	7	8;
		 */
		public int[] NeighborRanks = new int[9];
		public int ReportFrequency = 1;
		//public int DatalayerID=-1;
		
		/**
		 * Will be used for creating slaves from saved data		 * 
		 */
		public double[] data;
	}
	
	public class GridScreenShot
	{
		public int SenderRank;
		public long Tick;
		public double[] data;
		public int BeginX;
		public int BeginY;
		public int SizeX;
		public int SizeY;
	}
}
