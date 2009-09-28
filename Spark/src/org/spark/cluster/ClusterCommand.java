package org.spark.cluster;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Communication commands between master and slaves
 * @author Monad
 */
@SuppressWarnings("serial")
public final class ClusterCommand implements Serializable {
	/**
	 * Serial Version UID 
	 */
	private static final long serialVersionUID = -7295129660961725658L;
	
	// Command's actions
	private final ArrayList<Action> actions;
	

	
	/**
	 * Action class for specifying command's action
	 */
	public static abstract class Action implements Serializable {
		// Default UID
		private static final long serialVersionUID = 1L;
		
		// TRUE means that the action can be executed simultaneously
		// with other actions
		protected boolean parallelFlag = false;
		
		/**
		 * Performs an action on a slave
		 */
		public abstract void executeOnSlave() throws Exception;
		
		/**
		 * Performs an action on a master
		 */
		public abstract void executeOnMaster() throws Exception;
	}
	
	

	/**
	 * Public default constructor.
	 * Creates a command without actions.
	 */
	public ClusterCommand() {
		actions = new ArrayList<Action>();
	}
	
	
	/**
	 * Public constructor
	 * @param action
	 */
	public ClusterCommand(final Action action) {
		this();
		addAction(action);
	}
	

	/**
	 * Adds new action
	 * @param action
	 */
	public void addAction(final Action action) {
		actions.add(action);
	}
	
	
	/**
	 * Executes all actions
	 */
	void execute() throws Exception {
		boolean slave = ClusterManager.getInstance().isSlave();

		// TODO: execute some actions in parallel
		// Not all actions can be executed in this way (barrier, for example)
		// If actions are executed in parallel,
		// then before barrier it is required to wait until actions end
		for (Action action : actions) {
			if (slave)
				action.executeOnSlave();
			else
				action.executeOnMaster();
		}

		// Advance time counter
		ClusterManager.getInstance().advanceTick();
	}

	
	/**
	 * On master sends the command and executes it
	 * On slaves receives the command and executes it
	 * @param cmd should be not null for a master
	 */
	public static void broadcastAndExecute(ClusterCommand cmd) throws Exception {
		ClusterManager manager = ClusterManager.getInstance();
		
		if (manager.isSlave()) {
//			System.out.println("Creating buffer");
			ObjectBuf<ClusterCommand> cmdBuf = ObjectBuf.buffer();
//			System.out.println("Broadcasting buffer");
			manager.getComm().broadcast(0, ClusterManager.CMD_DATA, cmdBuf);

			if (cmdBuf.get(0) == null)
				throw new Exception("Null command received!");

//			System.out.println("Executing command");
			cmdBuf.get(0).execute();
		}
		else {
			if (cmd == null)
				throw new Exception("Command could not be null for a master");
			
			ObjectBuf<ClusterCommand> cmdBuf = ObjectBuf.buffer(cmd);
//			System.out.println(cmdBuf.toString());
			manager.getComm().broadcast(0, ClusterManager.CMD_DATA, cmdBuf);
			
			cmd.execute();
		}
	}
	
	
	/**
	 * Barrier action
	 */
	public static final Action BARRIER = new Action() {

		@Override
		public void executeOnMaster() throws Exception {
			ClusterManager.getInstance().getComm().barrier();
		}

		@Override
		public void executeOnSlave() throws Exception {
			ClusterManager.getInstance().getComm().barrier();
		}
		
	};
	
	
	
	/**
	 * Sends all grids' data from slaves to a master
	 */
	public static final Action GET_GRIDS_DATA = new Action() {

		@Override
		public void executeOnMaster() throws Exception {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void executeOnSlave() throws Exception {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	
	
	/**
	 * Sends all agents' data from slaves to a master
	 */
	public static final Action GET_AGENTS_DATA = new Action() {

		@Override
		public void executeOnMaster() throws Exception {
			ClusterManager.getInstance().getGlobalSpace().receiveAgentsOnMaster();
		}

		@Override
		public void executeOnSlave() throws Exception {
			ClusterManager.getInstance().getGlobalSpace().sendAllAgentsToMaster();
		}
		
	};
	

	/**
	 * Sends all agents' data from slaves to a master
	 */
	public static final Action EXIT_IMMEDIATELY = new Action() {

		@Override
		public void executeOnMaster() throws Exception {
			System.exit(1);
		}

		@Override
		public void executeOnSlave() throws Exception {
			System.exit(1);
		}
		
	};

	
}
