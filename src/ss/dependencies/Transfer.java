package ss.dependencies;

import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import ss.utils.encryption.AES;
import ss.utils.encryption.RSA;
import ss.utils.encryption.SHA;
import ss.utils.file.Paths;
import ss.utils.logging.Logger;
import ss.utils.logging.MessageLevels;

public class Transfer
{
	
	public static String tempPath = "/tmp";
	private static int maxBufferSize = 1024;
	
	//Helps cut down on unnecessary code by completing the necessary file transfer steps
	private static void receiveFile(DataInputStream dis, int size, String filename) throws IOException
	{
		Logger logger = new Logger("Transfer - Receive File");
		
		logger.printMessage("Preparing file stream", MessageLevels.INFO);
		FileOutputStream fos = new FileOutputStream(filename); 
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		logger.printMessage("Prepared file stream", MessageLevels.INFO);
		logger.printMessage("Output file: " + filename, MessageLevels.DATA);
		
		logger.printMessage("Preparing byte buffer and file stream", MessageLevels.INFO);
		logger.printMessage("Bytes remaining: " + size, MessageLevels.DATA);
		int bufferSize = Math.min(size, maxBufferSize);
		byte[] buffer = new byte[bufferSize];
		logger.printMessage("Prepared buffer and file stream", MessageLevels.INFO);
		logger.printMessage("Byte buffer size: " + size, MessageLevels.DATA);
		
        while (bufferSize != 0) {
        	logger.printMessage("Reading data from socket into byte buffer", MessageLevels.INFO);
        	dis.read(buffer, 0, bufferSize);
        	logger.printMessage("Read data to buffer", MessageLevels.INFO);
        	
        	logger.printMessage("Writing data from byte buffer into file stream", MessageLevels.INFO);
        	bos.write(buffer, 0, bufferSize);
        	logger.printMessage("Wrote data to file stream", MessageLevels.INFO);
        	
        	logger.printMessage("Updating buffer size", MessageLevels.INFO);
        	size = size-maxBufferSize < 0 ? 0 : size-maxBufferSize;
        	logger.printMessage("Bytes remaining: " + size, MessageLevels.DATA);
        	bufferSize = Math.min(size, maxBufferSize);
        	logger.printMessage("Updated buffer size", MessageLevels.INFO);
			logger.printMessage("Byte buffer size: " + bufferSize, MessageLevels.DATA);
        }
        
        logger.printMessage("Obtained all file contents", MessageLevels.INFO);
		
		logger.printMessage("Closing file stream", MessageLevels.INFO);
		bos.close();
		logger.printMessage("Closed file stream", MessageLevels.INFO);
	}

	//Helps cut down on unnecessary code by completing the necessary file transfer steps
	private static void sendFile(DataOutputStream dos, String filename) throws IOException
	{
		Logger logger = new Logger("Transfer - Send File");
		
		logger.printMessage("Preparing file stream", MessageLevels.INFO);
		File file = new File(filename);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		logger.printMessage("Prepared file stream", MessageLevels.INFO);
		logger.printMessage("Input file: " + filename, MessageLevels.DATA);
		
		logger.printMessage("Preparing byte buffer", MessageLevels.INFO);
		int bytesAvailable = bis.available();
		logger.printMessage("Bytes remaining: " + bytesAvailable, MessageLevels.DATA);
		int bufferSize = Math.min(bytesAvailable, maxBufferSize);
		byte[] buffer = new byte[bufferSize];
		logger.printMessage("Prepared byte buffer", MessageLevels.INFO);
		logger.printMessage("Byte buffer size: " + bufferSize, MessageLevels.DATA);

        // Read file
        logger.printMessage("Reading data from file stream into byte buffer", MessageLevels.INFO);
	    int length = bis.read(buffer, 0, bufferSize);
	    while (length > 0) {
			logger.printMessage("Read data from file stream", MessageLevels.INFO);
			
			logger.printMessage("Writing data from buffer into socket", MessageLevels.INFO);
			dos.write(buffer, 0, bufferSize);
			logger.printMessage("Wrote data into socket", MessageLevels.INFO);
			
			logger.printMessage("Updating buffer size", MessageLevels.INFO);
			bytesAvailable = bis.available();
			logger.printMessage("Bytes remaining: " + bytesAvailable, MessageLevels.DATA);
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			logger.printMessage("Updated buffer size", MessageLevels.DATA);
			logger.printMessage("Byte buffer size: " + bufferSize, MessageLevels.DATA);
			
			logger.printMessage("Reading data from file stream into byte buffer", MessageLevels.INFO);
			length = bis.read(buffer, 0, bufferSize);
	    }
	    
	    logger.printMessage("Reached end of file", MessageLevels.INFO);
		
	    logger.printMessage("Closing file stream", MessageLevels.INFO);
		bis.close();
		logger.printMessage("Closed file stream", MessageLevels.INFO);
	}
	
