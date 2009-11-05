package org.spark.cluster;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import org.spark.core.Agent;
import org.spark.core.Observer;
import org.spark.data.DataLayer;
import org.spark.data.GridCommunicator;
import org.spark.space.BoundedSpace;
import org.spark.space.GlobalSpace;
import org.spark.space.SpaceAgent;
import org.spark.core.SparkModel;
import org.w3c.dom.Document;

import com.spinn3r.log5j.Logger;


public class ClusterManager {
	private static ClusterManager instance;
	private static AutoShutdown autoShutdown;
	private static final Logger logger = Logger.getLogger();
	
	protected Comm comm;
	protected boolean isSlave;
	
	protected GlobalSpace globalSpace;
	protected GridCommunicator gridCommunicator;
	
	//  Used for tag modification
	private int tick;
	
	
	/**
	 *  Tag's codes 
	 */
	/* Destination send codes */
	/* They are arranged in the pattern:
	 * 1 2 3
	 * 4 5 6
	 * 7 8 9
	 */
	public static final int SEND_NW		= 0x01;
	public static final int SEND_NORTH	= 0x02;
	public static final int SEND_NE		= 0x03;
	public static final int SEND_WEST	= 0x04;
	public static final int SEND_MYSELF	= 0x05;
	public static final int SEND_EAST	= 0x06;
	public static final int SEND_SW		= 0x07;
	public static final int SEND_SOUTH	= 0x08;
	public static final int SEND_SE		= 0x09;
	public static final int SEND_MASTER	= 0x0F;

	/* Destination receive codes */
	/* These codes are opposite to send codes to match tags:
	 * 9 8 7
	 * 6 5 4
	 * 3 2 1
	 */
	public static final int RECV_NW		= 0x09;
	public static final int RECV_NORTH	= 0x08;
	public static final int RECV_NE		= 0x07;
	public static final int RECV_WEST	= 0x06;
	public static final int RECV_MYSELF = 0x05;
	public static final int RECV_EAST	= 0x04;
	public static final int RECV_SW		= 0x03;
	public static final int RECV_SOUTH	= 0x02;
	public static final int RECV_SE		= 0x01;
	public static final int RECV_MASTER	= 0x0F;

	/* Data type codes */
	public static final int GRID_DATA	= 0x10;
	public static final int AGENT_DATA	= 0x20;
	public static final int SETUP_DATA	= 0x30;
	public static final int CMD_DATA	= 0x40;
	public static final int OTHER_DATA	= 0xF0;
	
	
	/**
	 * Computes the send tag
	 * @param dx should be between -1 and 1
	 * @param dy should be between -1 and 1
	 * @return
	 */
	public static int sendTag(int dx, int dy) {
		return (dy + 1) * 3 + (dx + 1) + 1;
	}

	
	/**
	 * Computes the receive tag
	 * @param dx should be between -1 and 1
	 * @param dy should be between -1 and 1
	 * @return
	 */
	public static int recvTag(int dx, int dy) {
		return (1 - dy) * 3 + (1 - dx) + 1;
	}
	
	
	/**
	 * Modifies tag in accordance with internal time
	 * @param tag
	 * @return
	 */
	protected int modifyTag(int tag) {
		return tag | (tick << 8);
	}
	
	
	/**
	 * Adds 1 to the internal time.
	 * Should be called by all cluster nodes.
	 * Now it is called from ClusterCommand.execute()
	 */
	protected void advanceTick() {
		tick++;
	}

	
	
	public Comm getComm() {
		return comm;
	}
	
	
	public GlobalSpace getGlobalSpace() {
		return globalSpace;
	}
	
	
	public GridCommunicator getGridCommunicator() {
		return gridCommunicator;
	}
	
	
	public static ClusterManager getInstance() {
		return instance;
	}
	
	
	public static AutoShutdown getAutoShutdown() {
		return autoShutdown;
	}
	
	
	public static void init(String[] args) throws Exception {
		if ((instance != null) && (instance.comm.rank()==0)) {
			// TODO: shut down slaves
			throw new Exception("Cluster manager is already initialized");
		}
		
		instance = new ClusterManager(args);
		autoShutdown = new AutoShutdown();
	}
	
	
	private ClusterManager(String[] args) throws IOException {
		logger.info("Initializing Comm");
		
		Comm.init(args);
		comm = Comm.world();
		
		// TODO: start up slaves
		if (comm.rank() == 0) {
			isSlave = false;
			// TODO: send xml-document
			// comm.broadcast(0, tag, buf);
		}
		else {
			isSlave = true;
			// TODO: get all model data files (.xml and .class)
			// xml file can be transferred as an xml document class
			
			// comm.broadcast(0, tag, buf);
		}
	}
	

