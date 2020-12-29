package ss.utils.encryption; 

import java.nio.charset.StandardCharsets; 
import java.security.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException; 

/* 
	The sha_utils class contains a set of methods that will enable us to complete the following: 
 	1. Calculate the hash of a given string (for testing purposes) 
	2. Calculate the hash of a given file and store it in a file 
	3. Verify the hash of a given file and its supposed hash 
*/ 
public class SHA
{
	//gen_hash takes a message and returns its hash form as a String (not used) 
	public static String getHash(String message) throws NoSuchAlgorithmException
	{
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] encoded = digest.digest(message.getBytes(StandardCharsets.UTF_8));
		
		StringBuffer buffer = new StringBuffer(); 
		for(int i = 0; i < encoded.length; i++) {
			String hex = Integer.toHexString(0xff & encoded[i]);
			
			if(hex.length() == 1)
				buffer.append('0');
			
			buffer.append(hex);
		}
		return buffer.toString();
	}

	//gen_hash_file generates the hash of a file, it takes the source and destination file names as inputs 
	public static void getHashFile(String inputFile, String hashFile) throws NoSuchAlgorithmException, FileNotFoundException, IOException
	{
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		processFile(digest, inputFile, hashFile);
	}

	//test_hash will be our verification mehtod for ensuring message integrity 
	public static boolean testHash(String inputFile, String hashFile, String tempPath) throws NoSuchAlgorithmException, IOException 
	{
		String tempFile = tempPath + "/sha256.txt";
		
		//for verification, we calculate the hash of the input file and compare it to the provided hash file 
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		processFile(digest, inputFile, tempFile); //calculate the hash of the provided file
		
		byte[] input1 = new byte[256]; //The hash size will be 256 bits
		byte[] input2 = new byte[256]; 
		
		try (FileInputStream in = new FileInputStream(hashFile)) {
			while(in.read(input1) != -1);
		}
		
		try (FileInputStream in = new FileInputStream(tempFile)) {
			while(in.read(input2) != -1);
		}

		for(int i = 0; i < 256; i++)
			if(input1[i] != input2[i])
				return false;
		
		return true; 
	}

	private static void processFile(MessageDigest digest, String inputFile, String outputFile) throws FileNotFoundException, IOException
	{
		try(FileInputStream in = new FileInputStream(inputFile);
				FileOutputStream out = new FileOutputStream(outputFile)) {
			
			byte[] inputBuffer = new byte[256];
			
			int length; 
			while((length = in.read(inputBuffer)) != -1)
				digest.update(inputBuffer, 0, length); 
			
			byte[] outputBuffer = digest.digest(); 
			if(outputBuffer != null)
				out.write(outputBuffer);
		}
	}
}
