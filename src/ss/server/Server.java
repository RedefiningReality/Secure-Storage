package ss.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ss.dependencies.Transfer;
import ss.utils.file.PropertiesFile;
import ss.utils.logging.Logger;
import ss.utils.logging.MessageLevels;

public class Server {
	
	private ServerSocket serverSocket;
	private Logger logger;
	
	public Server() throws IOException
	{
		initLogger(true, MessageLevels.INFO);
		
		logger.printMessage("Initialising properties file", MessageLevels.INFO);
		initProperties();
		logger.printMessage("Obtained properties from properties file", MessageLevels.INFO);
		
		this.serverSocket = new ServerSocket(ServerProperties.port);
	}
	
	public Server(boolean verbose, int messageLevel) throws IOException
	{
		initLogger(verbose, messageLevel);
		
		logger.printMessage("Initialising properties file", MessageLevels.INFO);
		initProperties();
		logger.printMessage("Obtained properties from properties file", MessageLevels.INFO);
		
		this.serverSocket = new ServerSocket(ServerProperties.port);
	}
	
	private void initLogger(boolean verbose, int messageLevel) throws IOException
	{
		Logger.setVerbose(verbose);
		Logger.setMessageLevel(messageLevel);
		Logger.setLogFile("securestore.log");
		logger = new Logger("Server");
	}
	
	private static void initProperties() throws FileNotFoundException, IOException
	{
		PropertiesFile.initialize();
		
		ServerProperties.otherIP = initProperty("other.ip", ServerProperties.otherIP);
		ServerProperties.port = Integer.parseInt(initProperty("port", String.valueOf(ServerProperties.port)));
		
		ServerProperties.thisPublic = initProperty("keys.this.public", ServerProperties.thisPublic);
		ServerProperties.thisPrivate = initProperty("keys.this.private", ServerProperties.thisPrivate);
		
		ServerProperties.webPublic = initProperty("keys.web.public", ServerProperties.webPublic);
		ServerProperties.otherPublic = initProperty("keys.other.public", ServerProperties.otherPublic);
		
		ServerProperties.aesKey = initProperty("keys.storage.aes.key", ServerProperties.aesKey);
		ServerProperties.aesIV = initProperty("keys.storage.aes.iv", ServerProperties.aesIV);
		
		ServerProperties.tmpPath = initProperty("paths.temp", ServerProperties.tmpPath);
		Transfer.tempPath = ServerProperties.tmpPath;
		ServerProperties.storagePath = initProperty("paths.storage", ServerProperties.storagePath);
		
		PropertiesFile.saveToFile();
	}
	
	private static String initProperty(String property, String defaultValue) throws IOException
	{
		String value = PropertiesFile.getProperty(property);
		
		if (value == null) {
			PropertiesFile.addProperty(property, defaultValue);
			return defaultValue;
		} else
			return value;
	}
	
	public void start() throws IOException
	{
		logger.printMessage("Starting server", MessageLevels.INFO);
		while(true) {
			Socket socket = null;
			
			try {
				logger.printMessage("Listening for client connections", MessageLevels.INFO);
				socket = serverSocket.accept();
				
				logger.printMessage("Connected to client " + socket.getRemoteSocketAddress() + " on port " 
							+ socket.getLocalPort(), MessageLevels.INFO);
				
				DataInputStream dis = new DataInputStream(socket.getInputStream()); 
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
				new ServerThread(dis, dos).start();
				
			} catch(Exception e) {
				logger.printMessage("Caught an exception while communicating with client "
						+ socket.getRemoteSocketAddress() + ". Closing socket", MessageLevels.ERROR);
				
				if (socket != null)
					socket.close();
				
				logger.printMessage("Exception: " + e.getMessage(), MessageLevels.ERROR); 
			}
		}
	}
}