	private static boolean sendEncryptedHash(DataInputStream dis, DataOutputStream dos, String filename, String privateKeyPath) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException
	{
		Logger logger = new Logger("Transfer - Send Hash (Encrypted)");
		
		String decryptedSHA = Paths.combinePaths(tempPath, "decrypted.txt");
		String sentSHA = Paths.combinePaths(tempPath, "sent.txt");
		
		logger.printMessage("Determining file hash", MessageLevels.INFO);
		SHA.getHashFile(filename, decryptedSHA);
		logger.printMessage("Determined hash", MessageLevels.INFO);
		logger.printMessage("Original file: "  + filename, MessageLevels.DATA);
		logger.printMessage("Hash file: " + decryptedSHA, MessageLevels.DATA);
		
		logger.printMessage("Encrypting hash file with private key", MessageLevels.INFO);
		logger.printMessage("Private key file: " + privateKeyPath, MessageLevels.INFO);
		RSA rsa = new RSA(null, privateKeyPath);
		rsa.encryptFile(decryptedSHA, sentSHA, true);
		logger.printMessage("Encrypted hash", MessageLevels.INFO);
		logger.printMessage("Encrypted hash file: " + sentSHA, MessageLevels.DATA);
		
		int size = Paths.getSize(sentSHA);
		logger.printMessage("Encrypted hash file size: " + size, MessageLevels.DATA);
		
		logger.printMessage("Sending encrypted hash file size", MessageLevels.INFO);
		dos.writeUTF(Integer.toString(size));
		logger.printMessage("Sent size", MessageLevels.INFO);
		logger.printMessage("Sending encrypted hash file", MessageLevels.INFO);
		sendFile(dos, sentSHA);
		logger.printMessage("Sent encrypted hash file", MessageLevels.INFO);
		
		logger.printMessage("Obtaining result message", MessageLevels.INFO);
		String result = dis.readUTF();
		logger.printMessage("Obtained result", MessageLevels.INFO);
		logger.printMessage("Result message: " + result, MessageLevels.DATA);
		return result.toLowerCase().equals("hash success");
	}
	
	private static boolean sendHash(DataInputStream dis, DataOutputStream dos, String filename) throws NoSuchAlgorithmException, FileNotFoundException, IOException
	{
		Logger logger = new Logger("Transfer - Send Hash");
		
		String sentSHA = Paths.combinePaths(tempPath, "sent.txt");
		
		logger.printMessage("Determining file hash", MessageLevels.INFO);
		SHA.getHashFile(filename, sentSHA);
		logger.printMessage("Determined hash", MessageLevels.INFO);
		logger.printMessage("Original file: "  + filename, MessageLevels.DATA);
		logger.printMessage("Hash file: " + sentSHA, MessageLevels.DATA);
		
		int size = Paths.getSize(sentSHA);
		logger.printMessage("Hash file size: " + size, MessageLevels.DATA);
		
		logger.printMessage("Sending hash file size", MessageLevels.INFO);
		dos.writeUTF(Integer.toString(size));
		logger.printMessage("Sent size", MessageLevels.INFO);
		logger.printMessage("Sending hash file", MessageLevels.INFO);
		sendFile(dos, sentSHA);
		logger.printMessage("Sent hash file", MessageLevels.INFO);
		
		logger.printMessage("Obtaining result message", MessageLevels.INFO);
		String result = dis.readUTF();
		logger.printMessage("Obtained result", MessageLevels.INFO);
		logger.printMessage("Result message: " + result, MessageLevels.DATA);
		return result.toLowerCase().equals("hash success");
	}