	/**
	 * Returns true if the process is on a slave machine
	 * @return
	 */
	public boolean isSlave() {
		return isSlave;
	}

	
	// TODO: think about better solution
	private Document xmlDoc = null;
	
	/**
	 * Sends the model description xml-document to slaves
	 * @throws Exception
	 */
	public void sendModelDescription(Document xmlDoc) throws Exception {
		this.xmlDoc = xmlDoc;
		
		if (isSlave)
			throw new Error("Only master can send initialization commands");

		logger.info("Sending xml document...");
		
		ObjectBuf<Document> xmlDocBuffer = ObjectBuf.buffer(xmlDoc);
		comm.broadcast(comm.rank(), SETUP_DATA | SEND_MASTER, xmlDocBuffer);
		
		logger.info("sending xml document: complete");
	}

	/**
	 * Receives the xml-document of a model
	 * @return
	 */
	public Document receiveModelDescription() throws Exception {
		if (!isSlave) {
			if (xmlDoc != null)
				return xmlDoc;
		}
		
		logger.info("Receiving xml document...");
		
		// receive the space information
		ObjectBuf<Document> xmlDocBuffer = ObjectBuf.buffer();
		comm.broadcast(0, SETUP_DATA | RECV_MASTER, xmlDocBuffer);
		
		logger.info("xml document is received");
		
		return xmlDocBuffer.get(0);
	}

	/**
	 * Waits initialization commands from master
	 * Works only on slaves
	 * @throws IOException 
	 */
	public void waitInitCommands(SparkModel model) throws Exception {
		if (!isSlave) {
//			throw new Exception("Master cannot wait for initialization commands");
			return;
		}

		/* Space */
		logger.info("Receiving global space...");
		
		// receive the space information
		ObjectBuf<GlobalSpace> globalSpaceBuffer = ObjectBuf.buffer();
		comm.broadcast(0, SETUP_DATA | RECV_MASTER, globalSpaceBuffer);
		
		logger.info("Global space is received");
		
		// create a local space
		globalSpace = globalSpaceBuffer.get(0);
		globalSpace.createLocalSpace();
		
		/* Data layers */

		ObjectBuf<GridCommunicator> gridBuffer = ObjectBuf.buffer();

		// receiving grids
		logger.info("Receiving grids... (" + comm.rank() + ")");
		comm.scatter(0, GRID_DATA | RECV_MASTER, null, gridBuffer);
		logger.info("receiving grids: complete (" + comm.rank() + ")");

		gridCommunicator = gridBuffer.get(0);
		gridCommunicator.createLocalGrids();
		
		
		/* Agents */
		
		logger.info("Receiving agents...");
		
		// receive the agent information
		ObjectBuf<SpaceAgent[]> agentBuffer = ObjectBuf.buffer();
		comm.scatter(0, AGENT_DATA | RECV_MASTER, null, agentBuffer);
		
		logger.info("Agents are received");
		
		logger.info("Starting synchronization of border agents (" + comm.rank() + ")");
		globalSpace.synchronizeBorders();
		logger.info("Synchronization of border agents: complete (" + comm.rank() + ")");

		/* Global model variables */
		
		ObjectBuf<ModelReplicator> modelBuffer = ObjectBuf.buffer();
		logger.info("Receiving global parameters...");
		comm.broadcast(0, SETUP_DATA | RECV_MASTER, modelBuffer);
		logger.info("Receiving global parameters: complete");
		
		modelBuffer.get(0).loadStaticFields(model);
	}
	
	
	/**
	 * Sends initialization commands to slaves
	 * Works only on a master
	 * Should be called after model.setup()
	 */
	@SuppressWarnings("unchecked")
	public void sendInitCommands(SparkModel model) throws Exception {
		if (isSlave)
			throw new Error("Only master can send initialization commands");

		/* Space */
		
		// TODO: send observer's type and execution mode
		Observer observer = Observer.getInstance();
		globalSpace = new GlobalSpace(comm.size() - 1, (BoundedSpace) Observer.getDefaultSpace());

		logger.info("Sending global space...");
		
		ObjectBuf<GlobalSpace> globalSpaceBuffer = ObjectBuf.buffer(globalSpace);
		comm.broadcast(comm.rank(), SETUP_DATA | SEND_MASTER, globalSpaceBuffer);
		
		logger.info("sending global space: complete");

		
		/* Data layers */
		
		ObjectBuf<GridCommunicator>[] gridBuffers = new ObjectBuf[comm.size()];
		
		// Prepare grid buffers
		for (int i = 0; i < comm.size(); i++) {
			if (i == comm.rank()) {
				// Create empty buffer for the master
				gridBuffers[i] = ObjectBuf.buffer();
				continue;
			}

			GridCommunicator gridCommunicator = new GridCommunicator();
			gridCommunicator.prepareGridsForTransferring(globalSpace, i);
			
			gridBuffers[i] = ObjectBuf.buffer(gridCommunicator);
		}
		
		// Send grid buffers
		logger.info("Sending grids...");
		comm.scatter(comm.rank(), GRID_DATA | SEND_MASTER, gridBuffers, gridBuffers[comm.rank()]);
		logger.info("sending grids: complete");
		
		
		/* Agents */
		
		ArrayList<SpaceAgent>[] agents = new ArrayList[comm.size()];
		for (int i = 0; i < agents.length; i++)
			agents[i] = new ArrayList<SpaceAgent>(1000);
		
		Agent[] all = observer.getAgents();
		
		for (int i = 0; i < all.length; i++) {
			if (all[i] instanceof SpaceAgent) {
				SpaceAgent agent = (SpaceAgent) all[i];
				agent.setDeepSerialization(true);
				agents[globalSpace.getRank(agent.getPosition())].add(agent);
			}
		}

		ObjectBuf<SpaceAgent[]>[] agentBuffers = new ObjectBuf[comm.size()];
		
		for (int i = 0; i < comm.size(); i++) {
			SpaceAgent[] tmp = new SpaceAgent[agents[i].size()];
			agentBuffers[i] = ObjectBuf.objectBuffer(agents[i].toArray(tmp));
		}
		
		logger.info("Sending agents...");
		comm.scatter(comm.rank(), AGENT_DATA | SEND_MASTER, agentBuffers, agentBuffers[comm.rank()]);
		logger.info("sending agents: complete");
		
		/* Global model variables */
		
		ModelReplicator modelReplicator = new ModelReplicator(model);
		ObjectBuf<ModelReplicator> modelBuffer = ObjectBuf.buffer(modelReplicator);
		
		logger.info("Sending global variables...");
		comm.broadcast(comm.rank(), SETUP_DATA | SEND_MASTER, modelBuffer);
		logger.info("sending global variables: complete");
		
	}


	
	/**
	 * Class for replication of static fields of a model.
	 * The main purpose of this class is to set up global static variables
	 * in the models on slaves.
	 * Should be used after initialization of all data layers.
	 * @author Monad
	 */
	static class ModelReplicator implements Serializable {
		// Default serial id
		private static final long serialVersionUID = 1L;

