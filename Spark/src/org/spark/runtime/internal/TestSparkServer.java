package org.spark.runtime.internal;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.BasicConfigurator;
import org.spark.runtime.commands.Command_AddDataProcessor;
import org.spark.runtime.commands.Command_AddLocalDataSender;
import org.spark.runtime.commands.Command_Exit;
import org.spark.runtime.commands.ModelManagerCommand;
import org.spark.runtime.data.DataRow;
import org.spark.runtime.internal.data.DataProcessor;
import org.spark.runtime.internal.manager.IModelManager;
import org.spark.runtime.internal.manager.ModelManager_Basic;

import com.spinn3r.log5j.Logger;

/**
 * A test implementation of a SPARK server
 * @author Monad
 *
 */
public class TestSparkServer implements Runnable {
	private static final Logger logger = Logger.getLogger();
	
	/* Socket for communications with a client */
	private final Socket clientSocket;
	
	/* Main model manager */
	private final IModelManager manager;
	
	
	/**
	 * Creates a test server
	 */
	public TestSparkServer(Socket clientSocket) {
		this.manager = new ModelManager_Basic();
		this.clientSocket = clientSocket;
		logger.info("Creating a server for the client: " + clientSocket.toString());
	}
	
	
	/**
	 * Main server's method
	 */
	public void run() {
		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;
		
		logger.info("Starting a model manager...");
		Thread managerThread = new Thread(manager);
		managerThread.start();
		logger.info("Model manager is started");
		
		logger.info("Server is started");
		
		try {
			ois = new ObjectInputStream(clientSocket.getInputStream());
			oos = new ObjectOutputStream(clientSocket.getOutputStream());
			Object data;
			
			logger.info("Waiting for data...");

			while ((data = ois.readObject()) != null) {
				if (data instanceof ModelManagerCommand) {
					if (data instanceof Command_AddLocalDataSender) {
						DataProcessor dp = new MyDataSender(oos);
						data = new Command_AddDataProcessor(dp);
					}
					
					manager.sendCommand((ModelManagerCommand) data);
				}
				else {
					System.err.println("Bad message from a client: " + data.toString());
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				manager.sendCommand(new Command_Exit());
				
				if (ois != null)
					ois.close();
				
				if (oos != null)
					oos.close();
				
				clientSocket.close();
				
				logger.info("Stopping manager thread...");
				
				managerThread.join(5000);
				logger.info("Done");
			}
			catch (Exception e) {}
		}
	}
	
	
	/**
	 * Main method
	 * @param args
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		ServerSocket serverSocket = null;
		int port = 12345;
		
		if (args.length >= 2) {
			port = Integer.parseInt(args[1]);
		}
		
		try {
			serverSocket = new ServerSocket(port);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		while (true) {
			Socket clientSocket = null;
			logger.info("Waiting for a connection...");
		
			try {
				clientSocket = serverSocket.accept();
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}

			TestSparkServer server = new TestSparkServer(clientSocket);
			server.run();
			// Start up the server
//			new Thread(server).start();
		}

	}
	
	
	
	/**
	 * An implementation of data processor which sends all data to a client
	 * @author Monad
	 *
	 */
	private static class MyDataSender extends DataProcessor {
		private ObjectOutputStream oos;
		
		
		public MyDataSender(ObjectOutputStream oos) throws Exception {
			this.oos = oos;
		}
		
		
		@Override
		public void finalizeProcessing() throws Exception {
			// TODO Auto-generated method stub
			if (oos != null)
				oos.reset();
		}

		@Override
		public void processDataRow(DataRow row) throws Exception {
			oos.reset();
			oos.writeObject(row);
		}
		
	}
}
