package ss.dependencies;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
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

import ss.server.ServerProperties;
import ss.utils.encryption.AES;
import ss.utils.file.Paths;
import ss.utils.logging.Logger;
import ss.utils.logging.MessageLevels;

public class Master {
	
	public static void download(DataInputStream dis, DataOutputStream dos) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException
	{
		Logger logger = new Logger("Master - Download");
		
		logger.printMessage("Receiving filename from web server", MessageLevels.INFO);
		// Receive filename from web server
		String filename = dis.readUTF();
		logger.printMessage("Received filename", MessageLevels.INFO);
		logger.printMessage("Filename: " + filename, MessageLevels.DATA);
		
		// Prepare file paths for decryption
		String decrypted = Paths.combinePaths(ServerProperties.tmpPath, filename);
		String encrypted = Paths.combinePaths(ServerProperties.storagePath, filename + ".enc");
		
		// Store AES encrypted file
		logger.printMessage("Decrypting AES-256 encrypted file", MessageLevels.INFO);
		logger.printMessage("Encrypted file path: " + encrypted, MessageLevels.DATA);
		AES storageCrypter = new AES(ServerProperties.aesKey, ServerProperties.aesIV);
		storageCrypter.decryptFile(encrypted, decrypted);
		logger.printMessage("Decrypted file", MessageLevels.INFO);
		logger.printMessage("Plaintext file path: " + decrypted, MessageLevels.DATA);
		
		// Send file to web server
		logger.printMessage("Sending plaintext file to web server", MessageLevels.INFO);
		AES crypter = Transfer.sendSymmetric(dis, dos, ServerProperties.thisPrivate, ServerProperties.webPublic);
		Transfer.sendFile(dis, dos, crypter, filename, ServerProperties.tmpPath);
		
		logger.printMessage("Deleting plaintext file", MessageLevels.INFO);
		new File(decrypted).delete();
		logger.printMessage("Deleted plaintext file", MessageLevels.INFO);
	}
	
	public static void upload(DataInputStream dis, DataOutputStream dos) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException
	{
		Logger logger = new Logger("Master - Upload");
		
		// Receive file from web server
		logger.printMessage("Receiving file from web server", MessageLevels.INFO);
		AES webCrypter = Transfer.receiveSymmetric(dis, dos, ServerProperties.webPublic, ServerProperties.thisPrivate);
		String filename = Transfer.receiveFile(dis, dos, webCrypter, ServerProperties.tmpPath);
		logger.printMessage("Terminating connection to web server", MessageLevels.INFO);
		dis.close();
		dos.close();
		logger.printMessage("Connection terminated", MessageLevels.INFO);
		
		// Prepare file paths for storage
		String decrypted = Paths.combinePaths(ServerProperties.tmpPath, filename);
		String encrypted = Paths.combinePaths(ServerProperties.storagePath, filename + ".enc");
		
		// Store AES encrypted file
		logger.printMessage("Storing received file as AES-256 encrypted file", MessageLevels.INFO);
		logger.printMessage("Plaintext file path: " + decrypted, MessageLevels.DATA);
		AES storageCrypter = new AES(ServerProperties.aesKey, ServerProperties.aesIV);
		storageCrypter.encryptFile(decrypted, encrypted);
		logger.printMessage("Encrypted and stored file", MessageLevels.INFO);
		logger.printMessage("Encrypted file path: " + encrypted, MessageLevels.DATA);
		
		logger.printMessage("Deleting plaintext file", MessageLevels.INFO);
		new File(decrypted).delete();
		logger.printMessage("Deleted plaintext file", MessageLevels.INFO);
		
		try {
			// Connect to slave
			logger.printMessage("Connecting to slave", MessageLevels.INFO);
			Socket socket = connectToSlave();
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			
			logger.printMessage("Receiving banner from slave", MessageLevels.INFO);
			String banner = dis.readUTF();
			logger.printMessage("Connected to slave " + socket.getRemoteSocketAddress()
					+ " on port " + socket.getPort(), MessageLevels.INFO);
			logger.printMessage("Slave banner: " + banner, MessageLevels.INFO);
			
			logger.printMessage("Sending operation to slave", MessageLevels.INFO);
			dos.writeUTF("upload slave");
			logger.printMessage("Sent operation", MessageLevels.INFO);
			logger.printMessage("Operation: upload slave", MessageLevels.DATA);
			
			// Send AES encrypted file to slave
			logger.printMessage("Sending file to slave", MessageLevels.INFO);
			AES slaveCrypter = Transfer.sendSymmetric(dis, dos, ServerProperties.thisPrivate, ServerProperties.otherPublic);
			Transfer.sendFile(dis, dos, slaveCrypter, filename + ".enc", ServerProperties.storagePath);
		} catch (IOException ex) {
			logger.printMessage("Unable to connect to slave", MessageLevels.WARNING);
			logger.printMessage("Exception message: " + ex.getMessage(), MessageLevels.WARNING);
		}
	}
	