		// Class representing a (static) field in a model class
		static class ModelField implements Serializable {
			private static final long serialVersionUID = 1L;
			// Name of the field
			public String	name;
			// Value of the field
			// In the case of a primitive data type, an actual value is stored here
			// In the case of a data layer, its name is stored here
			public Object	value;
			// Field type
			public int		type;
			
			public final static int PRIMITIVE_TYPE = 1;
			public final static int DATA_LAYER = 2;

			/**
			 * Saves the information about the field
			 * @param field
			 * @param model
			 */
			public void saveValue(Field field, SparkModel model) throws Exception {
				name = field.getName();
				
				Object val = field.get(model);
				if (field.getType().isPrimitive()) {
					value = val;
					type = PRIMITIVE_TYPE; 
				}
				else if (val != null && val instanceof DataLayer) {
					value = getDataLayerName((DataLayer)val);
					type = DATA_LAYER;
				}
				else {
					throw new Exception("Unsupported field type: " + field.toString());
				}
			}
			
			/**
			 * Loads the field value into the model
			 * @param model
			 * @throws Exception
			 */
			public void loadValue(SparkModel model) throws Exception {
				Field field = model.getClass().getField(name);

				switch (type) {
				case PRIMITIVE_TYPE:
					field.set(model, value);
					break;
					
				case DATA_LAYER:
					DataLayer dataLayer = Observer.getInstance().getDataLayer((String) value); 
					field.set(model, dataLayer);
					// System.out.println(dataLayer);
					break;
				}
			}
			
			
		}

		
		private ModelField[] staticFields;
		
		
		public ModelReplicator(SparkModel model) throws Exception {
			Field[] fields = model.getClass().getFields();
			ArrayList<Field> staticFields = new ArrayList<Field>();
			
			for (Field field : fields) {
				if (Modifier.isStatic(field.getModifiers())) {
					staticFields.add(field);
				}
				else {
					System.err.println("Warning: non-static field in the model class: " + field.toString());
				}
			}
			
			this.staticFields = new ModelField[staticFields.size()];
			
			for (int i = 0; i < staticFields.size(); i++) {
				this.staticFields[i] = new ModelField();
				this.staticFields[i].saveValue(staticFields.get(i), model);
			}
		}
		
		
		/**
		 * Loads saved static fields into a given model
		 * @param model
		 * @throws Exception
		 */
		public void loadStaticFields(SparkModel model) throws Exception {
			for (int i = 0; i < staticFields.length; i++) {
				staticFields[i].loadValue(model);
			}
		}
		
		
		// Storage for all data layer names
		private static String[] names = null;
		
