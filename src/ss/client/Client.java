package ss.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import ss.dependencies.Transfer;
import ss.utils.encryption.AES;
import ss.utils.file.Paths;
import ss.utils.file.PropertiesFile;
import ss.utils.logging.Logger;
import ss.utils.logging.MessageLevels;

public class Client
{
	private final Socket socket;
	private final DataInputStream dis;
	private final DataOutputStream dos;
	
	private final String storagePublic;
	private Logger logger;
	
	public Client(int storageServer) throws IOException
	{
		initLogger(true, MessageLevels.ERROR);
		
		logger.printMessage("Initialising properties file", MessageLevels.INFO);
		initProperties();
		logger.printMessage("Obtained properties from properties file", MessageLevels.INFO);
		
		InetAddress ip;
		
		logger.printMessage("Connecting to storage server " + storageServer, MessageLevels.INFO);
		if (storageServer == 1) {
			this.storagePublic = ClientProperties.storage1Public;
			ip = InetAddress.getByName(ClientProperties.storage1IP);
		} else {
			this.storagePublic = ClientProperties.storage2Public;
			ip = InetAddress.getByName(ClientProperties.storage2IP);
		}
		logger.printMessage("Storage server public RSA key: " + storagePublic, MessageLevels.DATA);
		
		this.socket = new Socket(ip, ClientProperties.port);
		
		this.dis = new DataInputStream(socket.getInputStream());
		this.dos = new DataOutputStream(socket.getOutputStream());
		logger.printMessage("Connected to storage server " + socket.getRemoteSocketAddress()
				+ " on port " + socket.getPort(), MessageLevels.INFO);
	}
	
	public Client(int storageServer, boolean verbose, int messageLevel) throws IOException
	{
		initLogger(verbose, messageLevel);
		
		logger.printMessage("Initialising properties file", MessageLevels.INFO);
		initProperties();
		logger.printMessage("Obtained properties from properties file", MessageLevels.INFO);
		
		InetAddress ip;
		
		logger.printMessage("Connecting to storage server " + storageServer, MessageLevels.INFO);
		if (storageServer == 1) {
			this.storagePublic = ClientProperties.storage1Public;
			ip = InetAddress.getByName(ClientProperties.storage1IP);
		} else {
			this.storagePublic = ClientProperties.storage2Public;
			ip = InetAddress.getByName(ClientProperties.storage2IP);
		}
		logger.printMessage("Storage server public RSA key: " + storagePublic, MessageLevels.DATA);
		
		this.socket = new Socket(ip, ClientProperties.port);
		
		this.dis = new DataInputStream(socket.getInputStream());
		this.dos = new DataOutputStream(socket.getOutputStream());
		logger.printMessage("Connected to storage server " + socket.getRemoteSocketAddress()
				+ " on port " + socket.getPort(), MessageLevels.INFO);
	}
	
	private void initLogger(boolean verbose, int messageLevel) throws IOException
	{
		Logger.setVerbose(verbose);
		Logger.setMessageLevel(messageLevel);
		Logger.suppressPrintToConsole();
		Logger.setLogFile("securestore.log");
		logger = new Logger("Client");
	}
	
