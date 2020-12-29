package ss.dependencies;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
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

public class Slave {
	
	public static void upload(DataInputStream dis, DataOutputStream dos) throws NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException
	{
		Logger logger = new Logger("Slave - Upload");
		
		// Receive symmetric key and iv
		logger.printMessage("Receiving file from master", MessageLevels.INFO);
		AES crypter = Transfer.receiveSymmetric(dis, dos, ServerProperties.otherPublic, ServerProperties.thisPrivate);
		
		// Receive and store file
		Transfer.receiveFile(dis, dos, crypter, ServerProperties.storagePath);
		logger.printMessage("Received and stored file", MessageLevels.INFO);
	}
	
	public static void rm(DataInputStream dis, DataOutputStream dos) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException
	{
		Logger logger = new Logger("Slave - Remove");
		
		String filename = command(dis, dos);
		
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
				logger.printMessage("Client requested nonexistent file", MessageLevels.WARNING);
			}
		}
	}
	
	private static String command(DataInputStream dis, DataOutputStream dos) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException
	{
		Logger logger = new Logger("Slave - Command");
		
		// Receive argument from master
		logger.printMessage("Receiving argument from client (master)", MessageLevels.INFO);
		String arg = dis.readUTF();
		logger.printMessage("Received argument", MessageLevels.INFO);
		logger.printMessage("Argument: " + arg, MessageLevels.DATA);
		
		// Verify master authenticity
		logger.printMessage("Verifying master authenticity", MessageLevels.INFO);
		Transfer.receiveSymmetric(dis, dos, ServerProperties.otherPublic, ServerProperties.thisPrivate);
		
		return arg;
	}

}