	//We verify the hash of every message (with exception to the file sizes = need to consider updating this) so this will be helpful
	private static boolean verifyEncryptedHash(DataInputStream dis, DataOutputStream dos, String decryptedFile, String publicKeyPath) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException
	{
		Logger logger = new Logger("Transfer - Verify Hash (Encrypted)");
		
		String receivedSHA = Paths.combinePaths(tempPath, "/received.txt");
		String decryptedSHA = Paths.combinePaths(tempPath, "decrypted.txt");
		
		RSA rsa = new RSA(publicKeyPath);
		
		logger.printMessage("Receiving encrypted hash file size", MessageLevels.INFO);
		String received = dis.readUTF();
		int size = Integer.parseInt(received);
		logger.printMessage("Received size", MessageLevels.INFO);
		logger.printMessage("Encrypted hash file size: " + size, MessageLevels.DATA);
		
		logger.printMessage("Receiving encrypted hash file", MessageLevels.INFO);
		receiveFile(dis, size, receivedSHA);
		logger.printMessage("Received encrypted hash file", MessageLevels.INFO);
		logger.printMessage("Encrypted hash file: " + receivedSHA, MessageLevels.DATA);
		
		logger.printMessage("Decrypting hash file with public key", MessageLevels.INFO);
		logger.printMessage("Public key file: " + publicKeyPath, MessageLevels.INFO);
		rsa.decryptFile(receivedSHA, decryptedSHA, false);
		logger.printMessage("Decrypted hash", MessageLevels.INFO);
		logger.printMessage("Decrypted hash file: " + decryptedSHA, MessageLevels.DATA);
		
		logger.printMessage("Testing hash of received file against decrypted hash file", MessageLevels.INFO);
		logger.printMessage("Received file: " + decryptedFile, MessageLevels.DATA);
		if(SHA.testHash(decryptedFile, decryptedSHA, tempPath)) {
			logger.printMessage("Hashes are the same", MessageLevels.INFO);
			logger.printMessage("Sending back success result message", MessageLevels.INFO);
			logger.printMessage("Result message: Hash success", MessageLevels.DATA);
			dos.writeUTF("Hash success");
			logger.printMessage("Sent result", MessageLevels.INFO);
			return true;
		} else {
			logger.printMessage("Hashes are different", MessageLevels.WARNING);
			logger.printMessage("Sending back fail result message", MessageLevels.INFO);
			logger.printMessage("Result message: Hash fail", MessageLevels.DATA);
			dos.writeUTF("Hash fail");
			logger.printMessage("Sent result", MessageLevels.INFO);
		}
		
		return false;
	}
	
	private static boolean verifyHash(DataInputStream dis, DataOutputStream dos, String decryptedFile) throws IOException, NoSuchAlgorithmException
	{
		Logger logger = new Logger("Transfer - Verify Hash");
		
		String receivedSHA = Paths.combinePaths(tempPath, "received.txt");
		
		logger.printMessage("Receiving hash file size", MessageLevels.INFO);
		String received = dis.readUTF();
		int size = Integer.parseInt(received);
		logger.printMessage("Received size", MessageLevels.INFO);
		logger.printMessage("Hash file size: " + size, MessageLevels.DATA);
		
		logger.printMessage("Receiving hash file", MessageLevels.INFO);
		receiveFile(dis, size, receivedSHA);
		logger.printMessage("Received hash file", MessageLevels.INFO);
		logger.printMessage("Hash file: " + receivedSHA, MessageLevels.DATA);
		
		logger.printMessage("Testing hash of received file against hash file", MessageLevels.INFO);
		logger.printMessage("Received file: " + decryptedFile, MessageLevels.DATA);
		if(SHA.testHash(decryptedFile, receivedSHA, tempPath)) {
			logger.printMessage("Hashes are the same", MessageLevels.INFO);
			logger.printMessage("Sending back success result message", MessageLevels.INFO);
			logger.printMessage("Result message: Hash success", MessageLevels.DATA);
			dos.writeUTF("Hash success");
			logger.printMessage("Sent result", MessageLevels.INFO);
			return true;
		} else {
			logger.printMessage("Hashes are different", MessageLevels.WARNING);
			logger.printMessage("Sending back fail result message", MessageLevels.INFO);
			logger.printMessage("Result message: Hash fail", MessageLevels.DATA);
			dos.writeUTF("Hash fail");
			logger.printMessage("Sent result", MessageLevels.INFO);
		}
		
		return false;
	}
	