		/**
		 * Finds the name of a given data layer
		 * @param dataLayer
		 * @return null if the data layer is not found
		 */
		public static String getDataLayerName(DataLayer dataLayer) {
			if (names == null) {
				names = Observer.getInstance().getDataLayers();
				if (names == null)
					return null;
			}
			
			for (String name : names) {
				if (dataLayer == Observer.getInstance().getDataLayer(name))
					return name;
			}
			
			return null;
		}
		
	}
	

	/**
	 * Class for sending/receiving data between peers 
	 * @author Monad
	 *
	 */
	public static class DataCommunicator<T> implements Runnable {
		private int dstRank;
		private int sendTag, receiveTag;
		private Comm comm;
		
		private ObjectBuf<T> buffer;
		private ObjectBuf<T> receiveBuffer;
		
		private ObjectBuf<T[]> arrayBuffer;
		private ObjectBuf<T[]> receiveArrayBuffer;

		
		public DataCommunicator(Comm comm, int dstRank, T data, int sendTag, int receiveTag) {
			this.comm = comm;
			this.dstRank = dstRank;
			this.buffer = ObjectBuf.buffer(data);
			this.sendTag = sendTag;
			this.receiveTag = receiveTag;
		}

		
		public DataCommunicator(Comm comm, int dstRank, T[] data, int sendTag, int receiveTag) {
			this.comm = comm;
			this.dstRank = dstRank;
			this.arrayBuffer = ObjectBuf.objectBuffer(data);
			this.sendTag = sendTag;
			this.receiveTag = receiveTag;
		}

		
		
		public DataCommunicator(Comm comm, int dstRank, T data) {
			this(comm, dstRank, data, 0, 0);
		}

		
		public DataCommunicator(Comm comm, int dstRank, T[] data) {
			this(comm, dstRank, data, 0, 0);
		}

		
		@SuppressWarnings("unchecked")
		public DataCommunicator(Comm comm, int dstRank, ArrayList<T> data) {
			this(comm, dstRank, data.toArray((T[]) new Object[data.size()]), 0, 0);
		}
		
		
		public T getReceivedData() {
			if (receiveBuffer != null)
				return receiveBuffer.get(0);
			
			return null;
		}
		
		
		public T[] getReceivedArrayData() {
			if (receiveArrayBuffer != null)
				return receiveArrayBuffer.get(0);
			
			return null;
		}

		
		
		public void run() {
			try {
				int myRank = comm.rank();
				if (buffer != null) {
					receiveBuffer = ObjectBuf.buffer();

					logger.info("sendReceive from " + myRank + " to " + dstRank);
					
					comm.sendReceive(dstRank, sendTag, buffer, myRank, receiveTag, receiveBuffer);
				}
				else if (arrayBuffer != null) {
					receiveArrayBuffer = ObjectBuf.buffer();
					comm.sendReceive(dstRank, sendTag, arrayBuffer, myRank, receiveTag, receiveArrayBuffer);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	
	public static class AutoShutdown {
		private static final Logger logger = Logger.getLogger();
		private boolean reseted;
		private boolean stopped;
		private long time;
		
		public AutoShutdown() {
			reseted = false;
			stopped = true;
			time = 1000;
			
			Thread thread = new Thread(new Runnable() {
				public void run() {
					while(true) {
						while (stopped) {
							try {
								Thread.sleep(10);
							}
							catch(Exception e) {
								logger.error(e);
								e.printStackTrace();
							}
						}
					
						reseted = false;
						try {
							Thread.sleep(time);
						}
						catch (Exception e) {
							logger.error(e);
							e.printStackTrace();
						}
						
						if (!reseted) {
							logger.info("Auto shutdown");
							System.exit(0);
						}
					}
				}
			}, "AutoShutdown");
			
			
			thread.start();
		}
		
		
		public void reset() {
			reseted = true;
		}
		
		
		public void start(long time) {
			if (time < 1)
				time = 1;
			this.time = time;

			stopped = false;
			logger.info("AutoShutdown thread is started");
		}
		
		
		public void stop() {
			reseted = true;
			stopped = true;
			logger.info("AutoShutdown thread is stopped");
		}
	}
}