	private static void initProperties() throws FileNotFoundException, IOException
	{
		PropertiesFile.initialize();
		
		ClientProperties.port = Integer.parseInt(initProperty("port", String.valueOf(ClientProperties.port)));
		
		ClientProperties.thisPublic = initProperty("keys.this.public", ClientProperties.thisPublic);
		ClientProperties.thisPrivate = initProperty("keys.this.private", ClientProperties.thisPrivate);
		
		ClientProperties.storage1IP = initProperty("servers.1.ip", ClientProperties.storage1IP);
		ClientProperties.storage1Public = initProperty("keys.servers.1.public", ClientProperties.storage1Public);
		
		ClientProperties.storage2IP = initProperty("servers.2.ip", ClientProperties.storage2IP);
		ClientProperties.storage2Public = initProperty("keys.servers.2.public", ClientProperties.storage2Public);
		
		ClientProperties.tmpPath = initProperty("paths.temp", ClientProperties.tmpPath);
		Transfer.tempPath = ClientProperties.tmpPath;
		ClientProperties.storagePath = initProperty("paths.storage", ClientProperties.storagePath);
		
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
	
	public boolean start(String operation, String argument) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException
	{
		boolean valid = true;
		
		logger.printMessage("Receiving banner from server", MessageLevels.INFO);
		String received = dis.readUTF();
		logger.printMessage("Received banner", MessageLevels.INFO);
		logger.printMessage("Banner: " + received, MessageLevels.DATA);
		if (!received.toLowerCase().equals("please provide your operation of choice")) {
			logger.printMessage("Unrecognized banner", MessageLevels.ERROR);
			logger.printMessage("Terminating connection with server", MessageLevels.ERROR);
			dis.close();
			dos.close();
			socket.close();
			System.exit(MessageLevels.ERROR);
		}
		
		switch(operation.toLowerCase()) {
			case "download":
				logger.printMessage("Sending download operation to server", MessageLevels.INFO);
				logger.printMessage("Operation: download", MessageLevels.DATA);
				dos.writeUTF("download");
				logger.printMessage("Sent operation", MessageLevels.INFO);
				download(argument);
				break;
			case "upload":
				logger.printMessage("Sending upload operation to server", MessageLevels.INFO);
				logger.printMessage("Operation: upload master", MessageLevels.DATA);
				dos.writeUTF("upload master");
				logger.printMessage("Sent operation", MessageLevels.INFO);
				upload(argument);
				break;
			case "rm":
				logger.printMessage("Sending remove operation to server", MessageLevels.INFO);
				logger.printMessage("Operation: rm master", MessageLevels.DATA);
				dos.writeUTF("rm master");
				logger.printMessage("Sent operation", MessageLevels.INFO);
				command(argument);
				System.out.println("Removed file " + argument);
				break;
			case "ls":
				logger.printMessage("Sending list operation to server", MessageLevels.INFO);
				logger.printMessage("Operation: ls", MessageLevels.DATA);
				dos.writeUTF("ls");
				logger.printMessage("Sent operation", MessageLevels.INFO);
				ls(argument);
				break;
			default:
				System.out.println("Invalid operation");
				valid = false;
		}
		
		logger.printMessage("Terminating connection with server", MessageLevels.INFO);
		dis.close();
		dos.close();
		socket.close();
		logger.printMessage("Terminated connection", MessageLevels.INFO);
		
		return valid;
	}
	
	private void upload(String filename) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, IOException, InvalidAlgorithmParameterException
	{
		logger.printMessage("Sending file to master", MessageLevels.INFO);
		AES crypter = Transfer.sendSymmetric(dis, dos, ClientProperties.thisPrivate, storagePublic);
		Transfer.sendFile(dis, dos, crypter, filename, ClientProperties.storagePath);
		
		System.out.println("Uploaded file " + Paths.combinePaths(ClientProperties.storagePath, filename));
	}
	
	private void download(String filename) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidKeySpecException
	{
		logger.printMessage("Sending filename to master", MessageLevels.INFO);
		logger.printMessage("Filename: " + filename, MessageLevels.DATA);
		dos.writeUTF(filename);
		logger.printMessage("Sent filename", MessageLevels.INFO);
		
		logger.printMessage("Receiving file from master", MessageLevels.INFO);
		AES crypter = Transfer.receiveSymmetric(dis, dos, storagePublic, ClientProperties.thisPrivate);
		Transfer.receiveFile(dis, dos, crypter, ClientProperties.storagePath);
		
		System.out.println("Downloaded file " + filename + " to " + ClientProperties.storagePath);
	}
	
	private void ls(String dir) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, IOException
	{
		command(dir);
		
		logger.printMessage("Receiving directory contents from server", MessageLevels.INFO);
		String result = dis.readUTF();
		logger.printMessage("Received directory contents", MessageLevels.INFO);
		logger.printMessage("Directory contents: " + result.replace('\n', ' '), MessageLevels.DATA);
		
		logger.printMessage("Displaying directory contents", MessageLevels.INFO);
		System.out.println(result);
	}
	
	private void command(String argument) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException
	{
		logger.printMessage("Sending argument to server (master storage server)", MessageLevels.INFO);
		logger.printMessage("Argument: " + argument, MessageLevels.DATA);
		dos.writeUTF(argument);
		logger.printMessage("Sent argument", MessageLevels.INFO);
		
		logger.printMessage("Verifying master authenticity", MessageLevels.INFO);
		Transfer.sendSymmetric(dis, dos, ClientProperties.thisPrivate, storagePublic);
	}
}