	public static void sendFile(DataInputStream dis, DataOutputStream dos, AES crypter, String filename, String filePath) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, FileNotFoundException, IllegalBlockSizeException, BadPaddingException, IOException
	{
		Logger logger = new Logger("Transfer - Send File Wrapper");
		
		String decrypted = Paths.combinePaths(filePath, filename);
		String encrypted = Paths.combinePaths(tempPath, filename + ".txt");
		
		// Encrypt file
		logger.printMessage("Encrypting file to send using AES", MessageLevels.INFO);
		crypter.encryptFile(decrypted, encrypted);
		logger.printMessage("Encrypted file", MessageLevels.INFO);
		logger.printMessage("Original file: " + decrypted, MessageLevels.DATA);
		logger.printMessage("Encrypted file: " + encrypted, MessageLevels.DATA);
		
		// Send file name and size
		logger.printMessage("Sending filename", MessageLevels.INFO);
		logger.printMessage("Filename: " + filename, MessageLevels.DATA);
		dos.writeUTF(filename);
		logger.printMessage("Sent filename", MessageLevels.INFO);
		
		int size = Paths.getSize(encrypted);
		logger.printMessage("Sending encrypted file size", MessageLevels.INFO);
		logger.printMessage("Encrypted file size: " + size, MessageLevels.DATA);
		dos.writeUTF(Integer.toString(size));
		logger.printMessage("Sent size", MessageLevels.INFO);
		
		// Send encrypted file
		logger.printMessage("Sending encrypted file", MessageLevels.INFO);
		Transfer.sendFile(dos, encrypted);
		logger.printMessage("Sent encrypted file", MessageLevels.INFO);
		
		// Send hash
		logger.printMessage("Sending original file hash", MessageLevels.INFO);
		if (!Transfer.sendHash(dis, dos, decrypted)) {
			logger.printMessage("Error during file transmission. Recipient failed to verify hash", MessageLevels.ERROR);
			logger.printMessage("Terminating connection", MessageLevels.ERROR);
			dis.close();
			dos.close();
			Thread.currentThread().interrupt();
		}
		logger.printMessage("File hash verified", MessageLevels.INFO);
		logger.printMessage("File transfer successful", MessageLevels.INFO);
	}
	
	public static String receiveFile(DataInputStream dis, DataOutputStream dos, AES crypter, String filePath) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
	{
		Logger logger = new Logger("Transfer - Receive File Wrapper");
		
		String encrypted = Paths.combinePaths(tempPath, "encrypted.txt");
		
		// Receive file name and size
		logger.printMessage("Receiving filename", MessageLevels.INFO);
		String filename = dis.readUTF();
		logger.printMessage("Received filename", MessageLevels.INFO);
		logger.printMessage("Filename: " + filename, MessageLevels.DATA);
		
		logger.printMessage("Receiving encrypted file size", MessageLevels.INFO);
		String received = dis.readUTF();
		int size = Integer.parseInt(received);
		logger.printMessage("Received size", MessageLevels.INFO);
		logger.printMessage("Encrypted file size: " + size, MessageLevels.DATA);
		
		// Receive encrypted file
		logger.printMessage("Receiving encrypted file", MessageLevels.INFO);
		Transfer.receiveFile(dis, size, encrypted);
		logger.printMessage("Received encrypted file", MessageLevels.INFO);
		
		// Store file
		logger.printMessage("Decrypting file received using AES", MessageLevels.INFO);
		String decrypted = Paths.combinePaths(filePath, filename);
		crypter.decryptFile(encrypted, decrypted);
		logger.printMessage("Decrypted file", MessageLevels.INFO);
		logger.printMessage("Original file: " + encrypted, MessageLevels.DATA);
		logger.printMessage("Decrypted file: " + decrypted, MessageLevels.DATA);
		
		// Check hash
		logger.printMessage("Verifying hash of decrypted file", MessageLevels.INFO);
		if (!Transfer.verifyHash(dis, dos, decrypted)) {
			logger.printMessage("Error during file transmission. Failed to verify hash", MessageLevels.ERROR);
			logger.printMessage("Terminating connection", MessageLevels.ERROR);
			dis.close();
			dos.close();
			Thread.currentThread().interrupt();
		}
		logger.printMessage("File hash verified", MessageLevels.INFO);
		logger.printMessage("File transfer successful", MessageLevels.INFO);
		
		return filename;
	}
	