	public static void rm(DataInputStream dis, DataOutputStream dos) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException
	{
		Logger logger = new Logger("Master - Remove");
		
		String filename = command(dis, dos);
		
		logger.printMessage("Terminating connection to web server", MessageLevels.INFO);
		dis.close();
		dos.close();
		logger.printMessage("Connection terminated", MessageLevels.INFO);
		
		// Remove file
		String path = Paths.combinePaths(ServerProperties.storagePath, filename + ".enc");
		
		logger.printMessage("Attempting to remove file " + path, MessageLevels.INFO);
		File file = new File(path);
		if (file.exists()) {
			logger.printMessage("Removing file " + path, MessageLevels.INFO);
			file.delete();
			logger.printMessage("Removed file", MessageLevels.INFO);
		} else {
			logger.printMessage("File " + path + " not found", MessageLevels.INFO);
			
			path = Paths.combinePaths(ServerProperties.storagePath, filename);
			logger.printMessage("Attempting to remove file " + path, MessageLevels.INFO);
			file = new File(path);
			if (file.exists()) {
				logger.printMessage("Removing file " + path, MessageLevels.INFO);
				file.delete();
				logger.printMessage("Removed file", MessageLevels.INFO);
			} else {
				logger.printMessage("File " + path + " not found", MessageLevels.INFO);
				logger.printMessage("Client requested removal of nonexistent file", MessageLevels.WARNING);
			}
		}
		
		notifySlave("rm slave", filename);
	}
	
	public static void ls(DataInputStream dis, DataOutputStream dos) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException
	{
		Logger logger = new Logger("Master - List");
		
		String dir = command(dis, dos);
		
		// Get files list
		logger.printMessage("List directory: " + dir, MessageLevels.DATA);
		logger.printMessage("Formatting list directory full path", MessageLevels.INFO);
		File path;
		path = new File(Paths.combinePaths(ServerProperties.storagePath, dir));
		logger.printMessage("Obtained list directory full path", MessageLevels.INFO);
		logger.printMessage("Full path: " + path, MessageLevels.DATA);
		
		logger.printMessage("Obtaining contents of desired directory", MessageLevels.INFO);
		String[] contents = path.list();
		String result = "";
		for (String element : contents) {
			if (element.endsWith(".enc"))
				element = Paths.removeFileExtension(element);
			result += element + "\n";
		}
		logger.printMessage("Obtained contents", MessageLevels.INFO);
		logger.printMessage("Directory contents: " + result.replace('\n', ' '), MessageLevels.DATA);
		
		// Send back files list
		logger.printMessage("Sending directory contents to client", MessageLevels.INFO);
		dos.writeUTF(result);
		logger.printMessage("Sent contents", MessageLevels.INFO);
	}
	
	private static String command(DataInputStream dis, DataOutputStream dos) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException
	{
		Logger logger = new Logger("Master - Command");
		
		// Receive argument from web server
		logger.printMessage("Receiving argument from client (web server)", MessageLevels.INFO);
		String argument = dis.readUTF();
		logger.printMessage("Received argument", MessageLevels.INFO);
		logger.printMessage("Argument: " + argument, MessageLevels.DATA);
		
		// Verify web server authenticity
		logger.printMessage("Verifying web server authenticity", MessageLevels.INFO);
		Transfer.receiveSymmetric(dis, dos, ServerProperties.webPublic, ServerProperties.thisPrivate);
		
		return argument;
	}
	
	private static void notifySlave(String slaveProtocol, String argument) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException
	{
		Logger logger = new Logger("Master - Notify Slave");
		
		try {
			// Connect to slave
			logger.printMessage("Connecting to slave", MessageLevels.INFO);
			Socket socket = connectToSlave();
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			
			// Receive banner and reply with command
			logger.printMessage("Receiving banner from slave", MessageLevels.INFO);
			String banner = dis.readUTF();
			logger.printMessage("Connected to slave " + socket.getRemoteSocketAddress()
					+ " on port " + socket.getPort(), MessageLevels.INFO);
			logger.printMessage("Slave banner: " + banner, MessageLevels.INFO);
			
			logger.printMessage("Sending operation to slave", MessageLevels.INFO);
			dos.writeUTF(slaveProtocol);
			logger.printMessage("Sent operation", MessageLevels.INFO);
			logger.printMessage("Operation: " + slaveProtocol, MessageLevels.DATA);
			
			// Provide slave with argument
			logger.printMessage("Sending argument to slave", MessageLevels.INFO);
			dos.writeUTF(argument);
			logger.printMessage("Sent argument", MessageLevels.INFO);
			logger.printMessage("Argument: " + argument, MessageLevels.DATA);
			
			// Verify authenticity
			logger.printMessage("Verifying authenticity of connection", MessageLevels.INFO);
			Transfer.sendSymmetric(dis, dos, ServerProperties.thisPrivate, ServerProperties.otherPublic);
		} catch (IOException ex) {
			logger.printMessage("Unable to connect to slave", MessageLevels.WARNING);
			logger.printMessage("Exception message: " + ex.getMessage(), MessageLevels.WARNING);
		}
	}
	
	private static Socket connectToSlave() throws IOException
	{
		InetAddress ip = InetAddress.getByName(ServerProperties.otherIP); 
		return new Socket(ip, ServerProperties.port);
	}
	
}
