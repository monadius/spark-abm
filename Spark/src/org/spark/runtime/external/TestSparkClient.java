package org.spark.runtime.external;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JOptionPane;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.spark.runtime.commands.FileTransfer;
import org.spark.runtime.commands.ModelManagerCommand;
import org.spark.runtime.data.DataRow;
import org.spark.runtime.external.data.LocalDataReceiver;
import org.spark.runtime.internal.manager.IModelManager;
import org.w3c.dom.Node;

import com.spinn3r.log5j.Logger;

/**
 * A test implementation of a SPARK client
 * @author Monad
 *
 */
public class TestSparkClient {
	private static final Logger logger = Logger.getLogger();
	
	
	
	
	
	/**
	 * Main method
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// The first thing to do is to set up the logger
		try {
			if (new File("spark.log4j.properties").exists()) {
				PropertyConfigurator.configure("spark.log4j.properties");
			} else {
				BasicConfigurator.configure();
				logger.error("File spark.log4j.properties is not found: using default output streams for log information");
			}
		} catch (Exception e) {
			e.printStackTrace();
			BasicConfigurator.configure();
		}
		
		
		Socket socket = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		
		String address = JOptionPane.showInputDialog("Enter SPARK server address", "localhost:12345");
		if (address == null)
			return;
		
		String[] elements = address.split(":");
		String host = elements[0];
		String port = elements.length > 1 ? elements[1] : "12345";
		int p = Integer.parseInt(port);
		
		try {
			logger.info("Connecting to server " + host + ":" + p);
			socket = new Socket(host, p);
			
			logger.info("Obtaining output stream...");
			oos = new ObjectOutputStream(socket.getOutputStream());

//			logger.info("Obtaining input stream...");
//			ois = new ObjectInputStream(socket.getInputStream());
			
			
			logger.info("Creating communication objects...");
			ClientModelManager manager = new ClientModelManager(oos);
			ClientDataReceiver receiver = new ClientDataReceiver(socket);
			
			logger.info("Starting the main application");
			Coordinator.init(manager, receiver);
			receiver.run();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		finally {
			if (oos != null)
				oos.close();
			
			if (ois != null)
				ois.close();
			
			if (socket != null)
				socket.close();
		}
		
		System.exit(0);
	}
	
	
	/**
	 * A client implementation of a data receiver
	 * @author Monad
	 *
	 */
	private static class ClientDataReceiver extends LocalDataReceiver implements Runnable {
		private ObjectInputStream ois;
		private Socket socket;
		
		
		public ClientDataReceiver(Socket socket) {
			this.socket = socket; 
		}
		
		
		public void run() {
			Object data;
			
			try {
				logger.info("Obtaining input stream...");
				this.ois = new ObjectInputStream(socket.getInputStream());

				while ((data = ois.readObject()) != null) {
					if (data instanceof DataRow) {
						receive((DataRow) data);
					}
					else {
						logger.error("Bad data received: " + data);
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	/**
	 * A client implementation of a model manager
	 * @author Monad
	 *
	 */
	private static class ClientModelManager implements IModelManager {
		private ObjectOutputStream oos;
		
		
		public ClientModelManager(ObjectOutputStream oos) {
			this.oos = oos;
		}
		
		
		public FileTransfer createFileTransfer(Node filesNode, File xmlModelFile) {
			return new FileTransfer(filesNode, xmlModelFile.getParentFile());
		}
		


		public void runOnce() {
		}

		public void run() {
		}
		

		public synchronized void sendCommand(ModelManagerCommand cmd) {
			try {
				oos.reset();
				oos.writeObject(cmd);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