	public static AES sendSymmetric(DataInputStream dis, DataOutputStream dos, String senderPrivateKey, String receiverPublicKey) throws NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException
	{
		Logger logger = new Logger("Transfer - Send AES");
		
		String symKeyFile = Paths.combinePaths(tempPath, "symmetric.txt");
		String sentFile = Paths.combinePaths(tempPath, "sent.txt");
		String ivFile = Paths.combinePaths(tempPath, "iv.txt");
		
		RSA receiverRSA = new RSA(receiverPublicKey);
		
		logger.printMessage("Creating temporary AES key files", MessageLevels.INFO);
		AES symmetric = new AES(symKeyFile, ivFile, false);
		logger.printMessage("Created key files", MessageLevels.INFO);
		logger.printMessage("AES symmetric key: " + symKeyFile, MessageLevels.DATA);
		logger.printMessage("AES IV: " + ivFile, MessageLevels.DATA);
		
		// Send symmetric key
		logger.printMessage("Encrypting AES symmetric key file", MessageLevels.INFO);
		receiverRSA.encryptFile(symKeyFile, sentFile, false);
		logger.printMessage("Encrypted key file", MessageLevels.INFO);
		logger.printMessage("Encrypted AES symmetric key: " + sentFile, MessageLevels.DATA);
		
		int size = Paths.getSize(sentFile);
		logger.printMessage("Sending encrypted key file size", MessageLevels.INFO);
		logger.printMessage("Encrypted key file size: " + size, MessageLevels.DATA);
		dos.writeUTF(Integer.toString(size));
		logger.printMessage("Sent size", MessageLevels.INFO);
		
		logger.printMessage("Sending encrypted key file", MessageLevels.INFO);
		Transfer.sendFile(dos, sentFile);
		logger.printMessage("Sent encrypted key file", MessageLevels.INFO);
		
		// Validate sender and symmetric key sent
		logger.printMessage("Sending original symmetric key file hash", MessageLevels.INFO);
		if (!Transfer.sendEncryptedHash(dis, dos, symKeyFile, senderPrivateKey)) {
			logger.printMessage("Error during file transmission. Recipient failed to verify symmetric key hash", MessageLevels.ERROR);
			logger.printMessage("Terminating connection", MessageLevels.ERROR);
			dis.close();
			dos.close();
			Thread.currentThread().interrupt();
		}
		logger.printMessage("File integrity and sender authenticity verified", MessageLevels.INFO);
		logger.printMessage("Symmetric key file transfer successful", MessageLevels.INFO);
		
		// Send iv file
		logger.printMessage("Encrypting AES IV file", MessageLevels.INFO);
		receiverRSA.encryptFile(ivFile, sentFile, false);
		logger.printMessage("Encrypted IV file", MessageLevels.INFO);
		logger.printMessage("Encrypted AES IV: " + sentFile, MessageLevels.DATA);
		
		size = Paths.getSize(sentFile);
		logger.printMessage("Sending encrypted IV file size", MessageLevels.INFO);
		logger.printMessage("Encrypted IV file size: " + size, MessageLevels.DATA);
		dos.writeUTF(Integer.toString(size));
		logger.printMessage("Sent size", MessageLevels.INFO);
		
		logger.printMessage("Sending encrypted IV file", MessageLevels.INFO);
		Transfer.sendFile(dos, sentFile);
		logger.printMessage("Sent ecrypted IV file", MessageLevels.INFO);
		
		// Validate sender and iv sent
		logger.printMessage("Sending original IV file hash", MessageLevels.INFO);
		if (!Transfer.sendEncryptedHash(dis, dos, ivFile, senderPrivateKey)) {
			logger.printMessage("Error during file transmission. Recipient failed to verify IV hash", MessageLevels.ERROR);
			logger.printMessage("Terminating connection", MessageLevels.ERROR);
			dis.close();
			dos.close();
			Thread.currentThread().interrupt();
		}
		logger.printMessage("File integrity and sender authenticity verified", MessageLevels.INFO);
		logger.printMessage("IV file transfer successful", MessageLevels.INFO);
		
		logger.printMessage("Established AES-encrypted connection with server", MessageLevels.INFO);
		
		return symmetric;
	}
	
	public static AES receiveSymmetric(DataInputStream dis, DataOutputStream dos, String senderPublicKey, String receiverPrivateKey) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException
	{
		Logger logger = new Logger("Transfer - Receive AES");
		
		String receivedFile = Paths.combinePaths(tempPath, "received.txt");
		String symKeyFile = Paths.combinePaths(tempPath, "symmetric.txt");
		String ivFile = Paths.combinePaths(tempPath, "iv.txt");
		
		RSA receiverRSA = new RSA(null, receiverPrivateKey);
		
		// Receive symmetric key
		logger.printMessage("Receiving encrypted AES symmetric key file size", MessageLevels.INFO);		
		String received = dis.readUTF();
		int size = Integer.parseInt(received);
		logger.printMessage("Received size", MessageLevels.INFO);
		logger.printMessage("Encrypted key file size: " + size, MessageLevels.DATA);
		
		logger.printMessage("Receiving encrypted AES symmetric key file", MessageLevels.INFO);
		Transfer.receiveFile(dis, size, receivedFile);
		logger.printMessage("Received encrypted key file", MessageLevels.INFO);
		logger.printMessage("Encrypted AES symmetric key: " + receivedFile, MessageLevels.DATA);
		
		logger.printMessage("Decrypting AES symmetric key file", MessageLevels.INFO);
		receiverRSA.decryptFile(receivedFile, symKeyFile, true);
		logger.printMessage("Decrypted key file", MessageLevels.INFO);
		logger.printMessage("Decrypted AES symmetric key: " + symKeyFile, MessageLevels.DATA);
		
		// Validate sender authenticity (should be other storage device) and symmetric key
		logger.printMessage("Verifying hash of decrypted symmetric key", MessageLevels.INFO);
		if (!Transfer.verifyEncryptedHash(dis, dos, symKeyFile, senderPublicKey)) {
			logger.printMessage("Error during file transmission. Failed to verify symmetric key hash", MessageLevels.ERROR);
			logger.printMessage("Terminating connection", MessageLevels.ERROR);
			dis.close();
			dos.close();
			Thread.currentThread().interrupt();
		}
		logger.printMessage("File integrity and sender authenticity verified", MessageLevels.INFO);
		logger.printMessage("Symmetric key file transfer successful", MessageLevels.INFO);
		
		// Receive iv file
		logger.printMessage("Receiving encrypted AES IV file size", MessageLevels.INFO);
		received = dis.readUTF();
		size = Integer.parseInt(received);
		logger.printMessage("Received size", MessageLevels.INFO);
		logger.printMessage("Encrypted IV file size: " + size, MessageLevels.DATA);
		
		logger.printMessage("Receiving encrypted AES IV file", MessageLevels.INFO);
		Transfer.receiveFile(dis, size, receivedFile);
		logger.printMessage("Received encrypted IV file", MessageLevels.INFO);
		logger.printMessage("Encrypted AES IV: " + receivedFile, MessageLevels.DATA);
		
		logger.printMessage("Decrypting AES IV file", MessageLevels.INFO);
		receiverRSA.decryptFile(receivedFile, ivFile, true);
		logger.printMessage("Decrypted IV file", MessageLevels.INFO);
		logger.printMessage("Decrypted AES IV: " + symKeyFile, MessageLevels.DATA);
		
		// Validate sender and iv
		logger.printMessage("Verifying hash of decrypted IV", MessageLevels.INFO);
		if (!Transfer.verifyEncryptedHash(dis, dos, ivFile, senderPublicKey)) {
			logger.printMessage("Error during file transmission. Failed to verify IV hash", MessageLevels.ERROR);
			logger.printMessage("Terminating connection", MessageLevels.ERROR);
			dis.close();
			dos.close();
			Thread.currentThread().interrupt();
		}
		logger.printMessage("File integrity and sender authenticity verified", MessageLevels.INFO);
		logger.printMessage("IV file transfer successful", MessageLevels.INFO);
		
		logger.printMessage("Established AES-encrypted connection with client", MessageLevels.INFO);
		
		return new AES(symKeyFile, ivFile);
	}
}

